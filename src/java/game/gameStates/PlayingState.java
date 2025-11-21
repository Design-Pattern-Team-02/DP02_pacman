package game.gameStates;
import game.UIPanel;
import game.panel.GameplayPanel;

import javax.swing.*;
import java.io.IOException;

public class PlayingState implements GameState{
    private static final int width = 448;
    private static final int height = 496;
    private static UIPanel uiPanel;
    private GameplayPanel gameplay;

    @Override
    public void changePanel(JFrame window) {
        JPanel gameWindow = new JPanel();

        try {
            gameplay = new GameplayPanel(448, 496);
            gameWindow.add(gameplay);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(window, "Failed to load game resources.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        uiPanel = new UIPanel(256, 496);
        gameWindow.add(uiPanel);

        window.setContentPane(gameWindow);
        window.pack();
        window.setLocationRelativeTo(null);

        // 표시된 뒤 안전하게 포커스를 요청
        SwingUtilities.invokeLater(() -> {
            gameplay.requestFocusInWindow();
        });
    }

    @Override
    public void exitPanel() {
        // 필요시 정리 코드 추가
        gameplay.stop();
        gameplay=null;
        uiPanel=null;
    }

    public static UIPanel getUIPanel() {
        return uiPanel;
    }
}
