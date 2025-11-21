package game.entities.ghostDecorator;

import game.entities.ghosts.Ghost;
import game.entities.levelStrategies.LevelStrategy;

/**
 * Decorator 패턴: 고스트에 레벨별 속도 증가 기능 추가
 *
 * 기능:
 * - LevelStrategy에서 정의한 속도 증가율을 적용
 * - 레벨 1: 0% 증가 (기본 속도 2)
 * - 레벨 2: 10% 증가 (속도 2 → 2.2)
 * - 레벨 3: 20% 증가 (속도 2 → 2.4)
 *
 * 패턴 적용:
 * - Decorator: 기존 Ghost 기능에 속도 증가 추가
 * - Strategy: 레벨별로 다른 속도 증가율 적용
 *
 * 구현 방식:
 * - getSpd() 메서드를 오버라이드하여 증가된 속도 반환
 * - 실제 이동은 before_updatePosition에서 증가된 속도로 계산
 */
public class SpeedBoostGhostDecorator extends GhostDecorator {
    private LevelStrategy levelStrategy;
    private int boostedSpeed;

    public SpeedBoostGhostDecorator(Ghost ghost, LevelStrategy levelStrategy) {
        super(ghost);
        this.levelStrategy = levelStrategy;

        // 기본 속도에 증가율 적용
        int baseSpeed = ghost.getSpd();
        double increaseRate = levelStrategy.getSpeedIncreaseRate();
        this.boostedSpeed = (int) Math.round(baseSpeed * (1.0 + increaseRate));
    }

    /**
     * 증가된 속도 반환
     * Ghost의 모든 이동 계산은 이 값을 사용
     */
    @Override
    public int getSpd() {
        return boostedSpeed;
    }

    /**
     * 실제 이동 시 증가된 속도 적용
     * xSpd, ySpd의 방향은 유지하되 속도만 증가
     */
    @Override
    public void before_updatePosition() {
        ghost.before_updatePosition();

        // 속도가 증가했으므로 xSpd, ySpd도 비례하여 조정
        int originalSpd = super.getSpd();
        if (originalSpd > 0 && boostedSpeed != originalSpd) {
            int currentXSpd = ghost.getxSpd();
            int currentYSpd = ghost.getySpd();

            // 방향은 유지하되 속도만 증가
            if (currentXSpd != 0) {
                int direction = currentXSpd > 0 ? 1 : -1;
                ghost.setxSpd(direction * boostedSpeed);
            }
            if (currentYSpd != 0) {
                int direction = currentYSpd > 0 ? 1 : -1;
                ghost.setySpd(direction * boostedSpeed);
            }
        }
    }
}