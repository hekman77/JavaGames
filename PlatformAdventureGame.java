// BROKEN JAVA PLATFORMER GAME MADE BY CLAUDE AI CLAUDE 3.7 SONNET

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class PlatformAdventureGame extends JFrame {
    // Main class to initialize the game
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PlatformAdventureGame();
        });
    }

    // Game constants
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int FPS = 60;
    private static final int DELAY = 1000 / FPS;

    // Game objects
    private GamePanel gamePanel;
    private javax.swing.Timer gameTimer;
    private GameState gameState;

    public PlatformAdventureGame() {
        setTitle("Platform Adventure");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gameState = new GameState();
        gamePanel = new GamePanel(gameState);
        add(gamePanel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        initInput();
        startGameLoop();
    }

    private void initInput() {
        InputHandler inputHandler = new InputHandler(gameState);
        addKeyListener(inputHandler);
        setFocusable(true);
        requestFocus();
    }

    private void startGameLoop() {
        gameTimer = new javax.swing.Timer(DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameState.update();
                gamePanel.repaint();
            }
        });
        gameTimer.start();
    }
}

// Game state that holds game logic and objects
class GameState {
    private Player player;
    private ArrayList<Platform> platforms;
    private ArrayList<Enemy> enemies;
    private ArrayList<Collectible> collectibles;
    private int currentLevel;
    private boolean gameOver;
    private int score;
    private Map<Integer, Level> levels;

    public GameState() {
        currentLevel = 1;
        score = 0;
        gameOver = false;
        levels = new HashMap<>();

        // Create level designs
        initLevels();
        loadLevel(currentLevel);
    }

    private void initLevels() {
        // Level 1
        Level level1 = new Level();
        level1.addPlatform(new Platform(0, 500, 300, 20));
        level1.addPlatform(new Platform(400, 500, 400, 20));
        level1.addPlatform(new Platform(200, 400, 200, 20));
        level1.addPlatform(new Platform(500, 350, 200, 20));
        level1.addEnemy(new Enemy(450, 480, 100, 50));
        level1.addCollectible(new Collectible(250, 380, 20, 20, 10));
        level1.addCollectible(new Collectible(550, 330, 20, 20, 20));
        level1.setStartPosition(50, 450);
        level1.setExitPosition(700, 330);
        levels.put(1, level1);

        // Level 2
        Level level2 = new Level();
        level2.addPlatform(new Platform(0, 550, 800, 20));
        level2.addPlatform(new Platform(100, 450, 150, 20));
        level2.addPlatform(new Platform(300, 400, 150, 20));
        level2.addPlatform(new Platform(500, 350, 150, 20));
        level2.addPlatform(new Platform(300, 250, 200, 20));
        level2.addPlatform(new Platform(100, 200, 150, 20));
        level2.addEnemy(new Enemy(150, 430, 80, 40));
        level2.addEnemy(new Enemy(350, 380, 80, 40));
        level2.addEnemy(new Enemy(550, 330, 80, 40));
        level2.addCollectible(new Collectible(150, 180, 20, 20, 30));
        level2.addCollectible(new Collectible(400, 230, 20, 20, 30));
        level2.addCollectible(new Collectible(600, 330, 20, 20, 20));
        level2.setStartPosition(50, 500);
        level2.setExitPosition(130, 180);
        levels.put(2, level2);

        // Level 3
        Level level3 = new Level();
        level3.addPlatform(new Platform(0, 550, 800, 20));
        level3.addPlatform(new Platform(50, 450, 100, 20));
        level3.addPlatform(new Platform(200, 400, 100, 20));
        level3.addPlatform(new Platform(350, 350, 100, 20));
        level3.addPlatform(new Platform(500, 300, 100, 20));
        level3.addPlatform(new Platform(650, 250, 100, 20));
        level3.addPlatform(new Platform(500, 200, 100, 20));
        level3.addPlatform(new Platform(350, 150, 100, 20));
        level3.addPlatform(new Platform(200, 100, 100, 20));
        level3.addEnemy(new Enemy(80, 430, 50, 30));
        level3.addEnemy(new Enemy(230, 380, 50, 30));
        level3.addEnemy(new Enemy(380, 330, 50, 30));
        level3.addEnemy(new Enemy(530, 280, 50, 30));
        level3.addEnemy(new Enemy(680, 230, 50, 30));
        level3.addEnemy(new Enemy(530, 180, 50, 30));
        level3.addEnemy(new Enemy(380, 130, 50, 30));
        level3.addCollectible(new Collectible(100, 430, 20, 20, 20));
        level3.addCollectible(new Collectible(250, 380, 20, 20, 20));
        level3.addCollectible(new Collectible(400, 330, 20, 20, 20));
        level3.addCollectible(new Collectible(550, 280, 20, 20, 20));
        level3.addCollectible(new Collectible(700, 230, 20, 20, 20));
        level3.addCollectible(new Collectible(550, 180, 20, 20, 20));
        level3.addCollectible(new Collectible(400, 130, 20, 20, 20));
        level3.addCollectible(new Collectible(250, 80, 20, 20, 100));
        level3.setStartPosition(50, 500);
        level3.setExitPosition(250, 80);
        levels.put(3, level3);
    }

