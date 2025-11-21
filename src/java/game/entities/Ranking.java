package game.entities;

import java.time.LocalDateTime;

public class Ranking {
//    mapName 별로 랭킹 분리
    private String nickname;
    private String mapName;
    private int score;
    private LocalDateTime timeStamp;

    // Getters and Setters
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getMapName() { return mapName; }
    public void setMapName(String mapName) { this.mapName = mapName; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public LocalDateTime getTimeStamp() { return timeStamp; }
    public void setTimeStamp(LocalDateTime timeStamp) { this.timeStamp = timeStamp; }
}
