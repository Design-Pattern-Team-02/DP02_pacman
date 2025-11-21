package game.entities.superPacGums;

import java.awt.*;

public class SheildPacmanSuperPacGum extends PacmanSuperPacGum {
    private int frameCount = 0;
    public SheildPacmanSuperPacGum(int xPos, int yPos) {
        super(xPos, yPos);
    }
    @Override
    public void render(Graphics2D g) {
        // render light green
        if (frameCount%60 < 30) {
            g.setColor(new Color(0, 0, 255));
            g.fillOval(this.xPos, this.yPos, this.size, this.size);
        }
    }
    @Override
    public void update() {
        frameCount++;
    }
}
