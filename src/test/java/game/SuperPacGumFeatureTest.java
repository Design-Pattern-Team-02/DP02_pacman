package game;

import game.Game;
import game.entities.Entity;
import game.entities.Pacman;
import game.entities.ghostDecorator.SlowGhostDecorator;
import game.entities.ghosts.Ghost;
import game.entities.pacmanDecorator.FastPacmanDecorator;
import game.entities.pacmanDecorator.SheildPacmanDecorator;
import game.entities.superPacGums.*;
import game.utils.CollisionDetector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;

public class SuperPacGumFeatureTest {

    private TestPacman rawPacman;
    private TestGhost rawGhost;
    private StubCollisionDetector collisionDetector;

    @Before
    public void setUp() {
        Game.setFirstInput(false);
        // 기본 Pacman, Ghost, CollisionDetector 초기화
        rawPacman = new TestPacman(100, 100);
        rawGhost = new TestGhost(200, 200);
        Game game = null;
        collisionDetector = new StubCollisionDetector(game);
        rawPacman.setCollisionDetector(collisionDetector);
    }

    @Test
    public void testFastPacmanFeature() {
        int speedRatio = 2;
        FastPacmanDecorator fastPacman = new FastPacmanDecorator(rawPacman, speedRatio);

        // 속도 테스트를 위해 xSpd를 설정
        rawPacman.setXSpd(2);

        int baseSpeed = rawPacman.getSpd(); // 기본 속도 (보통 2)
        Assert.assertEquals("Initially speed should be normal.", baseSpeed, fastPacman.getSpd());

        // SuperPacGum 섭취
        fastPacman.superPacGumEaten(new StubFastSPG());

        // Decorator 로직: if(flag & pacman.onTheGrid()) -> TestPacman은 onTheGrid 항상 true
        Assert.assertEquals("Speed should be doubled after eating SPG.", baseSpeed * speedRatio, fastPacman.getSpd());
        Assert.assertEquals("X Speed should be doubled.", rawPacman.getxSpd() * speedRatio, fastPacman.getxSpd());

        // 지속시간(5초 = 300프레임) 테스트
        // 299번 업데이트 (Timer: 0 -> 299)
        for (int i = 0; i < 299; i++) {
            fastPacman.before_updatePosition();
        }

        Assert.assertEquals("Should still be fast just before timer ends.", baseSpeed * speedRatio, fastPacman.getSpd());

        // 300번째 업데이트 (Timer 299 -> 300 -> flag false)
        fastPacman.before_updatePosition();
        Assert.assertEquals("Speed should return to normal after 5 seconds.", baseSpeed, fastPacman.getSpd());
    }

    @Test
    public void testSheildPacmanFeature() {
        SheildPacmanDecorator sheildPacman = new SheildPacmanDecorator(rawPacman, collisionDetector);

        // 시나리오 1: 쉴드 없이 유령 충돌
        collisionDetector.setReturnObject(rawGhost);
        sheildPacman.before_updatePosition(); // 충돌 감지 로직 실행

        Assert.assertTrue("Initially, ghost collision should notify observer.", rawPacman.ghostCollisionNotified);

        // 상태 초기화
        rawPacman.ghostCollisionNotified = false;

        // 시나리오 2: 쉴드 아이템 섭취
        sheildPacman.superPacGumEaten(new StubSheildSPG());

        // 충돌 발생 시도
        sheildPacman.before_updatePosition();
        Assert.assertFalse("With Shield, ghost collision should NOT notify observer.", rawPacman.ghostCollisionNotified);

        // 시나리오 3: 시간 경과 후 쉴드 해제
        // 300프레임 경과
        for (int i = 0; i < 300; i++) {
            sheildPacman.before_updatePosition();
        }

        // 쉴드 해제 후 업데이트 -> 충돌 감지 재개
        sheildPacman.before_updatePosition();
        Assert.assertTrue("After timer, ghost collision should notify observer again.", rawPacman.ghostCollisionNotified);
    }

