package game.entities.levelStrategies;

/**
 * 레벨 3 전략: 순간이동 + 투명화 + 최대 난이도
 *
 * 특징:
 * - 1~5초 사이 랜덤 주기로 진행 방향으로 6칸 순간이동
 * - 2~8초 사이 랜덤 주기로 1초간 투명화 + 충돌 무시
 * - 고스트 속도 20% 증가
 * - 해산시간 40% 감소 (7초 → 4.2초)
 */
public class Level3Strategy implements LevelStrategy {

    @Override
    public int getTeleportInterval() {
        // 1~5초 사이 랜덤 (60~300 프레임)
        return 60 + (int)(Math.random() * 240); // 60 + (0~240)
    }

    @Override
    public int getInvisibleInterval() {
        // 2~8초 사이 랜덤 (120~480 프레임)
        return 120 + (int)(Math.random() * 360); // 120 + (0~360)
    }

    @Override
    public int getInvisibleDuration() {
        return 60 * 1; // 1초간 투명 유지
    }

    @Override
    public double getSpeedIncreaseRate() {
        return 0.2; // 20% 속도 증가
    }

    @Override
    public double getFrightenedTimerReduction() {
        return 0.4; // 40% 해산시간 감소
    }

    @Override
    public String getLevelName() {
        return "Level 3: Teleport + Invisible Mode (Speed +20%, Frightened Time -40%)";
    }
}