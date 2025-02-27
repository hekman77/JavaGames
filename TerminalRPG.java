import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.io.*;

public class TerminalRPG {
    private static final Scanner scanner = new Scanner(System.in);
    private static Player player;
    private static World world;
    private static boolean running = true;
    private static final int SAVE_VERSION = 1;

    public static void main(String[] args) {
        clearScreen();
        printTitle();

        System.out.println("1. New Game");
        System.out.println("2. Load Game");
        System.out.println("3. Exit");
        System.out.print("> ");

        int choice = getIntInput(1, 3);

        switch (choice) {
            case 1:
                createNewGame();
                break;
            case 2:
                loadGame();
                break;
            case 3:
                System.out.println("Thanks for playing!");
                System.exit(0);
        }

        gameLoop();
    }

    private static void createNewGame() {
        clearScreen();
        System.out.println("=== Character Creation ===");
        System.out.print("Enter your character's name: ");
        String name = scanner.nextLine();

        System.out.println("\nChoose your class:");
        System.out.println("1. Warrior (High HP, Strong Melee)");
        System.out.println("2. Mage (Low HP, Powerful Spells)");
        System.out.println("3. Rogue (Medium HP, Critical Hits)");
        System.out.print("> ");

        int classChoice = getIntInput(1, 3);
        String className;
        int baseHp, baseAtk, baseDef, baseMag;

        switch (classChoice) {
            case 1: // Warrior
                className = "Warrior";
                baseHp = 100;
                baseAtk = 15;
                baseDef = 10;
                baseMag = 5;
                break;
            case 2: // Mage
                className = "Mage";
                baseHp = 70;
                baseAtk = 7;
                baseDef = 5;
                baseMag = 20;
                break;
            default: // Rogue
                className = "Rogue";
                baseHp = 85;
                baseAtk = 12;
                baseDef = 7;
                baseMag = 8;
                break;
        }

        player = new Player(name, className, baseHp, baseAtk, baseDef, baseMag);
        world = new World();

        System.out.println("\nWelcome, " + player.getName() + " the " + player.getClassName() + "!");
        System.out.println("Press Enter to begin your adventure...");
        scanner.nextLine();
    }

    private static void loadGame() {
        try {
            FileInputStream fileIn = new FileInputStream("savegame.dat");
            ObjectInputStream in = new ObjectInputStream(fileIn);

            int version = in.readInt();
            if (version != SAVE_VERSION) {
                System.out.println("Save file is from an incompatible version!");
                System.exit(1);
            }

            player = (Player) in.readObject();
            world = (World) in.readObject();

            in.close();
            fileIn.close();

            System.out.println("Game loaded successfully!");
            System.out.println("Press Enter to continue...");
            scanner.nextLine();

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading save file: " + e.getMessage());
            System.out.println("Starting new game instead...");
            createNewGame();
        }
    }

    private static void saveGame() {
        try {
            FileOutputStream fileOut = new FileOutputStream("savegame.dat");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            out.writeInt(SAVE_VERSION);
            out.writeObject(player);
            out.writeObject(world);

            out.close();
            fileOut.close();

            System.out.println("Game saved successfully!");

        } catch (IOException e) {
            System.out.println("Error saving game: " + e.getMessage());
        }
    }

    private static void gameLoop() {
        while (running) {
            clearScreen();
            displayGameInfo();

            System.out.println("\nWhat would you like to do?");
            System.out.println("1. Explore");
            System.out.println("2. View Character");
            System.out.println("3. View Inventory");
            System.out.println("4. Rest (Heal " + (player.getMaxHp() / 5) + " HP)");
            System.out.println("5. Save Game");
            System.out.println("6. Exit Game");
            System.out.print("> ");

            int choice = getIntInput(1, 6);

            switch (choice) {
                case 1:
                    explore();
                    break;
                case 2:
                    viewCharacter();
                    break;
                case 3:
                    manageInventory();
                    break;
                case 4:
                    rest();
                    break;
                case 5:
                    saveGame();
                    waitForEnter();
                    break;
                case 6:
                    confirmExit();
                    break;
            }

            // Check if player is dead
            if (player.getCurrentHp() <= 0) {
                playerDied();
                running = false;
            }
        }
    }

