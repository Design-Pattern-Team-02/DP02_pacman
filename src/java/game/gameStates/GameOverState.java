package game.gameStates;

import game.GameManager;
import game.entities.Ranking;
import game.panel.GameOverPanel;
import game.ranking.RankingManager;

import javax.swing.*;

public class GameOverState implements GameState {
    private final RankingManager rankingManager;
    private final String nickname;
    private final int score;

    public GameOverState() {
        this.nickname = GameManager.getInstance().getPlayerNickname();
        this.score = GameManager.getInstance().getScore();
        this.rankingManager = RankingManager.getInstance();
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

    public void saveRanking(){
        Ranking ranking = new Ranking();
        ranking.setNickname(nickname);
        ranking.setMapName(GameManager.getInstance().getSelectedMapName());
        ranking.setScore(score);
        ranking.setTimeStamp(java.time.LocalDateTime.now());
        rankingManager.saveRanking(ranking);
    }
}