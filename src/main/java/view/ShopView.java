package view;

import model.Game;
import controller.Constants;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

public class ShopView extends JDialog {
    private Game game;
    private JLabel coinsLabel;
    private Timer powerupDurationTimer;
    
    // Labels to show remaining durations
    private JLabel oAtarActiveLabel;
    private JLabel oAiryamanActiveLabel;
    
    // Track if powerups are active
    private boolean isOAtarActive = false;
    private boolean isOAiryamanActive = false;
    private int oAtarSecondsLeft = 0;
    private int oAiryamanSecondsLeft = 0;
    
    public ShopView(JFrame parent) {
        super(parent, "Blueprint Hell Shop", true);
        this.game = Game.getInstance();
        
        System.out.println("Opening Shop View with " + game.getCoins() + " coins");
        
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        // Set a nice background color
        getContentPane().setBackground(new Color(35, 40, 50));
        
        initializeComponents();
        
        // Set up the duration timer to update countdown labels
        powerupDurationTimer = new Timer(1000, e -> updatePowerupDurations());
        powerupDurationTimer.start();
    }
    
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            System.out.println("Shop View becoming visible");
        } else if (powerupDurationTimer != null) {
            powerupDurationTimer.stop();
        }
        super.setVisible(visible);
    }
    
    @Override
    public void dispose() {
        if (powerupDurationTimer != null) {
            powerupDurationTimer.stop();
        }
        super.dispose();
    }
    
    private void updatePowerupDurations() {
        // Update O'Atar timer if active
        if (isOAtarActive) {
            oAtarSecondsLeft--;
            if (oAtarSecondsLeft <= 0) {
                isOAtarActive = false;
                oAtarActiveLabel.setText("Not Active");
                oAtarActiveLabel.setForeground(Color.GRAY);
            } else {
                oAtarActiveLabel.setText("Active: " + oAtarSecondsLeft + "s");
            }
        }
        
        // Update O'Airyaman timer if active
        if (isOAiryamanActive) {
            oAiryamanSecondsLeft--;
            if (oAiryamanSecondsLeft <= 0) {
                isOAiryamanActive = false;
                oAiryamanActiveLabel.setText("Not Active");
                oAiryamanActiveLabel.setForeground(Color.GRAY);
            } else {
                oAiryamanActiveLabel.setText("Active: " + oAiryamanSecondsLeft + "s");
            }
        }
    }
    
    private void initializeComponents() {
        // Create a nice title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(25, 118, 210));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel titleLabel = new JLabel("Power-up Shop");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        coinsLabel = new JLabel("Coins: " + game.getCoins());
        coinsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        coinsLabel.setForeground(Color.YELLOW);
        titlePanel.add(coinsLabel, BorderLayout.EAST);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Main panel with shop items
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        itemsPanel.setBackground(new Color(35, 40, 50));
        
        // Create labels for power-up status
        oAtarActiveLabel = new JLabel("Not Active");
        oAtarActiveLabel.setForeground(Color.GRAY);
        
        oAiryamanActiveLabel = new JLabel("Not Active");
        oAiryamanActiveLabel.setForeground(Color.GRAY);
        
        // Add shop items with status labels
        addShopItem(itemsPanel, "O' Atar", 
                "Disable impact effects for 10 seconds", 
                Constants.O_ATAR_COST, 
                e -> purchaseOAtar(),
                oAtarActiveLabel);
                
        addShopItem(itemsPanel, "O' Airyaman", 
                "Disable packet collisions for 5 seconds", 
                Constants.O_AIRYAMAN_COST, 
                e -> purchaseOAiryaman(),
                oAiryamanActiveLabel);
                
        addShopItem(itemsPanel, "O' Anahita", 
                "Reset all packet noise to zero", 
                Constants.O_ANAHITA_COST, 
                e -> purchaseOAnahita(),
                null); // No duration for this one
        
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(35, 40, 50));
        add(scrollPane, BorderLayout.CENTER);
        
        // Close button with improved styling
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setBackground(new Color(100, 100, 100));
        closeButton.setForeground(Color.WHITE);
        closeButton.addActionListener(e -> {
            System.out.println("Shop View: Close button clicked");
            dispose();
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(35, 40, 50));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void addShopItem(JPanel panel, String name, String description, int cost, 
                            ActionListener purchaseAction, JLabel statusLabel) {
        // Create a styled panel for each item
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BorderLayout(15, 0));
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(15, 10, 15, 10)
        ));
        itemPanel.setBackground(new Color(45, 50, 60));
        
        // Create an icon panel (you can replace with actual icons if available)
        JPanel iconPanel = new JPanel();
        iconPanel.setPreferredSize(new Dimension(50, 50));
        iconPanel.setBackground(new Color(25, 118, 210));
        
        // Display the first letter of the power-up as an icon
        JLabel iconLabel = new JLabel(name.substring(name.lastIndexOf(' ') + 1, name.lastIndexOf(' ') + 2));
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 20));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconPanel.add(iconLabel);
        
        itemPanel.add(iconPanel, BorderLayout.WEST);
        
        // Information panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(45, 50, 60));
        
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descLabel.setForeground(new Color(200, 200, 200));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel costLabel = new JLabel("Cost: " + cost + " coins");
        costLabel.setFont(new Font("Arial", Font.BOLD, 12));
        costLabel.setForeground(Color.YELLOW);
        costLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(descLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(costLabel);
        
        // Add status label if provided
        if (statusLabel != null) {
            statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            infoPanel.add(statusLabel);
        }
        
        itemPanel.add(infoPanel, BorderLayout.CENTER);
        
        // Buy button with improved styling
        JButton buyButton = new JButton("Buy");
        buyButton.setFont(new Font("Arial", Font.BOLD, 14));
        buyButton.setBackground(new Color(50, 150, 50));
        buyButton.setForeground(Color.WHITE);
        buyButton.setFocusPainted(false);
        
        // Add tooltip
        buyButton.setToolTipText("Click to purchase this power-up");
        
        // Add purchase action
        buyButton.addActionListener(e -> {
            if (game.getCoins() >= cost) {
                purchaseAction.actionPerformed(e);
                updateCoinsLabel();
                JOptionPane.showMessageDialog(this, "Purchase successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Not enough coins! You need " + cost + " coins, but you only have " + game.getCoins(), 
                    "Purchase Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        itemPanel.add(buyButton, BorderLayout.EAST);
        
        panel.add(itemPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
    }
    
    private void updateCoinsLabel() {
        coinsLabel.setText("Coins: " + game.getCoins());
    }
    
    private void purchaseOAtar() {
        if (game.spendCoins(Constants.O_ATAR_COST)) {
            game.disableImpact(Constants.O_ATAR_DURATION);
            controller.SoundManager.getInstance().playSound("powerup");
            
            // Update status
            isOAtarActive = true;
            oAtarSecondsLeft = Constants.O_ATAR_DURATION / 60; // Convert frames to seconds
            oAtarActiveLabel.setText("Active: " + oAtarSecondsLeft + "s");
            oAtarActiveLabel.setForeground(new Color(0, 255, 0));
        }
    }
    
    private void purchaseOAiryaman() {
        if (game.spendCoins(Constants.O_AIRYAMAN_COST)) {
            game.disableCollisions(Constants.O_AIRYAMAN_DURATION);
            controller.SoundManager.getInstance().playSound("powerup");
            
            // Update status
            isOAiryamanActive = true;
            oAiryamanSecondsLeft = Constants.O_AIRYAMAN_DURATION / 60; // Convert frames to seconds
            oAiryamanActiveLabel.setText("Active: " + oAiryamanSecondsLeft + "s");
            oAiryamanActiveLabel.setForeground(new Color(0, 255, 0));
        }
    }
    
    private void purchaseOAnahita() {
        if (game.spendCoins(Constants.O_ANAHITA_COST)) {
            game.resetAllPacketNoise();
            controller.SoundManager.getInstance().playSound("powerup");
        }
    }
} 