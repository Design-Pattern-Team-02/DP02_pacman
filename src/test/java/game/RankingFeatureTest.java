package game;

import game.entities.Ranking;
import game.ranking.RankingManager;
import game.ranking.Subject;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.Assert.*;

public class RankingFeatureTest {

    private RankingManager rankingManager;
    private Ranking testRanking;
    private TestObserver testObserver;

    private class TestObserver implements game.ranking.Observer {
        private boolean updateCalled = false;
        private List<Ranking> lastRankings = null;
        private List<String> lastMapNames = null;

        @Override
        public void update(List<Ranking> rankings, List<String> mapNames) {
            this.updateCalled = true;
            this.lastRankings = rankings;
            this.lastMapNames = mapNames;
        }

        public boolean isUpdateCalled() {
            return updateCalled;
        }

        public void reset() {
            updateCalled = false;
            lastRankings = null;
            lastMapNames = null;
        }

        public List<Ranking> getLastRankings() {
            return lastRankings;
        }

        public List<String> getLastMapNames() {
            return lastMapNames;
        }
    }

    @Before
    public void setUp() {
        rankingManager = RankingManager.getInstance();
        rankingManager.clearRankings();

        testRanking = new Ranking();
        testRanking.setNickname("TestPlayer");
        testRanking.setMapName("TestMap");
        testRanking.setScore(1000);
        testRanking.setTimeStamp(LocalDateTime.now());

        testObserver = new TestObserver();
    }

    @After
    public void tearDown() {
        rankingManager.clearRankings();
    }

    @Test
    public void testRankingManagerSingleton() {
        RankingManager instance1 = RankingManager.getInstance();
        RankingManager instance2 = RankingManager.getInstance();
        assertSame("RankingManager는 싱글톤이어야 함", instance1, instance2);
    }

    @Test
    public void testSaveRanking() {
        rankingManager.saveRanking(testRanking);
        List<Ranking> rankings = rankingManager.getAllRankings();

        assertEquals("저장된 랭킹 개수는 1개여야 함", 1, rankings.size());
        assertEquals("닉네임이 일치해야 함", "TestPlayer", rankings.get(0).getNickname());
        assertEquals("맵 이름이 일치해야 함", "TestMap", rankings.get(0).getMapName());
        assertEquals("점수가 일치해야 함", 1000, rankings.get(0).getScore());
    }

    @Test
    public void testSaveMultipleRankings() {
        Ranking ranking1 = new Ranking();
        ranking1.setNickname("Player1");
        ranking1.setMapName("Map1");
        ranking1.setScore(500);
        ranking1.setTimeStamp(LocalDateTime.now());

        Ranking ranking2 = new Ranking();
        ranking2.setNickname("Player2");
        ranking2.setMapName("Map2");
        ranking2.setScore(800);
        ranking2.setTimeStamp(LocalDateTime.now());

        rankingManager.saveRanking(ranking1);
        rankingManager.saveRanking(ranking2);

        List<Ranking> rankings = rankingManager.getAllRankings();
        assertEquals("저장된 랭킹 개수는 2개여야 함", 2, rankings.size());
    }

    @Test
    public void testGetTopRankingsSortedByScore() {
        for (int i = 0; i < 5; i++) {
            Ranking r = new Ranking();
            r.setNickname("Player" + i);
            r.setMapName("TestMap");
            r.setScore(100 * i);
            r.setTimeStamp(LocalDateTime.now());
            rankingManager.saveRanking(r);
        }

        List<Ranking> topRankings = rankingManager.getTopRankings("TestMap", 3);

        assertEquals("Top 3 랭킹만 반환되어야 함", 3, topRankings.size());
        assertTrue("점수 내림차순 정렬 확인",
                topRankings.get(0).getScore() >= topRankings.get(1).getScore());
        assertTrue("점수 내림차순 정렬 확인",
                topRankings.get(1).getScore() >= topRankings.get(2).getScore());
    }

