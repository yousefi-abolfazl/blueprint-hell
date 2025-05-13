package blueprinthell;

import controller.SceneController;
import view.GameFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameFrame.getINSTANCE(); // Initialize the game frame
            SceneController.getInstance(); // Initialize the scene controller
        });
    }
}
