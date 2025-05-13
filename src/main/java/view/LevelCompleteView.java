package view;

import controller.SceneController;
import model.Game;

import javax.swing.*;
import java.awt.*;

public class LevelCompleteView extends JDialog {
    private Game game;
    
    public LevelCompleteView(JFrame parent) {
        super(parent, "Level Complete", true);
        this.game = Game.getInstance();
        
        setSize(400, 350);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        initializeComponents();
    }
    
    private void initializeComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Success Label
        JLabel successLabel = new JLabel("LEVEL COMPLETE!");
        successLabel.setFont(new Font("Arial", Font.BOLD, 24));
        successLabel.setForeground(new Color(0, 150, 0));
        successLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Stats
        JLabel packetLossLabel = new JLabel("Packet Loss: " + game.getPacketLoss() + "%");
        packetLossLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel coinsLabel = new JLabel("Coins Collected: " + game.getCoins());
        coinsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Level
        JLabel levelLabel = new JLabel("Level " + game.getCurrentLevel() + " Completed!");
        levelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Buttons
        JButton nextLevelButton = new JButton("Next Level");
        nextLevelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextLevelButton.addActionListener(e -> {
            dispose();
            int nextLevel = game.getCurrentLevel() + 1;
            // Check if next level exists
            if (nextLevel <= 2) { // Currently only 2 levels
                SceneController.getInstance().startLevel(nextLevel);
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Congratulations! You've completed all available levels.",
                    "Game Complete",
                    JOptionPane.INFORMATION_MESSAGE
                );
                SceneController.getInstance().showMainMenu();
            }
        });
        
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
        mainPanel.add(successLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(levelLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(packetLossLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(coinsLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(nextLevelButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(retryButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(menuButton);
        
        add(mainPanel, BorderLayout.CENTER);
    }
} 