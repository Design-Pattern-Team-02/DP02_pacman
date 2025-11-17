package mapeditor.states;

import mapeditor.model.MapData;
import mapeditor.model.EntityType;
import mapeditor.controller.MapEditorManager;
import java.awt.Graphics2D;
import java.awt.Point;

/**
 * EditorStateContext - State Pattern Context + Mediator
 * 에디터의 현재 상태를 관리하고 상태 전환을 담당
 * State와 MapEditorManager 간의 통신을 중재
 *
 * 디자인 패턴 적용:
 * - State Pattern의 Context 역할
 * - Mediator Pattern: State와 Command 실행 간의 중재
 * - 클라이언트는 이 Context를 통해 상태와 상호작용
 * - 상태 전환 로직을 중앙화하여 관리
 */
public class EditorStateContext {
    private EditorState currentState;
    private MapData mapData;
    private MapEditorManager manager;

    // 미리 생성된 상태 객체들 (Flyweight 패턴 적용 가능)
    private final IdleState idleState;
    private final PlacementState placementState;
    private final EraseState eraseState;

    public EditorStateContext(MapData mapData) {
        this.mapData = mapData;
        // manager는 나중에 설정됨 (순환 의존성 방지)

        // 상태 객체 초기화
        this.idleState = new IdleState(this, mapData);
        this.placementState = new PlacementState(this, mapData);
        this.eraseState = new EraseState(this, mapData);

        // 초기 상태는 Idle
        this.currentState = idleState;
        this.currentState.enter();
    }

    /**
     * MapEditorManager 설정 (순환 의존성 방지를 위해 별도 메서드로 분리)
     */
    public void setManager(MapEditorManager manager) {
        this.manager = manager;
    }

    /**
     * 상태 전환
     * @param newState 새로운 상태
     */
    public void changeState(EditorState newState) {
        // 동일한 상태로의 전환은 무시 (재진입 방지)
        if (currentState == newState) {
            return;
        }

        if (currentState != null) {
            currentState.exit();
        }
        currentState = newState;
        if (currentState != null) {
            currentState.enter();
        }
    }

    /**
     * Idle 상태로 전환
     */
    public void setIdleState() {
        changeState(idleState);
    }

    /**
     * Placement 상태로 전환
     * @param entityType 배치할 엔티티 타입
     */
    public void setPlacementState(EntityType entityType) {
        if (entityType == null) {
            setIdleState();
            return;
        }
        placementState.setEntityType(entityType);
        changeState(placementState);
    }

    /**
     * Erase 상태로 전환
     */
    public void setEraseState() {
        changeState(eraseState);
    }

    // 이벤트 위임 메서드들
    public void handleMouseClick(int gridX, int gridY, int button) {
        currentState.handleMouseClick(gridX, gridY, button);
    }

    public void handleMouseMove(int gridX, int gridY) {
        currentState.handleMouseMove(gridX, gridY);
    }

    public void handleMouseExit() {
        currentState.handleMouseExit();
    }

    public void render(Graphics2D g, int cellWidth, int cellHeight, Point mousePosition) {
        currentState.render(g, cellWidth, cellHeight, mousePosition);
    }

    public String getCurrentStateName() {
        return currentState.getStateName();
    }

    public EntityType getSelectedEntityType() {
        return currentState.getSelectedEntityType();
    }

    public EditorState getCurrentState() {
        return currentState;
    }

    /**
     * Mediator Pattern - 엔티티 배치 요청을 중재
     * State가 요청하면 MapEditorManager의 Command를 실행
     * @param x x 좌표
     * @param y y 좌표
     * @param entityType 배치할 엔티티 타입
     * @return 배치 성공 여부
     */
    public boolean requestPlaceEntity(int x, int y, EntityType entityType) {
        // MapEditorManager를 통해 Command Pattern 실행
        return manager.placeEntity(x, y, entityType);
    }

    /**
     * Mediator Pattern - 엔티티 제거 요청을 중재
     * State가 요청하면 MapEditorManager의 Command를 실행
     * @param x x 좌표
     * @param y y 좌표
     * @return 제거 성공 여부
     */
    public boolean requestRemoveEntity(int x, int y) {
        // MapEditorManager를 통해 Command Pattern 실행
        return manager.removeEntity(x, y);
    }
}