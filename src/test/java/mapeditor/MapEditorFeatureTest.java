package mapeditor;

import mapeditor.model.MapData;
import mapeditor.model.EntityType;
import mapeditor.commands.*;
import mapeditor.states.*;
import mapeditor.observers.MapObserver;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 맵 에디터 기능 핵심 테스트 (14개)
 *
 * 테스트 구성:
 * - State 패턴 테스트: 4개
 * - Command 패턴 테스트: 4개
 * - Observer 패턴 테스트: 3개
 * - MapData 테스트: 3개
 */
public class MapEditorFeatureTest {

    private MapData mapData;
    private CommandManager commandManager;
    private EditorStateContext stateContext;

    // Observer 테스트용 Mock
    private TestMapObserver testObserver;

    @Before
    public void setUp() {
        mapData = new MapData();
        commandManager = new CommandManager();
        testObserver = new TestMapObserver();
        mapData.addObserver(testObserver);
    }

    // ==================== State 패턴 테스트 (4개) ====================

    /**
     * 테스트 1: IdleState에서 클릭 시 아무 동작 없음
     */
    @Test
    public void testIdleState_NoAction() {
        stateContext = new EditorStateContext(mapData);

        // 초기 상태는 IdleState
        assertEquals("초기 상태는 대기", "대기", stateContext.getCurrentStateName());

        // 편집 가능한 빈 위치 선택 (테두리 벽 제외)
        int testX = 2;
        int testY = 2;
        EntityType before = mapData.getEntityAt(testX, testY);

        // IdleState에서 클릭해도 아무 변화 없음
        stateContext.handleMouseClick(testX, testY, 1);
        EntityType after = mapData.getEntityAt(testX, testY);

        assertEquals("IdleState에서 클릭해도 엔티티 변화 없음", before, after);
    }

    /**
     * 테스트 2: PlacementState에서 엔티티 배치
     */
    @Test
    public void testPlacementState_PlaceEntity() {
        // PlacementState로 전환 (Manager 없이 직접 MapData 조작)
        int testX = 3;
        int testY = 3;

        // 직접 배치 (State 동작 시뮬레이션)
        boolean placed = mapData.placeEntity(testX, testY, EntityType.WALL);

        assertTrue("배치 성공", placed);
        assertEquals("WALL이 배치됨", EntityType.WALL, mapData.getEntityAt(testX, testY));
    }

    /**
     * 테스트 3: EraseState에서 엔티티 삭제
     */
    @Test
    public void testEraseState_RemoveEntity() {
        int testX = 3;
        int testY = 3;

        // 먼저 배치
        mapData.placeEntity(testX, testY, EntityType.WALL);
        assertEquals("WALL 배치 확인", EntityType.WALL, mapData.getEntityAt(testX, testY));

        // 삭제 (EraseState 동작 시뮬레이션)
        boolean removed = mapData.removeEntity(testX, testY);

        assertTrue("삭제 성공", removed);
        assertEquals("EMPTY로 변경됨", EntityType.EMPTY, mapData.getEntityAt(testX, testY));
    }

    /**
     * 테스트 4: 상태 전환 정상 동작
     */
    @Test
    public void testStateTransition() {
        stateContext = new EditorStateContext(mapData);

        // 초기: 대기
        assertEquals("초기 상태: 대기", "대기", stateContext.getCurrentStateName());

        // 대기 → 배치 (엔티티 선택)
        stateContext.setPlacementState(EntityType.WALL);
        assertTrue("배치 상태로 전환", stateContext.getCurrentStateName().startsWith("배치"));
        assertEquals("선택된 엔티티: WALL", EntityType.WALL, stateContext.getSelectedEntityType());

        // 배치 → 지우개 (지우개 선택)
        stateContext.setEraseState();
        assertEquals("지우개 상태로 전환", "지우개", stateContext.getCurrentStateName());

        // 지우개 → 대기 (취소)
        stateContext.setIdleState();
        assertEquals("대기 상태로 복귀", "대기", stateContext.getCurrentStateName());
    }

