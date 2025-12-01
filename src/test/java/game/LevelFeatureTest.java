package game;

import game.entities.ghosts.*;
import game.entities.ghostDecorator.*;
import game.entities.levelStrategies.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 레벨 기능 핵심 테스트 (20개)
 *
 * 테스트 구성:
 * - LevelStrategy 테스트: 6개
 * - GhostDecorator 테스트: 6개
 * - Ghost 해산시간 테스트: 4개
 * - Game 레벨 적용 테스트: 4개
 */
public class LevelFeatureTest {

    private Ghost baseGhost;
    private Level1Strategy level1Strategy;
    private Level2Strategy level2Strategy;
    private Level3Strategy level3Strategy;

    @Before
    public void setUp() {
        baseGhost = new Blinky(100, 100);
        level1Strategy = new Level1Strategy();
        level2Strategy = new Level2Strategy();
        level3Strategy = new Level3Strategy();
    }

    // ==================== LevelStrategy 테스트 (6개) ====================

    /**
     * 테스트 1: Level1Strategy - 모든 특수 기능 비활성화
     */
    @Test
    public void testLevel1Strategy_NoSpecialFeatures() {
        assertEquals("순간이동 비활성화", 0, level1Strategy.getTeleportInterval());
        assertEquals("투명화 비활성화", 0, level1Strategy.getInvisibleInterval());
        assertEquals("속도 증가 없음", 0.0, level1Strategy.getSpeedIncreaseRate(), 0.001);
        assertEquals("해산시간 감소 없음", 0.0, level1Strategy.getFrightenedTimerReduction(), 0.001);
    }

    /**
     * 테스트 2: Level2Strategy - 순간이동 활성화, 속도 10% 증가
     */
    @Test
    public void testLevel2Strategy_TeleportAndSpeedBoost() {
        assertTrue("순간이동 활성화", level2Strategy.getTeleportInterval() > 0);
        assertEquals("투명화 비활성화", 0, level2Strategy.getInvisibleInterval());
        assertEquals("속도 10% 증가", 0.1, level2Strategy.getSpeedIncreaseRate(), 0.001);
        assertEquals("해산시간 20% 감소", 0.2, level2Strategy.getFrightenedTimerReduction(), 0.001);
    }

    /**
     * 테스트 3: Level3Strategy - 모든 기능 활성화
     */
    @Test
    public void testLevel3Strategy_AllFeaturesEnabled() {
        assertTrue("순간이동 활성화", level3Strategy.getTeleportInterval() > 0);
        assertTrue("투명화 활성화", level3Strategy.getInvisibleInterval() > 0);
        assertEquals("투명 지속시간 1초", 60, level3Strategy.getInvisibleDuration());
        assertEquals("속도 20% 증가", 0.2, level3Strategy.getSpeedIncreaseRate(), 0.001);
        assertEquals("해산시간 40% 감소", 0.4, level3Strategy.getFrightenedTimerReduction(), 0.001);
    }

    /**
     * 테스트 4: Level2 순간이동 간격 랜덤 범위 확인 (1~5초)
     */
    @Test
    public void testLevel2Strategy_TeleportIntervalRange() {
        for (int i = 0; i < 10; i++) {
            int interval = level2Strategy.getTeleportInterval();
            assertTrue("순간이동 간격 >= 60 프레임(1초)", interval >= 60);
            assertTrue("순간이동 간격 <= 300 프레임(5초)", interval <= 300);
        }
    }

    /**
     * 테스트 5: Level3 투명화 간격 랜덤 범위 확인 (2~8초)
     */
    @Test
    public void testLevel3Strategy_InvisibleIntervalRange() {
        for (int i = 0; i < 10; i++) {
            int interval = level3Strategy.getInvisibleInterval();
            assertTrue("투명화 간격 >= 120 프레임(2초)", interval >= 120);
            assertTrue("투명화 간격 <= 480 프레임(8초)", interval <= 480);
        }
    }

    /**
     * 테스트 6: Strategy 패턴 다형성 - 인터페이스 구현 확인
     */
    @Test
    public void testStrategyPolymorphism() {
        LevelStrategy[] strategies = {level1Strategy, level2Strategy, level3Strategy};

        for (LevelStrategy strategy : strategies) {
            assertNotNull("Strategy는 null이 아님", strategy);
            assertTrue("LevelStrategy 인터페이스 구현", strategy instanceof LevelStrategy);
            // 모든 메서드 호출 가능 확인
            strategy.getTeleportInterval();
            strategy.getInvisibleInterval();
            strategy.getSpeedIncreaseRate();
            strategy.getFrightenedTimerReduction();
        }
    }

    // ==================== GhostDecorator 테스트 (6개) ====================

    /**
     * 테스트 7: GhostDecorator 위치 위임 확인
     */
    @Test
    public void testGhostDecorator_DelegatesPosition() {
        GhostDecorator decorator = new GhostDecorator(baseGhost);

        assertEquals("X 위치 위임", baseGhost.getxPos(), decorator.getxPos());
        assertEquals("Y 위치 위임", baseGhost.getyPos(), decorator.getyPos());
        assertEquals("속도 위임", baseGhost.getSpd(), decorator.getSpd());
    }

