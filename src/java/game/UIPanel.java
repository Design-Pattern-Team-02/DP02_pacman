package game;

import game.entities.PacGum;
import game.entities.superPacGums.SuperPacGum;
import game.entities.ghosts.Ghost;
import game.ghostStates.FrightenedMode;

import javax.swing.*;
import javax.swing.SwingConstants;
import java.awt.*;

//Panneau de l'interface utilisateur
public class UIPanel extends JPanel implements Observer {
    public static int width;
    public static int height;

    private int score = 0;
    private JLabel scoreLabel;
    private JLabel levelLabel = new JLabel("Level: " + Game.getGameLevel());

    public UIPanel(int width, int height) {
        this.width = width;
        this.height = height;

        setPreferredSize(new Dimension(width, height));
        setLayout(new GridLayout(2, 1));   // ⬅⬅ 화면을 위/아래 2등분
        this.setBackground(Color.black);

        // 상단 패널: Level 표시
        JPanel topPanel = new JPanel();
        topPanel.setBackground(Color.black);
        topPanel.setLayout(new BorderLayout());

        levelLabel = new JLabel("Level: " + Game.getGameLevel(), SwingConstants.CENTER);
        levelLabel.setFont(levelLabel.getFont().deriveFont(20.0F));
        levelLabel.setForeground(Color.white);
        levelLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        topPanel.add(levelLabel, BorderLayout.CENTER);

        // 하단 패널: Score 표시
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.black);
        bottomPanel.setLayout(new BorderLayout());

        scoreLabel = new JLabel("Score: " + score, SwingConstants.CENTER);
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(20.0F));
        scoreLabel.setForeground(Color.white);
        scoreLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        bottomPanel.add(scoreLabel, BorderLayout.CENTER);

        // 두 패널을 위아래로 추가
        this.add(topPanel);       // GridLayout의 첫 칸 = 상단 50%
        this.add(bottomPanel);    // GridLayout의 두 번째 칸 = 하단 50%
    }

    public void updateScore(int incrScore) {
        this.score += incrScore;
        this.scoreLabel.setText("Score: " + score);
    }

    public int getScore() {
        return score;
    }

    //L'interface est notifiée lorsque Pacman est en contact avec une PacGum, une SuperPacGum ou un fantôme, et on met à jour le score affiché en conséquence
    @Override
    public void updatePacGumEaten(PacGum pg) {
        updateScore(10);
    }

    @Override
    public void updateSuperPacGumEaten(SuperPacGum spg) {
        updateScore(100);
    }

    @Override
    public void updateGhostCollision(Ghost gh) {
        if (gh.getState() instanceof FrightenedMode) { //Dans le cas où Pacman est en contact avec un fantôme on ne met à jour le score que lorsque ce dernier est en mode "frightened"
            updateScore(500);
        }
    }
}
