package mapeditor.model;

import mapeditor.observers.MapObserver;
import java.util.*;

/**
 * MapData - 맵 데이터 모델 (Subject in Observer Pattern)
 * 56x63 그리드의 맵 데이터를 관리하고 옵저버들에게 변경사항을 통지
 *
 * 디자인 패턴 적용:
 * - Observer Pattern의 Subject 역할
 * - 데이터 변경 시 모든 등록된 Observer에게 자동 통지
 * - Single Responsibility: 맵 데이터 관리만 담당
 */
public class MapData {
    // 논리적 그리드 크기 (맵 에디터에서 사용)
    public static final int WIDTH = 14;
    public static final int HEIGHT = 15;

    // 실제 CSV 크기 (4x4 확장 + 하단 2행 추가)
    public static final int CSV_WIDTH = 56;  // WIDTH * 4
    public static final int CSV_HEIGHT = 62; // HEIGHT * 4 + 2 (하단 벽 2행 추가)

    // 고스트 집 위치와 크기 (중앙에 배치)
    private static final int GHOST_HOUSE_X = 5;  // 중앙 X 좌표 (14칸 기준)
    private static final int GHOST_HOUSE_Y = 6;  // 중앙 Y 좌표 (15칸 기준)
    private static final int GHOST_HOUSE_WIDTH = 5;
    private static final int GHOST_HOUSE_HEIGHT = 3;

    private EntityType[][] grid;
    private Map<EntityType, Integer> entityCounts;
    private List<MapObserver> observers;
    private boolean[][] editableGrid;  // 편집 가능 영역 표시

    public MapData() {
        this.grid = new EntityType[HEIGHT][WIDTH];
        this.entityCounts = new HashMap<>();
        this.observers = new ArrayList<>();
        this.editableGrid = new boolean[HEIGHT][WIDTH];

        // 그리드를 초기화 (테두리 벽과 고스트 집 포함)
        resetGrid();
    }