    public void loadLevel(int levelNum) {
        Level level = levels.get(levelNum);
        if (level != null) {
            platforms = new ArrayList<>(level.getPlatforms());
            enemies = new ArrayList<>(level.getEnemies());
            collectibles = new ArrayList<>(level.getCollectibles());
            player = new Player(level.getStartX(), level.getStartY());
            currentLevel = levelNum;
        } else {
            // Game completed
            gameOver = true;
        }
    }

    public void update() {
        if (gameOver) return;

        player.update();

        // Check platform collisions
        player.setOnGround(false);
        for (Platform platform : platforms) {
            if (player.collidesWith(platform)) {
                player.handlePlatformCollision(platform);
            }
        }

        // Check enemy collisions
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            enemy.update();

            // Update enemy platform collisions
            for (Platform platform : platforms) {
                if (enemy.collidesWith(platform)) {
                    enemy.handlePlatformCollision(platform);
                }
            }

            if (player.collidesWith(enemy)) {
                if (player.getVelocityY() > 0 && player.getY() + player.getHeight() < enemy.getY() + enemy.getHeight() / 2) {
                    // Player jumped on enemy
                    enemyIterator.remove();
                    player.setVelocityY(-15); // Bounce
                    score += 50;
                } else {
                    // Player collided with enemy
                    player.takeDamage(1);
                    if (player.getHealth() <= 0) {
                        gameOver = true;
                    }
                }
            }
        }

        // Check collectible collisions
        Iterator<Collectible> collectibleIterator = collectibles.iterator();
        while (collectibleIterator.hasNext()) {
            Collectible collectible = collectibleIterator.next();
            if (player.collidesWith(collectible)) {
                collectibleIterator.remove();
                score += collectible.getValue();
            }
        }

        // Check level exit (imagine it's at the top right corner)
        if (player.getX() > levels.get(currentLevel).getExitX() &&
                player.getY() < levels.get(currentLevel).getExitY() + 50) {
            currentLevel++;
            loadLevel(currentLevel);
        }

        // Check if player fell off the screen
        if (player.getY() > 600) {
            player.takeDamage(1);
            if (player.getHealth() <= 0) {
                gameOver = true;
            } else {
                // Reset player position
                player.setX(levels.get(currentLevel).getStartX());
                player.setY(levels.get(currentLevel).getStartY());
                player.setVelocityY(0);
            }
        }
    }

    // Getters
    public Player getPlayer() { return player; }
    public ArrayList<Platform> getPlatforms() { return platforms; }
    public ArrayList<Enemy> getEnemies() { return enemies; }
    public ArrayList<Collectible> getCollectibles() { return collectibles; }
    public boolean isGameOver() { return gameOver; }
    public int getScore() { return score; }
    public int getCurrentLevel() { return currentLevel; }
    public Map<Integer, Level> getLevels() { return levels; } // Added getter for levels
}

// Level class to hold level design
class Level {
    private ArrayList<Platform> platforms;
    private ArrayList<Enemy> enemies;
    private ArrayList<Collectible> collectibles;
    private int startX, startY;
    private int exitX, exitY;

    public Level() {
        platforms = new ArrayList<>();
        enemies = new ArrayList<>();
        collectibles = new ArrayList<>();
    }

    public void addPlatform(Platform platform) {
        platforms.add(platform);
    }

    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    public void addCollectible(Collectible collectible) {
        collectibles.add(collectible);
    }

    public void setStartPosition(int x, int y) {
        startX = x;
        startY = y;
    }

