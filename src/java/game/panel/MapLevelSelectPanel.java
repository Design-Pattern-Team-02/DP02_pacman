package game.panel;

import game.Game;
import game.GameManager;
import game.gameStates.PlayingState;
import game.utils.ResourceUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.List;

public class MapLevelSelectPanel extends JPanel {
    private final List<String> maps;
    private int currentIndex = 0;

    private final JLabel imageLabel = new JLabel();
    private final JLabel nameLabel = new JLabel("", SwingConstants.CENTER);

    private final JButton leftButton = new JButton("<");
    private final JButton rightButton = new JButton(">");
    private final JPanel levelButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    private final ResourceUtils resourceUtils;

    public MapLevelSelectPanel() {
        this.resourceUtils = new ResourceUtils();
        this.maps = resourceUtils.listLevelNames();

        setBackground(Color.BLACK);
        setLayout(new BorderLayout());
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        if (!maps.isEmpty()) {
            currentIndex = 0;
            loadCurrentMap();
        } else {
            nameLabel.setText("No maps found");
        }
    }

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Color.BLACK);

        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(Color.BLACK);

        styleButton(leftButton);
        styleButton(rightButton);
        leftButton.setPreferredSize(new Dimension(60, 60));
        rightButton.setPreferredSize(new Dimension(60, 60));

        leftButton.addActionListener(this::onLeft);
        rightButton.addActionListener(this::onRight);

        navPanel.add(leftButton, BorderLayout.WEST);

        JPanel display = new JPanel(new BorderLayout());
        display.setBackground(Color.BLACK);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        display.add(imageLabel, BorderLayout.CENTER);

        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 18f));
        nameLabel.setForeground(Color.WHITE);
        display.add(nameLabel, BorderLayout.SOUTH);

        navPanel.add(display, BorderLayout.CENTER);
        navPanel.add(rightButton, BorderLayout.EAST);

        center.add(navPanel, BorderLayout.CENTER);
        return center;
    }

    private JPanel createBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.BLACK);
        levelButtonPanel.setOpaque(false);

        for (int i = 1; i <= 3; i++) {
            final int level = i;
            JButton b = new JButton("Level " + i);
            b.setPreferredSize(new Dimension(100, 40));
            styleButton(b);
            b.addActionListener(e -> onLevelSelected(level, (JButton) e.getSource()));
            levelButtonPanel.add(b);
        }

        bottom.add(levelButtonPanel, BorderLayout.CENTER);
        return bottom;
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(Color.BLACK);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.DARK_GRAY);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.BLACK);
            }
        });
    }

    private void onLeft(ActionEvent e) {
        if (maps.isEmpty()) return;
        currentIndex = (currentIndex - 1 + maps.size()) % maps.size();
        loadCurrentMap();
    }

    private void onRight(ActionEvent e) {
        if (maps.isEmpty()) return;
        currentIndex = (currentIndex + 1) % maps.size();
        loadCurrentMap();
    }

    private void loadCurrentMap() {
        String name = maps.get(currentIndex);
        nameLabel.setText("Map: " + name);
        BufferedImage img = resourceUtils.loadMapImage(name);
        if (img != null) {
            int maxW = 400;
            double ratio = (double) img.getHeight() / img.getWidth();
            int maxH = (int) (maxW * ratio);
            Image scaled = img.getScaledInstance(maxW, maxH, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaled));
        } else {
            imageLabel.setIcon(null);
        }

        GameManager.getInstance().setSelectedMapName(name);
    }

    private void onLevelSelected(int level, JButton source) {
        Game.setGameLevel(level);
        GameManager.getInstance().changeState(new PlayingState());
        for (Component c : levelButtonPanel.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                b.setBackground(Color.BLACK);
                b.setForeground(Color.WHITE);
            }
        }
        source.setBackground(Color.DARK_GRAY);
        source.setForeground(Color.WHITE);
    }
}
