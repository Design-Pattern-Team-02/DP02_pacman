package game.entities.ghostDecorator;

import game.Game;
import game.entities.ghosts.Ghost;
import game.entities.levelStrategies.LevelStrategy;

import java.awt.*;

/**
 * Decorator 패턴: 고스트에 투명화 기능 추가
 *
 * 기능:
 * - LevelStrategy에서 정의한 랜덤 주기(2~8초)마다 투명화 발동
 * - 투명 상태에서 완전히 안 보임 (렌더링 안 됨)
 * - 투명 상태에서 Pacman과 충돌 안 됨 (빈 히트박스 반환)
 * - 1초 후 자동으로 원래 상태로 복귀
 *
 * 적용 대상: 레벨 3의 모든 고스트 (Blinky, Pinky, Inky, Clyde)
 *
 * 패턴 적용:
 * - Decorator: 기존 Ghost 기능에 투명화 추가
 * - Strategy: 레벨별로 다른 발동 주기와 지속 시간 적용
 */
public class InvisibleGhostDecorator extends GhostDecorator {
    private int invisibleTimer = 0;      // 다음 투명화까지 남은 시간
    private int invisibleDuration = 0;   // 현재 투명 상태 유지 시간
    private boolean isInvisible = false; // 현재 투명 상태 여부
    private int nextInvisibleTime = 0;   // 다음 투명화까지 필요한 시간
    private LevelStrategy levelStrategy;

    public InvisibleGhostDecorator(Ghost ghost, LevelStrategy levelStrategy) {
        super(ghost);
        this.levelStrategy = levelStrategy;
        this.nextInvisibleTime = levelStrategy.getInvisibleInterval(); // 첫 랜덤 시간 설정
    }

    @Override
    public void before_updatePosition() {
        // 기존 고스트 로직 먼저 실행 (Template Method 패턴)
        ghost.before_updatePosition();

        // 게임 시작 전이면 투명화 안 함
        if (!Game.getFirstInput()) return;

        // Strategy에서 투명화 설정 가져오기
        int interval = levelStrategy.getInvisibleInterval();
        int duration = levelStrategy.getInvisibleDuration();

        if (interval == 0 || duration == 0) return; // 비활성화 상태

        if (isInvisible) {
            // 투명 상태: 지속 시간 체크
            invisibleDuration++;

            if (invisibleDuration >= duration) {
                // 투명 상태 종료
                isInvisible = false;
                invisibleDuration = 0;
                invisibleTimer = 0;
                System.out.println("=== 투명 상태 종료 ===");
                // 다음 투명화를 위한 새로운 랜덤 시간 설정
                nextInvisibleTime = levelStrategy.getInvisibleInterval();
            }
        } else {
            // 일반 상태: 다음 투명화까지 타이머
            invisibleTimer++;

            if (invisibleTimer >= nextInvisibleTime) {
                // 투명 상태 시작
                isInvisible = true;
                System.out.println("=== 투명 상태 시작 ===");
                invisibleTimer = 0;
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (isInvisible) {
            // 투명 상태: 완전히 안 보이게 (렌더링 스킵)
            // 아무것도 그리지 않음
        } else {
            // 일반 상태: 정상 렌더링
            ghost.render(g);
        }
    }

    @Override
    public Rectangle getHitbox() {
        if (isInvisible) {
            // 투명 상태: 빈 히트박스 반환 (충돌 안 됨)
            return new Rectangle(0, 0, 0, 0);
        }
        // 일반 상태: 정상 히트박스
        return ghost.getHitbox();
    }
}