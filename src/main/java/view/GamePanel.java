package view;

public class GamePanel {
    static GamePanel INSTANCE;

    public GamePanel() {
    }

    public static GamePanel getInstance() {
        if (INSTANCE == null) INSTANCE = new GamePanel();
        return INSTANCE;
    }
}