    private static void explore() {
        clearScreen();

        // 70% chance of encounter, 20% chance of finding item, 10% chance of nothing
        int randomEvent = ThreadLocalRandom.current().nextInt(1, 11);

        if (randomEvent <= 7) {
            // Encounter an enemy
            Enemy enemy = world.generateEnemy(player.getLevel());
            combat(enemy);
        } else if (randomEvent <= 9) {
            // Find an item
            Item foundItem = world.generateRandomItem(player.getLevel());
            System.out.println("You found a " + foundItem.getName() + "!");
            System.out.println(foundItem.getDescription());

            System.out.println("\nAdd to inventory? (Y/N)");
            System.out.print("> ");
            String choice = scanner.nextLine().trim().toUpperCase();

            if (choice.equals("Y")) {
                if (player.addToInventory(foundItem)) {
                    System.out.println("Added " + foundItem.getName() + " to inventory.");
                } else {
                    System.out.println("Your inventory is full! You left the item behind.");
                }
            } else {
                System.out.println("You left the item behind.");
            }

            waitForEnter();
        } else {
            // Nothing happens
            System.out.println("You explore the area but find nothing of interest.");
            waitForEnter();
        }
    }

    private static void combat(Enemy enemy) {
        System.out.println("You encountered a " + enemy.getName() + "!");
        System.out.println(enemy.getDescription());
        waitForEnter();

        boolean inCombat = true;
        boolean playerTurn = true;

        while (inCombat) {
            clearScreen();

            // Display combat status
            System.out.println("=== COMBAT ===");
            System.out.println("You: " + player.getName() + " (" + player.getCurrentHp() + "/" + player.getMaxHp() + " HP)");
            System.out.println("Enemy: " + enemy.getName() + " (" + enemy.getCurrentHp() + "/" + enemy.getMaxHp() + " HP)");
            System.out.println("=============");

            if (playerTurn) {
                System.out.println("\nYour turn:");
                System.out.println("1. Attack");
                System.out.println("2. Use Special Ability");
                System.out.println("3. Use Item");
                System.out.println("4. Try to Run");
                System.out.print("> ");

                int choice = getIntInput(1, 4);

                switch (choice) {
                    case 1: // Attack
                        int damage = player.attack();
                        int actualDamage = enemy.takeDamage(damage);
                        System.out.println("You attack the " + enemy.getName() + " for " + actualDamage + " damage!");
                        break;
                    case 2: // Special ability based on class
                        useSpecialAbility(enemy);
                        break;
                    case 3: // Use item
                        if (useItem()) {
                            // If item was used, don't end turn yet
                            continue;
                        }
                        break;
                    case 4: // Run
                        if (ThreadLocalRandom.current().nextInt(1, 101) <= 40) { // 40% chance to escape
                            System.out.println("You successfully escaped!");
                            waitForEnter();
                            return;
                        } else {
                            System.out.println("You failed to escape!");
                        }
                        break;
                }

                waitForEnter();
                playerTurn = false;
            } else {
                // Enemy's turn
                System.out.println("\nEnemy's turn:");
                int damage = enemy.attack();
                int actualDamage = player.takeDamage(damage);
                System.out.println("The " + enemy.getName() + " attacks you for " + actualDamage + " damage!");

                waitForEnter();
                playerTurn = true;
            }

            // Check if combat is over
            if (enemy.getCurrentHp() <= 0) {
                enemyDefeated(enemy);
                inCombat = false;
            } else if (player.getCurrentHp() <= 0) {
                inCombat = false; // Player died, handled in main game loop
            }
        }
    }

