package mapeditor;

import javax.swing.*;
import java.awt.*;

/**
 * MapEditorLauncher - 맵 에디터 실행 클래스
 * 독립적으로 맵 에디터를 실행하는 진입점
 *
 * 실행 방법:
 * 1. 터미널/명령 프롬프트에서: java mapeditor.MapEditorLauncher
 * 2. IDE에서 이 클래스를 직접 실행
 */
public class MapEditorLauncher {

    public static void main(String[] args) {
        // 시스템 Look and Feel 설정
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("시스템 Look and Feel 설정 실패: " + e.getMessage());
        }

        // Splash Screen 표시 (선택적)
        showSplashScreen();

        // EDT(Event Dispatch Thread)에서 GUI 생성
        SwingUtilities.invokeLater(() -> {
            try {
                MapEditorFrame frame = new MapEditorFrame();
                frame.setVisible(true);
                System.out.println("Pacman Map Editor가 성공적으로 시작되었습니다.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "맵 에디터 실행 중 오류가 발생했습니다:\n" + e.getMessage(),
                    "실행 오류",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    /**
     * Splash Screen 표시 (선택적)
     */
    private static void showSplashScreen() {
        JWindow splash = new JWindow();
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.BLACK);
        content.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));

        // 타이틀
        JLabel titleLabel = new JLabel("Pacman Map Editor", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        content.add(titleLabel, BorderLayout.NORTH);

        // 설명
        JTextArea infoText = new JTextArea(
            "디자인 패턴이 적용된 맵 에디터\n\n" +
            "적용된 패턴:\n" +
            "• Observer Pattern\n" +
            "• State Pattern\n" +
            "• Command Pattern\n" +
            "• Singleton Pattern\n" +
            "• MVC Pattern"
        );
        infoText.setEditable(false);
        infoText.setBackground(Color.BLACK);
        infoText.setForeground(Color.WHITE);
        infoText.setFont(new Font("Arial", Font.PLAIN, 14));
        infoText.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        content.add(infoText, BorderLayout.CENTER);

        // 로딩 메시지
        JLabel loadingLabel = new JLabel("로딩 중...", SwingConstants.CENTER);
        loadingLabel.setForeground(Color.YELLOW);
        loadingLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        content.add(loadingLabel, BorderLayout.SOUTH);

        splash.setContentPane(content);
        splash.setSize(400, 300);
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);

        // 2초 후 자동으로 닫힘
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // 무시
        }
        splash.setVisible(false);
        splash.dispose();
    }
}