package game.ranking;

import game.GameManager;
import game.entities.Ranking;

import javax.swing.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RankingManager implements Subject{
    private volatile static RankingManager INSTANCE;
    private static final String RANKING_FILE = "rankings.csv";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final List<Observer> observers = new ArrayList<>();

    private RankingManager() {}

    public static RankingManager getInstance() {
        if (INSTANCE == null) {
            synchronized (RankingManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RankingManager();
                }
            }
        }
        return INSTANCE;
    }
    public void saveRanking(Ranking ranking) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RANKING_FILE, true))) {
            String line = String.format("%s,%s,%d,%s%n",
                    ranking.getNickname(),
                    ranking.getMapName(),
                    ranking.getScore(),
                    ranking.getTimeStamp().format(FORMATTER));
            writer.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Ranking> getAllRankings() {
        List<Ranking> rankings = new ArrayList<>();
        File file = new File(RANKING_FILE);

        if (!file.exists()) {
            return rankings;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    Ranking r = new Ranking();
                    r.setNickname(parts[0]);
                    r.setMapName(parts[1]);
                    r.setScore(Integer.parseInt(parts[2]));
                    r.setTimeStamp(LocalDateTime.parse(parts[3], FORMATTER));
                    rankings.add(r);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rankings;
    }

    public List<Ranking> getTopRankings(String mapName, int limit) {
        return getAllRankings().stream()
                .filter(r -> r.getMapName().equals(mapName))
                .sorted(Comparator.comparingInt(Ranking::getScore).reversed()
                        .thenComparing(Ranking::getTimeStamp))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<String> getAllMapNames() {
        return getAllRankings().stream()
                .map(Ranking::getMapName)
                .distinct()
                .collect(Collectors.toList());
    }

    public void clearRankings() {
        File file = new File(RANKING_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }
    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
//          display update
            observer.update(getAllRankings(), getAllMapNames());
        }
    }

}