    private static void useSpecialAbility(Enemy enemy) {
        switch (player.getClassName()) {
            case "Warrior":
                // Warrior's Mighty Blow: 150% damage but 70% accuracy
                if (ThreadLocalRandom.current().nextInt(1, 101) <= 70) {
                    int damage = (int)(player.attack() * 1.5);
                    int damageDealt = enemy.takeDamage(damage);
                    System.out.println("You perform a MIGHTY BLOW for " + damageDealt + " damage!");
                } else {
                    System.out.println("Your Mighty Blow missed!");
                }
                break;
            case "Mage":
                // Mage's Fireball: Magic-based damage to enemy
                int mageDamage = player.getMagic() * 2;
                int mageDamageDealt = enemy.takeDamage(mageDamage);
                System.out.println("You cast FIREBALL for " + mageDamageDealt + " damage!");
                break;
            case "Rogue":
                // Rogue's Backstab: Chance for critical hit (3x damage)
                if (ThreadLocalRandom.current().nextInt(1, 101) <= 30) {
                    int critDamage = player.attack() * 3;
                    int critDamageDealt = enemy.takeDamage(critDamage);
                    System.out.println("CRITICAL BACKSTAB! You deal " + critDamageDealt + " damage!");
                } else {
                    int normalDamage = player.attack();
                    int normalDamageDealt = enemy.takeDamage(normalDamage);
                    System.out.println("Your backstab deals " + normalDamageDealt + " damage.");
                }
                break;
        }
    }

    private static boolean useItem() {
        if (player.getInventory().isEmpty()) {
            System.out.println("You don't have any items!");
            waitForEnter();
            return false;
        }

        System.out.println("\nSelect an item to use:");
        for (int i = 0; i < player.getInventory().size(); i++) {
            System.out.println((i + 1) + ". " + player.getInventory().get(i).getName());
        }
        System.out.println((player.getInventory().size() + 1) + ". Cancel");
        System.out.print("> ");

        int choice = getIntInput(1, player.getInventory().size() + 1);

        if (choice == player.getInventory().size() + 1) {
            System.out.println("Canceled.");
            return false;
        }

        Item selectedItem = player.getInventory().get(choice - 1);

        if (selectedItem.getType().equals("Potion")) {
            int healAmount = selectedItem.getValue();
            player.heal(healAmount);
            System.out.println("You used " + selectedItem.getName() + " and recovered " + healAmount + " HP!");
            player.getInventory().remove(choice - 1);
            waitForEnter();
            return true;
        } else {
            System.out.println("You can't use that item in combat!");
            waitForEnter();
            return false;
        }
    }

    private static void enemyDefeated(Enemy enemy) {
        int xpGained = enemy.getXpReward();
        int goldGained = enemy.getGoldReward();

        System.out.println("You defeated the " + enemy.getName() + "!");
        System.out.println("You gained " + xpGained + " XP and " + goldGained + " gold!");

        player.addXp(xpGained);
        player.addGold(goldGained);

        // Chance for enemy to drop an item
        if (ThreadLocalRandom.current().nextInt(1, 101) <= 30) { // 30% chance
            Item droppedItem = world.generateRandomItem(player.getLevel());
            System.out.println("The enemy dropped: " + droppedItem.getName() + "!");

            System.out.println("\nAdd to inventory? (Y/N)");
            System.out.print("> ");
            String choice = scanner.nextLine().trim().toUpperCase();

            if (choice.equals("Y")) {
                if (player.addToInventory(droppedItem)) {
                    System.out.println("Added " + droppedItem.getName() + " to inventory.");
                } else {
                    System.out.println("Your inventory is full! You left the item behind.");
                }
            } else {
                System.out.println("You left the item behind.");
            }
        }

        // Check if player leveled up
        if (player.checkLevelUp()) {
            System.out.println("\nLEVEL UP! You are now level " + player.getLevel() + "!");
            System.out.println("Your stats have increased!");
        }

        waitForEnter();
    }

    private static void viewCharacter() {
        clearScreen();
        System.out.println("=== Character Sheet ===");
        System.out.println("Name: " + player.getName() + " the " + player.getClassName());
        System.out.println("Level: " + player.getLevel());
        System.out.println("XP: " + player.getCurrentXp() + "/" + player.getXpToNextLevel());
        System.out.println("Gold: " + player.getGold());
        System.out.println("\nStats:");
        System.out.println("HP: " + player.getCurrentHp() + "/" + player.getMaxHp());
        System.out.println("Attack: " + player.getAttack());
        System.out.println("Defense: " + player.getDefense());
        System.out.println("Magic: " + player.getMagic());

        System.out.println("\nSpecial Ability: ");
        switch (player.getClassName()) {
            case "Warrior":
                System.out.println("Mighty Blow - A powerful attack with 150% damage but 70% accuracy");
                break;
            case "Mage":
                System.out.println("Fireball - A spell that deals damage based on your Magic stat");
                break;
            case "Rogue":
                System.out.println("Backstab - 30% chance to deal triple damage");
                break;
        }

        waitForEnter();
    }

