package view;

import javax.swing.*;
import java.awt.*;

import static controller.Constants.GAME_FRAME_DIMENSION;
import java.awt.geom.RoundRectangle2D;


public class MainMenuView extends JPanel{
    private final int cellSize = 50;
    private final String imagePath = "src/main/resources/images/bluePrintHell.png";
    private ImageIcon backgroundImage;
    private int imageHeight;
    private int imageWidth;
    private int imageX;
    private int imageY;
    private JButton startGameButton;
    private JButton exitButton;
    private JButton settingsButton;
    private JButton gameStageButton;

    public MainMenuView() {
        loadBackgroundImage();
        setupMainMenu();
        setupButtons();
    }

    public void loadBackgroundImage() {
        try {
            backgroundImage = new ImageIcon(imagePath);
            if (backgroundImage.getIconWidth() <= 0) {
                System.err.println("Failed to load image: " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
        imageHeight = (int) GAME_FRAME_DIMENSION.getWidth() / 7;
        imageWidth = (int) GAME_FRAME_DIMENSION.getWidth() / 2 + 80;
        imageX = (int) GAME_FRAME_DIMENSION.getWidth() / 2 - imageWidth / 2;
        imageY = 10;

    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(new Color(15,16,22,255));

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.DARK_GRAY);

        int width = getWidth();
        int height = getHeight();
        int cols = width/cellSize;
        int rows =height/cellSize;

        for (int i = 0; i <= cols; i++) {
            int x = i * cellSize;
            g2d.drawLine(x, 0, x, height);
        }

        for (int i = 0; i <= rows; i++) {
            int y = i * cellSize;
            g2d.drawLine(0, y, width, y);
        }
        
        RoundRectangle2D roundedRect = new RoundRectangle2D.Float(
            imageX, imageY, imageWidth, imageHeight, 30, 30);
        g2d.setClip(roundedRect);
        g2d.drawImage(backgroundImage.getImage(), imageX, imageY, imageWidth, imageHeight, this);

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
