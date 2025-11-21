package game.panel;

import game.GameManager;
import game.gameStates.StartMenuState;
import game.ranking.RankingBoardPanelAfter;
import game.ranking.RankingBoardPanelBefore;
import game.ranking.RankingManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GameOverPanel extends JPanel {
    public GameOverPanel(JFrame parent, String nickname, int score) {
        setPreferredSize(new Dimension(448, 496));
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        // 상단: Game Over 타이틀
        JLabel title = new JLabel("Game Over", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 36f));
        title.setBorder(new EmptyBorder(30, 10, 10, 10));
        add(title, BorderLayout.NORTH);

        // 중앙: 닉네임과 점수
        JLabel scoreLabel = new JLabel(nickname + " : " + score, SwingConstants.CENTER);
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD, 28f));
        scoreLabel.setBorder(new EmptyBorder(20, 10, 20, 10));
        add(scoreLabel, BorderLayout.CENTER);

        // 하단 버튼 패널 (Replay, Ranking)
        JPanel buttons = new JPanel(new GridLayout(1, 2, 20, 0));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(20, 40, 40, 40));

        Font btnFont = new Font("SansSerif", Font.BOLD, 18);
        JButton replayButton = makeMenuButton("Main Menu", btnFont);
        JButton rankingButton = makeMenuButton("Ranking", btnFont);

        buttons.add(rankingButton);
        buttons.add(replayButton);

        add(buttons, BorderLayout.SOUTH);

        // 액션 연결
        GameManager gameManager = GameManager.getInstance();
        replayButton.addActionListener((ActionEvent e) -> gameManager.changeState(new StartMenuState()));

        rankingButton.addActionListener((ActionEvent e) -> {
            RankingBoardPanelAfter boardPanel = new RankingBoardPanelAfter();
            RankingManager rankingManager = RankingManager.getInstance();
            rankingManager.registerObserver(boardPanel);
            rankingManager.notifyObservers();
            parent.getContentPane().removeAll();
            parent.getContentPane().add(boardPanel);
            parent.revalidate();
            parent.repaint();
            parent.setSize(648, 496);
            parent.setLocationRelativeTo(null);
            parent.setVisible(true);
        });
    }

    private JButton makeMenuButton(String text, Font font) {
        JButton b = new JButton(text);
        b.setFont(font);
        b.setForeground(Color.WHITE);
        b.setBackground(Color.BLACK);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2)); // 심플한 흰색 테두리
        return b;
    }
}