    private static void manageInventory() {
        clearScreen();
        System.out.println("=== Inventory ===");
        if (player.getInventory().isEmpty()) {
            System.out.println("Your inventory is empty.");
            waitForEnter();
            return;
        }

        for (int i = 0; i < player.getInventory().size(); i++) {
            Item item = player.getInventory().get(i);
            System.out.println((i + 1) + ". " + item.getName() + " - " + item.getDescription());
        }

        System.out.println("\nWhat would you like to do?");
        System.out.println("1. Use Item");
        System.out.println("2. Drop Item");
        System.out.println("3. Back to Main Menu");
        System.out.print("> ");

        int choice = getIntInput(1, 3);

        switch (choice) {
            case 1: // Use item
                System.out.println("Select an item to use (or " + (player.getInventory().size() + 1) + " to cancel):");
                System.out.print("> ");

                int itemChoice = getIntInput(1, player.getInventory().size() + 1);

                if (itemChoice == player.getInventory().size() + 1) {
                    System.out.println("Canceled.");
                    break;
                }

                Item selectedItem = player.getInventory().get(itemChoice - 1);

                if (selectedItem.getType().equals("Potion")) {
                    int healAmount = selectedItem.getValue();
                    player.heal(healAmount);
                    System.out.println("You used " + selectedItem.getName() + " and recovered " + healAmount + " HP!");
                    player.getInventory().remove(itemChoice - 1);
                } else if (selectedItem.getType().equals("Equipment")) {
                    player.equipItem(selectedItem);
                    System.out.println("You equipped " + selectedItem.getName() + "!");
                    player.getInventory().remove(itemChoice - 1);
                } else {
                    System.out.println("You can't use that item right now.");
                }
                break;
            case 2: // Drop item
                System.out.println("Select an item to drop (or " + (player.getInventory().size() + 1) + " to cancel):");
                System.out.print("> ");

                int dropChoice = getIntInput(1, player.getInventory().size() + 1);

                if (dropChoice == player.getInventory().size() + 1) {
                    System.out.println("Canceled.");
                    break;
                }

                Item droppedItem = player.getInventory().get(dropChoice - 1);
                player.getInventory().remove(dropChoice - 1);
                System.out.println("You dropped " + droppedItem.getName() + ".");
                break;
            case 3: // Back
                return;
        }

        waitForEnter();
    }

    private static void rest() {
        int healAmount = player.getMaxHp() / 5;
        player.heal(healAmount);
        System.out.println("You rest and recover " + healAmount + " HP.");

        // Small chance of enemy ambush while resting
        if (ThreadLocalRandom.current().nextInt(1, 101) <= 20) { // 20% chance
            System.out.println("But your rest is interrupted!");
            waitForEnter();
            Enemy enemy = world.generateEnemy(player.getLevel());
            combat(enemy);
        } else {
            waitForEnter();
        }
    }

    private static void confirmExit() {
        System.out.println("Would you like to save before exiting? (Y/N)");
        System.out.print("> ");
        String choice = scanner.nextLine().trim().toUpperCase();

        if (choice.equals("Y")) {
            saveGame();
        }

        System.out.println("Thanks for playing!");
        running = false;
    }

    private static void playerDied() {
        clearScreen();
        System.out.println("=== GAME OVER ===");
        System.out.println("You have been defeated!");
        System.out.println("Your final stats:");
        System.out.println("Level: " + player.getLevel());
        System.out.println("Gold collected: " + player.getGold());

        System.out.println("\nWould you like to play again? (Y/N)");
        System.out.print("> ");
        String choice = scanner.nextLine().trim().toUpperCase();

        if (choice.equals("Y")) {
            main(new String[0]); // Restart the game
        } else {
            System.out.println("Thanks for playing!");
            System.exit(0);
        }
    }

