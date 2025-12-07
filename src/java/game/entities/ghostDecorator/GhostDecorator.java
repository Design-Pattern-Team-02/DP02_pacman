package game.entities.ghostDecorator;

import game.entities.ghosts.Ghost;
import game.entities.superPacGums.SuperPacGum;
import game.ghostStates.GhostState;

import java.awt.*;

/**
 * GhostDecorator: Ghost를 감싸는 기본 Decorator
 * 이 클래스는 자체 위치를 가지지 않고 내부 ghost에 모든 호출을 위임
 * getxPos(), getyPos() 등의 위치 관련 메서드는 항상 내부 ghost를 참조
 */
public class GhostDecorator extends Ghost {
    protected Ghost ghost;
    public GhostDecorator(Ghost ghost){
        super(ghost.getxPos(), ghost.getyPos(), "");
        this.ghost = ghost;
    }

    // ========== 위치 관련 메서드: 내부 ghost에 위임 ==========

    @Override
    public int getxPos() {
        return ghost.getxPos();
    }

    @Override
    public int getyPos() {
        return ghost.getyPos();
    }

    @Override
    public int getSize() {
        return ghost.getSize();
    }

    @Override
    public Rectangle getHitbox() {
        return ghost.getHitbox();
    }

    // ========== 기타 메서드: 내부 ghost에 위임 ==========

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
    @Override
    public boolean isDestroyed(){
        return ghost.isDestroyed();
    }
}