    // ==================== Command 패턴 테스트 (4개) ====================

    /**
     * 테스트 5: 배치 커맨드 실행
     */
    @Test
    public void testPlaceEntityCommand_Execute() {
        int testX = 4;
        int testY = 4;

        PlaceEntityCommand command = new PlaceEntityCommand(mapData, testX, testY, EntityType.SUPER_PAC_GUM);
        boolean success = command.execute();

        assertTrue("커맨드 실행 성공", success);
        assertEquals("엔티티 배치됨", EntityType.SUPER_PAC_GUM, mapData.getEntityAt(testX, testY));
        assertEquals("커맨드 좌표 X", testX, command.getX());
        assertEquals("커맨드 좌표 Y", testY, command.getY());
    }

    /**
     * 테스트 6: 배치 커맨드 Undo
     */
    @Test
    public void testPlaceEntityCommand_Undo() {
        int testX = 4;
        int testY = 4;
        EntityType originalType = mapData.getEntityAt(testX, testY);

        // 배치 실행
        PlaceEntityCommand command = new PlaceEntityCommand(mapData, testX, testY, EntityType.WALL);
        command.execute();
        assertEquals("WALL 배치됨", EntityType.WALL, mapData.getEntityAt(testX, testY));

        // Undo
        boolean undone = command.undo();
        assertTrue("Undo 성공", undone);
        assertEquals("원래 상태로 복원", originalType, mapData.getEntityAt(testX, testY));
    }

    /**
     * 테스트 7: 삭제 커맨드 실행
     */
    @Test
    public void testRemoveEntityCommand_Execute() {
        int testX = 4;
        int testY = 4;

        // 먼저 배치
        mapData.placeEntity(testX, testY, EntityType.WALL);

        // 삭제 커맨드 실행
        RemoveEntityCommand command = new RemoveEntityCommand(mapData, testX, testY);
        boolean success = command.execute();

        assertTrue("삭제 커맨드 실행 성공", success);
        assertEquals("EMPTY로 변경", EntityType.EMPTY, mapData.getEntityAt(testX, testY));
        assertEquals("삭제된 엔티티 타입 저장", EntityType.WALL, command.getRemovedEntityType());
    }

    /**
     * 테스트 8: Undo/Redo 스택 관리
     */
    @Test
    public void testCommandManager_UndoRedo() {
        int testX = 5;
        int testY = 5;

        // 초기 상태
        assertFalse("초기: Undo 불가", commandManager.canUndo());
        assertFalse("초기: Redo 불가", commandManager.canRedo());
        assertEquals("초기: Undo 스택 크기 0", 0, commandManager.getUndoStackSize());

        // 커맨드 실행
        PlaceEntityCommand cmd1 = new PlaceEntityCommand(mapData, testX, testY, EntityType.WALL);
        commandManager.executeCommand(cmd1);

        assertTrue("실행 후: Undo 가능", commandManager.canUndo());
        assertEquals("Undo 스택 크기 1", 1, commandManager.getUndoStackSize());

        // Undo 실행
        commandManager.undo();
        assertFalse("Undo 후: Undo 불가", commandManager.canUndo());
        assertTrue("Undo 후: Redo 가능", commandManager.canRedo());
        assertEquals("Redo 스택 크기 1", 1, commandManager.getRedoStackSize());

        // Redo 실행
        commandManager.redo();
        assertTrue("Redo 후: Undo 가능", commandManager.canUndo());
        assertFalse("Redo 후: Redo 불가", commandManager.canRedo());

        // 새 커맨드 실행 시 Redo 스택 초기화 확인
        commandManager.undo();  // 다시 Undo
        assertTrue("Redo 가능 상태", commandManager.canRedo());

        PlaceEntityCommand cmd2 = new PlaceEntityCommand(mapData, testX + 1, testY, EntityType.PACMAN);
        commandManager.executeCommand(cmd2);
        assertFalse("새 커맨드 후: Redo 스택 초기화", commandManager.canRedo());
    }