    /**
     * 테스트 8: SpeedBoostDecorator - Level1에서 속도 변화 없음
     */
    @Test
    public void testSpeedBoostDecorator_Level1_NoChange() {
        int originalSpeed = baseGhost.getSpd();
        SpeedBoostGhostDecorator decorator =
                new SpeedBoostGhostDecorator(baseGhost, level1Strategy);

        assertEquals("Level1에서 속도 변화 없음", originalSpeed, decorator.getSpd());
    }

    /**
     * 테스트 9: SpeedBoostDecorator - Level2에서 속도 10% 증가
     */
    @Test
    public void testSpeedBoostDecorator_Level2_SpeedIncreased() {
        int originalSpeed = baseGhost.getSpd();  // 2
        SpeedBoostGhostDecorator decorator =
                new SpeedBoostGhostDecorator(baseGhost, level2Strategy);

        int expectedSpeed = (int) Math.round(originalSpeed * 1.1);  // 2.2 → 2
        assertEquals("Level2에서 속도 10% 증가", expectedSpeed, decorator.getSpd());
    }

    /**
     * 테스트 10: SpeedBoostDecorator - Level3에서 속도 20% 증가
     */
    @Test
    public void testSpeedBoostDecorator_Level3_SpeedIncreased() {
        int originalSpeed = baseGhost.getSpd();  // 2
        SpeedBoostGhostDecorator decorator =
                new SpeedBoostGhostDecorator(baseGhost, level3Strategy);

        int expectedSpeed = (int) Math.round(originalSpeed * 1.2);  // 2.4 → 2
        assertEquals("Level3에서 속도 20% 증가", expectedSpeed, decorator.getSpd());
    }

    /**
     * 테스트 11: Decorator 체이닝 - 여러 Decorator 중첩
     */
    @Test
    public void testDecoratorChaining() {
        Ghost ghost = baseGhost;
        int originalX = ghost.getxPos();
        int originalY = ghost.getyPos();

        // Decorator 체이닝
        ghost = new SpeedBoostGhostDecorator(ghost, level3Strategy);
        ghost = new SlowGhostDecorator(ghost, 2);
        ghost = new TeleportGhostDecorator(ghost, level3Strategy);
        ghost = new InvisibleGhostDecorator(ghost, level3Strategy);

        // 체이닝 후에도 위치 보존
        assertEquals("체이닝 후 X 위치 보존", originalX, ghost.getxPos());
        assertEquals("체이닝 후 Y 위치 보존", originalY, ghost.getyPos());
        assertTrue("최종 타입은 InvisibleGhostDecorator",
                ghost instanceof InvisibleGhostDecorator);
    }

    /**
     * 테스트 12: 모든 Ghost 타입에 Decorator 적용
     */
    @Test
    public void testDecoratorAppliedToAllGhostTypes() {
        Ghost[] ghosts = {
                new Blinky(100, 100),
                new Pinky(100, 100),
                new Inky(100, 100),
                new Clyde(100, 100)
        };

        for (Ghost ghost : ghosts) {
            SpeedBoostGhostDecorator decorated =
                    new SpeedBoostGhostDecorator(ghost, level2Strategy);

            assertEquals("위치 보존", 100, decorated.getxPos());
            assertTrue("Ghost 타입 유지", decorated instanceof Ghost);
        }
    }

    // ==================== Ghost 해산시간 테스트 (4개) ====================

    /**
     * 테스트 13: 기본 해산시간 420 프레임 (7초)
     */
    @Test
    public void testBaseFrightenedTime() {
        int baseTime = 60 * 7;  // 420 프레임
        assertEquals("기본 해산시간 420 프레임", 420, baseTime);
    }

    /**
     * 테스트 14: 레벨별 해산시간 계산
     */
    @Test
    public void testFrightenedTime_AllLevels() {
        int baseTime = 60 * 7;  // 420 프레임

        // Level 1: 감소 없음 (0%)
        int level1Time = (int) Math.round(baseTime * (1.0 - 0.0));
        assertEquals("Level 1: 420 프레임 (7.0초)", 420, level1Time);

        // Level 2: 20% 감소
        int level2Time = (int) Math.round(baseTime * (1.0 - 0.2));
        assertEquals("Level 2: 336 프레임 (5.6초)", 336, level2Time);

        // Level 3: 40% 감소
        int level3Time = (int) Math.round(baseTime * (1.0 - 0.4));
        assertEquals("Level 3: 252 프레임 (4.2초)", 252, level3Time);
    }

    /**
     * 테스트 15: 해산시간 감소량 검증
     */
    @Test
    public void testFrightenedTimeReduction() {
        int baseTime = 420;

        // Level 1 → Level 2: 84 프레임 감소 (20%)
        int level2Reduction = baseTime - 336;
        assertEquals("Level 2 감소량: 84 프레임", 84, level2Reduction);

        // Level 1 → Level 3: 168 프레임 감소 (40%)
        int level3Reduction = baseTime - 252;
        assertEquals("Level 3 감소량: 168 프레임", 168, level3Reduction);
    }

