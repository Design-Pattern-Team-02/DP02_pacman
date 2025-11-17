package mapeditor.commands;

import mapeditor.model.MapData;
import mapeditor.model.EntityType;

/**
 * RemoveEntityCommand - 엔티티 제거 커맨드
 * 맵에서 엔티티를 제거하는 액션을 캡슐화
 *
 * Command Pattern 구현체:
 * - 엔티티 제거를 실행하고 되돌릴 수 있음
 * - 제거된 엔티티를 기억하여 완벽한 Undo 지원
 */
public class RemoveEntityCommand implements Command {
    private MapData mapData;
    private int x, y;
    private EntityType removedEntityType;
    private boolean executed;

    public RemoveEntityCommand(MapData mapData, int x, int y) {
        this.mapData = mapData;
        this.x = x;
        this.y = y;
        this.executed = false;
    }

    @Override
    public boolean execute() {
        // 제거할 엔티티 저장
        removedEntityType = mapData.getEntityAt(x, y);

        if (removedEntityType == null || removedEntityType == EntityType.EMPTY) {
            return false; // 제거할 것이 없음
        }

        // 제거 실행
        boolean success = mapData.removeEntity(x, y);
        if (success) {
            executed = true;
        }
        return success;
    }

    @Override
    public boolean undo() {
        if (!executed || removedEntityType == null) {
            return false;
        }

        // 제거된 엔티티 복원
        boolean success = mapData.placeEntity(x, y, removedEntityType);
        if (success) {
            executed = false;
        }
        return success;
    }

    @Override
    public boolean redo() {
        if (executed) {
            return false;
        }
        return execute();
    }

    @Override
    public String getDescription() {
        return String.format("제거: %s from (%d, %d)",
            removedEntityType != null ? removedEntityType.getDisplayName() : "없음",
            x, y);
    }

    // Getter methods for testing
    public int getX() { return x; }
    public int getY() { return y; }
    public EntityType getRemovedEntityType() { return removedEntityType; }
}