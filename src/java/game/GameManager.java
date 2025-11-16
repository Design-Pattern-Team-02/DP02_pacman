package game;

import game.gameStates.GameState;
import game.gameStates.StartMenuState;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class GameManager {
    private static final GameManager INSTANCE = new GameManager();
    private JFrame window;
    private GameState currentState;
    private String playerNickname;

    private GameManager() {}

    public static GameManager getInstance() {
        return INSTANCE;
    }

    // 상태 전환: 내부에서 EDT로 안전하게 처리
    public void changeState(GameState next) {
        if (window == null) throw new IllegalStateException("GameManager not initialized (call init)");
        if (currentState != null) currentState.exitPanel();
        currentState = next;
        window.getContentPane().removeAll();
        currentState.changePanel(window);
        window.revalidate();
        window.repaint();
        window.pack();
        window.setLocationRelativeTo(null);
    }

    public void initGameFrame() {
        SwingUtilities.invokeLater(() -> {
            window = new JFrame();
            window.setTitle("Pacman");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);
            changeState(new StartMenuState());
        });

    }

    public void setPlayerNickname(String nickname) {
        this.playerNickname = nickname;
    }
    public String getPlayerNickname() {
        return playerNickname;
    }
}