    /**
     * 그리드를 초기화 (테두리 벽과 고스트 집 포함)
     */
    public void resetGrid() {
        // 1. 전체를 빈 공간으로 초기화
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                grid[y][x] = EntityType.EMPTY;
                editableGrid[y][x] = true;  // 기본적으로 편집 가능
            }
        }

        // 2. 테두리 벽 추가 (편집 가능)
        for (int x = 0; x < WIDTH; x++) {
            grid[0][x] = EntityType.WALL;  // 상단
            grid[HEIGHT - 1][x] = EntityType.WALL;  // 하단
        }
        for (int y = 0; y < HEIGHT; y++) {
            grid[y][0] = EntityType.WALL;  // 좌측
            grid[y][WIDTH - 1] = EntityType.WALL;  // 우측
        }

        // 3. 고스트 집 구조 추가 (편집 불가능)
        createGhostHouse();

        // 4. 엔티티 개수 재계산
        recountEntities();

        // 모든 옵저버에게 리셋 통지
        notifyMapReset();
    }

    /**
     * 고스트 집 구조 생성 및 고스트 자동 배치
     */
    private void createGhostHouse() {
        int startX = GHOST_HOUSE_X;
        int startY = GHOST_HOUSE_Y;

        // 고스트 집 구조:
        // x x - x x
        // x . . . x
        // x x x x x

        // 첫 번째 줄: 벽 벽 GhostHouse벽 벽 벽
        grid[startY][startX] = EntityType.WALL;
        grid[startY][startX + 1] = EntityType.WALL;
        grid[startY][startX + 2] = EntityType.GHOST_HOUSE_WALL;  // 유령 전용 입구
        grid[startY][startX + 3] = EntityType.WALL;
        grid[startY][startX + 4] = EntityType.WALL;

        // 두 번째 줄: 벽 Pinky Inky 빈공간 벽
        grid[startY + 1][startX] = EntityType.WALL;
        grid[startY + 1][startX + 1] = EntityType.PINKY;   // 고스트하우스 안
        grid[startY + 1][startX + 2] = EntityType.INKY;    // 고스트하우스 안
        grid[startY + 1][startX + 3] = EntityType.EMPTY;
        grid[startY + 1][startX + 4] = EntityType.WALL;

        // 세 번째 줄: 벽 벽 벽 벽 벽
        grid[startY + 2][startX] = EntityType.WALL;
        grid[startY + 2][startX + 1] = EntityType.WALL;
        grid[startY + 2][startX + 2] = EntityType.WALL;
        grid[startY + 2][startX + 3] = EntityType.WALL;
        grid[startY + 2][startX + 4] = EntityType.WALL;

        // Blinky: 고스트하우스 문 바로 위 (outsideHouse)
        grid[startY - 1][startX + 2] = EntityType.BLINKY;

        // 고스트 집 영역 편집 불가능 설정
        for (int y = 0; y < GHOST_HOUSE_HEIGHT; y++) {
            for (int x = 0; x < GHOST_HOUSE_WIDTH; x++) {
                editableGrid[startY + y][startX + x] = false;
            }
        }
        // Blinky 위치도 편집 불가
        editableGrid[startY - 1][startX + 2] = false;
    }

    /**
     * 엔티티 개수 재계산
     */
    private void recountEntities() {
        entityCounts.clear();
        for (EntityType type : EntityType.values()) {
            entityCounts.put(type, 0);
        }

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                EntityType type = grid[y][x];
                incrementEntityCount(type);
            }
        }
    }

    /**
     * 특정 위치에 엔티티 배치
     * @param x x 좌표
     * @param y y 좌표
     * @param entityType 배치할 엔티티 타입
     * @return 배치 성공 여부
     */
    public boolean placeEntity(int x, int y, EntityType entityType) {
        if (!isValidPosition(x, y)) {
            return false;
        }

        // 편집 불가능한 영역 체크 (고스트 집)
        if (!editableGrid[y][x]) {
            return false;  // 고스트 집 영역은 수정 불가
        }

        // 필수 엔티티의 개수 제한 확인
        if (entityType.isRequired() && entityType.getMaxCount() > 0) {
            int currentCount = entityCounts.getOrDefault(entityType, 0);
            if (currentCount >= entityType.getMaxCount()) {
                return false; // 이미 최대 개수가 배치됨
            }
        }

        // 기존 엔티티 제거
        EntityType previousType = grid[y][x];
        if (previousType != EntityType.EMPTY) {
            decrementEntityCount(previousType);
        }

        // 새 엔티티 배치
        grid[y][x] = entityType;
        incrementEntityCount(entityType);

        // 옵저버들에게 통지
        notifyEntityPlaced(x, y, entityType);
        notifyEntityCountChanged(entityType);
        if (previousType != EntityType.EMPTY && previousType != entityType) {
            notifyEntityCountChanged(previousType);
        }

        // 검증 상태 확인
        notifyValidationStateChanged(isMapValid());

        return true;
    }

    /**
     * 특정 위치의 엔티티 제거
     * @param x x 좌표
     * @param y y 좌표
     * @return 제거 성공 여부
     */
    public boolean removeEntity(int x, int y) {
        if (!isValidPosition(x, y)) {
            return false;
        }

        // 편집 불가능한 영역 체크 (고스트 집)
        if (!editableGrid[y][x]) {
            return false;  // 고스트 집 영역은 수정 불가
        }

        EntityType previousType = grid[y][x];
        if (previousType == EntityType.EMPTY) {
            return false; // 이미 빈 공간
        }

        grid[y][x] = EntityType.EMPTY;
        decrementEntityCount(previousType);
        incrementEntityCount(EntityType.EMPTY);

        // 옵저버들에게 통지
        notifyEntityRemoved(x, y);
        notifyEntityCountChanged(previousType);
        notifyEntityCountChanged(EntityType.EMPTY);

        // 검증 상태 확인
        notifyValidationStateChanged(isMapValid());

        return true;
    }

    /**
     * 특정 위치의 엔티티 타입 반환
     */
    public EntityType getEntityAt(int x, int y) {
        if (!isValidPosition(x, y)) {
            return null;
        }
        return grid[y][x];
    }

    /**
     * 특정 위치가 편집 가능한지 확인
     */
    public boolean isEditable(int x, int y) {
        if (!isValidPosition(x, y)) {
            return false;
        }
        return editableGrid[y][x];
    }

    /**
     * 고스트 집 영역인지 확인
     */
    public boolean isGhostHouseArea(int x, int y) {
        return x >= GHOST_HOUSE_X && x < GHOST_HOUSE_X + GHOST_HOUSE_WIDTH &&
               y >= GHOST_HOUSE_Y && y < GHOST_HOUSE_Y + GHOST_HOUSE_HEIGHT;
    }

    /**
     * 특정 엔티티 타입의 현재 개수 반환
     */
    public int getEntityCount(EntityType entityType) {
        return entityCounts.getOrDefault(entityType, 0);
    }

    /**
     * 맵이 유효한지 검증 (모든 필수 엔티티가 배치되었는지)
     */
    public boolean isMapValid() {
        for (EntityType type : EntityType.values()) {
            if (type.isRequired() && type.getMaxCount() > 0) {
                if (getEntityCount(type) != type.getMaxCount()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 검증 실패 메시지 생성
     */
    public String getValidationErrorMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("다음 필수 엔티티가 부족합니다:\n");

        boolean hasError = false;
        for (EntityType type : EntityType.values()) {
            if (type.isRequired() && type.getMaxCount() > 0) {
                int count = getEntityCount(type);
                if (count != type.getMaxCount()) {
                    sb.append(String.format("- %s: %d/%d\n",
                        type.getDisplayName(), count, type.getMaxCount()));
                    hasError = true;
                }
            }
        }

        return hasError ? sb.toString() : "";
    }

    /**
     * 2차원 배열 복사본 반환 (데이터 캡슐화)
     */
    public EntityType[][] getGridCopy() {
        EntityType[][] copy = new EntityType[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++) {
            System.arraycopy(grid[y], 0, copy[y], 0, WIDTH);
        }
        return copy;
    }

    /**
     * 논리적 그리드를 실제 CSV 크기로 확장 (4x4 확장 + 오프셋)
     * 14×15 → 56×62
     * 오프셋 적용: X -2, Y +2 (고스트하우스 문이 CSV (26, 26)에 위치하도록)
     */
    public EntityType[][] getExpandedGridForCSV() {
        EntityType[][] expanded = new EntityType[CSV_HEIGHT][CSV_WIDTH];

        // 오프셋: 고스트하우스를 default_map.csv와 동일한 위치에 배치
        // Blinky 위치: 논리적 (7, 5) → CSV (7*4-2, 5*4+1) = (26, 21)
        final int OFFSET_X = -2;
        final int OFFSET_Y = 1;

        // 먼저 전체를 EMPTY로 초기화
        for (int y = 0; y < CSV_HEIGHT; y++) {
            for (int x = 0; x < CSV_WIDTH; x++) {
                expanded[y][x] = EntityType.EMPTY;
            }
        }

        // 논리적 그리드의 각 칸을 4×4로 확장 (오프셋 적용)
        for (int logicalY = 0; logicalY < HEIGHT; logicalY++) {
            for (int logicalX = 0; logicalX < WIDTH; logicalX++) {
                EntityType entity = grid[logicalY][logicalX];

                // CSV 좌표 계산 (4배 확장 + 오프셋)
                int csvX = logicalX * 4 + OFFSET_X;
                int csvY = logicalY * 4 + OFFSET_Y;

                // 벽의 경우 4×4 전체를 채움
                if (entity == EntityType.WALL || entity == EntityType.GHOST_HOUSE_WALL) {
                    for (int dy = 0; dy < 4; dy++) {
                        for (int dx = 0; dx < 4; dx++) {
                            int finalX = csvX + dx;
                            int finalY = csvY + dy;
                            if (finalX >= 0 && finalX < CSV_WIDTH && finalY >= 0 && finalY < CSV_HEIGHT) {
                                expanded[finalY][finalX] = entity;
                            }
                        }
                    }
                }
                // 팩검/슈퍼팩검은 4×4 블록의 (1,1) 위치에 배치
                else if (entity == EntityType.PAC_GUM || entity == EntityType.SUPER_PAC_GUM) {
                    int finalX = csvX + 1;
                    int finalY = csvY + 1;
                    if (finalX >= 0 && finalX < CSV_WIDTH && finalY >= 0 && finalY < CSV_HEIGHT) {
                        expanded[finalY][finalX] = entity;
                    }
                }
                // 다른 엔티티(팩맨, 유령)는 좌측 상단에만 배치
                else if (entity != EntityType.EMPTY) {
                    if (csvX >= 0 && csvX < CSV_WIDTH && csvY >= 0 && csvY < CSV_HEIGHT) {
                        expanded[csvY][csvX] = entity;
                    }
                }
            }
        }

        // 하단 2행(60-61)을 벽으로 채움
        for (int y = 60; y < 62; y++) {
            for (int x = 0; x < CSV_WIDTH; x++) {
                expanded[y][x] = EntityType.WALL;
            }
        }

        return expanded;
    }

    /**
     * 모든 빈 공간을 PacGum으로 채우기 (고스트 하우스 내부 제외)
     */
    public void fillEmptyWithPacGum() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                // 빈 공간이고, 고스트 하우스 영역이 아닌 경우만 팩검 배치
                if (grid[y][x] == EntityType.EMPTY && !isGhostHouseArea(x, y)) {
                    grid[y][x] = EntityType.PAC_GUM;
                    incrementEntityCount(EntityType.PAC_GUM);
                    decrementEntityCount(EntityType.EMPTY);
                    notifyEntityPlaced(x, y, EntityType.PAC_GUM);
                }
            }
        }
        notifyEntityCountChanged(EntityType.PAC_GUM);
        notifyEntityCountChanged(EntityType.EMPTY);
    }

    // ========== Observer Pattern 관련 메서드 ==========

    /**
     * 옵저버 등록
     */
    public void addObserver(MapObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * 옵저버 제거
     */
    public void removeObserver(MapObserver observer) {
        observers.remove(observer);
    }

    // 통지 메서드들
    private void notifyEntityPlaced(int x, int y, EntityType entityType) {
        for (MapObserver observer : observers) {
            observer.onEntityPlaced(x, y, entityType);
        }
    }

    private void notifyEntityRemoved(int x, int y) {
        for (MapObserver observer : observers) {
            observer.onEntityRemoved(x, y);
        }
    }

    private void notifyMapReset() {
        for (MapObserver observer : observers) {
            observer.onMapReset();
        }
    }

    private void notifyEntityCountChanged(EntityType entityType) {
        int count = getEntityCount(entityType);
        for (MapObserver observer : observers) {
            observer.onEntityCountChanged(entityType, count);
        }
    }

    private void notifyValidationStateChanged(boolean isValid) {
        for (MapObserver observer : observers) {
            observer.onValidationStateChanged(isValid);
        }
    }

    // ========== 유틸리티 메서드 ==========

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    private void incrementEntityCount(EntityType type) {
        entityCounts.put(type, entityCounts.getOrDefault(type, 0) + 1);
    }

    private void decrementEntityCount(EntityType type) {
        int count = entityCounts.getOrDefault(type, 0);
        if (count > 0) {
            entityCounts.put(type, count - 1);
        }
    }
}