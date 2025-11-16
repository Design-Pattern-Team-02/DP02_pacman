package game.gameStates;

import javax.swing.*;

public interface GameState {
//  If the state is activated, change game panel.
    void changePanel(JFrame window);
//  If the state is deactivated, do cleanup the last state.
    void exitPanel();
}
