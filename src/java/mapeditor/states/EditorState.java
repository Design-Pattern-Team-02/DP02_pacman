package mapeditor.states;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import mapeditor.model.EntityType;

/**
 * EditorState Interface - State Pattern
 * 에디터의 다양한 상태(선택, 배치, 삭제 등)를 관리하기 위한 State 인터페이스
 *
 * 디자인 패턴 적용:
 * - State Pattern: 상태에 따라 동작이 달라지는 복잡한 조건문을 제거
 * - 각 상태는 독립적인 클래스로 구현되어 Open-Closed Principle 준수
 * - 새로운 상태 추가 시 기존 코드 수정 없이 확장 가능
 */
public interface EditorState {

    /**
     * 마우스 클릭 이벤트 처리
     * @param gridX 그리드 x 좌표
     * @param gridY 그리드 y 좌표
     * @param button 마우스 버튼 (MouseEvent.BUTTON1, BUTTON2, BUTTON3)
     */
    void handleMouseClick(int gridX, int gridY, int button);

    /**
     * 마우스 이동 이벤트 처리
     * @param gridX 그리드 x 좌표
     * @param gridY 그리드 y 좌표
     */
    void handleMouseMove(int gridX, int gridY);

    /**
     * 마우스 드래그 이벤트 처리 (클릭 + 드래그)
     * @param gridX 그리드 x 좌표
     * @param gridY 그리드 y 좌표
     */
    void handleMouseDrag(int gridX, int gridY);

    /**
     * 마우스가 그리드를 벗어났을 때 처리
     */
    void handleMouseExit();

    /**
     * 상태별 커스텀 렌더링 (미리보기, 하이라이트 등)
     * @param g Graphics2D 객체
     * @param cellWidth 셀 너비
     * @param cellHeight 셀 높이
     * @param mousePosition 현재 마우스 위치 (null일 수 있음)
     */
    void render(Graphics2D g, int cellWidth, int cellHeight, Point mousePosition);

    /**
     * 현재 상태 이름 반환 (UI 표시용)
     * @return 상태 이름
     */
    String getStateName();

    /**
     * 상태 진입 시 호출
     */
    void enter();

    /**
     * 상태 종료 시 호출
     */
    void exit();

    /**
     * 현재 선택된 엔티티 타입 반환 (해당되는 경우)
     * @return 선택된 엔티티 타입, 없으면 null
     */
    EntityType getSelectedEntityType();
}