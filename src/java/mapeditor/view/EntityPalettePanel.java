package mapeditor.view;

import mapeditor.controller.MapEditorManager;
import mapeditor.model.EntityType;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * EntityPalettePanel - 엔티티 선택 팔레트
 * 상단에 배치되는 엔티티 선택 버튼들
 *
 * Observer Pattern의 일부:
 * - 선택 상태에 따라 버튼 하이라이트 업데이트
 * - MapEditorManager와 연동하여 상태 변경
 */
public class EntityPalettePanel extends JPanel {
    private MapEditorManager manager;
    private Map<EntityType, JToggleButton> entityButtons;
    private ButtonGroup buttonGroup;
    private JToggleButton eraseButton;
    private JButton cancelButton;

    public EntityPalettePanel() {
        this.manager = MapEditorManager.getInstance();
        this.entityButtons = new HashMap<>();
        this.buttonGroup = new ButtonGroup();

        initializePanel();
        createButtons();
    }

    /**
     * 패널 초기화
     */
    private void initializePanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        setBorder(BorderFactory.createTitledBorder("엔티티 팔레트"));
        setPreferredSize(new Dimension(0, 80));
        setBackground(new Color(240, 240, 240));
    }

    /**
     * 버튼들 생성 및 배치
     */
    private void createButtons() {
        // 필수 엔티티 그룹
        JPanel requiredPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        requiredPanel.setBorder(BorderFactory.createTitledBorder("필수 엔티티"));
        requiredPanel.setBackground(new Color(255, 255, 230));

        addEntityButton(requiredPanel, EntityType.PACMAN, Color.YELLOW);
        addEntityButton(requiredPanel, EntityType.BLINKY, Color.RED);
        addEntityButton(requiredPanel, EntityType.PINKY, Color.PINK);
        addEntityButton(requiredPanel, EntityType.INKY, Color.CYAN);
        addEntityButton(requiredPanel, EntityType.CLYDE, Color.ORANGE);

        add(requiredPanel);

        // 자유 배치 엔티티 그룹
        JPanel freePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        freePanel.setBorder(BorderFactory.createTitledBorder("자유 배치"));
        freePanel.setBackground(new Color(230, 255, 230));

        addEntityButton(freePanel, EntityType.WALL, Color.BLUE);
        addEntityButton(freePanel, EntityType.GHOST_HOUSE_WALL, new Color(100, 100, 255));
        addEntityButton(freePanel, EntityType.SUPER_PAC_GUM, Color.WHITE);

        add(freePanel);

        // 도구 그룹
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        toolPanel.setBorder(BorderFactory.createTitledBorder("도구"));
        toolPanel.setBackground(new Color(255, 230, 230));

        // 지우개 버튼
        eraseButton = new JToggleButton("지우개");
        eraseButton.setPreferredSize(new Dimension(80, 40));
        eraseButton.setBackground(new Color(255, 200, 200));
        eraseButton.addActionListener(e -> {
            if (eraseButton.isSelected()) {
                manager.setEraseMode();
                clearOtherSelections(eraseButton);
            }
        });
        buttonGroup.add(eraseButton);
        toolPanel.add(eraseButton);

        // 취소 버튼
        cancelButton = new JButton("X (취소)");
        cancelButton.setPreferredSize(new Dimension(80, 40));
        cancelButton.setBackground(new Color(200, 200, 200));
        cancelButton.addActionListener(e -> {
            buttonGroup.clearSelection();
            manager.cancelSelection();
            updateButtonStates();
        });
        toolPanel.add(cancelButton);

        add(toolPanel);

        // Undo/Redo 버튼 그룹
        JPanel undoRedoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        undoRedoPanel.setBorder(BorderFactory.createTitledBorder("편집"));
        undoRedoPanel.setBackground(new Color(230, 230, 255));

        JButton undoButton = new JButton("↶ Undo");
        undoButton.setPreferredSize(new Dimension(80, 40));
        undoButton.addActionListener(e -> {
            if (manager.canUndo()) {
                manager.undo();
                updateButtonStates();
            }
        });
        undoRedoPanel.add(undoButton);

        JButton redoButton = new JButton("↷ Redo");
        redoButton.setPreferredSize(new Dimension(80, 40));
        redoButton.addActionListener(e -> {
            if (manager.canRedo()) {
                manager.redo();
                updateButtonStates();
            }
        });
        undoRedoPanel.add(redoButton);

        add(undoRedoPanel);
    }

    /**
     * 엔티티 버튼 추가
     */
    private void addEntityButton(JPanel panel, EntityType entityType, Color color) {
        JToggleButton button = createEntityButton(entityType, color);
        entityButtons.put(entityType, button);
        buttonGroup.add(button);
        panel.add(button);
    }

    /**
     * 엔티티 버튼 생성
     */
    private JToggleButton createEntityButton(EntityType entityType, Color color) {
        JToggleButton button = new JToggleButton();
        button.setPreferredSize(new Dimension(60, 40));
        button.setBackground(color);
        button.setToolTipText(entityType.getDisplayName());

        // 버튼 텍스트 설정
        String buttonText = String.format("<html><center>%s<br>(%c)</center></html>",
            entityType.getDisplayName().split(" ")[0],
            entityType.getSymbol());
        button.setText(buttonText);

        // 더 어두운 색상 텍스트 (대비 개선)
        if (color.equals(Color.YELLOW) || color.equals(Color.WHITE) ||
            color.equals(Color.CYAN) || color.equals(Color.PINK)) {
            button.setForeground(Color.BLACK);
        } else {
            button.setForeground(Color.WHITE);
        }

        button.setFont(new Font("Arial", Font.BOLD, 10));

        // 액션 리스너 추가
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (button.isSelected()) {
                    manager.selectEntity(entityType);
                    clearOtherSelections(button);
                } else {
                    manager.cancelSelection();
                }
                updateButtonStates();
            }
        });

        return button;
    }

    /**
     * 다른 버튼들의 선택 해제
     */
    private void clearOtherSelections(JToggleButton selectedButton) {
        for (JToggleButton button : entityButtons.values()) {
            if (button != selectedButton) {
                button.setSelected(false);
            }
        }
        if (selectedButton != eraseButton) {
            eraseButton.setSelected(false);
        }
    }

    /**
     * 버튼 상태 업데이트 (필수 엔티티 최대 개수 도달 시 비활성화)
     */
    public void updateButtonStates() {
        for (Map.Entry<EntityType, JToggleButton> entry : entityButtons.entrySet()) {
            EntityType type = entry.getKey();
            JToggleButton button = entry.getValue();

            if (type.isRequired() && type.getMaxCount() > 0) {
                int currentCount = manager.getEntityCount(type);
                boolean canPlaceMore = currentCount < type.getMaxCount();

                // 이미 최대 개수에 도달했으면 버튼 비활성화
                button.setEnabled(canPlaceMore || button.isSelected());

                // 툴팁 업데이트
                button.setToolTipText(String.format("%s (%d/%d)",
                    type.getDisplayName(), currentCount, type.getMaxCount()));
            }
        }

        // Undo/Redo 버튼 상태도 업데이트할 수 있음
        // (별도의 메서드로 분리하는 것이 좋음)
    }

    /**
     * 선택 해제
     */
    public void clearSelection() {
        buttonGroup.clearSelection();
        updateButtonStates();
    }
}