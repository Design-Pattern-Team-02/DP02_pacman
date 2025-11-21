package game.entities.pacmanDecorator;

import game.entities.PacGum;
import game.entities.Pacman;
import game.entities.ghosts.Ghost;
import game.entities.superPacGums.SheildPacmanSuperPacGum;
import game.entities.superPacGums.SuperPacGum;
import game.utils.CollisionDetector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class SheildPacmanDecorator extends PacmanDecorator {
    private boolean flag;
    private int sheildTimer;
    private CollisionDetector collisionDetector;

    public SheildPacmanDecorator(Pacman pacman, CollisionDetector collisionDetector){
        super(pacman);
        this.flag = false;
        this.sheildTimer = 0;
        this.collisionDetector = collisionDetector;
    }
    @Override
    public void superPacGumEaten(SuperPacGum spg){
        if(spg instanceof SheildPacmanSuperPacGum){
            sheildTimer = 0;
            flag = true;
        }
        pacman.superPacGumEaten(spg);
    }

    @Override
    public void before_updatePosition(){
        if(flag){
            sheildTimer += 1;
            if(sheildTimer >= (60 * 5)){
                sheildTimer = 0;
                flag = false;
            }
        }

        //On teste à chaque fois si Pacman est en contact avec une PacGum, une SuperPacGum, ou un fantôme, et les observers sont notifiés en conséquence
        PacGum pg = (PacGum) collisionDetector.checkCollision(this, PacGum.class);
        if (pg != null) {
            pacman.notifyObserverPacGumEaten(pg);
        }

        SuperPacGum spg = (SuperPacGum) collisionDetector.checkCollision(this, SuperPacGum.class);
        if (spg != null) {
            pacman.notifyObserverSuperPacGumEaten(spg);
        }

        if(!flag){
            Ghost gh = (Ghost) collisionDetector.checkCollision(this, Ghost.class);
            if (gh != null) {
                pacman.notifyObserverGhostCollision(gh);
            }
        }
    }

    @Override
    public BufferedImage getSprite() {
        BufferedImage sprite = pacman.getSprite();
        if(flag) {
            try {
                String spriteName = "sheild_pacman.png";
                sprite = ImageIO.read(getClass().getClassLoader().getResource("img/" + spriteName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sprite;
    }
}