    public void setExitPosition(int x, int y) {
        exitX = x;
        exitY = y;
    }

    public ArrayList<Platform> getPlatforms() { return platforms; }
    public ArrayList<Enemy> getEnemies() { return enemies; }
    public ArrayList<Collectible> getCollectibles() { return collectibles; }
    public int getStartX() { return startX; }
    public int getStartY() { return startY; }
    public int getExitX() { return exitX; }
    public int getExitY() { return exitY; }
}

// Game panel for rendering
class GamePanel extends JPanel {
    private GameState gameState;
    private Image backgroundImage;

    public GamePanel(GameState gameState) {
        this.gameState = gameState;
        setPreferredSize(new Dimension(800, 600));
        // Load background image
        // backgroundImage = new ImageIcon("background.png").getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw background
        g2d.setColor(new Color(135, 206, 235)); // Sky blue
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw platforms
        g2d.setColor(new Color(34, 139, 34)); // Forest green
        for (Platform platform : gameState.getPlatforms()) {
            g2d.fillRect(platform.getX(), platform.getY(), platform.getWidth(), platform.getHeight());
        }

        // Draw collectibles
        g2d.setColor(Color.YELLOW);
        for (Collectible collectible : gameState.getCollectibles()) {
            g2d.fillOval(collectible.getX(), collectible.getY(), collectible.getWidth(), collectible.getHeight());
        }

        // Draw enemies
        g2d.setColor(Color.RED);
        for (Enemy enemy : gameState.getEnemies()) {
            g2d.fillRect(enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight());
        }

        // Draw player
        Player player = gameState.getPlayer();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(player.getX(), player.getY(), player.getWidth(), player.getHeight());

        // Draw exit
        Level currentLevel = gameState.getLevels().get(gameState.getCurrentLevel());
        if (currentLevel != null) {
            g2d.setColor(new Color(255, 215, 0)); // Gold
            g2d.fillRect(currentLevel.getExitX(), currentLevel.getExitY(), 30, 30);
        }

        // Draw UI information
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Level: " + gameState.getCurrentLevel(), 20, 30);
        g2d.drawString("Score: " + gameState.getScore(), 20, 50);
        g2d.drawString("Health: " + gameState.getPlayer().getHealth(), 20, 70);
        g2d.drawString("Controls: W (jump), A (left), D (right), R (restart)", 20, 90);

        // Draw game over message if needed
        if (gameState.isGameOver()) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 36));

            if (gameState.getCurrentLevel() > 3) {
                g2d.drawString("Game Completed!", 250, 250);
            } else {
                g2d.drawString("Game Over", 300, 250);
            }

            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString("Final Score: " + gameState.getScore(), 310, 300);
            g2d.drawString("Press R to restart", 310, 350);
        }
    }
}

// Input handler for keyboard controls
class InputHandler extends KeyAdapter {
    private GameState gameState;
    private boolean leftPressed, rightPressed;

