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
    private CustomButton startGameButton;
    private CustomButton exitButton;
    private CustomButton settingsButton;
    private CustomButton gameStagesButton;

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
        g2d.setClip(null);
    }
    public void setupMainMenu() {
        setLayout(null);
        setSize(GAME_FRAME_DIMENSION);
        setLocation(0,0);
        setVisible(true);
    }

    public void setupButtons() {
        int centerX = (int)(GAME_FRAME_DIMENSION.getWidth() / 2);
        int buttonWidth = 180;
        int buttonHeight = 80;
        int startY = 700;
        int gap = 70;
        startGameButton = new CustomButton("Start Game");
        exitButton = new CustomButton("Exit");
        settingsButton = new CustomButton("Settings");
        gameStagesButton = new CustomButton("Stages");

        startGameButton.setBounds(centerX - buttonWidth/2, startY, buttonWidth, buttonHeight);
        exitButton.setBounds(2 * centerX - buttonWidth/2 - 3 * gap , imageY + imageHeight / 2 - buttonHeight /2, buttonWidth, buttonHeight);
        settingsButton.setBounds(buttonWidth/2 + gap, imageY + imageHeight / 2 - buttonHeight / 2, buttonWidth, buttonHeight);
        gameStagesButton.setBounds(centerX - buttonWidth/2 + gap + 20, startY, buttonWidth, buttonHeight);

        add(startGameButton);
        add(exitButton);
        add(settingsButton);
        //add(gameStagesButton);

        exitButton.addActionListener(al -> System.exit(0));    

        
        

    }

    class CustomButton extends JButton {
        private Color hoverBackground = new Color(70, 130, 180, 255); // Blue steel
        private Color normalBackground = new Color(41, 95, 140, 255); // Dark blue
        private Color textColor = new Color(240, 240, 240); // light white
        private boolean isHovered = false;
        private final int cornerRadius = 25;
        
        public CustomButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(textColor);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    isHovered = true;
                    repaint();
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    isHovered = false;
                    repaint();
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (isHovered) {
                g2.setColor(hoverBackground);
            } else {
                g2.setColor(normalBackground);
            }
            
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
            
            g2.setColor(new Color(140, 200, 255, 150));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));
            
            if (isHovered) {
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(110, 170, 220, 180),
                    0, getHeight(), new Color(60, 120, 190, 100));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(3, 3, getWidth() - 6, getHeight() - 6, cornerRadius - 2, cornerRadius - 2));
                

                g2.setColor(new Color(180, 220, 255, 90));
                g2.setStroke(new BasicStroke(3f));
                g2.draw(new RoundRectangle2D.Double(2, 2, getWidth() - 4, getHeight() - 4, cornerRadius, cornerRadius));
            } else {
                
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(55, 110, 160, 255),
                    0, getHeight(), new Color(35, 85, 130, 255));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius));
            }
            
            FontMetrics metrics = g2.getFontMetrics(getFont());
            int x = (getWidth() - metrics.stringWidth(getText())) / 2;
            int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
            
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.setColor(new Color(10, 10, 10, 180));
            g2.drawString(getText(), x + 1, y + 1);
            

            if (isHovered) {
                g2.setColor(new Color(200, 230, 255));
            } else {
                g2.setColor(textColor);
            }
            g2.drawString(getText(), x, y);
            
            if (isHovered) {
                g2.setColor(new Color(220, 240, 255, 60));
                g2.drawString(getText(), x, y - 1);
            }
        }
        
        @Override
        public boolean contains(int x, int y) {
            return new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius).contains(x, y);
        }
    }
}