    private static void displayGameInfo() {
        System.out.println("=== " + player.getName() + " the " + player.getClassName() + " ===");
        System.out.println("Level: " + player.getLevel() + " | HP: " + player.getCurrentHp() + "/" + player.getMaxHp() + " | Gold: " + player.getGold());
        System.out.println("XP: " + player.getCurrentXp() + "/" + player.getXpToNextLevel());
    }

    private static int getIntInput(int min, int max) {
        while (true) {
            try {
                int input = Integer.parseInt(scanner.nextLine().trim());
                if (input >= min && input <= max) {
                    return input;
                } else {
                    System.out.print("Please enter a number between " + min + " and " + max + ": ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    private static void waitForEnter() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void clearScreen() {
        // This will work in most terminals but not all IDEs
        System.out.print("\033[H\033[2J");
        System.out.flush();

        // Alternative method: print many new lines
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    private static void printTitle() {
        System.out.println("  _______                  _             _   _____  _____   _____ ");
        System.out.println(" |__   __|                (_)           | | |  __ \\|  __ \\ / ____|");
        System.out.println("    | | ___ _ __ _ __ ___  _ _ __   __ _| | | |__) | |__) | |  __ ");
        System.out.println("    | |/ _ \\ '__| '_ ` _ \\| | '_ \\ / _` | | |  _  /|  ___/| | |_ |");
        System.out.println("    | |  __/ |  | | | | | | | | | | (_| | | | | \\ \\| |    | |__| |");
        System.out.println("    |_|\\___|_|  |_| |_| |_|_|_| |_|\\__,_|_| |_|  \\_\\_|     \\_____|");
        System.out.println("                                                                   ");
        System.out.println("              A text-based adventure in your terminal              ");
        System.out.println("====================================================================");
        System.out.println();
    }
}

// Player class
class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String className;
    private int level;
    private int currentXp;
    private int xpToNextLevel;
    private int gold;

    private int maxHp;
    private int currentHp;
    private int baseAttack;
    private int baseDefense;
    private int baseMagic;

    private Item equippedWeapon;
    private Item equippedArmor;

    private List<Item> inventory;
    private static final int MAX_INVENTORY_SIZE = 10;

    public Player(String name, String className, int baseHp, int baseAtk, int baseDef, int baseMag) {
        this.name = name;
        this.className = className;
        this.level = 1;
        this.currentXp = 0;
        this.xpToNextLevel = 100;
        this.gold = 50;

        this.maxHp = baseHp;
        this.currentHp = baseHp;
        this.baseAttack = baseAtk;
        this.baseDefense = baseDef;
        this.baseMagic = baseMag;

        this.inventory = new ArrayList<>();

        // Add starting equipment based on class
        if (className.equals("Warrior")) {
            equippedWeapon = new Item("Rusty Sword", "A basic sword with a dull edge", "Equipment", "Weapon", 5);
            equippedArmor = new Item("Leather Armor", "Basic protective gear", "Equipment", "Armor", 3);
            addToInventory(new Item("Health Potion", "Restores 30 HP", "Potion", "Healing", 30));
        } else if (className.equals("Mage")) {
            equippedWeapon = new Item("Apprentice Staff", "A simple magical staff", "Equipment", "Weapon", 3);
            equippedArmor = new Item("Cloth Robe", "Offers minimal protection", "Equipment", "Armor", 1);
            addToInventory(new Item("Mana Biscuit", "Restores 40 HP", "Potion", "Healing", 40));
        } else if (className.equals("Rogue")) {
            equippedWeapon = new Item("Dull Dagger", "A small but effective blade", "Equipment", "Weapon", 4);
            equippedArmor = new Item("Cloth Vest", "Light and flexible protection", "Equipment", "Armor", 2);
            addToInventory(new Item("Health Potion", "Restores 30 HP", "Potion", "Healing", 30));
        }
    }

    public int attack() {
        int weaponBonus = (equippedWeapon != null) ? equippedWeapon.getValue() : 0;
        int attackValue = baseAttack + weaponBonus;

        // Add some randomness to attack
        return ThreadLocalRandom.current().nextInt(attackValue - 2, attackValue + 3);
    }

    public int takeDamage(int damage) {
        int armorBonus = (equippedArmor != null) ? equippedArmor.getValue() : 0;
        int reducedDamage = Math.max(1, damage - (baseDefense + armorBonus) / 2);
        currentHp -= reducedDamage;
        return reducedDamage;
    }

    public void heal(int amount) {
        currentHp = Math.min(maxHp, currentHp + amount);
    }

    public void addXp(int amount) {
        currentXp += amount;
    }

    public boolean checkLevelUp() {
        if (currentXp >= xpToNextLevel) {
            levelUp();
            return true;
        }
        return false;
    }

    private void levelUp() {
        level++;
        currentXp -= xpToNextLevel;
        xpToNextLevel = 100 * level;

        // Increase stats based on class
        if (className.equals("Warrior")) {
            maxHp += 15;
            baseAttack += 3;
            baseDefense += 2;
            baseMagic += 1;
        } else if (className.equals("Mage")) {
            maxHp += 8;
            baseAttack += 1;
            baseDefense += 1;
            baseMagic += 4;
        } else if (className.equals("Rogue")) {
            maxHp += 10;
            baseAttack += 2;
            baseDefense += 1;
            baseMagic += 2;
        }

        // Heal on level up
        currentHp = maxHp;
    }

    public boolean addToInventory(Item item) {
        if (inventory.size() < MAX_INVENTORY_SIZE) {
            inventory.add(item);
            return true;
        }
        return false;
    }

    public void equipItem(Item item) {
        if (item.getSubtype().equals("Weapon")) {
            if (equippedWeapon != null) {
                inventory.add(equippedWeapon);
            }
            equippedWeapon = item;
        } else if (item.getSubtype().equals("Armor")) {
            if (equippedArmor != null) {
                inventory.add(equippedArmor);
            }
            equippedArmor = item;
        }
    }

    public void addGold(int amount) {
        gold += amount;
    }

    // Getters and setters
    public String getName() { return name; }
    public String getClassName() { return className; }
    public int getLevel() { return level; }
    public int getCurrentXp() { return currentXp; }
    public int getXpToNextLevel() { return xpToNextLevel; }
    public int getGold() { return gold; }
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public int getAttack() {
        int weaponBonus = (equippedWeapon != null) ? equippedWeapon.getValue() : 0;
        return baseAttack + weaponBonus;
    }
    public int getDefense() {
        int armorBonus = (equippedArmor != null) ? equippedArmor.getValue() : 0;
        return baseDefense + armorBonus;
    }
    public int getMagic() { return baseMagic; }
    public List<Item> getInventory() { return inventory; }
}

// Item class
class Item implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private String type; // "Equipment", "Potion", etc.
    private String subtype; // "Weapon", "Armor", "Healing", etc.
    private int value; // Damage for weapons, defense for armor, healing for potions, etc.

    public Item(String name, String description, String type, String subtype, int value) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.name = name;
        this.description = description;
        this.type = type;
        this.subtype = subtype;
        this.value = value;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getSubtype() { return subtype; }
    public int getValue() { return value; }
}

// Enemy class
class Enemy implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private int level;
    private int maxHp;
    private int currentHp;
    private int attack;
    private int defense;
    private int xpReward;
    private int goldReward;

    public Enemy(String name, String description, int level, int maxHp, int attack, int defense, int xpReward, int goldReward) {
        this.name = name;
        this.description = description;
        this.level = level;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.xpReward = xpReward;
        this.goldReward = goldReward;
    }

    public int attack() {
        // Add randomness to attack
        return ThreadLocalRandom.current().nextInt(attack - 2, attack + 3);
    }

    public int takeDamage(int damage) {
        int reducedDamage = Math.max(1, damage - defense / 2);
        currentHp -= reducedDamage;
        return reducedDamage;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getLevel() { return level; }
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public int getXpReward() { return xpReward; }
    public int getGoldReward() { return goldReward; }
}

// World class
class World implements Serializable {
    private static final long serialVersionUID = 1L;

    private String[] enemyNames = {
            "Goblin", "Orc", "Skeleton", "Wolf", "Bandit", "Troll", "Spider",
            "Slime", "Zombie", "Ghost", "Vampire", "Demon", "Dragon"
    };

    private String[] enemyDescriptions = {
            "A small, green creature with sharp teeth.",
            "A brutish humanoid with tusks and green skin.",
            "An animated pile of bones.",
            "A fierce wild canine with sharp teeth.",
            "A ruthless thief looking for easy prey.",
            "A large, brutish creature with regenerative abilities.",
            "A large arachnid with venomous fangs.",
            "A gelatinous blob that oozes across the ground.",
            "A reanimated corpse with a hunger for flesh.",
            "A translucent spirit that wails eerily.",
            "A pale humanoid with fangs and a thirst for blood.",
            "A fiendish creature from another plane.",
            "A massive, scaled beast with fiery breath."
    };

    private String[] weaponNames = {
            "Short Sword", "Long Sword", "War Axe", "Mace", "Spear",
            "Staff of Magic", "Enchanted Wand", "Arcane Scepter",
            "Dagger", "Curved Blade", "Throwing Knives"
    };

    private String[] armorNames = {
            "Chainmail", "Plate Armor", "Shield", "Helmet",
            "Wizard Robe", "Enchanted Cloak", "Arcane Gloves",
            "Leather Armor", "Stealth Cloak", "Shadow Boots"
    };

    private String[] potionNames = {
            "Health Potion", "Greater Health Potion", "Healing Elixir",
            "Rejuvenation Tonic", "Vitality Draught", "Mending Salve"
    };

    public Enemy generateEnemy(int playerLevel) {
        // Adjust difficulty based on player level
        int enemyLevel = Math.max(1, playerLevel + ThreadLocalRandom.current().nextInt(-1, 2));

        // Select enemy type based on player level
        int index;
        if (playerLevel < 3) {
            // Low level enemies only
            index = ThreadLocalRandom.current().nextInt(0, 5);
        } else if (playerLevel < 7) {
            // Mid-range enemies
            index = ThreadLocalRandom.current().nextInt(3, 9);
        } else {
            // Any enemy, more likely to be high level
            index = ThreadLocalRandom.current().nextInt(5, enemyNames.length);
        }

        String name = enemyNames[index];
        String description = enemyDescriptions[index];

        // Scale stats based on enemy level
        int maxHp = (20 + 10 * index) * enemyLevel / 2;
        int attack = (5 + index) * enemyLevel / 2;
        int defense = (2 + index / 2) * enemyLevel / 2;

        // Rewards
        int xpReward = (10 + 5 * index) * enemyLevel;
        int goldReward = (5 + 3 * index) * enemyLevel;

        return new Enemy(name, description, enemyLevel, maxHp, attack, defense, xpReward, goldReward);
    }

    public Item generateRandomItem(int playerLevel) {
        String type = randomItemType();

        if (type.equals("Equipment")) {
            String subtype = ThreadLocalRandom.current().nextInt(2) == 0 ? "Weapon" : "Armor";

            if (subtype.equals("Weapon")) {
                int index = ThreadLocalRandom.current().nextInt(weaponNames.length);
                String name = weaponNames[index];
                String description = "A quality weapon that improves your attack.";
                int value = 3 + index + playerLevel;

                return new Item(name, description, type, subtype, value);
            } else {
                int index = ThreadLocalRandom.current().nextInt(armorNames.length);
                String name = armorNames[index];
                String description = "Protective gear that increases your defense.";
                int value = 2 + index / 2 + playerLevel;

                return new Item(name, description, type, subtype, value);
            }
        } else { // Potion
            int index = ThreadLocalRandom.current().nextInt(potionNames.length);
            String name = potionNames[index];
            int healAmount = 20 + 10 * index + 5 * playerLevel;
            String description = "Restores " + healAmount + " HP when used.";

            return new Item(name, description, "Potion", "Healing", healAmount);
        }
    }

    private String randomItemType() {
        // 60% chance for equipment, 40% chance for potion
        return ThreadLocalRandom.current().nextInt(100) < 60 ? "Equipment" : "Potion";
    }
}