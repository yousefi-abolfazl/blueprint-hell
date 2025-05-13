package view;

import model.Game;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ShopView extends JDialog {
    private Game game;
    private JLabel coinsLabel;
    
    public ShopView(JFrame parent) {
        super(parent, "Shop", true);
        this.game = Game.getInstance();
        
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        initializeComponents();
    }
    
    private void initializeComponents() {
        // Header panel with coins display
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        coinsLabel = new JLabel("Coins: " + game.getCoins());
        coinsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(coinsLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // Main panel with shop items
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Add shop items
        addShopItem(itemsPanel, "O' Atar", "Disable impact effects for 10 seconds", 3, e -> purchaseOAtar());
        addShopItem(itemsPanel, "O' Airyaman", "Disable packet collisions for 5 seconds", 4, e -> purchaseOAiryaman());
        addShopItem(itemsPanel, "O' Anahita", "Reset all packet noise to zero", 5, e -> purchaseOAnahita());
        
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        add(scrollPane, BorderLayout.CENTER);
        
        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void addShopItem(JPanel panel, String name, String description, int cost, ActionListener purchaseAction) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BorderLayout());
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
            BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel descLabel = new JLabel(description);
        JLabel costLabel = new JLabel("Cost: " + cost + " coins");
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(descLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(costLabel);
        
        JButton buyButton = new JButton("Buy");
        buyButton.addActionListener(e -> {
            if (game.getCoins() >= cost) {
                purchaseAction.actionPerformed(e);
                updateCoinsLabel();
                JOptionPane.showMessageDialog(this, "Purchase successful!");
            } else {
                JOptionPane.showMessageDialog(this, "Not enough coins!", "Purchase Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        itemPanel.add(infoPanel, BorderLayout.CENTER);
        itemPanel.add(buyButton, BorderLayout.EAST);
        
        panel.add(itemPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
    }
    
    private void updateCoinsLabel() {
        coinsLabel.setText("Coins: " + game.getCoins());
    }
    
    private void purchaseOAtar() {
        if (game.spendCoins(3)) {
            game.disableImpact(10);
        }
    }
    
    private void purchaseOAiryaman() {
        if (game.spendCoins(4)) {
            game.disableCollisions(5);
        }
    }
    
    private void purchaseOAnahita() {
        if (game.spendCoins(5)) {
            game.resetAllPacketNoise();
        }
    }
} 