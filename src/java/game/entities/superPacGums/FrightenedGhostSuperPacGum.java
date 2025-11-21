package game.entities.superPacGums;

import java.awt.*;

public class FrightenedGhostSuperPacGum extends GhostSuperPacGum {
    private int frameCount = 0;
    public FrightenedGhostSuperPacGum(int xPos, int yPos) {
        super(xPos, yPos);
    }
    @Override
    public void render(Graphics2D g) {
        // render light green
        if (frameCount%60 < 30) {
            g.setColor(new Color(0, 255, 0));
            g.fillOval(this.xPos, this.yPos, this.size, this.size);
        }
    }
    @Override
    public void update() {
        frameCount++;
    }
}
