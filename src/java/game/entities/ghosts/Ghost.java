package game.entities.ghosts;

import game.Game;
import game.entities.MovingEntity;
import game.entities.levelStrategies.LevelStrategy;
import game.ghostStates.*;
import game.ghostStrategies.IGhostStrategy;
import game.entities.superPacGums.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

//Classe abtraite pour décrire les fantômes
public abstract class Ghost extends MovingEntity {
    protected GhostState state;

    protected final GhostState chaseMode;
    protected final GhostState scatterMode;
    protected final GhostState frightenedMode;
    protected final GhostState eatenMode;
    protected final GhostState houseMode;

    protected int modeTimer = 0;
    protected int frightenedTimer = 0;
    protected boolean isChasing = false;

    protected static BufferedImage frightenedSprite1;
    protected static BufferedImage frightenedSprite2;
    protected static BufferedImage eatenSprite;

    protected IGhostStrategy strategy;

    public Ghost(int xPos, int yPos, String spriteName) {
        super(32, xPos, yPos, 2, spriteName, 2, 0.1f);

        //Création des différents états des fantômes
        chaseMode = new ChaseMode(this);
        scatterMode = new ScatterMode(this);
        frightenedMode = new FrightenedMode(this);
        eatenMode = new EatenMode(this);
        houseMode = new HouseMode(this);

        state = houseMode; //état initial

        try {
            frightenedSprite1 = ImageIO.read(getClass().getClassLoader().getResource("img/ghost_frightened.png"));
            frightenedSprite2 = ImageIO.read(getClass().getClassLoader().getResource("img/ghost_frightened_2.png"));
            eatenSprite = ImageIO.read(getClass().getClassLoader().getResource("img/ghost_eaten.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Méthodes pour les transitions entre les différents états
    public void switchChaseMode() {
        state = chaseMode;
    }
    public void switchScatterMode() {
        state = scatterMode;
    }

    public void switchFrightenedMode() {
        state = frightenedMode;
    }

    public void switchEatenMode() {
        state = eatenMode;
    }

    public void switchHouseMode() {
        state = houseMode;
    }

    public void switchChaseModeOrScatterMode() {
        if (isChasing) {
            switchChaseMode();
        }else{
            switchScatterMode();
        }
    }

    public IGhostStrategy getStrategy() {
        return this.strategy;
    }

    public void setStrategy(IGhostStrategy strategy) {
        this.strategy = strategy;
    }

    public GhostState getState() {
        return state;
    }

    /**
     * 레벨 전략에 따른 해산(Frightened) 시간 계산
     * 기본 7초 (420 프레임)에서 레벨별 감소율 적용
     *
     * @return 조정된 해산 시간 (프레임 단위)
     */
    protected int getAdjustedFrightenedTime() {
        LevelStrategy levelStrategy = Game.getLevelStrategy();
        if (levelStrategy == null) {
            return 60 * 7; // 기본 7초
        }

        int baseTime = 60 * 7; // 420 프레임 (7초)
        double reduction = levelStrategy.getFrightenedTimerReduction();
        int adjustedTime = (int) Math.round(baseTime * (1.0 - reduction));

        return adjustedTime;
    }

    @Override
    public void before_updatePosition(){
        if (!Game.getFirstInput()) return; //Les fantômes ne bougent pas tant que le joueur n'a pas bougé

        //Si le fantôme est dans l'état effrayé, un timer de 7s se lance, et l'état sera notifié ensuite afin d'appliquer la transition adéquate
        if (state == frightenedMode) {
            frightenedTimer++;

            int frightenedDuration = getAdjustedFrightenedTime();
            if (frightenedTimer >= frightenedDuration) {
                state.timerFrightenedModeOver();
            }
        }

        //Les fantômes alternent entre l'état chaseMode et scatterMode avec un timer
        //Si le fantôme est dans l'état chaseMode ou scatterMode, un timer se lance, et au bout de 5s ou 20s selon l'état, l'état est notifié ensuite afin d'appliquer la transition adéquate
        if (state == chaseMode || state == scatterMode) {
            modeTimer++;

            if ((isChasing && modeTimer >= (60 * 20)) || (!isChasing && modeTimer >= (60 * 5))) {
                state.timerModeOver();
                modeTimer = 0;
                isChasing = !isChasing;
            }
        }

        //Si le fantôme est sur la case juste au dessus de sa maison, l'état est notifié afin d'appliquer la transition adéquate
        if (xPos == 208 && yPos == 168) {
            state.outsideHouse();
        }

        //Si le fantôme est sur la case au milieu sa maison, l'état est notifié afin d'appliquer la transition adéquate
        if (xPos == 208 && yPos == 200) {
            state.insideHouse();
        }

        //Selon l'état, le fantôme calcule sa prochaine direction, et sa position est ensuite mise à jour
        state.computeNextDir();
    }

    @Override
    public void render(Graphics2D g) {
        //Différents sprites sont utilisés selon l'état du fantôme (après réflexion, il aurait peut être été plus judicieux de faire une méthode "render" dans GhostState)
        int adjustedFrightenedTime = getAdjustedFrightenedTime();
        int warningTime = (int)(adjustedFrightenedTime * 0.7); // 70% 지점부터 깜빡임

        if (state == frightenedMode) {
            if (frightenedTimer <= warningTime || frightenedTimer%20 > 10) {
                g.drawImage(frightenedSprite1.getSubimage((int)subimage * size, 0, size, size), this.xPos, this.yPos,null);
            }else{
                g.drawImage(frightenedSprite2.getSubimage((int)subimage * size, 0, size, size), this.xPos, this.yPos,null);
            }
        }else if (state == eatenMode) {
            g.drawImage(eatenSprite.getSubimage(direction * size, 0, size, size), this.xPos, this.yPos,null);
        }else{
            g.drawImage(sprite.getSubimage((int)subimage * size + direction * size * nbSubimagesPerCycle, 0, size, size), this.xPos, this.yPos,null);
        }

    }

    @Override
    public void superPacGumEaten(SuperPacGum spg){
        if(!(spg instanceof GhostSuperPacGum)){
            throw new IllegalArgumentException("Invalid SuperPacGum type. Expected GhostSuperPacGum, but got: " + spg.getClass().getSimpleName());
        }
        else if(spg instanceof FrightenedGhostSuperPacGum){
            frightenedTimer = 0;
            state.frightenedGhostSuperPacGunEaten();

            // 해산시간 로그 출력
            LevelStrategy levelStrategy = Game.getLevelStrategy();
            if (levelStrategy != null) {
                int frightenedTime = getAdjustedFrightenedTime();
                double reduction = levelStrategy.getFrightenedTimerReduction();
                System.out.println("고스트 해산 시작: " + (frightenedTime / 60.0) + "초 " +
                        "(기본 7초에서 " + (int)(reduction * 100) + "% 감소)");
            }
        }
    }
}