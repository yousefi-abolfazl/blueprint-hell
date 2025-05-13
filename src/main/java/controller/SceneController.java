package controller;

import model.*;
import view.GameFrame;
import view.GamePanel;
import view.MainMenuView;
import view.ShopView;

import javax.swing.*;
import java.awt.*;

public class SceneController {
    private static SceneController INSTANCE;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private Timer gameTimer;
    private String currentCard = "MainMenu";
    
    private SceneController() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // Add scenes to card layout
        contentPanel.add(new MainMenuView(), "MainMenu");
        contentPanel.add(GamePanel.getInstance(), "GamePanel");
        
        // Set up game timer for updates
        gameTimer = new Timer(16, e -> {
            if (currentCard.equals("GamePanel") && contentPanel.isShowing()) {
                updateGame();
            }
        });
        gameTimer.start();
        
        // Set the content panel in the game frame
        GameFrame.getINSTANCE().setContentPane(contentPanel);
        GameFrame.getINSTANCE().revalidate();
        
        // Initialize sound manager
        SoundManager.getInstance();
    }
    
    private void updateGame() {
        // Call the GamePanel update method directly
        GamePanel.getInstance().update();
        
        // Update HUD and repaint
        GamePanel.getInstance().updateHUD();
        GamePanel.getInstance().repaint();
        
        // Check game over condition
        if (Game.getInstance().isGameOver()) {
            showGameOver();
        }
        
        // Check level completion
        if (isLevelComplete()) {
            showLevelComplete();
        }
    }
    
    private boolean isLevelComplete() {
        // Level is complete if all destination systems have received packets
        Game game = Game.getInstance();
        int totalPacketsReceived = 0;
        int requiredPackets = 0;
        
        for (NetworkSystem system : game.getSystems()) {
            if (system instanceof DestinationSystem) {
                totalPacketsReceived += ((DestinationSystem) system).getPacketsReceived();
                requiredPackets += 5; // Each destination needs 5 packets
            }
        }
        
        return totalPacketsReceived >= requiredPackets && !game.isGameOver();
    }
    
    public static SceneController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SceneController();
        }
        return INSTANCE;
    }
    
    public void showMainMenu() {
        currentCard = "MainMenu";
        cardLayout.show(contentPanel, currentCard);
    }
    
    public void startGame() {
        setupLevel(Game.getInstance().getCurrentLevel());
        currentCard = "GamePanel";
        cardLayout.show(contentPanel, currentCard);
        
        System.out.println("Starting game: requesting focus on GamePanel");
        
        // Make sure panel gets focus so keyboard controls work
        SwingUtilities.invokeLater(() -> {
            GamePanel panel = GamePanel.getInstance();
            panel.requestFocusInWindow();
            
            // First try this
            if (!panel.hasFocus()) {
                System.out.println("First focus attempt failed, trying again...");
                panel.requestFocus();
            }
            
            // If that didn't work, try with a delay
            if (!panel.hasFocus()) {
                System.out.println("Second focus attempt failed, trying with delay...");
                Timer focusTimer = new Timer(100, e -> {
                    panel.requestFocusInWindow();
                    ((Timer)e.getSource()).stop();
                });
                focusTimer.setRepeats(false);
                focusTimer.start();
            }
        });
        
        SoundManager.getInstance().startBackgroundMusic();
    }
    
    public void startLevel(int level) {
        Game.getInstance().setCurrentLevel(level);
        setupLevel(level);
        currentCard = "GamePanel";
        cardLayout.show(contentPanel, currentCard);
        
        System.out.println("Starting level " + level + ": requesting focus on GamePanel");
        
        // Make sure panel gets focus so keyboard controls work
        SwingUtilities.invokeLater(() -> {
            GamePanel panel = GamePanel.getInstance();
            panel.requestFocusInWindow();
            
            // First try this
            if (!panel.hasFocus()) {
                System.out.println("First focus attempt failed, trying again...");
                panel.requestFocus();
            }
            
            // If that didn't work, try with a delay
            if (!panel.hasFocus()) {
                System.out.println("Second focus attempt failed, trying with delay...");
                Timer focusTimer = new Timer(100, e -> {
                    panel.requestFocusInWindow();
                    ((Timer)e.getSource()).stop();
                });
                focusTimer.setRepeats(false);
                focusTimer.start();
            }
        });
        
        SoundManager.getInstance().startBackgroundMusic();
    }
    
    private void setupLevel(int level) {
        // Reset game state
        Game.getInstance().resetGame();
        Game.getInstance().setCurrentLevel(level);
        
        // Load level in the GamePanel
        GamePanel.getInstance().loadLevel(level);
    }
    
    public void showGameOver() {
        SoundManager.getInstance().playSound("game_over");
        JOptionPane.showMessageDialog(
            contentPanel,
            "Game Over! Packet Loss: " + Game.getInstance().getPacketLoss() + "%",
            "Game Over",
            JOptionPane.INFORMATION_MESSAGE
        );
        
        showMainMenu();
    }
    
    public void showLevelComplete() {
        Game game = Game.getInstance();
        
        SoundManager.getInstance().playSound("level_complete");
        int currentLevel = game.getCurrentLevel();
        
        JOptionPane.showMessageDialog(
            contentPanel,
            "Level " + currentLevel + " Complete!\n" +
            "Packet Loss: " + game.getPacketLoss() + "%\n" +
            "Coins Collected: " + game.getCoins(),
            "Level Complete",
            JOptionPane.INFORMATION_MESSAGE
        );
        
        if (currentLevel < 2) { // We have 2 levels total
            startLevel(currentLevel + 1);
        } else {
            JOptionPane.showMessageDialog(
                contentPanel,
                "Congratulations! You've completed all available levels.",
                "Game Complete",
                JOptionPane.INFORMATION_MESSAGE
            );
            showMainMenu();
        }
    }
    
    public void exitGame() {
        System.exit(0);
    }
    
    public void toggleShop() {
        Game.getInstance().setPaused(true);
        ShopView shopView = new ShopView(GameFrame.getINSTANCE());
        shopView.setVisible(true);
        Game.getInstance().setPaused(false);
    }
}
