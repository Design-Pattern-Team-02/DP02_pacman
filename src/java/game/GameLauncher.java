package game;

public class GameLauncher {

    public static void main(String[] args) {
        GameManager gameManager = GameManager.getInstance();
        gameManager.initGameFrame();
    }
}