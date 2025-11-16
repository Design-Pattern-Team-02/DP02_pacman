package game.gameStates;

import game.panel.StartMenuPanel;
import javax.swing.JFrame;

public class StartMenuState implements GameState{
    @Override
    public void changePanel(JFrame window) {
//        window.setContentPane(new StartMenuPanel(window));
        StartMenuPanel menu = new StartMenuPanel(window);
        window.setContentPane(menu);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    @Override
    public void exitPanel() {
        // 필요시 정리 코드 추가
    }
}
