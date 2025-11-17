package mapeditor.commands;

/**
 * Command Interface - Command Pattern
 * 모든 에디터 액션을 캡슐화하는 커맨드 인터페이스
 *
 * 디자인 패턴 적용:
 * - Command Pattern: 요청을 객체로 캡슐화하여 매개변수화, 큐잉, 로깅, Undo 가능
 * - 각 액션을 독립적인 객체로 만들어 실행과 취소를 분리
 * - Undo/Redo 스택 구현의 기반
 */
public interface Command {

    /**
     * 커맨드 실행
     * @return 실행 성공 여부
     */
    boolean execute();

    /**
     * 커맨드 실행 취소
     * @return 취소 성공 여부
     */
    boolean undo();

    /**
     * 커맨드 재실행 (기본적으로 execute()와 동일, 필요시 오버라이드)
     * @return 재실행 성공 여부
     */
    default boolean redo() {
        return execute();
    }

    /**
     * 커맨드 설명 (디버깅 및 히스토리 표시용)
     * @return 커맨드 설명 문자열
     */
    String getDescription();

    /**
     * 다른 커맨드와 병합 가능한지 확인
     * (연속된 같은 타입의 액션을 하나로 합치기 위함)
     * @param other 다른 커맨드
     * @return 병합 가능 여부
     */
    default boolean canMergeWith(Command other) {
        return false;
    }

    /**
     * 다른 커맨드와 병합
     * @param other 병합할 커맨드
     * @return 병합된 새 커맨드 (실패시 null)
     */
    default Command mergeWith(Command other) {
        return null;
    }
}