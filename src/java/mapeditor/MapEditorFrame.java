package mapeditor;

import mapeditor.controller.MapEditorManager;
import mapeditor.view.EntityPalettePanel;
import mapeditor.view.MapGridPanel;
import mapeditor.view.EntityCounterPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * MapEditorFrame - 맵 에디터 메인 프레임
 * 모든 UI 컴포넌트를 통합하는 메인 윈도우
 *
 * 디자인 패턴 통합:
 * - 모든 패턴들이 조화롭게 작동하는 통합점
 * - MVC 패턴의 View 역할
 */
public class MapEditorFrame extends JFrame {
    private MapEditorManager manager;
    private EntityPalettePanel palettePanel;
    private MapGridPanel gridPanel;
    private EntityCounterPanel counterPanel;

    public MapEditorFrame() {
        this.manager = MapEditorManager.getInstance();
        initializeFrame();
        createComponents();
        setupLayout();
        setupKeyboardShortcuts();
        addWindowListeners();
    }

    /**
     * 프레임 초기화
     */
    private void initializeFrame() {
        setTitle("Pacman Map Editor - 28×31 Grid (CSV: 56×62)");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        // 아이콘 설정 (있는 경우)
        try {
            Image icon = Toolkit.getDefaultToolkit().getImage("src/resources/img/pacman.png");
            setIconImage(icon);
        } catch (Exception e) {
            // 아이콘 로드 실패 시 무시
        }
    }

    /**
     * 컴포넌트 생성
     */
    private void createComponents() {
        // 상단 팔레트 패널
        palettePanel = new EntityPalettePanel();

        // 중앙 그리드 패널
        gridPanel = new MapGridPanel();
        JScrollPane gridScrollPane = new JScrollPane(gridPanel);
        gridScrollPane.setPreferredSize(new Dimension(
            MapGridPanel.CELL_SIZE * mapeditor.model.MapData.WIDTH + 20,
            MapGridPanel.CELL_SIZE * mapeditor.model.MapData.HEIGHT + 20
        ));

        // 오른쪽 카운터 패널
        counterPanel = new EntityCounterPanel();
    }

    /**
     * 레이아웃 설정
     */
    private void setupLayout() {
        setLayout(new BorderLayout());

        // 상단 팔레트
        add(palettePanel, BorderLayout.NORTH);

        // 중앙 영역 (그리드 + 카운터)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(gridPanel), BorderLayout.CENTER);
        centerPanel.add(counterPanel, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null); // 화면 중앙에 배치
    }

