package game.entities.pacmanDecorator;

import game.entities.PacGum;
import game.entities.Pacman;
import game.entities.ghosts.Ghost;
import game.entities.superPacGums.SuperPacGum;
import game.ghostStates.GhostState;
import game.utils.KeyHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class PacmanDecorator extends Pacman {
    protected Pacman pacman;
    public PacmanDecorator(Pacman pacman){
        super(pacman.getxPos(), pacman.getyPos());
        this.pacman = pacman;
    }
    @Override
    public void input(KeyHandler k){pacman.input(k);}
//    @Override public void render(Graphics2D g){
//        pacman.render(g);
//    }
    @Override
    public void superPacGumEaten(SuperPacGum spg){
        pacman.superPacGumEaten(spg);
    }
    @Override
    public void updatePosition(int xSpd, int ySpd, int spd){
        pacman.updatePosition(xSpd, ySpd, spd);
    }
    @Override
    public void before_updatePosition(){
        pacman.before_updatePosition();
    }
    @Override
    public boolean updatePositionCondition(){return pacman.updatePositionCondition();}
    @Override
    public int getSpd(){
        return pacman.getSpd();
    }
    @Override
    public int getxSpd(){
        return pacman.getxSpd();
    }
    @Override
    public int getySpd(){return pacman.getySpd();}
    @Override
    public int getxPos(){return pacman.getxPos();}
    @Override
    public int getyPos(){return pacman.getyPos();}
    @Override public boolean onTheGrid(){return pacman.onTheGrid();}
    @Override public Rectangle getHitbox(){
        return pacman.getHitbox();
    }
    @Override public boolean isDestroyed(){
        return pacman.isDestroyed();
    }
    @Override public BufferedImage getSprite() {return pacman.getSprite();}

    @Override public int getDirection(){return pacman.getDirection();}
    @Override public float getSubimage() {return pacman.getSubimage();}
    @Override public int getNbSubimagesPerCycle() {return nbSubimagesPerCycle;}

    @Override public void notifyObserverPacGumEaten(PacGum pg) {pacman.notifyObserverPacGumEaten(pg);}
    @Override public void notifyObserverSuperPacGumEaten(SuperPacGum spg) {pacman.notifyObserverSuperPacGumEaten(spg);}
    @Override public void notifyObserverGhostCollision(Ghost gh) {pacman.notifyObserverGhostCollision(gh);}
//    @Override public BufferedImage getSprite() { return pacman.getSprite();}
}
