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
        
        contentPanel.add(new MainMenuView(), "MainMenu");
        contentPanel.add(GamePanel.getInstance(), "GamePanel");
        
        gameTimer = new Timer(16, e -> {
            if (currentCard.equals("GamePanel") && contentPanel.isShowing()) {
                updateGame();
            }
        });
        gameTimer.start();
        
        GameFrame.getINSTANCE().setContentPane(contentPanel);
        GameFrame.getINSTANCE().revalidate();
        
        SoundManager.getInstance();
    }
    
    private void updateGame() {

        Game game = Game.getInstance();
        GamePanel panel = GamePanel.getInstance();

        if (panel.isGameRunning() && !game.isPaused()) {
         
            for (NetworkSystem system : game.getSystems()) {
                system.update();
            }
            for (Wire wire : game.getWires()) {
                wire.update();
            }

            game.update();
         
            panel.updateHUD();
         
            panel.repaint();
            
            if (game.isGameOver()) {
                panel.setGameRunning(false);
                showGameOver();
            } else if (isLevelComplete()) {
                panel.setGameRunning(false);
                showLevelComplete();
            }
        }
    }
    
    private boolean isLevelComplete() {
        Game game = Game.getInstance();
        int totalPacketsReceived = 0;
        int requiredPackets = 0;
        
        for (NetworkSystem system : game.getSystems()) {
            if (system instanceof DestinationSystem) {
                totalPacketsReceived += ((DestinationSystem) system).getPacketsReceived();
                requiredPackets += 5;
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
        
        SwingUtilities.invokeLater(() -> {
            GamePanel panel = GamePanel.getInstance();
            forceFocusWithDelay(panel, 0);
            forceFocusWithDelay(panel, 100);
            forceFocusWithDelay(panel, 500);
            forceFocusWithDelay(panel, 1000);
        });
        
        SoundManager.getInstance().startBackgroundMusic();
    }
    
    private void forceFocusWithDelay(GamePanel panel, int delay) {
        Timer timer = new Timer(delay, e -> {
            System.out.println("Forcing focus after " + delay + "ms delay");
            ((Timer)e.getSource()).stop();
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    public void startLevel(int level) {
        Game.getInstance().setCurrentLevel(level);
        setupLevel(level);
        currentCard = "GamePanel";
        cardLayout.show(contentPanel, currentCard);
        
        System.out.println("Starting level " + level + ": requesting focus on GamePanel");
        
        SwingUtilities.invokeLater(() -> {
            GamePanel panel = GamePanel.getInstance();
            forceFocusWithDelay(panel, 0);
            forceFocusWithDelay(panel, 100);
            forceFocusWithDelay(panel, 500);
            forceFocusWithDelay(panel, 1000);
        });
        
        SoundManager.getInstance().startBackgroundMusic();
    }
    
    private void setupLevel(int level) {
        Game.getInstance().resetGame();
        Game.getInstance().setCurrentLevel(level);
        
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
        
        if (currentLevel < 2) {
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

    public void returnToMainMenu() {
        GamePanel.getInstance().setGameRunning(false);
        SoundManager.getInstance().stopBackgroundMusic();
        showMainMenu();
    }
}
