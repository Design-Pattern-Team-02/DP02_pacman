package mapeditor.commands;

import java.util.Stack;

/**
 * CommandManager - 커맨드 매니저
 * 실행된 커맨드들을 관리하고 Undo/Redo 기능 제공
 *
 * Command Pattern 적용:
 * - Invoker 역할: 커맨드를 실행하고 히스토리 관리
 * - Undo/Redo 스택을 통한 작업 히스토리 관리
 * - 향후 매크로 기능 확장 가능
 */
public class CommandManager {
    private Stack<Command> undoStack;
    private Stack<Command> redoStack;
    private int maxHistorySize;
    private boolean mergingEnabled;

    // 기본 최대 히스토리 크기
    private static final int DEFAULT_MAX_HISTORY = 100;

    public CommandManager() {
        this(DEFAULT_MAX_HISTORY);
    }

    public CommandManager(int maxHistorySize) {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        this.maxHistorySize = maxHistorySize;
        this.mergingEnabled = true;
    }

    /**
     * 커맨드 실행 및 히스토리 추가
     * @param command 실행할 커맨드
     * @return 실행 성공 여부
     */
    public boolean executeCommand(Command command) {
        boolean success = command.execute();

        if (success) {
            // 병합 가능한지 확인
            if (mergingEnabled && !undoStack.isEmpty()) {
                Command lastCommand = undoStack.peek();
                if (lastCommand.canMergeWith(command)) {
                    Command merged = lastCommand.mergeWith(command);
                    if (merged != null) {
                        undoStack.pop();
                        undoStack.push(merged);
                        clearRedoStack();
                        return true;
                    }
                }
            }

            // 일반 추가
            addToUndoStack(command);
            clearRedoStack(); // 새 커맨드 실행 시 redo 스택 초기화
        }

        return success;
    }

    /**
     * 마지막 커맨드 실행 취소
     * @return 취소 성공 여부
     */
    public boolean undo() {
        if (canUndo()) {
            Command command = undoStack.pop();
            boolean success = command.undo();

            if (success) {
                redoStack.push(command);
            } else {
                // 실패 시 스택에 다시 추가
                undoStack.push(command);
            }

            return success;
        }
        return false;
    }

    /**
     * 마지막으로 취소한 커맨드 재실행
     * @return 재실행 성공 여부
     */
    public boolean redo() {
        if (canRedo()) {
            Command command = redoStack.pop();
            boolean success = command.redo();

            if (success) {
                undoStack.push(command);
            } else {
                // 실패 시 스택에 다시 추가
                redoStack.push(command);
            }

            return success;
        }
        return false;
    }

    /**
     * 모든 커맨드를 한 번에 실행 취소
     * @param count 취소할 커맨드 개수
     * @return 취소된 커맨드 개수
     */
    public int undoMultiple(int count) {
        int undoneCount = 0;
        for (int i = 0; i < count && canUndo(); i++) {
            if (undo()) {
                undoneCount++;
            } else {
                break;
            }
        }
        return undoneCount;
    }

    /**
     * 모든 커맨드를 한 번에 재실행
     * @param count 재실행할 커맨드 개수
     * @return 재실행된 커맨드 개수
     */
    public int redoMultiple(int count) {
        int redoneCount = 0;
        for (int i = 0; i < count && canRedo(); i++) {
            if (redo()) {
                redoneCount++;
            } else {
                break;
            }
        }
        return redoneCount;
    }

    /**
     * Undo 가능 여부 확인
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Redo 가능 여부 확인
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Undo 스택 크기 반환
     */
    public int getUndoStackSize() {
        return undoStack.size();
    }

    /**
     * Redo 스택 크기 반환
     */
    public int getRedoStackSize() {
        return redoStack.size();
    }

    /**
     * 마지막 실행된 커맨드 설명 반환
     */
    public String getLastCommandDescription() {
        if (!undoStack.isEmpty()) {
            return undoStack.peek().getDescription();
        }
        return null;
    }

    /**
     * 마지막으로 취소된 커맨드 설명 반환
     */
    public String getLastUndoneCommandDescription() {
        if (!redoStack.isEmpty()) {
            return redoStack.peek().getDescription();
        }
        return null;
    }

    /**
     * 모든 히스토리 초기화
     */
    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
    }

    /**
     * 커맨드 병합 활성화/비활성화
     */
    public void setMergingEnabled(boolean enabled) {
        this.mergingEnabled = enabled;
    }

    /**
     * Undo 스택에 커맨드 추가
     */
    private void addToUndoStack(Command command) {
        undoStack.push(command);

        // 최대 크기 제한
        if (undoStack.size() > maxHistorySize) {
            // 가장 오래된 커맨드 제거
            Stack<Command> temp = new Stack<>();
            while (!undoStack.isEmpty()) {
                temp.push(undoStack.pop());
            }
            temp.pop(); // 가장 오래된 것 제거
            while (!temp.isEmpty()) {
                undoStack.push(temp.pop());
            }
        }
    }

    /**
     * Redo 스택 초기화
     */
    private void clearRedoStack() {
        redoStack.clear();
    }

    /**
     * 히스토리 정보 문자열 반환 (디버깅용)
     */
    public String getHistoryInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Undo 스택 (").append(undoStack.size()).append("개):\n");
        for (int i = undoStack.size() - 1; i >= Math.max(0, undoStack.size() - 5); i--) {
            sb.append("  - ").append(undoStack.get(i).getDescription()).append("\n");
        }
        if (undoStack.size() > 5) {
            sb.append("  ...\n");
        }

        sb.append("\nRedo 스택 (").append(redoStack.size()).append("개):\n");
        for (int i = redoStack.size() - 1; i >= Math.max(0, redoStack.size() - 5); i--) {
            sb.append("  - ").append(redoStack.get(i).getDescription()).append("\n");
        }
        if (redoStack.size() > 5) {
            sb.append("  ...\n");
        }

        return sb.toString();
    }
}