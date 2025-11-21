package game.entities.levelStrategies;

/**
 * Strategy 패턴: 레벨별 고스트 행동 규칙을 정의하는 인터페이스
 *
 * 역할:
 * - 각 레벨마다 다른 능력 활성화 규칙 제공
 * - 순간이동과 투명화의 발동 주기 결정
 * - 고스트 속도 증가율 제공
 * - 해산(Frightened) 시간 감소율 제공
 * - 런타임에 레벨 전략 교체 가능
 */
public interface LevelStrategy {
    /**
     * 순간이동 발동 주기 (프레임 단위, 60fps 기준)
     * @return 주기 (0이면 비활성화)
     */
    int getTeleportInterval();

    /**
     * 투명화 발동 주기 (프레임 단위, 60fps 기준)
     * @return 주기 (0이면 비활성화)
     */
    int getInvisibleInterval();

    /**
     * 투명화 지속 시간 (프레임 단위, 60fps 기준)
     * @return 지속 시간
     */
    int getInvisibleDuration();

    /**
     * 고스트 속도 증가율
     * @return 속도 증가율 (0.0 = 0%, 0.2 = 20%, 0.4 = 40%)
     */
    double getSpeedIncreaseRate();

    /**
     * 해산(Frightened) 시간 감소율
     * @return 시간 감소율 (0.0 = 0%, 0.2 = 20%, 0.4 = 40%)
     */
    double getFrightenedTimerReduction();

    /**
     * 레벨 이름
     * @return 레벨 표시 이름
     */
    String getLevelName();
}