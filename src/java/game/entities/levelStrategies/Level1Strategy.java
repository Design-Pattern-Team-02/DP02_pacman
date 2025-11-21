package game.entities.levelStrategies;

/**
 * 레벨 1 전략: 기본 게임 모드
 *
 * 특징:
 * - 기존 게임 플레이 유지
 * - 속도 증가 없음 (0%)
 * - 해산시간 감소 없음 (0%)
 */
public class Level1Strategy implements LevelStrategy {

    @Override
    public int getTeleportInterval() {
        return 0; // 순간이동 비활성화
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
        return 0.0; // 0% 속도 증가
    }

    @Override
    public double getFrightenedTimerReduction() {
        return 0.0; // 0% 해산시간 감소
    }

    @Override
    public String getLevelName() {
        return "Level 1: Basic Mode (Speed +0%, Frightened Time -0%)";
    }
}