    public InputHandler(GameState gameState) {
        this.gameState = gameState;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (gameState.isGameOver()) {
            if (keyCode == KeyEvent.VK_R) {
                // Restart game
                gameState = new GameState();
            }
            return;
        }

        switch (keyCode) {
            case KeyEvent.VK_A:  // Changed from LEFT to A
                leftPressed = true;
                gameState.getPlayer().setVelocityX(-5);
                break;
            case KeyEvent.VK_D:  // Changed from RIGHT to D
                rightPressed = true;
                gameState.getPlayer().setVelocityX(5);
                break;
            case KeyEvent.VK_W:  // Changed from UP to W
                if (gameState.getPlayer().isOnGround()) {
                    gameState.getPlayer().setVelocityY(-15);
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

        switch (keyCode) {
            case KeyEvent.VK_A:  // Changed from LEFT to A
                leftPressed = false;
                if (rightPressed) {
                    gameState.getPlayer().setVelocityX(5);
                } else {
                    gameState.getPlayer().setVelocityX(0);
                }
                break;
            case KeyEvent.VK_D:  // Changed from RIGHT to D
                rightPressed = false;
                if (leftPressed) {
                    gameState.getPlayer().setVelocityX(-5);
                } else {
                    gameState.getPlayer().setVelocityX(0);
                }
                break;
        }
    }
}

// Game object classes
abstract class GameObject {
    protected int x, y, width, height;

    public GameObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean collidesWith(GameObject other) {
        return x < other.x + other.width &&
                x + width > other.x &&
                y < other.y + other.height &&
                y + height > other.y;
    }

    // Getters and setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}

class Player extends GameObject {
    private static final double GRAVITY = 0.8;
    private double velocityX, velocityY;
    private boolean onGround;
    private int health;

    public Player(int x, int y) {
        super(x, y, 30, 50);
        velocityX = 0;
        velocityY = 0;
        onGround = false;
        health = 3;
    }

    public void update() {
        // Apply gravity
        if (!onGround) {
            velocityY += GRAVITY;
        }

        // Update position
        x += velocityX;
        y += velocityY;

        // Keep player within bounds
        if (x < 0) x = 0;
        if (x + width > 800) x = 800 - width;
    }

    public void handlePlatformCollision(Platform platform) {
        Rectangle playerRect = new Rectangle(x, y, width, height);
        Rectangle platformRect = new Rectangle(platform.getX(), platform.getY(), platform.getWidth(), platform.getHeight());
        Rectangle intersection = playerRect.intersection(platformRect);

        // Bottom collision (player is on top of platform)
        if (intersection.width > intersection.height && velocityY > 0 && y + height - intersection.height <= platform.getY()) {
            y = platform.getY() - height;
            velocityY = 0;
            onGround = true;
        }
        // Top collision (player is below platform)
        else if (intersection.width > intersection.height && velocityY < 0) {
            y = platform.getY() + platform.getHeight();
            velocityY = 0;
        }
        // Left collision (player is to the right of platform)
        else if (intersection.width < intersection.height && velocityX < 0) {
            x = platform.getX() + platform.getWidth();
            velocityX = 0;
        }
        // Right collision (player is to the left of platform)
        else if (intersection.width < intersection.height && velocityX > 0) {
            x = platform.getX() - width;
            velocityX = 0;
        }
    }

    public void takeDamage(int amount) {
        health -= amount;
    }

    // Getters and setters
    public double getVelocityX() { return velocityX; }
    public void setVelocityX(double velocityX) { this.velocityX = velocityX; }
    public double getVelocityY() { return velocityY; }
    public void setVelocityY(double velocityY) { this.velocityY = velocityY; }
    public boolean isOnGround() { return onGround; }
    public void setOnGround(boolean onGround) { this.onGround = onGround; }
    public int getHealth() { return health; }
}

class Platform extends GameObject {
    public Platform(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
}

class Enemy extends GameObject {
    private double velocityX, velocityY;
    private int movementRange;
    private int startX;

    public Enemy(int x, int y, int width, int height) {
        super(x, y, width, height);
        velocityX = 2;
        velocityY = 0;
        startX = x;
        movementRange = 100;
    }

    public void update() {
        // Apply gravity
        velocityY += 0.8;

        // Update position
        x += velocityX;
        y += velocityY;

        // Handle movement range
        if (x > startX + movementRange) {
            velocityX = -2;
        } else if (x < startX - movementRange) {
            velocityX = 2;
        }
    }

    public void handlePlatformCollision(Platform platform) {
        Rectangle enemyRect = new Rectangle(x, y, width, height);
        Rectangle platformRect = new Rectangle(platform.getX(), platform.getY(), platform.getWidth(), platform.getHeight());
        Rectangle intersection = enemyRect.intersection(platformRect);

        // Bottom collision (enemy is on top of platform)
        if (intersection.width > intersection.height && velocityY > 0 && y + height - intersection.height <= platform.getY()) {
            y = platform.getY() - height;
            velocityY = 0;
        }
        // Top collision (enemy is below platform)
        else if (intersection.width > intersection.height && velocityY < 0) {
            y = platform.getY() + platform.getHeight();
            velocityY = 0;
        }
        // Left collision (enemy is to the right of platform)
        else if (intersection.width < intersection.height && velocityX < 0) {
            x = platform.getX() + platform.getWidth();
            velocityX = -velocityX;
        }
        // Right collision (enemy is to the left of platform)
        else if (intersection.width < intersection.height && velocityX > 0) {
            x = platform.getX() - width;
            velocityX = -velocityX;
        }
    }
}

class Collectible extends GameObject {
    private int value;

    public Collectible(int x, int y, int width, int height, int value) {
        super(x, y, width, height);
        this.value = value;
    }

    public int getValue() { return value; }
}