    /**
     * 키보드 단축키 설정
     */
    private void setupKeyboardShortcuts() {
        // 메뉴바 생성
        JMenuBar menuBar = new JMenuBar();

        // 파일 메뉴
        JMenu fileMenu = new JMenu("파일");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem newMapItem = new JMenuItem("새 맵");
        newMapItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        newMapItem.addActionListener(e -> handleNewMap());
        fileMenu.add(newMapItem);

        JMenuItem saveItem = new JMenuItem("저장");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> handleSave());
        fileMenu.add(saveItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("종료");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(e -> handleExit());
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // 편집 메뉴
        JMenu editMenu = new JMenu("편집");
        editMenu.setMnemonic(KeyEvent.VK_E);

        JMenuItem undoItem = new JMenuItem("실행 취소");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
        undoItem.addActionListener(e -> {
            if (manager.canUndo()) {
                manager.undo();
                palettePanel.updateButtonStates();
            }
        });
        editMenu.add(undoItem);

        JMenuItem redoItem = new JMenuItem("다시 실행");
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
        redoItem.addActionListener(e -> {
            if (manager.canRedo()) {
                manager.redo();
                palettePanel.updateButtonStates();
            }
        });
        editMenu.add(redoItem);

        editMenu.addSeparator();

        JMenuItem clearItem = new JMenuItem("모두 지우기");
        clearItem.addActionListener(e -> handleClearAll());
        editMenu.add(clearItem);

        menuBar.add(editMenu);

        // 도움말 메뉴
        JMenu helpMenu = new JMenu("도움말");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem aboutItem = new JMenuItem("정보");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        JMenuItem patternsItem = new JMenuItem("적용된 디자인 패턴");
        patternsItem.addActionListener(e -> showDesignPatternsInfo());
        helpMenu.add(patternsItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    /**
     * 윈도우 리스너 추가
     */
    private void addWindowListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });
    }

    /**
     * 새 맵 생성
     */
    private void handleNewMap() {
        // 다이얼로그 없이 바로 새 맵 생성
        manager.createNewMap();
        palettePanel.clearSelection();
    }

    /**
     * 저장 처리
     */
    private void handleSave() {
        if (!manager.validateMap()) {
            // 유효성 검사 실패 시 콘솔에 오류 메시지 출력
            System.err.println("맵 저장 실패: " + manager.getValidationErrorMessage());
            return;
        }

        // PacGum 자동 채우기
        manager.fillEmptySpacesWithPacGum();

        // CSV 파일과 배경 이미지로 저장
        try {
            mapeditor.model.EntityType[][] mapData = manager.getMapDataCopy();
            String csvPath = mapeditor.utils.CsvMapWriter.saveMap(mapData, null);

            // 이미지 경로 계산
            String fileName = new java.io.File(csvPath).getName();
            String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
            String imgPath = "src/resources/img/" + nameWithoutExt + "_bg.png";

            // 콘솔에 저장 완료 메시지 출력
            System.out.println("맵이 성공적으로 저장되었습니다!");
            System.out.println("CSV 파일: " + csvPath);
            System.out.println("배경 이미지: " + imgPath);
            System.out.println("게임에서 이 맵을 사용하려면 Game.java에서 \"level/" + fileName + "\"로 변경하세요.");

            manager.setLastSavedFilePath(csvPath);

        } catch (Exception e) {
            // 오류 발생 시 콘솔에 출력
            System.err.println("맵 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 모두 지우기
     */
    private void handleClearAll() {
        // 다이얼로그 없이 바로 모든 엔티티 지우기
        manager.resetMap();
        palettePanel.clearSelection();
    }

    /**
     * 종료 처리
     */
    private void handleExit() {
        // 다이얼로그 없이 바로 종료
        System.exit(0);
    }

    /**
     * About 정보 출력
     */
    private void showAboutDialog() {
        // 콘솔에 정보 출력
        System.out.println("\n=== Pacman Map Editor ===");
        System.out.println("버전: 1.0.0");
        System.out.println("개발: 디자인 패턴 수업 과제");
        System.out.println("56×63 그리드의 Pacman 맵을 생성하는 에디터입니다.");
        System.out.println("다양한 디자인 패턴이 적용되었습니다.\n");
    }

    /**
     * 디자인 패턴 정보 출력
     */
    private void showDesignPatternsInfo() {
        // 콘솔에 디자인 패턴 정보 출력
        System.out.println("\n=== 적용된 디자인 패턴 ===\n");
        System.out.println("1. Observer Pattern");
        System.out.println("   - MapData (Subject) ↔ UI Components (Observers)");
        System.out.println("   - 맵 데이터 변경 시 자동 UI 업데이트\n");
        System.out.println("2. State Pattern");
        System.out.println("   - EditorState 인터페이스와 구현체들");
        System.out.println("   - IdleState, PlacementState, EraseState");
        System.out.println("   - 상태에 따른 마우스 이벤트 처리 변경\n");
        System.out.println("3. Command Pattern");
        System.out.println("   - PlaceEntityCommand, RemoveEntityCommand");
        System.out.println("   - Undo/Redo 기능 구현");
        System.out.println("   - CommandManager로 히스토리 관리\n");
        System.out.println("4. Singleton Pattern");
        System.out.println("   - MapEditorManager");
        System.out.println("   - 전역 에디터 상태 관리\n");
        System.out.println("5. MVC Pattern");
        System.out.println("   - Model: MapData, EntityType");
        System.out.println("   - View: UI Components (Panels)");
        System.out.println("   - Controller: MapEditorManager, StateContext\n");
        System.out.println("각 패턴이 서로 조화롭게 작동하여");
        System.out.println("유지보수가 쉽고 확장 가능한 구조를 제공합니다.\n");
    }

    /**
     * 메인 메서드
     */
    public static void main(String[] args) {
        // Look and Feel 설정
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // 기본 Look and Feel 사용
        }

        // EDT에서 실행
        SwingUtilities.invokeLater(() -> {
            MapEditorFrame frame = new MapEditorFrame();
            frame.setVisible(true);
        });
    }
}