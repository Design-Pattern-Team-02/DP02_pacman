package mapeditor.controller;

import mapeditor.model.MapData;
import mapeditor.model.EntityType;
import mapeditor.states.EditorStateContext;
import mapeditor.commands.CommandManager;
import mapeditor.commands.PlaceEntityCommand;
import mapeditor.commands.RemoveEntityCommand;
import mapeditor.observers.MapObserver;

/**
 * MapEditorManager - Singleton Pattern
 * 맵 에디터의 중앙 관리자로 모든 컴포넌트를 조율
 *
 * 디자인 패턴 적용:
 * - Singleton Pattern: 전체 애플리케이션에서 단일 인스턴스만 존재
 * - Facade Pattern 요소: 복잡한 서브시스템에 대한 단순한 인터페이스 제공
 * - 모든 패턴들을 통합하는 중앙 제어점
 */
public class MapEditorManager {
    private static MapEditorManager instance;

    // 핵심 컴포넌트들
    private MapData mapData;
    private EditorStateContext stateContext;
    private CommandManager commandManager;

    // 에디터 설정
    private boolean autoSaveEnabled;
    private String lastSavedFilePath;

    /**
     * Private constructor - Singleton Pattern 핵심
     */
    private MapEditorManager() {
        initializeComponents();
    }

    /**
     * Singleton 인스턴스 반환
     * Thread-safe lazy initialization
     */
    public static synchronized MapEditorManager getInstance() {
        if (instance == null) {
            instance = new MapEditorManager();
        }
        return instance;
    }

    /**
     * 컴포넌트 초기화
     */
    private void initializeComponents() {
        this.mapData = new MapData();
        this.stateContext = new EditorStateContext(mapData);
        this.commandManager = new CommandManager();
        this.autoSaveEnabled = false;
        this.lastSavedFilePath = null;
    }

    /**
     * 새 맵 생성
     */
    public void createNewMap() {
        mapData.resetGrid();
        commandManager.clearHistory();
        lastSavedFilePath = null;
        stateContext.setIdleState();
    }

    /**
     * 엔티티 배치 (Command Pattern 통합)
     * @param x x 좌표
     * @param y y 좌표
     * @param entityType 배치할 엔티티 타입
     * @return 배치 성공 여부
     */
    public boolean placeEntity(int x, int y, EntityType entityType) {
        PlaceEntityCommand command = new PlaceEntityCommand(mapData, x, y, entityType);
        return commandManager.executeCommand(command);
    }

    /**
     * 엔티티 제거 (Command Pattern 통합)
     * @param x x 좌표
     * @param y y 좌표
     * @return 제거 성공 여부
     */
    public boolean removeEntity(int x, int y) {
        RemoveEntityCommand command = new RemoveEntityCommand(mapData, x, y);
        return commandManager.executeCommand(command);
    }

    /**
     * 실행 취소
     */
    public boolean undo() {
        return commandManager.undo();
    }

    /**
     * 재실행
     */
    public boolean redo() {
        return commandManager.redo();
    }

    /**
     * 현재 에디터 상태 변경
     * @param entityType 선택된 엔티티 (null이면 Idle 상태)
     */
    public void selectEntity(EntityType entityType) {
        if (entityType == null) {
            stateContext.setIdleState();
        } else {
            stateContext.setPlacementState(entityType);
        }
    }

    /**
     * 지우개 모드로 전환
     */
    public void setEraseMode() {
        stateContext.setEraseState();
    }

    /**
     * 선택 취소 (Idle 상태로)
     */
    public void cancelSelection() {
        stateContext.setIdleState();
    }

    /**
     * 맵 검증
     */
    public boolean validateMap() {
        return mapData.isMapValid();
    }

    /**
     * 검증 오류 메시지 반환
     */
    public String getValidationErrorMessage() {
        return mapData.getValidationErrorMessage();
    }

    /**
     * 빈 공간을 PacGum으로 채우기
     */
    public void fillEmptySpacesWithPacGum() {
        mapData.fillEmptyWithPacGum();
    }

    /**
     * 맵 초기화
     */
    public void resetMap() {
        createNewMap();
    }

    /**
     * 옵저버 등록
     */
    public void addObserver(MapObserver observer) {
        mapData.addObserver(observer);
    }

    /**
     * 옵저버 제거
     */
    public void removeObserver(MapObserver observer) {
        mapData.removeObserver(observer);
    }

    /**
     * 현재 선택된 엔티티 타입 반환
     */
    public EntityType getSelectedEntityType() {
        return stateContext.getSelectedEntityType();
    }

    /**
     * 현재 상태 이름 반환
     */
    public String getCurrentStateName() {
        return stateContext.getCurrentStateName();
    }

    /**
     * Undo 가능 여부
     */
    public boolean canUndo() {
        return commandManager.canUndo();
    }

    /**
     * Redo 가능 여부
     */
    public boolean canRedo() {
        return commandManager.canRedo();
    }

    /**
     * 특정 엔티티의 현재 개수 반환
     */
    public int getEntityCount(EntityType entityType) {
        return mapData.getEntityCount(entityType);
    }

    /**
     * 특정 위치의 엔티티 타입 반환
     */
    public EntityType getEntityAt(int x, int y) {
        return mapData.getEntityAt(x, y);
    }

    /**
     * 맵 데이터 복사본 반환
     */
    public EntityType[][] getMapDataCopy() {
        return mapData.getGridCopy();
    }

    /**
     * 자동 저장 설정
     */
    public void setAutoSaveEnabled(boolean enabled) {
        this.autoSaveEnabled = enabled;
    }

    /**
     * 자동 저장 활성화 여부
     */
    public boolean isAutoSaveEnabled() {
        return autoSaveEnabled;
    }

    /**
     * 마지막 저장 경로 설정
     */
    public void setLastSavedFilePath(String path) {
        this.lastSavedFilePath = path;
    }

    /**
     * 마지막 저장 경로 반환
     */
    public String getLastSavedFilePath() {
        return lastSavedFilePath;
    }

    // Getter methods for direct component access (if needed)
    public MapData getMapData() {
        return mapData;
    }

    public EditorStateContext getStateContext() {
        return stateContext;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * 에디터 상태 정보 문자열 반환 (디버깅용)
     */
    public String getEditorInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Map Editor Status ===\n");
        sb.append("현재 상태: ").append(getCurrentStateName()).append("\n");
        sb.append("맵 유효성: ").append(validateMap() ? "유효" : "무효").append("\n");
        sb.append("Undo 가능: ").append(canUndo()).append("\n");
        sb.append("Redo 가능: ").append(canRedo()).append("\n");
        sb.append("\n필수 엔티티 현황:\n");

        for (EntityType type : EntityType.values()) {
            if (type.isRequired()) {
                int count = getEntityCount(type);
                int required = type.getMaxCount();
                sb.append(String.format("  %s: %d/%d %s\n",
                    type.getDisplayName(), count, required,
                    count == required ? "✓" : "✗"));
            }
        }

        return sb.toString();
    }
}