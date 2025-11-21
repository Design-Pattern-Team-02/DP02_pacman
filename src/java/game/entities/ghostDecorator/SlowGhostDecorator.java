package game.entities.ghostDecorator;

import game.entities.ghosts.Ghost;
import game.entities.superPacGums.FrightenedGhostSuperPacGum;
import game.entities.superPacGums.GhostSuperPacGum;
import game.entities.superPacGums.SlowGhostSuperPacGum;
import game.entities.superPacGums.SuperPacGum;
import game.ghostStates.GhostState;

import java.awt.*;

public class SlowGhostDecorator extends GhostDecorator {
    private boolean flag;
    private int slowTimer;
    private int spdRatio;

    public SlowGhostDecorator(Ghost ghost, int spdRatio){
        super(ghost);
        this.flag = false;
        this.slowTimer = 0;
        this.spdRatio = spdRatio;
    }
    @Override
    public void superPacGumEaten(SuperPacGum spg){
        if(spg instanceof SlowGhostSuperPacGum){
            slowTimer = 0;
            flag = true;
        }
        ghost.superPacGumEaten(spg);
    }

    @Override
    public void updatePosition(int xSpd, int ySpd, int spd){
        if(!flag) ghost.updatePosition(xSpd, ySpd, spd);
        if(flag && slowTimer%spdRatio==0){
            ghost.updatePosition(xSpd, ySpd, spd);
        }
    }

    @Override
    public void before_updatePosition(){
        if(flag){
            slowTimer += 1;
            if(slowTimer >= (60 * 7)){
                slowTimer = 0;
                flag = false;
            }
        }
        ghost.before_updatePosition();
    }
}
