package mapeditor.model;

/**
 * EntityType Enum
 * 맵 에디터에서 배치 가능한 모든 엔티티 타입을 정의
 *
 * 디자인 패턴 적용:
 * - Type-safe enum pattern으로 엔티티 타입을 안전하게 관리
 * - 각 타입별 속성(심볼, 이름, 필수여부)을 캡슐화
 */
public enum EntityType {
    // 필수 엔티티 (사용자가 배치해야 함)
    PACMAN('P', "Pacman", true, 1),
    CLYDE('c', "Clyde", true, 1),

    // 자동 배치 고스트 (편집 불가, 맵 에디터가 자동 배치)
    BLINKY('b', "Blinky", false, 0),
    PINKY('p', "Pinky", false, 0),
    INKY('i', "Inky", false, 0),

    // 자유 배치 엔티티
    WALL('x', "Wall", false, -1),
    GHOST_HOUSE_WALL('-', "GhostHouse Wall", false, -1),
    SUPER_PAC_GUM('o', "Super PacGum", false, -1),
    PAC_GUM('.', "PacGum", false, -1),

    // 빈 공간
    EMPTY(' ', "Empty", false, -1);

    private final char symbol;
    private final String displayName;
    private final boolean required;
    private final int maxCount;  // -1 for unlimited

    EntityType(char symbol, String displayName, boolean required, int maxCount) {
        this.symbol = symbol;
        this.displayName = displayName;
        this.required = required;
        this.maxCount = maxCount;
    }

    public char getSymbol() {
        return symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isRequired() {
        return required;
    }

    public int getMaxCount() {
        return maxCount;
    }

    /**
     * 심볼로 EntityType을 찾아 반환
     * @param symbol 찾을 심볼
     * @return 해당하는 EntityType, 없으면 EMPTY
     */
    public static EntityType fromSymbol(char symbol) {
        for (EntityType type : EntityType.values()) {
            if (type.symbol == symbol) {
                return type;
            }
        }
        return EMPTY;
    }
}