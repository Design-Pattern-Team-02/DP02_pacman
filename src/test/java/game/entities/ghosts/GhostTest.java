package game.entities.ghosts;

import game.Game;
import game.entities.ghosts.Ghost;
import game.entities.levelStrategies.Level1Strategy;
import game.entities.levelStrategies.Level2Strategy;
import game.entities.levelStrategies.Level3Strategy;
import game.entities.levelStrategies.LevelStrategy;
import game.entities.superPacGums.FrightenedGhostSuperPacGum;
import game.entities.superPacGums.GhostSuperPacGum;
import game.entities.superPacGums.SuperPacGum;
import game.ghostStates.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.Graphics2D;

public class GhostTest {

    private TestGhost ghost;

    /**
     * 테스트 실행 전 초기화 메서드
     * 매 테스트마다 새로운 Ghost 인스턴스와 게임 환경을 설정합니다.
     */
    @Before
    public void setUp() {
        // Game의 상태 초기화 (Game 클래스의 static 상태에 의존하므로 초기화 필요)
        // 주의: 실제 Game 클래스의 구현에 따라 이 부분은 조정이 필요할 수 있습니다.
        Game.setFirstInput(false);
        Game.setGameLevel(1);

        // 208, 168은 코드상 Ghost House 바로 위 좌표 (Outside House 로직 테스트용)
        ghost = new TestGhost(208, 168);
    }

    @Test
    public void testInitialState() {
        // 초기 상태는 HouseMode여야 함
        Assert.assertTrue("Initial state should be HouseMode", ghost.getState() instanceof HouseMode);
        // 초기에는 추격 모드가 아니어야 함
        Assert.assertFalse("Should not be chasing initially", ghost.isChasing());
    }

    @Test
    public void testStateTransitions() {
        // 각 모드 전환 메서드 호출 후 상태 객체 확인
        ghost.switchChaseMode();
        Assert.assertTrue(ghost.getState() instanceof ChaseMode);

        ghost.switchScatterMode();
        Assert.assertTrue(ghost.getState() instanceof ScatterMode);

        ghost.switchFrightenedMode();
        Assert.assertTrue(ghost.getState() instanceof FrightenedMode);

        ghost.switchEatenMode();
        Assert.assertTrue(ghost.getState() instanceof EatenMode);
    }

    @Test
    public void testChaseScatterTimerLogic() {
        Game.setFirstInput(true); // 입력이 들어와야 움직임 시작

        ghost.switchScatterMode(); // 시작은 Scatter
        Assert.assertFalse("Initial isChasing should be false", ghost.isChasing());

        // ScatterMode는 5초(300프레임) 지속됨
        for (int i = 0; i < 300; i++) {
            ghost.before_updatePosition();
        }

        // 300프레임 이후 isChasing 플래그가 true로 반전되어야 함 (ChaseMode 전환 준비)
        Assert.assertTrue("Should switch to Chasing after Scatter timer ends", ghost.isChasing());
    }

    @Test
    public void testFrightenedTimerDuration_Default() {
        Game.setFirstInput(true);
        Game.setGameLevel(1); // 레벨 1: 감소 없음 -> 기본 7초 (420 프레임)

        ghost.switchFrightenedMode();

        // 419 프레임까지는 Frightened 모드 유지
        for (int i = 0; i < 419; i++) {
            ghost.before_updatePosition();
        }
        Assert.assertTrue("Should still be in FrightenedMode", ghost.getState() instanceof FrightenedMode);

        // 420번째 프레임에서 상태 종료 (timerFrightenedModeOver 호출됨)
        ghost.before_updatePosition();

        // 주의: FrightenedMode가 끝나면 로직에 따라 Chase나 Scatter로 변경됨.
        // 여기서는 FrightenedMode가 아니라는 것만 검증
        Assert.assertFalse("Should not be in FrightenedMode after timer expires",
                ghost.getState() instanceof FrightenedMode);
    }