    @Test
    public void testSlowGhostFeature() {
        int slowRatio = 2; // 2번에 1번만 움직임
        SlowGhostDecorator slowGhost = new SlowGhostDecorator(rawGhost, slowRatio);
        int moveSpeed = 2;

        // 아이템 섭취
        slowGhost.superPacGumEaten(new StubSlowSPG());

        int startX = slowGhost.getxPos();

        // Frame 0: timer=0. 0%2 == 0 -> 이동함
        slowGhost.updatePosition(moveSpeed, 0, moveSpeed);
        slowGhost.before_updatePosition(); // Timer becomes 1

        Assert.assertEquals("Frame 0: Should move.", startX + moveSpeed, slowGhost.getxPos());

        startX = slowGhost.getxPos();

        // Frame 1: timer=1. 1%2 != 0 -> 이동 안 함
        slowGhost.updatePosition(moveSpeed, 0, moveSpeed);
        slowGhost.before_updatePosition(); // Timer becomes 2

        Assert.assertEquals("Frame 1: Should SKIP move.", startX, slowGhost.getxPos());

        startX = slowGhost.getxPos();

        // Frame 2: timer=2. 2%2 == 0 -> 이동함
        slowGhost.updatePosition(moveSpeed, 0, moveSpeed);

        Assert.assertEquals("Frame 2: Should move again.", startX + moveSpeed, slowGhost.getxPos());

        // 시간 경과 (7초 = 420프레임)
        for(int i=0; i<420; i++) {
            slowGhost.before_updatePosition();
        }

        // 효과 종료 후 정상 이동 확인
        startX = slowGhost.getxPos();
        slowGhost.updatePosition(moveSpeed, 0, moveSpeed);
        Assert.assertEquals("Should move normal 1.", startX + moveSpeed, slowGhost.getxPos());

        startX = slowGhost.getxPos();
        slowGhost.updatePosition(moveSpeed, 0, moveSpeed);
        Assert.assertEquals("Should move normal 2.", startX + moveSpeed, slowGhost.getxPos());
    }

    // =========================================================
    // Inner Classes (Stubs & Mocks)
    // =========================================================

    /**
     * 테스트용 Pacman 구현체
     * - onTheGrid()를 강제로 true로 설정하여 Decorator 로직 테스트 용이하게 함
     * - Observer 알림 여부를 저장하는 플래그 추가
     */
    private class TestPacman extends Pacman {
        public boolean ghostCollisionNotified = false;

        public TestPacman(int x, int y) {
            super(x, y);
            this.spd = 2; // 기본 속도 설정
            this.xSpd = 0;
            this.ySpd = 0;
        }

        public void setXSpd(int xSpd) { this.xSpd = xSpd; }

        // FastPacmanDecorator에서 onTheGrid()가 true여야 속도가 증가함
        @Override
        public boolean onTheGrid() {
            return true;
        }

        @Override
        public void notifyObserverGhostCollision(Ghost gh) {
            this.ghostCollisionNotified = true;
        }

        // 이미지 로딩 에러 방지를 위한 오버라이드 (필요시)
        @Override
        public java.awt.image.BufferedImage getSprite() { return null; }
    }

    /**
     * 테스트용 Ghost 구현체
     * - updatePosition 호출 시 실제 좌표를 변경하여 이동 여부 확인
     */
    private class TestGhost extends Ghost {
        public TestGhost(int x, int y) {
            super(x, y, "");
        }

        @Override
        public void updatePosition(int xSpd, int ySpd, int spd) {
            this.xPos += xSpd;
            this.yPos += ySpd;
        }
    }

    /**
     * 테스트용 충돌 감지기
     * - setReturnObject로 설정된 객체를 반환
     */
    private class StubCollisionDetector extends CollisionDetector {
        private Entity returnObject;

        public StubCollisionDetector(Game game) {
            super(game);
        }

        public void setReturnObject(Entity obj) {
            this.returnObject = obj;
        }

        @Override
        public Entity checkCollision(Entity e, Class type) {
            // 설정된 객체가 요청된 타입(PacGum, SuperPacGum, Ghost)과 일치하면 반환
            if (returnObject != null && type.isAssignableFrom(returnObject.getClass())) {
                return (Entity) returnObject;
            }
            return null;
        }

        @Override
        public Entity checkCollisionRect(Entity obj, Class<? extends Entity> collisionCheck) {
            return null;
        }
    }

    // --- SuperPacGum Stubs (Instanceof 체크 통과용) ---

    private class StubFastSPG extends FastPacmanSuperPacGum {
        public StubFastSPG() { super(0, 0); }
        @Override public void render(Graphics2D g) {}
    }

    private class StubSheildSPG extends SheildPacmanSuperPacGum {
        public StubSheildSPG() { super(0, 0); }
        @Override public void render(Graphics2D g) {}
    }

    private class StubSlowSPG extends SlowGhostSuperPacGum {
        public StubSlowSPG() { super(0, 0); }
        @Override public void render(Graphics2D g) {}
    }
}