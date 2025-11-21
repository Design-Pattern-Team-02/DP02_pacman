package game.entities.pacmanDecorator;

import game.entities.Pacman;
import game.entities.superPacGums.FastPacmanSuperPacGum;
import game.entities.superPacGums.SlowGhostSuperPacGum;
import game.entities.superPacGums.SuperPacGum;
import game.utils.KeyHandler;

import java.awt.*;

public class FastPacmanDecorator extends PacmanDecorator {
    private boolean flag;
    private int fastTimer;
    private int spdRatio;

    public FastPacmanDecorator(Pacman pacman, int spdRatio){
        super(pacman);
        this.flag = false;
        this.fastTimer = 0;
        this.spdRatio = spdRatio;
    }
    @Override
    public void superPacGumEaten(SuperPacGum spg){
        if(spg instanceof FastPacmanSuperPacGum){
            fastTimer = 0;
            flag = true;
        }
        pacman.superPacGumEaten(spg);
    }

    @Override
    public void before_updatePosition(){
        if(flag){
            fastTimer += 1;
            if(fastTimer >= (60 * 5)){
                fastTimer = 0;
                flag = false;
            }
        }
        pacman.before_updatePosition();
    }

    @Override
    public int getSpd(){
        if(flag & pacman.onTheGrid()) return pacman.getSpd() * spdRatio;
        return pacman.getSpd();
    }
    @Override
    public int getxSpd(){
        if(flag & pacman.onTheGrid()) return pacman.getxSpd() * spdRatio;
        return pacman.getxSpd();
    }
    @Override
    public int getySpd(){
        if(flag & pacman.onTheGrid()) return pacman.getySpd() * spdRatio;
        return pacman.getySpd();
    }
}
