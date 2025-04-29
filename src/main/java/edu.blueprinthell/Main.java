package edu.blueprinthell;

import javax.swing.*;
import java.awt.*;
import view.GameFrame;
import view.MainMenuView;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainMenuView mainMenu = new MainMenuView();
            GameFrame.getINSTANCE().add(mainMenu);
            GameFrame.getINSTANCE().repaint();
        });
    }
}