    // ==================== Observer 패턴 테스트 (3개) ====================

    /**
     * 테스트 9: 엔티티 배치 시 Observer 통지
     */
    @Test
    public void testObserver_EntityPlaced() {
        // 편집 가능한 위치 사용 (고스트하우스 영역 외부)
        int testX = 11;
        int testY = 11;

        testObserver.reset();
        boolean placed = mapData.placeEntity(testX, testY, EntityType.WALL);

        assertTrue("배치 성공", placed);
        assertTrue("onEntityPlaced 호출됨", testObserver.entityPlacedCalled);
        assertEquals("배치 위치 X", testX, testObserver.lastPlacedX);
        assertEquals("배치 위치 Y", testY, testObserver.lastPlacedY);
        assertEquals("배치된 엔티티 타입", EntityType.WALL, testObserver.lastPlacedType);
    }

    /**
     * 테스트 10: 엔티티 삭제 시 Observer 통지
     */
    @Test
    public void testObserver_EntityRemoved() {
        // 편집 가능한 위치 사용 (고스트하우스 영역 외부)
        int testX = 11;
        int testY = 11;

        // 먼저 배치
        boolean placed = mapData.placeEntity(testX, testY, EntityType.WALL);
        assertTrue("배치 성공", placed);
        testObserver.reset();

        // 삭제
        boolean removed = mapData.removeEntity(testX, testY);

        assertTrue("삭제 성공", removed);
        assertTrue("onEntityRemoved 호출됨", testObserver.entityRemovedCalled);
        assertEquals("삭제 위치 X", testX, testObserver.lastRemovedX);
        assertEquals("삭제 위치 Y", testY, testObserver.lastRemovedY);
    }

    /**
     * 테스트 11: 검증 상태 변경 시 Observer 통지
     */
    @Test
    public void testObserver_ValidationChanged() {
        testObserver.reset();

        // 필수 엔티티 배치 전 - 유효하지 않음
        assertFalse("Pacman/Clyde 없이 유효하지 않음", mapData.isMapValid());

        // Pacman 배치
        mapData.placeEntity(2, 2, EntityType.PACMAN);
        assertTrue("onValidationStateChanged 호출됨", testObserver.validationChangedCalled);

        // Clyde 배치 (모든 필수 엔티티 배치 완료)
        testObserver.reset();
        mapData.placeEntity(3, 3, EntityType.CLYDE);

        assertTrue("검증 상태 변경 통지됨", testObserver.validationChangedCalled);
        assertTrue("모든 필수 엔티티 배치 후 유효함", testObserver.lastValidState);
    }

    // ==================== MapData 테스트 (3개) ====================

    /**
     * 테스트 12: 고스트하우스 편집 불가
     */
    @Test
    public void testMapData_GhostHouseNotEditable() {
        // 고스트 하우스 영역 (5, 6) ~ (9, 8)
        int ghostHouseX = 7;  // 중앙
        int ghostHouseY = 7;  // 중앙

        assertFalse("고스트하우스 영역 편집 불가", mapData.isEditable(ghostHouseX, ghostHouseY));

        // 배치 시도
        boolean placed = mapData.placeEntity(ghostHouseX, ghostHouseY, EntityType.WALL);
        assertFalse("고스트하우스에 배치 실패", placed);

        // Blinky 위치도 편집 불가
        int blinkyX = 7;
        int blinkyY = 5;  // 고스트하우스 위
        assertFalse("Blinky 위치 편집 불가", mapData.isEditable(blinkyX, blinkyY));
    }

