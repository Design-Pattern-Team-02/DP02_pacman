package game.entities.levelStrategies;

/**
 * 레벨 2 전략: 순간이동 + 속도 증가
 *
 * 특징:
 * - 1~5초 사이 랜덤 주기로 진행 방향으로 6칸 순간이동
 * - 고스트 속도 10% 증가
 * - 해산시간 20% 감소 (7초 → 5.6초)
 */
public class Level2Strategy implements LevelStrategy {

    @Override
    public int getTeleportInterval() {
        // 1~5초 사이 랜덤 (60~300 프레임)
        return 60 + (int)(Math.random() * 240); // 60 + (0~240)
    }

    @Override
    public int getInvisibleInterval() {
        return 0; // 투명화 비활성화
    }

    @Override
    public int getInvisibleDuration() {
        return 0; // 투명화 비활성화
    }

    @Override
    public double getSpeedIncreaseRate() {
        return 0.1; // 10% 속도 증가
    }

    @Override
    public double getFrightenedTimerReduction() {
        return 0.2; // 20% 해산시간 감소
    }

    @Override
    public String getLevelName() {
        return "Level 2: Teleport Mode (Speed +10%, Frightened Time -20%)";
    }
}