package game.panel;

import game.GameManager;
import game.ranking.RankingBoardPanelBefore;
import game.ranking.RankingManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;

public class StartMenuPanel extends JPanel {
    private JTextField nicknameField;
    private JPanel mainMenuPanel;
    private JPanel nicknamePanel;
    private CardLayout cardLayout;

    public StartMenuPanel(JFrame parent) {
        setPreferredSize(new Dimension(448, 496));
        setBackground(Color.BLACK);

        // CardLayout을 사용하여 메인 메뉴와 닉네임 입력 화면 전환
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        // 메인 메뉴 패널 생성
        mainMenuPanel = createMainMenuPanel(parent);
        add(mainMenuPanel, "MAIN_MENU");

        // 닉네임 입력 패널 생성
        nicknamePanel = createNicknamePanel(parent);
        add(nicknamePanel, "NICKNAME");

        // 처음엔 메인 메뉴 표시
        cardLayout.show(this, "MAIN_MENU");
    }

    // 메인 메뉴 패널 생성
    private JPanel createMainMenuPanel(JFrame parent) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);

        // 타이틀
        JLabel titleLabel = new JLabel();
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(30, 10, 10, 10));
        try (InputStream is = getClass().getResourceAsStream("/img/pacman_title.png")) {
            if (is != null) {
                Image img = ImageIO.read(is);
                int targetW = 360;
                int targetH = img.getHeight(null) * targetW / Math.max(1, img.getWidth(null));
                Image scaled = img.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
                titleLabel.setIcon(new ImageIcon(scaled));
            } else {
                titleLabel.setText("PACMAN");
                titleLabel.setForeground(Color.YELLOW);
                titleLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
            }
        } catch (IOException ex) {
            titleLabel.setText("PACMAN");
            titleLabel.setForeground(Color.YELLOW);
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        }
        panel.add(titleLabel, BorderLayout.NORTH);

        // 버튼 패널
        JPanel buttons = new JPanel(new GridLayout(1, 3, 20, 0));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(20, 30, 40, 30));

        Font btnFont = new Font("SansSerif", Font.BOLD, 18);
        JButton rankingButton = makeMenuButton("Ranking", btnFont);
        JButton startButton = makeMenuButton("Start", btnFont);
        JButton mapEditorButton = makeMenuButton("Edit Map", btnFont);

        buttons.add(rankingButton);
        buttons.add(startButton);
        buttons.add(mapEditorButton);

        panel.add(buttons, BorderLayout.SOUTH);

        // Start 버튼 - 닉네임 입력 화면으로 전환
        startButton.addActionListener((ActionEvent e) -> {
            cardLayout.show(StartMenuPanel.this, "NICKNAME");
            // 닉네임 필드에 포커스
            SwingUtilities.invokeLater(() -> nicknameField.requestFocusInWindow());
        });

        rankingButton.addActionListener((ActionEvent e) -> {
            RankingBoardPanelBefore boardPanel = new RankingBoardPanelBefore();
            RankingManager rankingManager = RankingManager.getInstance();
            rankingManager.registerObserver(boardPanel);
            rankingManager.notifyObservers();
            parent.getContentPane().removeAll();
            parent.getContentPane().add(boardPanel);
            parent.revalidate();
            parent.repaint();
            parent.setSize(648, 496);
            parent.setLocationRelativeTo(null);
            parent.setVisible(true);
        });

        mapEditorButton.addActionListener((ActionEvent e) -> {
            //          execute map editor in a separate process
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "java",
                        "-cp",
                        System.getProperty("java.class.path"),
                        "mapeditor.MapEditorLauncher"
                );

                Process process = pb.start();
                int exitCode = process.waitFor();
                System.out.println("Exit code: " + exitCode);

            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        return panel;
    }

    // 닉네임 입력 패널 생성
    private JPanel createNicknamePanel(JFrame parent) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);

        // 상단: 작은 타이틀
        JLabel smallTitle = new JLabel("PACMAN");
        smallTitle.setHorizontalAlignment(SwingConstants.CENTER);
        smallTitle.setForeground(Color.YELLOW);
        smallTitle.setFont(new Font("SansSerif", Font.BOLD, 36));
        smallTitle.setBorder(new EmptyBorder(40, 10, 20, 10));
        panel.add(smallTitle, BorderLayout.NORTH);

        // 중앙: 닉네임 입력
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // "Enter Your Nickname" 안내 문구
        JLabel instructionLabel = new JLabel("Enter Your Nickname");
        instructionLabel.setForeground(Color.WHITE);
        instructionLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(instructionLabel, gbc);

        // 닉네임 입력 필드
        nicknameField = new JTextField(20);
        nicknameField.setFont(new Font("SansSerif", Font.PLAIN, 20));
        nicknameField.setPreferredSize(new Dimension(320, 45));
        nicknameField.setHorizontalAlignment(JTextField.CENTER);
        nicknameField.setBackground(Color.WHITE);
        nicknameField.setForeground(Color.BLACK);
        nicknameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.YELLOW, 3),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridy = 1;
        gbc.insets = new Insets(20, 10, 30, 10);
        centerPanel.add(nicknameField, gbc);

        panel.add(centerPanel, BorderLayout.CENTER);

        // 하단: 버튼들
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new GridBagLayout());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setPreferredSize(new Dimension(320, 50));

        Font btnFont = new Font("SansSerif", Font.BOLD, 18);
        JButton backButton = makeMenuButton("Back", btnFont);
        JButton playButton = makeMenuButton("Play", btnFont);

        buttonPanel.add(backButton);
        buttonPanel.add(playButton);

        GridBagConstraints btnGbc = new GridBagConstraints();
        btnGbc.insets = new Insets(0, 0, 40, 0);
        bottomPanel.add(buttonPanel, btnGbc);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Back 버튼 - 메인 메뉴로 돌아가기
        backButton.addActionListener((ActionEvent e) -> {
            cardLayout.show(StartMenuPanel.this, "MAIN_MENU");
        });

        // Play 버튼 - 닉네임 검증 후 게임 시작
        GameManager gameManager = GameManager.getInstance();
        playButton.addActionListener((ActionEvent e) -> {
            String nickname = nicknameField.getText().trim();

            if (nickname.isEmpty()) {
                JOptionPane.showMessageDialog(
                        parent,
                        "Please enter your nickname!",
                        "Nickname Required",
                        JOptionPane.WARNING_MESSAGE
                );
                nicknameField.requestFocusInWindow();
            } else {
                // 닉네임을 GameManager에 저장하고 게임 시작
                gameManager.setPlayerNickname(nickname);
//               여기에 MapLevelSelectPanel 호출
                MapLevelSelectPanel mapPanel = new MapLevelSelectPanel();
                parent.getContentPane().removeAll();
                parent.getContentPane().add(mapPanel);
                parent.revalidate();
                parent.repaint();
                parent.setSize(548, 596);
                parent.setLocationRelativeTo(null);
                parent.setVisible(true);
            }
        });

        // Enter 키로도 게임 시작
        nicknameField.addActionListener((ActionEvent e) -> {
            playButton.doClick();
        });

        return panel;
    }

    private JButton makeMenuButton(String text, Font font) {
        JButton b = new JButton(text);
        b.setFont(font);
        b.setForeground(Color.WHITE);
        b.setBackground(Color.BLACK);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        // 호버 효과
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b.setBackground(Color.DARK_GRAY);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                b.setBackground(Color.BLACK);
            }
        });

        return b;
    }

    // 닉네임을 가져오는 메서드
    public String getNickname() {
        return nicknameField.getText().trim();
    }
}