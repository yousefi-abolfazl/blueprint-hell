package controller;

import model.Game;
import view.GameFrame;
import view.GamePanel;
import view.MainMenuView;

import javax.swing.*;
import java.awt.*;

public class SceneController {
    private static SceneController INSTANCE;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private Timer gameTimer;
    
    private SceneController() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // Add scenes to card layout
        contentPanel.add(new MainMenuView(), "MainMenu");
        contentPanel.add(GamePanel.getInstance(), "GamePanel");
        
        // Set up game timer for updates
        gameTimer = new Timer(16, e -> {
            if (cardLayout.toString().contains("GamePanel")) {
                updateGame();
            }
        });
        gameTimer.start();
        
        // Set the content panel in the game frame
        GameFrame.getINSTANCE().setContentPane(contentPanel);
        GameFrame.getINSTANCE().revalidate();
    }
    
    private void updateGame() {
        // Update the game model
        Game.getInstance().update();
        
        // Repaint the panel
        GamePanel.getInstance().repaint();
        
        // Check game over condition
        if (Game.getInstance().isGameOver()) {
            showGameOver();
        }
    }
    
    public static SceneController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SceneController();
        }
        return INSTANCE;
    }
    
    public void showMainMenu() {
        cardLayout.show(contentPanel, "MainMenu");
    }
    
    public void startGame() {
        setupLevel(Game.getInstance().getCurrentLevel());
        cardLayout.show(contentPanel, "GamePanel");
    }
    
    public void startLevel(int level) {
        Game.getInstance().setCurrentLevel(level);
        setupLevel(level);
        cardLayout.show(contentPanel, "GamePanel");
    }
    
    private void setupLevel(int level) {
        // Reset game state
        Game.getInstance().resetGame();
        Game.getInstance().setCurrentLevel(level);
        
        // Load level systems and connections based on level number
        if (level == 1) {
            setupLevel1();
        } else if (level == 2) {
            setupLevel2();
        }
    }
    
    private void setupLevel1() {
        // Level 1 setup would be implemented here
        // This would create systems, ports, and initial connections
    }
    
    private void setupLevel2() {
        // Level 2 setup would be implemented here  
    }
    
    public void showGameOver() {
        // Show game over screen
        JOptionPane.showMessageDialog(
            contentPanel,
            "Game Over! Packet Loss: " + Game.getInstance().getPacketLoss() + "%",
            "Game Over",
            JOptionPane.INFORMATION_MESSAGE
        );
        
        showMainMenu();
    }
    
    public void exitGame() {
        System.exit(0);
    }
    
    public void toggleShop() {
        // This would open the shop UI overlay
        
    }
}
