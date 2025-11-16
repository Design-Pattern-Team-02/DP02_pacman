package game.entities.superPacGums;

import java.awt.*;

//Classe pour les SuperPacGums
public class FastPacmanSuperPacGum extends PacmanSuperPacGum {
    private int frameCount = 0;
    public FastPacmanSuperPacGum(int xPos, int yPos) {
        super(xPos, yPos);
    }

    @Override
    public void render(Graphics2D g) {
        // render light green
        if (frameCount%60 < 30) {
            g.setColor(new Color(255, 255, 0));
            g.fillOval(this.xPos, this.yPos, this.size, this.size);
        }
    }
    @Override
    public void update() {
        frameCount++;
    }
}