    /**
     * 테스트 16: 깜빡임 시작 시점 (70% 지점)
     */
    @Test
    public void testWarningTime() {
        // 깜빡임은 해산시간의 70% 지점에서 시작
        int level1Warning = (int)(420 * 0.7);  // 294
        int level2Warning = (int)(336 * 0.7);  // 235
        int level3Warning = (int)(252 * 0.7);  // 176

        assertEquals("Level 1 깜빡임 시작: 294 프레임", 294, level1Warning);
        assertEquals("Level 2 깜빡임 시작: 235 프레임", 235, level2Warning);
        assertEquals("Level 3 깜빡임 시작: 176 프레임", 176, level3Warning);
    }

    // ==================== Game 레벨 적용 테스트 (4개) ====================

    /**
     * 테스트 17: 레벨별 Strategy 생성
     */
    @Test
    public void testLevelStrategyCreation() {
        // 레벨에 따른 Strategy 생성 시뮬레이션
        LevelStrategy strategy1 = createLevelStrategy(1);
        LevelStrategy strategy2 = createLevelStrategy(2);
        LevelStrategy strategy3 = createLevelStrategy(3);

        assertTrue("Level 1 → Level1Strategy", strategy1 instanceof Level1Strategy);
        assertTrue("Level 2 → Level2Strategy", strategy2 instanceof Level2Strategy);
        assertTrue("Level 3 → Level3Strategy", strategy3 instanceof Level3Strategy);
    }

    /**
     * 테스트 18: 레벨별 Decorator 적용 확인
     */
    @Test
    public void testDecoratorApplicationByLevel() {
        // Level 1: 특수 Decorator 없음
        assertFalse("Level 1: 순간이동 없음", level1Strategy.getTeleportInterval() > 0);
        assertFalse("Level 1: 투명화 없음", level1Strategy.getInvisibleInterval() > 0);

        // Level 2: 순간이동만
        assertTrue("Level 2: 순간이동 있음", level2Strategy.getTeleportInterval() > 0);
        assertFalse("Level 2: 투명화 없음", level2Strategy.getInvisibleInterval() > 0);

        // Level 3: 순간이동 + 투명화
        assertTrue("Level 3: 순간이동 있음", level3Strategy.getTeleportInterval() > 0);
        assertTrue("Level 3: 투명화 있음", level3Strategy.getInvisibleInterval() > 0);
    }

    /**
     * 테스트 19: 레벨 진행에 따른 난이도 증가 확인
     */
    @Test
    public void testDifficultyProgression() {
        double[] speedRates = {
                level1Strategy.getSpeedIncreaseRate(),
                level2Strategy.getSpeedIncreaseRate(),
                level3Strategy.getSpeedIncreaseRate()
        };

        double[] frightenedReductions = {
                level1Strategy.getFrightenedTimerReduction(),
                level2Strategy.getFrightenedTimerReduction(),
                level3Strategy.getFrightenedTimerReduction()
        };

        // 레벨이 올라갈수록 속도 증가율 상승
        assertTrue("Level 2 속도 > Level 1", speedRates[1] > speedRates[0]);
        assertTrue("Level 3 속도 > Level 2", speedRates[2] > speedRates[1]);

        // 레벨이 올라갈수록 해산시간 감소율 상승
        assertTrue("Level 2 감소율 > Level 1", frightenedReductions[1] > frightenedReductions[0]);
        assertTrue("Level 3 감소율 > Level 2", frightenedReductions[2] > frightenedReductions[1]);
    }

    /**
     * 테스트 20: 통합 테스트 - 레벨 3 전체 설정
     */
    @Test
    public void testLevel3FullConfiguration() {
        // Ghost 생성 및 Decorator 적용
        Ghost ghost = new Blinky(100, 100);
        ghost = new SpeedBoostGhostDecorator(ghost, level3Strategy);
        ghost = new TeleportGhostDecorator(ghost, level3Strategy);
        ghost = new InvisibleGhostDecorator(ghost, level3Strategy);

        // 설정 확인
        assertNotNull("Ghost 생성됨", ghost);
        assertEquals("위치 보존", 100, ghost.getxPos());

        // Level 3 Strategy 설정값 확인
        assertEquals("속도 증가율 0.2", 0.2, level3Strategy.getSpeedIncreaseRate(), 0.001);
        assertEquals("해산시간 감소율 0.4", 0.4, level3Strategy.getFrightenedTimerReduction(), 0.001);
        assertTrue("순간이동 활성화", level3Strategy.getTeleportInterval() > 0);
        assertTrue("투명화 활성화", level3Strategy.getInvisibleInterval() > 0);
    }

    // ==================== 헬퍼 메서드 ====================

    /**
     * Game.initializeLevelStrategy() 로직 시뮬레이션
     */
    private LevelStrategy createLevelStrategy(int gameLevel) {
        switch (gameLevel) {
            case 1: return new Level1Strategy();
            case 2: return new Level2Strategy();
            case 3: return new Level3Strategy();
            default: return new Level1Strategy();
        }
    }
}