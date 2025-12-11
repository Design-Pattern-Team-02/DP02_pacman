package game.entities;

import game.Game;
import game.Observer;
import game.entities.ghosts.Ghost;
import game.entities.superPacGums.PacmanSuperPacGum;
import game.entities.superPacGums.SuperPacGum;
import game.utils.CollisionDetector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.Graphics2D;

public class PacmanTest {

    private TestPacman pacman;
    private StubCollisionDetector stubDetector;
    private SpyObserver spyObserver;

    private static final int START_X = 100;
    private static final int START_Y = 100;

    @Before
    public void setUp() {
        // 게임 상태 초기화
        Game.setFirstInput(false);

        // 테스트용 Pacman 생성 (이미지 로딩 예외 방지 등을 위해 래핑 가능하지만, 여기선 직접 생성)
        pacman = new TestPacman(START_X, START_Y);

        // 의존성 주입: 가짜 충돌 감지기 및 관찰자 설정
        Game game = null;
        stubDetector = new StubCollisionDetector(game);
        pacman.setCollisionDetector(stubDetector);

        spyObserver = new SpyObserver();
        pacman.registerObserver(spyObserver);
    }

    @Test
    public void testInitialState() {
        // 제공된 테스트 케이스: 초기 상태 검증
        Assert.assertEquals("Initial X position matches.", START_X, pacman.getX());
        Assert.assertEquals("Initial Y position matches.", START_Y, pacman.getY());
        Assert.assertEquals("Default speed should be 2.", 2, pacman.getSpeed());
    }

    @Test
    public void testObserverNotification_PacGumEaten() {
        // 준비: 충돌 감지기가 PacGum을 반환하도록 설정
        PacGum mockPg = new PacGum(0, 0);
        stubDetector.setReturnObject(mockPg);

        // 실행: 위치 업데이트 전 로직 수행 (충돌 체크 발생)
        pacman.before_updatePosition();

        // 검증: 옵저버가 알림을 받았는지 확인
        Assert.assertTrue("Observer should be notified about PacGum.", spyObserver.pacGumEatenCalled);
        Assert.assertEquals("Observer received the correct PacGum object.", mockPg, spyObserver.lastPacGum);
    }

    @Test
    public void testObserverNotification_SuperPacGumEaten() {
        // 준비: 유효한 SuperPacGum(PacmanSuperPacGum) 설정
        SuperPacGum mockSpg = new StubPacmanSuperPacGum(0, 0);
        stubDetector.setReturnObject(mockSpg);

        // 실행
        pacman.before_updatePosition();

        // 검증
        Assert.assertTrue("Observer should be notified about SuperPacGum.", spyObserver.superPacGumEatenCalled);
        Assert.assertEquals(mockSpg, spyObserver.lastSuperPacGum);
    }

    @Test
    public void testObserverNotification_GhostCollision() {
        // 준비: Ghost 설정
        Ghost mockGhost = new StubGhost(0, 0);
        stubDetector.setReturnObject(mockGhost);

        // 실행
        pacman.before_updatePosition();

        // 검증
        Assert.assertTrue("Observer should be notified about Ghost collision.", spyObserver.ghostCollisionCalled);
        Assert.assertEquals(mockGhost, spyObserver.lastGhost);
    }

    @Test
    public void testObserverRegistrationAndRemoval() {
        // 추가 옵저버 등록
        SpyObserver anotherObserver = new SpyObserver();
        pacman.registerObserver(anotherObserver);

        // 기존 옵저버 제거
        pacman.removeObserver(spyObserver);

        // 이벤트 발생 시뮬레이션
        stubDetector.setReturnObject(new PacGum(0, 0));
        pacman.before_updatePosition();

        // 검증: 제거된 옵저버는 알림 X, 등록된 옵저버는 알림 O
        Assert.assertFalse("Removed observer should NOT be notified.", spyObserver.pacGumEatenCalled);
        Assert.assertTrue("Registered observer MUST be notified.", anotherObserver.pacGumEatenCalled);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSuperPacGumEaten_ThrowsExceptionForInvalidType() {
        // 준비: PacmanSuperPacGum이 아닌 다른 타입의 SuperPacGum 생성
        SuperPacGum invalidSpg = new StubInvalidSuperPacGum(0, 0);

        // 실행: 예외 발생 예상
        pacman.superPacGumEaten(invalidSpg);
    }

    @Test
    public void testSuperPacGumEaten_ValidType() {
        // 준비: 올바른 타입
        PacmanSuperPacGum validSpg = new StubPacmanSuperPacGum(0, 0);

        try {
            pacman.superPacGumEaten(validSpg);
        } catch (IllegalArgumentException e) {
            Assert.fail("Should not throw exception for valid PacmanSuperPacGum.");
        }
    }

    // ==========================================
    // Inner Classes & Stubs for Testing
    // ==========================================

    /**
     * Pacman의 Protected 필드/메서드에 접근하거나
     * 이미지 로딩 문제를 회피하기 위한 테스트용 서브클래스
     */
    private class TestPacman extends Pacman {
        public TestPacman(int xPos, int yPos) {
            super(xPos, yPos);
        }
        // Protected 필드 접근을 위한 Public Getter
        public int getX() { return this.xPos; }
        public int getY() { return this.yPos; }
        public int getSpeed() { return this.spd; }
    }

    /**
     * Stub CollisionDetector:
     * 실제 충돌 로직 대신 테스트에서 설정한 객체를 반환합니다.
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
    }

    /**
     * Spy Observer:
     * Observer 인터페이스를 구현하며, 호출 여부와 매개변수를 기록합니다.
     */
    private class SpyObserver implements Observer {
        boolean pacGumEatenCalled = false;
        boolean superPacGumEatenCalled = false;
        boolean ghostCollisionCalled = false;

        PacGum lastPacGum;
        SuperPacGum lastSuperPacGum;
        Ghost lastGhost;

        @Override
        public void updatePacGumEaten(PacGum pg) {
            pacGumEatenCalled = true;
            lastPacGum = pg;
        }

        @Override
        public void updateSuperPacGumEaten(SuperPacGum spg) {
            superPacGumEatenCalled = true;
            lastSuperPacGum = spg;
        }

        @Override
        public void updateGhostCollision(Ghost gh) {
            ghostCollisionCalled = true;
            lastGhost = gh;
        }
    }

    // --- Abstract Class 구현체 (Stubs) ---

    // 테스트용 Ghost 구현체
    private class StubGhost extends Ghost {
        public StubGhost(int x, int y) {
            super(x, y, "");
        }
    }

    // 테스트용 PacmanSuperPacGum 구현체 (Valid)
    private class StubPacmanSuperPacGum extends PacmanSuperPacGum {
        public StubPacmanSuperPacGum(int x, int y) {
            super(x, y);
        }
        @Override
        public void render(Graphics2D g) {} // 렌더링 무시
    }

    // 테스트용 잘못된 SuperPacGum 구현체 (Invalid)
    private class StubInvalidSuperPacGum extends SuperPacGum {
        public StubInvalidSuperPacGum(int x, int y) {
            super(x, y);
        }
        @Override
        public void render(Graphics2D g) {}
    }
}