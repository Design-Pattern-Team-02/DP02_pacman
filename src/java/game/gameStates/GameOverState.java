package game.gameStates;

import game.panel.GameOverPanel;

import javax.swing.*;

public class GameOverState implements GameState {
    private final String nickname;
    private final int score;

    public GameOverState(String nickname, int score) {
        this.nickname = nickname;
        this.score = score;
    }

    @Override
    public void changePanel(JFrame window) {
        window.getContentPane().removeAll();
        window.getContentPane().add(new GameOverPanel(window, nickname, score));
        window.revalidate();
        window.repaint();
        window.pack();
        window.setLocationRelativeTo(null);
    }

    @Override
    public void exitPanel() {
        // 필요시 정리 코드 추가

    }
}