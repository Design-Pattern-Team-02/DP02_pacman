package mapeditor.observers;

import mapeditor.model.EntityType;

/**
 * MapObserver Interface - Observer Pattern
 * 맵 데이터 변경을 감지하고 UI 컴포넌트를 업데이트하기 위한 옵저버 인터페이스
 *
 * 디자인 패턴 적용:
 * - Observer Pattern: 맵 데이터(Subject)의 변경을 여러 UI 컴포넌트(Observers)에 통지
 * - 느슨한 결합(Loose Coupling)으로 UI와 데이터 모델 분리
 */
public interface MapObserver {

    /**
     * 특정 위치의 엔티티가 변경되었을 때 호출
     * @param x x 좌표
     * @param y y 좌표
     * @param entityType 배치된 엔티티 타입
     */
    void onEntityPlaced(int x, int y, EntityType entityType);

    /**
     * 특정 위치의 엔티티가 제거되었을 때 호출
     * @param x x 좌표
     * @param y y 좌표
     */
    void onEntityRemoved(int x, int y);

    /**
     * 맵 전체가 초기화되었을 때 호출
     */
    void onMapReset();

    /**
     * 필수 엔티티 개수가 변경되었을 때 호출
     * @param entityType 변경된 엔티티 타입
     * @param count 현재 개수
     */
    void onEntityCountChanged(EntityType entityType, int count);

    /**
     * 맵 검증 상태가 변경되었을 때 호출
     * @param isValid 검증 통과 여부
     */
    void onValidationStateChanged(boolean isValid);
}