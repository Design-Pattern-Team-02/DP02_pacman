package mapeditor.commands;

import mapeditor.model.MapData;
import mapeditor.model.EntityType;

/**
 * PlaceEntityCommand - 엔티티 배치 커맨드
 * 맵에 엔티티를 배치하는 액션을 캡슐화
 *
 * Command Pattern 구현체:
 * - 엔티티 배치를 실행하고 되돌릴 수 있음
 * - 이전 상태를 저장하여 완벽한 Undo 지원
 */
public class PlaceEntityCommand implements Command {
    private MapData mapData;
    private int x, y;
    private EntityType newEntityType;
    private EntityType previousEntityType;
    private boolean executed;

    public PlaceEntityCommand(MapData mapData, int x, int y, EntityType entityType) {
        this.mapData = mapData;
        this.x = x;
        this.y = y;
        this.newEntityType = entityType;
        this.executed = false;
    }

    @Override
    public boolean execute() {
        // 이전 상태 저장
        previousEntityType = mapData.getEntityAt(x, y);

        // 배치 실행
        boolean success = mapData.placeEntity(x, y, newEntityType);
        if (success) {
            executed = true;
        }
        return success;
    }

    @Override
    public boolean undo() {
        if (!executed) {
            return false;
        }

        // 이전 상태로 복원
        boolean success;
        if (previousEntityType == EntityType.EMPTY) {
            success = mapData.removeEntity(x, y);
        } else {
            success = mapData.placeEntity(x, y, previousEntityType);
        }

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
        return String.format("배치: %s at (%d, %d)",
            newEntityType.getDisplayName(), x, y);
    }

    @Override
    public boolean canMergeWith(Command other) {
        // 같은 위치에 연속적으로 배치하는 경우 병합 가능
        if (other instanceof PlaceEntityCommand) {
            PlaceEntityCommand otherPlace = (PlaceEntityCommand) other;
            return this.x == otherPlace.x && this.y == otherPlace.y;
        }
        return false;
    }

    @Override
    public Command mergeWith(Command other) {
        if (canMergeWith(other)) {
            PlaceEntityCommand otherPlace = (PlaceEntityCommand) other;
            // 새로운 커맨드 생성 (최초 previousEntityType 유지)
            PlaceEntityCommand merged = new PlaceEntityCommand(
                mapData, x, y, otherPlace.newEntityType
            );
            merged.previousEntityType = this.previousEntityType;
            return merged;
        }
        return null;
    }

    // Getter methods for testing
    public int getX() { return x; }
    public int getY() { return y; }
    public EntityType getNewEntityType() { return newEntityType; }
    public EntityType getPreviousEntityType() { return previousEntityType; }
}