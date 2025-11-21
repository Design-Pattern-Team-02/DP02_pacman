package game.entities.ghostDecorator;

import game.Game;
import game.entities.ghosts.Ghost;
import game.entities.levelStrategies.LevelStrategy;
import game.utils.WallCollisionDetector;

/**
 * Decorator 패턴: 고스트에 순간이동 기능 추가
 *
 * 기능:
 * - LevelStrategy에서 정의한 랜덤 주기(1~5초)마다 순간이동 발동
 * - 현재 진행 방향으로 6칸(48픽셀) 앞으로 이동
 * - 순간이동 범위 내 모든 칸에 벽이 있는지 체크
 * - 순간이동 경로에 벽이 있으면 순간이동 취소
 * - 순간이동 후 팩맨과 충돌하면 즉시 게임 오버
 *
 * 적용 대상: 레벨 2 이상의 모든 고스트 (Blinky, Pinky, Inky, Clyde)
 */
public class TeleportGhostDecorator extends GhostDecorator {
    private int teleportTimer = 0;
    private int nextTeleportTime = 0;
    private LevelStrategy levelStrategy;
    private static final int TELEPORT_DISTANCE = 6; // 6칸

    public TeleportGhostDecorator(Ghost ghost, LevelStrategy levelStrategy) {
        super(ghost);
        this.levelStrategy = levelStrategy;
        // 각 고스트마다 다른 시작 시간 (0~2초 랜덤)
        this.teleportTimer = (int)(Math.random() * 120);
        this.nextTeleportTime = levelStrategy.getTeleportInterval();
    }

    @Override
    public void before_updatePosition() {
        ghost.before_updatePosition();

        if (!Game.getFirstInput()) return;

        int interval = levelStrategy.getTeleportInterval();
        if (interval == 0) return;

        teleportTimer++;

        if (teleportTimer >= nextTeleportTime) {
            teleportTimer = 0;
            performTeleport();
            nextTeleportTime = levelStrategy.getTeleportInterval();
        }
    }

    /**
     * 순간이동 실행
     */
    private void performTeleport() {
        int cellSize = 8;
        int distance = TELEPORT_DISTANCE * cellSize; // 6칸 = 48픽셀

        int currentX = ghost.getxPos();
        int currentY = ghost.getyPos();
        int targetX = currentX;
        int targetY = currentY;

        // 현재 이동 방향 확인
        int xSpd = ghost.getxSpd();
        int ySpd = ghost.getySpd();

        // 정지 상태면 순간이동 안 함
        if (xSpd == 0 && ySpd == 0) {
            return;
        }

        // 목표 위치 계산
        if (xSpd > 0) targetX += distance;        // 오른쪽
        else if (xSpd < 0) targetX -= distance;   // 왼쪽
        else if (ySpd > 0) targetY += distance;   // 아래
        else if (ySpd < 0) targetY -= distance;   // 위

        System.out.println("=== 순간이동 시도 ===");
        System.out.println("현재: (" + currentX + ", " + currentY + ")");
        System.out.println("목표: (" + targetX + ", " + targetY + ")");

        // 순간이동 경로에 벽이 있는지 체크
        if (!isTeleportPathClear(currentX, currentY, targetX, targetY)) {
            System.out.println("경로에 벽 존재 - 순간이동 취소");
            return;
        }

        // 순간이동 실행 (Reflection 사용)
        System.out.println("순간이동 성공!");
        setGhostPositionDirectly(targetX, targetY);
    }

    /**
     * 순간이동 경로가 안전한지 체크
     * 시작점부터 목표점까지 8픽셀(1칸) 단위로 모든 중간 지점 체크
     */
    private boolean isTeleportPathClear(int startX, int startY, int targetX, int targetY) {
        int deltaX = targetX - startX;
        int deltaY = targetY - startY;

        // 이동 거리를 8픽셀 단위로 나눔
        int cellSize = 8;
        int steps = Math.max(Math.abs(deltaX), Math.abs(deltaY)) / cellSize;

        if (steps == 0) return true;

        // 각 칸마다 벽 체크 (시작점 제외, 목표점 포함)
        for (int i = 1; i <= steps; i++) {
            int checkX = startX + (deltaX * i / steps);
            int checkY = startY + (deltaY * i / steps);

            // WallCollisionDetector를 사용하여 벽 체크
            // dx, dy는 현재 위치에서 체크할 위치까지의 상대 거리
            int dx = checkX - ghost.getxPos();
            int dy = checkY - ghost.getyPos();

            if (WallCollisionDetector.checkWallCollision(ghost, dx, dy)) {
                System.out.println("  [" + i + "/" + steps + "] 벽 감지: (" + checkX + ", " + checkY + ")");
                return false;
            }
        }

        System.out.println("  경로 안전 (" + steps + "칸 체크 완료)");
        return true;
    }

    /**
     * 고스트를 목표 위치로 순간이동
     *
     * GhostDecorator는 자체 위치를 가지지 않고 내부 ghost에 위임하므로,
     * 가장 안쪽의 실제 Ghost 객체의 xPos, yPos만 변경
     */
    private void setGhostPositionDirectly(int newX, int newY) {
        try {
            // Decorator를 벗겨서 실제 Ghost 객체 찾기
            Ghost actualGhost = ghost;
            while (actualGhost instanceof GhostDecorator) {
                java.lang.reflect.Field ghostField = GhostDecorator.class.getDeclaredField("ghost");
                ghostField.setAccessible(true);
                actualGhost = (Ghost) ghostField.get(actualGhost);
            }

            // 실제 Ghost 객체의 xPos, yPos 변경
            Class<?> currentClass = actualGhost.getClass();
            java.lang.reflect.Field xPosField = null;
            java.lang.reflect.Field yPosField = null;

            // 상위 클래스를 순회하며 xPos, yPos 필드 찾기
            while (currentClass != null) {
                try {
                    xPosField = currentClass.getDeclaredField("xPos");
                    yPosField = currentClass.getDeclaredField("yPos");
                    break;
                } catch (NoSuchFieldException e) {
                    currentClass = currentClass.getSuperclass();
                }
            }

            if (xPosField != null && yPosField != null) {
                xPosField.setAccessible(true);
                yPosField.setAccessible(true);
                xPosField.set(actualGhost, newX);
                yPosField.set(actualGhost, newY);
            } else {
                System.err.println("ERROR: xPos, yPos 필드를 찾을 수 없음");
            }
        } catch (Exception e) {
            System.err.println("ERROR: 순간이동 실패 - " + e.getMessage());
            e.printStackTrace();
        }
    }
}