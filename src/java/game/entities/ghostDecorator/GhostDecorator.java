package game.entities.ghostDecorator;

import game.entities.ghosts.Ghost;
import game.entities.superPacGums.SuperPacGum;
import game.ghostStates.GhostState;

import java.awt.*;

public class GhostDecorator extends Ghost {
    protected Ghost ghost;
    public GhostDecorator(Ghost ghost){
        super(ghost.getxPos(), ghost.getyPos(), "");
        this.ghost = ghost;
    }
    @Override
    public void render(Graphics2D g){
        ghost.render(g);
    }
    @Override
    public void superPacGumEaten(SuperPacGum spg){
        ghost.superPacGumEaten(spg);
    }
    @Override
    public void updatePosition(int xSpd, int ySpd, int spd){
        ghost.updatePosition(xSpd, ySpd, spd);
    }
    @Override
    public void before_updatePosition(){
        ghost.before_updatePosition();
    }
    @Override
    public int getSpd(){
        return ghost.getSpd();
    }
    @Override
    public int getxSpd(){
        return ghost.getxSpd();
    }
    @Override
    public int getySpd(){
        return ghost.getySpd();
    }
    @Override
    public GhostState getState() {
        return ghost.getState();
    }
    @Override public Rectangle getHitbox(){
        return ghost.getHitbox();
    }
    @Override public boolean isDestroyed(){
        return ghost.isDestroyed();
    }
}
