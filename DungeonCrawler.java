import java.util.Scanner;
import java.util.Random;

public class DungeonCrawler {
    static class Player {
        String name;
        int health;
        int maxHealth;
        int attack;
        int numPotions;
        int roomsCleared;

        public Player(String name) {
            this.name = name;
            this.maxHealth = 100;
            this.health = maxHealth;
            this.attack = 15;
            this.numPotions = 3;
            this.roomsCleared = 0;
        }

        public boolean isAlive() {
            return health > 0;
        }

        public void takeDamage(int damage) {
            health -= damage;
            if (health < 0) health = 0;
        }

        public void usePotion() {
            if (numPotions > 0) {
                Random rand = new Random();
                int heal = rand.nextInt(11) + 15; // Heals 15-25 HP
                health = Math.min(health + heal, maxHealth);
                numPotions--;
                System.out.println("You healed for " + heal + " HP!");
            } else {
                System.out.println("No potions left!");
            }
        }
    }

    static class Enemy {
        String name;
        int health;
        int attack;

        public Enemy(String name, int health, int attack) {
            this.name = name;
            this.health = health;
            this.attack = attack;
        }

        public boolean isAlive() {
            return health > 0;
        }

        public void takeDamage(int damage) {
            health -= damage;
            if (health < 0) health = 0;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        System.out.println("Welcome to Dungeon Crawler!");
        System.out.print("Enter your name: ");
        String playerName = scanner.nextLine();
        Player player = new Player(playerName);

        System.out.println("\nWelcome, " + player.name + "! Your journey begins...");

        boolean gameOver = false;

        while (!gameOver && player.isAlive()) {
            System.out.println("\n----------------------------");
            System.out.println("Current HP: " + player.health + "/" + player.maxHealth);
            System.out.println("Potions: " + player.numPotions);
            System.out.println("Rooms cleared: " + player.roomsCleared);
            System.out.print("\nProceed to next room? (y/n): ");
            String choice = scanner.nextLine().trim().toLowerCase();

            if (!choice.equals("y")) {
                System.out.println("You flee the dungeon...");
                gameOver = true;
                continue;
            }

            player.roomsCleared++;
            System.out.println("\nEntering Room #" + player.roomsCleared + "...");

            // Random event (60% combat, 20% potion, 20% trap)
            int eventRoll = random.nextInt(10);
            if (eventRoll < 6) { // Combat encounter
                Enemy enemy;
                if (player.roomsCleared < 5) {
                    enemy = random.nextBoolean() ?
                            new Enemy("Goblin", 30, 10) :
                            new Enemy("Skeleton", 40, 15);
                } else { // Final boss
                    enemy = new Enemy("Dragon Lord", 100, 20);
                }
                System.out.println("A wild " + enemy.name + " appears!");

                // Combat loop
                boolean inCombat = true;
                while (inCombat && player.isAlive() && enemy.isAlive()) {
                    System.out.println("\n" + player.name + " HP: " + player.health + "/" + player.maxHealth);
                    System.out.println(enemy.name + " HP: " + enemy.health);
                    System.out.print("Choose action: (1) Attack (2) Use Potion: ");
                    String action = scanner.nextLine().trim();

                    if (action.equals("1")) { // Attack
                        int damage = random.nextInt(player.attack) + 1;
                        enemy.takeDamage(damage);
                        System.out.println("You hit the " + enemy.name + " for " + damage + " damage!");

                        if (!enemy.isAlive()) {
                            System.out.println("You defeated the " + enemy.name + "!");
                            inCombat = false;
                            continue;
                        }

                        // Enemy attack
                        int enemyDamage = random.nextInt(enemy.attack) + 1;
                        player.takeDamage(enemyDamage);
                        System.out.println("The " + enemy.name + " hits you for " + enemyDamage + " damage!");

                    } else if (action.equals("2")) { // Use potion
                        player.usePotion();

                        // Enemy attack
                        int enemyDamage = random.nextInt(enemy.attack) + 1;
                        player.takeDamage(enemyDamage);
                        System.out.println("The " + enemy.name + " hits you for " + enemyDamage + " damage!");
                    } else {
                        System.out.println("Invalid choice!");
                    }
                }

                if (!player.isAlive()) {
                    System.out.println("\nYOU DIED... GAME OVER!");
                    gameOver = true;
                } else if (enemy.name.equals("Dragon Lord")) {
                    System.out.println("\nCONGRATULATIONS! You've defeated the Dragon Lord and conquered the dungeon!");
                    gameOver = true;
                }
            } else if (eventRoll < 8) { // Found potion
                player.numPotions++;
                System.out.println("You found a potion! Total potions: " + player.numPotions);
            } else { // Trap
                int trapDamage = random.nextInt(10) + 5;
                player.takeDamage(trapDamage);
                System.out.println("You triggered a trap! You take " + trapDamage + " damage!");
                if (!player.isAlive()) {
                    System.out.println("\nYOU DIED... GAME OVER!");
                    gameOver = true;
                }
            }
        }
        scanner.close();
    }
}