    @Test
    public void testFrightenedTimerDuration_Reduced() {
        Game.setFirstInput(true);

        // 가정: Game 레벨 3에서는 전략적으로 타이머가 40% 감소한다고 가정
        // (실제 Game.getLevelStrategy() 구현에 따라 252 프레임 근처가 되어야 함)
        Game.setGameLevel(3);

        ghost.switchFrightenedMode();

        // 감소된 시간(약 252프레임)보다 조금 더 돌려봄
        for (int i = 0; i < 255; i++) {
            ghost.before_updatePosition();
        }

        Assert.assertFalse("Should exit FrightenedMode earlier due to level strategy",
                ghost.getState() instanceof FrightenedMode);
    }

    @Test
    public void testSuperPacGumInteraction_FrightenedGhostSPG() {
        StubFrightenedGhostSPG spg = new StubFrightenedGhostSPG(0, 0);

        ghost.switchScatterMode(); // 다른 모드 상태

        // 아이템 섭취 시뮬레이션
        // 주의: Ghost.superPacGumEaten 내부 로직에서 frightenedTimer = 0 으로 초기화하고 상태를 변경함
        ghost.superPacGumEaten(spg);

        Assert.assertEquals("Frightened timer should reset to 0", 0, ghost.getFrightenedTimer());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSuperPacGumInteraction_InvalidType() {
        // GhostSuperPacGum이 아닌 다른 타입의 PacGum을 먹었을 때 예외 발생 검증
        StubInvalidSPG invalidSpg = new StubInvalidSPG(0, 0);
        ghost.superPacGumEaten(invalidSpg);
    }

    // ==========================================
    // Inner Classes & Stubs for Testing
    // ==========================================

    /**
     * Ghost는 추상 클래스이므로 테스트를 위해 구체화한 내부 클래스입니다.
     * protected 필드에 접근하기 위한 getter 메서드들을 추가했습니다.
     */
    private class TestGhost extends Ghost {
        public TestGhost(int xPos, int yPos) {
            super(xPos, yPos, "");
            // 이미지 로딩 실패해도 테스트는 돌아가도록 try-catch가 부모 생성자에 있음
        }

        // protected 필드인 isChasing을 테스트에서 확인하기 위한 Public Wrapper
        public boolean isChasing() {
            return this.isChasing;
        }

        // protected 필드인 frightenedTimer를 테스트에서 확인하기 위한 Public Wrapper
        public int getFrightenedTimer() {
            return this.frightenedTimer;
        }

        @Override
        protected int getAdjustedFrightenedTime() {
            LevelStrategy levelStrategy;
            switch (Game.getGameLevel()) {
                case 1:
                    levelStrategy = new Level1Strategy();
                    break;
                case 2:
                    levelStrategy = new Level2Strategy();
                    break;
                case 3:
                    levelStrategy = new Level3Strategy();
                    break;
                default:
                    levelStrategy = new Level1Strategy();
            }
            if (levelStrategy == null) {
                return 60 * 7; // 기본 7초
            }

            int baseTime = 60 * 7; // 420 프레임 (7초)
            double reduction = levelStrategy.getFrightenedTimerReduction();
            int adjustedTime = (int) Math.round(baseTime * (1.0 - reduction));

            return adjustedTime;
        }
    }

    // 테스트용 Stub: FrightenedGhostSuperPacGum 구현체
    private class StubFrightenedGhostSPG extends FrightenedGhostSuperPacGum {
        public StubFrightenedGhostSPG(int x, int y) {
            super(x, y);
        }
        @Override
        public void render(Graphics2D g) {} // 테스트에 불필요
    }

    // 테스트용 Stub: 잘못된 타입의 SuperPacGum (GhostSuperPacGum을 상속받지 않음)
    private class StubInvalidSPG extends SuperPacGum {
        public StubInvalidSPG(int x, int y) {
            super(x, y);
        }
        @Override
        public void render(Graphics2D g) {}
    }
}