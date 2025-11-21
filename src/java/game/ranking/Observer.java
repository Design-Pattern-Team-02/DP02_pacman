package game.ranking;

import game.entities.Ranking;

import java.util.List;

public interface Observer {
    void update(List<Ranking> rankings, List<String> mapNames);
}