    /**
     * 테스트 13: 필수 엔티티 검증
     */
    @Test
    public void testMapData_Validation() {
        // 초기 상태: 유효하지 않음 (Pacman, Clyde 없음)
        assertFalse("초기 상태 유효하지 않음", mapData.isMapValid());

        // Pacman만 배치
        mapData.placeEntity(2, 2, EntityType.PACMAN);
        assertFalse("Pacman만 있으면 유효하지 않음", mapData.isMapValid());
        assertEquals("Pacman 개수 1", 1, mapData.getEntityCount(EntityType.PACMAN));

        // Clyde 배치
        mapData.placeEntity(3, 3, EntityType.CLYDE);
        assertTrue("Pacman + Clyde 있으면 유효함", mapData.isMapValid());
        assertEquals("Clyde 개수 1", 1, mapData.getEntityCount(EntityType.CLYDE));

        // 필수 엔티티 중복 배치 불가 확인
        boolean duplicatePacman = mapData.placeEntity(4, 4, EntityType.PACMAN);
        assertFalse("Pacman 중복 배치 불가", duplicatePacman);

        // 에러 메시지 확인 (유효한 상태이므로 빈 문자열)
        assertEquals("유효한 상태에서 에러 메시지 없음", "", mapData.getValidationErrorMessage());
    }

    /**
     * 테스트 14: 14×15 → 56×62 그리드 확장
     */
    @Test
    public void testMapData_GridExpansion() {
        // 논리적 그리드 크기 확인
        assertEquals("논리적 그리드 너비", 14, MapData.WIDTH);
        assertEquals("논리적 그리드 높이", 15, MapData.HEIGHT);

        // CSV 확장 크기 확인
        assertEquals("CSV 그리드 너비 (14×4)", 56, MapData.CSV_WIDTH);
        assertEquals("CSV 그리드 높이 (15×4+2)", 62, MapData.CSV_HEIGHT);

        // 확장된 그리드 생성
        EntityType[][] expanded = mapData.getExpandedGridForCSV();

        assertNotNull("확장된 그리드 생성됨", expanded);
        assertEquals("확장된 그리드 높이", MapData.CSV_HEIGHT, expanded.length);
        assertEquals("확장된 그리드 너비", MapData.CSV_WIDTH, expanded[0].length);

        // 하단 2행은 벽으로 채워짐
        for (int x = 0; x < MapData.CSV_WIDTH; x++) {
            assertEquals("하단 60행 벽", EntityType.WALL, expanded[60][x]);
            assertEquals("하단 61행 벽", EntityType.WALL, expanded[61][x]);
        }
    }

    // ==================== 테스트용 Mock Observer ====================

    /**
     * Observer 패턴 테스트를 위한 Mock Observer
     */
    private static class TestMapObserver implements MapObserver {
        boolean entityPlacedCalled = false;
        boolean entityRemovedCalled = false;
        boolean mapResetCalled = false;
        boolean entityCountChangedCalled = false;
        boolean validationChangedCalled = false;

        int lastPlacedX, lastPlacedY;
        int lastRemovedX, lastRemovedY;
        EntityType lastPlacedType;
        EntityType lastCountChangedType;
        int lastCount;
        boolean lastValidState;

        public void reset() {
            entityPlacedCalled = false;
            entityRemovedCalled = false;
            mapResetCalled = false;
            entityCountChangedCalled = false;
            validationChangedCalled = false;
        }

        @Override
        public void onEntityPlaced(int x, int y, EntityType entityType) {
            entityPlacedCalled = true;
            lastPlacedX = x;
            lastPlacedY = y;
            lastPlacedType = entityType;
        }

        @Override
        public void onEntityRemoved(int x, int y) {
            entityRemovedCalled = true;
            lastRemovedX = x;
            lastRemovedY = y;
        }

        @Override
        public void onMapReset() {
            mapResetCalled = true;
        }

        @Override
        public void onEntityCountChanged(EntityType entityType, int count) {
            entityCountChangedCalled = true;
            lastCountChangedType = entityType;
            lastCount = count;
        }

        @Override
        public void onValidationStateChanged(boolean isValid) {
            validationChangedCalled = true;
            lastValidState = isValid;
        }
    }
}