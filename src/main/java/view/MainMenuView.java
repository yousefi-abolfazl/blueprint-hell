package view;

import javax.swing.*;
import java.awt.*;

import static controller.Constants.GAME_FRAME_DIMENSION;

public class MainMenuView extends JPanel{
    private final int cellSize = 50;
    JButton startGameButton;
    JButton exitButton;
    JButton settingsButton;
    JButton gameStageButton;

    public MainMenuView() {
        setupMainMenu();
        setupButtons();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(new Color(15,16,22,255));
        g.setColor(Color.DARK_GRAY);

        int width = getWidth();
        int height = getHeight();
        int cols = width/cellSize;
        int rows =height/cellSize;

        for (int i = 0; i <= cols; i++) {
            int x = i * cellSize;
            g.drawLine(x, 0, x, height);
        }

        for (int i = 0; i <= rows; i++) {
            int y = i * cellSize;
            g.drawLine(0, y, width, y);
        }
    }
    public void setupMainMenu() {
        setLayout(null);
        setSize(GAME_FRAME_DIMENSION);
        setLocation(0,0);
        setVisible(true);
    }

    public void setupButtons() {
        JButton startGameButton = new JButton("Start Game");
        JButton exitButton = new JButton("Exit");
    }
}