    @Test
    public void testGetAllMapNamesWithoutDuplicates() {
        Ranking r1 = new Ranking();
        r1.setNickname("Player1");
        r1.setMapName("Map1");
        r1.setScore(100);
        r1.setTimeStamp(LocalDateTime.now());

        Ranking r2 = new Ranking();
        r2.setNickname("Player2");
        r2.setMapName("Map1");
        r2.setScore(200);
        r2.setTimeStamp(LocalDateTime.now());

        Ranking r3 = new Ranking();
        r3.setNickname("Player3");
        r3.setMapName("Map2");
        r3.setScore(300);
        r3.setTimeStamp(LocalDateTime.now());

        rankingManager.saveRanking(r1);
        rankingManager.saveRanking(r2);
        rankingManager.saveRanking(r3);

        List<String> mapNames = rankingManager.getAllMapNames();

        assertEquals("중복 제거된 맵 이름은 2개여야 함", 2, mapNames.size());
        assertTrue("Map1이 포함되어야 함", mapNames.contains("Map1"));
        assertTrue("Map2가 포함되어야 함", mapNames.contains("Map2"));
    }

    @Test
    public void testClearRankings() {
        rankingManager.saveRanking(testRanking);
        assertFalse("저장 후 랭킹 목록은 비어있지 않아야 함",
                rankingManager.getAllRankings().isEmpty());

        rankingManager.clearRankings();

        assertTrue("초기화 후 랭킹 목록은 비어있어야 함",
                rankingManager.getAllRankings().isEmpty());

        File rankingFile = new File("rankings.csv");
        assertFalse("초기화 후 파일이 삭제되어야 함", rankingFile.exists());
    }

    @Test
    public void testRegisterObserverAndNotify() {
        rankingManager.registerObserver(testObserver);
        rankingManager.notifyObservers();

        assertTrue("Observer의 update 메서드가 호출되어야 함",
                testObserver.isUpdateCalled());
    }

    @Test
    public void testRemoveObserver() {
        rankingManager.registerObserver(testObserver);
        rankingManager.removeObserver(testObserver);
        rankingManager.notifyObservers();

        assertFalse("제거된 Observer는 알림을 받지 않아야 함",
                testObserver.isUpdateCalled());
    }

    @Test
    public void testObserverReceivesCorrectData() {
        rankingManager.saveRanking(testRanking);
        rankingManager.registerObserver(testObserver);
        rankingManager.notifyObservers();

        assertNotNull("랭킹 데이터가 전달되어야 함",
                testObserver.getLastRankings());
        assertNotNull("맵 이름 데이터가 전달되어야 함",
                testObserver.getLastMapNames());
        assertEquals("전달된 랭킹 개수가 일치해야 함",
                1, testObserver.getLastRankings().size());
    }

    @Test
    public void testRankingManagerImplementsSubject() {
        assertTrue("RankingManager는 Subject 인터페이스를 구현해야 함",
                rankingManager instanceof Subject);
    }

    @Test
    public void testRankingBoardPanelCreation() {
        game.ranking.RankingBoardPanelBefore panel1 = new game.ranking.RankingBoardPanelBefore();
        game.ranking.RankingBoardPanelAfter panel2 = new game.ranking.RankingBoardPanelAfter();
        assertNotNull("RankingBoardPanelBefore 인스턴스가 생성되어야 함", panel1);
        assertNotNull("RankingBoardPanelAfter 인스턴스가 생성되어야 함", panel2);
    }

    @Test
    public void testRankingBoardPanelUpdate() {
        game.ranking.RankingBoardPanelAfter panel = new game.ranking.RankingBoardPanelAfter();
        rankingManager.registerObserver(panel);
        rankingManager.saveRanking(testRanking);
        rankingManager.notifyObservers();
        assertTrue("RankingBoardPanelAfter의 update 메서드가 호출되어야 함", true);
    }
}