package view;

import javax.swing.*;
import java.awt.*;
import static controller.Constants.GAME_FRAME_DIMENSION;

public class GameFrame extends JFrame {
    static GameFrame INSTANCE;

    public GameFrame() throws HeadlessException {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(GAME_FRAME_DIMENSION);
        setLocationRelativeTo(null);
        setLayout(null);
        
        // Disable frame resizing and maximizing
        setResizable(false);
        setUndecorated(true);
        
        // Set fullscreen properties
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        setVisible(true);
    }

    public static GameFrame getINSTANCE() {
        if (INSTANCE == null) INSTANCE = new GameFrame();
        return INSTANCE;
    }


}