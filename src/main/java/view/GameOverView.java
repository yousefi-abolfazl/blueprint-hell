package view;

import controller.SceneController;
import model.Game;

import javax.swing.*;
import java.awt.*;

public class GameOverView extends JDialog {
    private Game game;
    
    public GameOverView(JFrame parent) {
        super(parent, "Game Over", true);
        this.game = Game.getInstance();
        
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        initializeComponents();
    }
    
    private void initializeComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Game Over Label
        JLabel gameOverLabel = new JLabel("GAME OVER");
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gameOverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Stats
        JLabel packetLossLabel = new JLabel("Packet Loss: " + game.getPacketLoss() + "%");
        packetLossLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel coinsLabel = new JLabel("Coins Collected: " + game.getCoins());
        coinsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Game Level
        JLabel levelLabel = new JLabel("Level: " + game.getCurrentLevel());
        levelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Buttons
        JButton retryButton = new JButton("Retry Level");
        retryButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        retryButton.addActionListener(e -> {
            dispose();
            SceneController.getInstance().startLevel(game.getCurrentLevel());
        });
        
        JButton menuButton = new JButton("Return to Menu");
        menuButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuButton.addActionListener(e -> {
            dispose();
            SceneController.getInstance().showMainMenu();
        });
        
        // Add components to panel
        mainPanel.add(gameOverLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(packetLossLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(coinsLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(levelLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(retryButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(menuButton);
        
        add(mainPanel, BorderLayout.CENTER);
    }
} 