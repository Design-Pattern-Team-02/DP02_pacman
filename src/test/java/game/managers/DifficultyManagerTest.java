package game.managers;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.List;

/**
 * DifficultyManager 단위 테스트
 *
 * 테스트 목적:
 * 1. Singleton 패턴이 올바르게 동작하는지 확인
 * 2. 점수 기반 난이도 레벨업 로직 검증
 * 3. 레벨 정보 조회 기능 검증
 * 4. 레벨 리셋 기능 검증
 */
public class DifficultyManagerTest {
    private DifficultyManager difficultyManager;

    @Before
    public void setUp() throws Exception {
        // 각 테스트 전에 싱글톤 인스턴스를 리셋
        resetSingleton();
        difficultyManager = DifficultyManager.getInstance();
    }

    @After
    public void tearDown() throws Exception {
        // 싱글톤 리셋
        resetSingleton();
    }

    /**
     * Reflection을 사용하여 Singleton 인스턴스를 리셋
     */
    private void resetSingleton() throws Exception {
        Field instance = DifficultyManager.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    public void testSingletonInstance() {
        // Given
        DifficultyManager instance1 = DifficultyManager.getInstance();
        DifficultyManager instance2 = DifficultyManager.getInstance();

        // Then
        assertSame("두 인스턴스는 동일해야 함 (Singleton)", instance1, instance2);
    }

    @Test
    public void testInitialLevel() {
        // When
        int currentLevel = difficultyManager.getCurrentLevelNumber();
        DifficultyLevel level = difficultyManager.getCurrentLevel();

        // Then
        assertEquals("초기 레벨은 1이어야 함", 1, currentLevel);
        assertEquals("초기 레벨 객체의 레벨도 1이어야 함", 1, level.getLevel());
    }

    @Test
    public void testLevelUpAt1000Points() {
        // When
        boolean levelChanged = difficultyManager.updateLevel(1000);

        // Then
        assertTrue("1000점에서 레벨업 되어야 함", levelChanged);
        assertEquals("레벨 2에 도달해야 함", 2, difficultyManager.getCurrentLevelNumber());
    }

    @Test
    public void testLevelUpAt2000Points() {
        // When
        difficultyManager.updateLevel(2000);

        // Then
        assertEquals("레벨 3에 도달해야 함", 3, difficultyManager.getCurrentLevelNumber());
    }

    @Test
    public void testNoLevelUpBelowThreshold() {
        // When
        boolean levelChanged = difficultyManager.updateLevel(500);

        // Then
        assertFalse("점수가 충분하지 않으면 레벨업 안됨", levelChanged);
        assertEquals("레벨 1에 머물러야 함", 1, difficultyManager.getCurrentLevelNumber());
    }

    @Test
    public void testMultipleLevelUps() {
        // Given
        difficultyManager.updateLevel(1000); // Level 2

        // When
        boolean levelChanged = difficultyManager.updateLevel(2500); // Level 3

        // Then
        assertTrue("레벨업이 발생해야 함", levelChanged);
        assertEquals("레벨 3에 도달해야 함", 3, difficultyManager.getCurrentLevelNumber());
    }

    @Test
    public void testGhostSpeedIncreaseWithLevel() {
        // Given
        double level1Speed = difficultyManager.getCurrentLevel().getGhostSpeedMultiplier();

        // When
        difficultyManager.updateLevel(1000); // Level 2
        double level2Speed = difficultyManager.getCurrentLevel().getGhostSpeedMultiplier();

        // Then
        assertTrue("레벨이 올라가면 유령 속도가 증가해야 함", level2Speed > level1Speed);
    }

    @Test
    public void testGetScoreToNextLevel() {
        // Given
        int currentScore = 500;

        // When
        int scoreNeeded = difficultyManager.getScoreToNextLevel(currentScore);

        // Then
        assertEquals("다음 레벨까지 500점 필요", 500, scoreNeeded);
    }

    @Test
    public void testGetScoreToNextLevelAt1500Points() {
        // Given
        difficultyManager.updateLevel(1500); // Level 2
        int currentScore = 1500;

        // When
        int scoreNeeded = difficultyManager.getScoreToNextLevel(currentScore);

        // Then
        assertEquals("다음 레벨까지 500점 필요", 500, scoreNeeded);
    }

    @Test
    public void testGetScoreToNextLevelAtMaxLevel() {
        // Given
        difficultyManager.updateLevel(10000); // Max level

        // When
        int scoreNeeded = difficultyManager.getScoreToNextLevel(10000);

        // Then
        assertEquals("최고 레벨에서는 0 반환", 0, scoreNeeded);
    }

    @Test
    public void testReset() {
        // Given
        difficultyManager.updateLevel(3000); // Level 4
        int levelBeforeReset = difficultyManager.getCurrentLevelNumber();

        // When
        difficultyManager.reset();
        int levelAfterReset = difficultyManager.getCurrentLevelNumber();

        // Then
        assertTrue("reset 전 레벨은 1보다 높아야 함", levelBeforeReset > 1);
        assertEquals("reset 후 레벨은 1이어야 함", 1, levelAfterReset);
    }

    @Test
    public void testGetAllLevels() {
        // When
        List<DifficultyLevel> allLevels = difficultyManager.getAllLevels();

        // Then
        assertNotNull("레벨 리스트는 null이 아니어야 함", allLevels);
        assertTrue("최소 2개 이상의 레벨이 있어야 함", allLevels.size() >= 2);
    }

    @Test
    public void testLevelThresholdsAreOrdered() {
        // When
        List<DifficultyLevel> allLevels = difficultyManager.getAllLevels();

        // Then
        for (int i = 1; i < allLevels.size(); i++) {
            int prevThreshold = allLevels.get(i - 1).getScoreThreshold();
            int currentThreshold = allLevels.get(i).getScoreThreshold();
            assertTrue("레벨 임계값은 오름차순이어야 함",
                    currentThreshold > prevThreshold);
        }
    }

    @Test
    public void testDifficultyLevelProperties() {
        // When
        DifficultyLevel level = difficultyManager.getCurrentLevel();

        // Then
        assertNotNull("DifficultyLevel은 null이 아니어야 함", level);
        assertTrue("레벨 번호는 양수여야 함", level.getLevel() > 0);
        assertTrue("임계값은 0 이상이어야 함", level.getScoreThreshold() >= 0);
        assertTrue("유령 속도 배율은 양수여야 함", level.getGhostSpeedMultiplier() > 0);
        assertNotNull("레벨 설명은 null이 아니어야 함", level.getDescription());
    }

    @Test
    public void testConsecutiveLevelUpdatesWithSameScore() {
        // Given
        int score = 1500;
        difficultyManager.updateLevel(score);

        // When
        boolean levelChanged = difficultyManager.updateLevel(score);

        // Then
        assertFalse("같은 점수로 다시 업데이트하면 레벨업 안됨", levelChanged);
    }

    @Test
    public void testLevelDescriptionExists() {
        // When
        DifficultyLevel level = difficultyManager.getCurrentLevel();
        String description = level.getDescription();

        // Then
        assertNotNull("레벨 설명은 null이 아니어야 함", description);
        assertFalse("레벨 설명은 비어있지 않아야 함", description.isEmpty());
    }

    @Test
    public void testScorePerLevel() {
        // When
        int scorePerLevel = DifficultyManager.getScorePerLevel();

        // Then
        assertEquals("레벨당 점수는 1000이어야 함", 1000, scorePerLevel);
    }

    @Test
    public void testHighScoreLevelUp() {
        // When
        difficultyManager.updateLevel(5000);
        int levelNumber = difficultyManager.getCurrentLevelNumber();

        // Then
        assertTrue("5000점은 레벨 5 이상이어야 함", levelNumber >= 5);
    }

    @Test
    public void testLevelTransition() {
        // Given
        int level1 = difficultyManager.getCurrentLevelNumber();

        // When
        difficultyManager.updateLevel(999);  // 레벨 1 유지
        int level2 = difficultyManager.getCurrentLevelNumber();

        difficultyManager.updateLevel(1000); // 레벨 2로 상승
        int level3 = difficultyManager.getCurrentLevelNumber();

        // Then
        assertEquals("999점에서는 레벨 1", 1, level1);
        assertEquals("999점에서는 여전히 레벨 1", 1, level2);
        assertEquals("1000점에서는 레벨 2", 2, level3);
    }
}
