package game.ranking;

import game.GameManager;
import game.entities.Ranking;
import game.gameStates.GameOverState;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RankingBoardPanelAfter extends JPanel implements Observer {
    private List<Ranking> allRankings;
    private List<String> mapNames;
    private final JTable rankingTable;
    private final DefaultTableModel tableModel;
    private final JComboBox<String> mapSelector;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String ALL_MAPS = "All Maps";
    private boolean suppressAction = false;

    // 플레이어 정보 라벨 (중앙)
    private final JLabel playerInfoLabel;

    public RankingBoardPanelAfter() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(648, 496));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.BLACK);

        // 상단 패널 (제목 + 맵 선택)
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.BLACK);

        // 뒤로가기 버튼 (왼쪽)
        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setPreferredSize(new Dimension(100, 30));
        backButton.setOpaque(true);
        backButton.setContentAreaFilled(true);
        backButton.setBorderPainted(false);
        backButton.setBackground(Color.GRAY);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> {
            firePropertyChange("back", false, true);
            GameManager.getInstance().changeState(new GameOverState());
            RankingManager.getInstance().removeObserver(this);
        });
        topPanel.add(backButton, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.BLACK);
        playerInfoLabel = new JLabel("Your Score: -  Rank: -", SwingConstants.CENTER);
        playerInfoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        playerInfoLabel.setForeground(Color.WHITE);
        centerPanel.add(playerInfoLabel);
        topPanel.add(centerPanel, BorderLayout.CENTER);

        // 오른쪽에 빈 공간 추가 (대칭을 위해)
        JPanel rightSpacer = new JPanel();
        rightSpacer.setPreferredSize(new Dimension(100, 30));
        rightSpacer.setBackground(Color.BLACK);
        topPanel.add(rightSpacer, BorderLayout.EAST);

        JLabel titleLabel = new JLabel("TOP RANKINGS", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 28));
        titleLabel.setForeground(Color.YELLOW);
        topPanel.add(titleLabel, BorderLayout.NORTH);

        // 맵 선택 패널
        JPanel mapSelectPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        mapSelectPanel.setBackground(Color.BLACK);

        JLabel mapLabel = new JLabel("Select Map: ");
        mapLabel.setForeground(Color.WHITE);
        mapLabel.setFont(new Font("Arial", Font.BOLD, 14));

        mapSelector = new JComboBox<>();
        mapSelector.setFont(new Font("Arial", Font.PLAIN, 14));
        mapSelector.setPreferredSize(new Dimension(200, 30));

        // 액션 리스너: populate 중에는 동작하지 않게 함
        mapSelector.addActionListener(e -> {
            if (suppressAction) return;
            filterRankingsByMap();
        });

        mapSelectPanel.add(mapLabel);
        mapSelectPanel.add(mapSelector);
        topPanel.add(mapSelectPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // 테이블 생성
        String[] columnNames = {"Rank", "Nickname", "Map", "Score", "Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        rankingTable = new JTable(tableModel);
        rankingTable.setFont(new Font("Monospaced", Font.PLAIN, 14));
        rankingTable.setRowHeight(30);
        rankingTable.setBackground(Color.DARK_GRAY);
        rankingTable.setForeground(Color.WHITE);
        rankingTable.setGridColor(Color.GRAY);
        rankingTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));
        rankingTable.getTableHeader().setBackground(Color.ORANGE);
        rankingTable.getTableHeader().setForeground(Color.BLACK);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < rankingTable.getColumnCount(); i++) {
            rankingTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(rankingTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
        add(scrollPane, BorderLayout.CENTER);

        // 하단 정보
        JLabel infoLabel = new JLabel("Rankings are updated in real-time", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        infoLabel.setForeground(Color.LIGHT_GRAY);
        add(infoLabel, BorderLayout.SOUTH);
    }

    @Override
    public void update(List<Ranking> rankings, List<String> mapNames) {
        this.allRankings = rankings;
        this.mapNames = mapNames;
        updateMapSelector();
        filterRankingsByMap();
    }

    private void updateMapSelector() {
        // 아이템 추가 중 액션 이벤트 억제
        suppressAction = true;
        mapSelector.removeAllItems();
        mapSelector.addItem(ALL_MAPS);

        if (mapNames != null) {
            for (String mapName : mapNames) {
                mapSelector.addItem(mapName);
            }
        }

        // GameManager에 저장된 선택값을 복원 (아이템이 채워진 뒤에 설정)
        String saved = GameManager.getInstance().getSelectedMapName();
        String toSelect = ALL_MAPS;
        if (saved != null) {
            for (int i = 0; i < mapSelector.getItemCount(); i++) {
                if (saved.equals(mapSelector.getItemAt(i))) {
                    toSelect = saved;
                    break;
                }
            }
        }

        final String sel = toSelect;
        SwingUtilities.invokeLater(() -> {
            mapSelector.setSelectedItem(sel);
            filterRankingsByMap();
            suppressAction = false; // 선택 완료 후 액션 허용
        });
    }

    private void filterRankingsByMap() {
        String selectedMap = (String) mapSelector.getSelectedItem();
        if (selectedMap == null || allRankings == null) {
            return;
        }

        List<Ranking> filteredRankings;
        if (ALL_MAPS.equals(selectedMap)) {
            filteredRankings = allRankings;
        } else {
            filteredRankings = allRankings.stream()
                    .filter(r -> r.getMapName().equals(selectedMap))
                    .collect(Collectors.toList());
        }

        updateTable(filteredRankings);
    }

    private void updateTable(List<Ranking> rankings) {
        tableModel.setRowCount(0);

        if (rankings == null || rankings.isEmpty()) {
            // 플레이어 정보 초기화
            updatePlayerInfo("-", "-");
            return;
        }

        List<Ranking> sortedRankings = rankings.stream()
                .sorted(Comparator.comparingInt(Ranking::getScore).reversed()
                        .thenComparing(Ranking::getTimeStamp))
                .collect(Collectors.toList());

        for (int i = 0; i < sortedRankings.size(); i++) {
            Ranking r = sortedRankings.get(i);
            Object[] row = {
                    i + 1,
                    r.getNickname(),
                    r.getMapName(),
                    r.getScore(),
                    r.getTimeStamp().format(FORMATTER)
            };
            tableModel.addRow(row);
        }

        if (!sortedRankings.isEmpty()) {
            rankingTable.setRowSelectionInterval(0, 0);
        }

        // 현재 정렬된 리스트에서 플레이어 닉네임으로 등수 탐색 및 표시
        updatePlayerInfoFromRankings(sortedRankings);
    }

    private void updatePlayerInfoFromRankings(List<Ranking> sortedRankings) {
        int myScore = GameManager.getInstance().getScore();
        if (myScore <= 0 ) {
            updatePlayerInfo("-", "-");
            return;
        }

        for (int i = 0; i < sortedRankings.size(); i++) {
            Ranking r = sortedRankings.get(i);
            if (myScore == r.getScore()) {
                final String rankText = String.valueOf(i + 1);
                updatePlayerInfo(String.valueOf(myScore), rankText);
                return;
            }
        }

        // 찾지 못하면 표시 초기화
        updatePlayerInfo("-", "-");
    }

    private void updatePlayerInfo(String scoreText, String rankText) {
        final String text = "Your Score: " + scoreText + "  Rank: " + rankText;
        SwingUtilities.invokeLater(() -> playerInfoLabel.setText(text));
    }
}
