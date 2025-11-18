package mapeditor.states;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Color;
import mapeditor.model.EntityType;
import mapeditor.model.MapData;

/**
 * IdleState - 기본 대기 상태
 * 아무런 엔티티도 선택되지 않은 기본 상태
 *
 * State Pattern 구현체:
 * - 마우스 클릭/이동에 대한 기본 동작만 수행
 * - 엔티티 배치나 삭제 동작 없음
 */
public class IdleState implements EditorState {
    private EditorStateContext context;
    private MapData mapData;
    private Point currentGridPosition;

    public IdleState(EditorStateContext context, MapData mapData) {
        this.context = context;
        this.mapData = mapData;
    }

    @Override
    public void handleMouseClick(int gridX, int gridY, int button) {
        // Idle 상태에서는 클릭해도 아무 동작 없음
        // 필요시 우클릭으로 컨텍스트 메뉴 표시 등 구현 가능
    }

    @Override
    public void handleMouseMove(int gridX, int gridY) {
        currentGridPosition = new Point(gridX, gridY);
    }

    @Override
    public void handleMouseDrag(int gridX, int gridY) {
        // Idle 상태에서는 드래그해도 아무 동작 없음
    }

    @Override
    public void handleMouseExit() {
        currentGridPosition = null;
    }

    @Override
    public void render(Graphics2D g, int cellWidth, int cellHeight, Point mousePosition) {
        // Idle 상태에서는 현재 마우스 위치만 하이라이트
        if (currentGridPosition != null) {
            g.setColor(new Color(200, 200, 200, 50));
            g.fillRect(
                currentGridPosition.x * cellWidth,
                currentGridPosition.y * cellHeight,
                cellWidth,
                cellHeight
            );
        }
    }

    @Override
    public String getStateName() {
        return "대기";
    }

    @Override
    public void enter() {
        currentGridPosition = null;
    }

    @Override
    public void exit() {
        currentGridPosition = null;
    }

    @Override
    public EntityType getSelectedEntityType() {
        return null;
    }
}