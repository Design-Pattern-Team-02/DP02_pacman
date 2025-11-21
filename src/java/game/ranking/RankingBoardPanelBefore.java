package game.ranking;

import game.GameManager;
import game.entities.Ranking;
import game.gameStates.StartMenuState;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RankingBoardPanelBefore extends JPanel implements Observer {
    private List<Ranking> allRankings;
    private List<String> mapNames;
    private final JTable rankingTable;
    private final DefaultTableModel tableModel;
    private final JComboBox<String> mapSelector;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String ALL_MAPS = "All Maps";

    public RankingBoardPanelBefore() {
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
        backButton.setBackground(Color.GRAY);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> {
            firePropertyChange("back", false, true);
            GameManager.getInstance().changeState(new StartMenuState());
            RankingManager.getInstance().removeObserver(this);
        });
        topPanel.add(backButton, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("🏆 TOP RANKINGS 🏆", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
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
        mapSelector.addActionListener(e -> filterRankingsByMap());

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
        mapSelector.removeAllItems();
        mapSelector.addItem(ALL_MAPS);

        if (mapNames != null) {
            for (String mapName : mapNames) {
                mapSelector.addItem(mapName);
            }
        }
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
            return;
        }

        List<Ranking> sortedRankings = rankings.stream()
                .sorted(Comparator.comparingInt(Ranking::getScore).reversed()
                        .thenComparing(Ranking::getTimeStamp))
                .toList();

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
    }
}
