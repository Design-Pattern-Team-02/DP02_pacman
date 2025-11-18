package mapeditor;

import mapeditor.controller.MapEditorManager;
import mapeditor.view.EntityPalettePanel;
import mapeditor.view.MapGridPanel;
import mapeditor.view.EntityCounterPanel;

import javax.swing.*;
import java.awt.*;
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

        // 중앙 그리드 패널 (스크롤 없이 고정 크기)
        gridPanel = new MapGridPanel();

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

        // 중앙 영역 (그리드 + 카운터) - 스크롤 없이 고정 크기
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(gridPanel, BorderLayout.CENTER);
        centerPanel.add(counterPanel, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null); // 화면 중앙에 배치
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
     * 종료 처리
     */
    private void handleExit() {
        System.exit(0);
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