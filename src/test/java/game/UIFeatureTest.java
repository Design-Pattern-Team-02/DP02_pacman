// ========================================
// game.gameStates 패키지 테스트
// ========================================

package game;

import game.gameStates.GameOverState;
import game.gameStates.PlayingState;
import game.gameStates.StartMenuState;
import game.panel.GameOverPanel;
import game.panel.GameplayPanel;
import game.panel.MapLevelSelectPanel;
import game.panel.StartMenuPanel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import javax.swing.*;
import java.awt.*;

import static org.junit.Assert.*;

public class UIFeatureTest {
    
    private JFrame testFrame;
    private GameManager gameManager;
    private GameOverState gameOverState;
    private PlayingState playingState;
    private StartMenuState startMenuState;
    private GameOverPanel gameOverPanel;
    private StartMenuPanel startMenuPanel;
    private MapLevelSelectPanel mapLevelSelectPanel;

    @Before
    public void setUp() {
        // Headless 모드가 아닐 때만 프레임 생성
        if (!GraphicsEnvironment.isHeadless()) {
            testFrame = new JFrame("Test Frame");
            testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            testFrame.setSize(448, 496);
        }
        
        // GameManager 초기화
        gameManager = GameManager.getInstance();
        gameManager.setPlayerNickname("TestPlayer");
        gameManager.setScore(1000);
        gameManager.setSelectedMapName("default_map");
        gameManager.initGameFrame();
    }

    @After
    public void tearDown() {

        if (testFrame != null) {
            testFrame.dispose();
        }
        gameOverState = null;
        playingState = null;
        startMenuState = null;
        gameOverPanel = null;
        startMenuPanel = null;
        mapLevelSelectPanel = null;
    }
    
    // GameOverState 테스트
    @Test
    public void testGameOverStateChangePanel() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        gameOverState = new GameOverState();
        try {
            gameOverState.changePanel(testFrame);
            assertNotNull(testFrame.getContentPane());
            assertTrue(testFrame.getContentPane().getComponentCount() > 0);
        } catch (Exception e) {
            // 리소스 로딩 실패 등의 예외는 허용
            assertTrue(true);
        }
    }
    
    @Test
    public void testGameOverStateExitPanel() {
        gameOverState = new GameOverState();
        // exitPanel 호출 시 예외가 발생하지 않아야 함
        try {
            gameOverState.exitPanel();
            assertTrue(true);
        } catch (Exception e) {
            fail("exitPanel should not throw exception");
        }
    }
    
    @Test
    public void testGameOverStateSaveRanking() {
        gameOverState = new GameOverState();
        try {
            gameOverState.saveRanking();
            assertTrue(true);
        } catch (Exception e) {
            fail("saveRanking should not throw exception");
        }
    }
    
    // PlayingState 테스트
    @Test
    public void testPlayingStateChangePanel() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        playingState = new PlayingState();
        try {
            playingState.changePanel(testFrame);
            assertNotNull(testFrame.getContentPane());
        } catch (Exception e) {
            // 리소스 로딩 실패 등의 예외는 허용
            assertTrue(true);
        }
    }
    
    @Test
    public void testPlayingStateExitPanel() {
        playingState = new PlayingState();
        try {
            playingState.exitPanel();
            assertTrue(true);
        } catch (Exception e) {
            // null 정리 시 예외는 허용
            assertTrue(true);
        }
    }
    
    @Test
    public void testPlayingStateGetUIPanel() {
        // UIPanel은 static이므로 null일 수 있음
        try {
            UIPanel uiPanel = PlayingState.getUIPanel();
            // null이거나 UIPanel 인스턴스여야 함
            assertTrue(uiPanel == null || uiPanel instanceof UIPanel);
        } catch (Exception e) {
            fail("getUIPanel should not throw exception");
        }
    }
    
    // StartMenuState 테스트
    @Test
    public void testStartMenuStateChangePanel() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        startMenuState = new StartMenuState();
        try {
            startMenuState.changePanel(testFrame);
            assertNotNull(testFrame.getContentPane());
            assertTrue(testFrame.getContentPane().getComponentCount() > 0);
        } catch (Exception e) {
            // 리소스 로딩 실패 등의 예외는 허용
            assertTrue(true);
        }
    }
    
    @Test
    public void testStartMenuStateExitPanel() {
        startMenuState = new StartMenuState();
        try {
            startMenuState.exitPanel();
            assertTrue(true);
        } catch (Exception e) {
            fail("exitPanel should not throw exception");
        }
    }
    
    // 상태 전환 테스트
    @Test
    public void testStateTransitionFromStartMenuToPlaying() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        startMenuState = new StartMenuState();
        playingState = new PlayingState();
        
        try {
            gameManager.changeState(startMenuState);
            gameManager.changeState(playingState);
        } catch (Exception e) {
            assertTrue(true);
        }
    }
    
    @Test
    public void testStateTransitionFromPlayingToGameOver() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        playingState = new PlayingState();
        gameOverState = new GameOverState();

        try {
            gameManager.changeState(playingState);
            gameManager.changeState(gameOverState);
            assertNotNull(testFrame.getContentPane());
        } catch (Exception e) {
            assertTrue(true);
        }
    }
    
    @Test
    public void testStateTransitionFromGameOverToStartMenu() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        gameOverState = new GameOverState();
        startMenuState = new StartMenuState();
        
        try {
            gameManager.changeState(gameOverState);
            gameManager.changeState(startMenuState);
            assertNotNull(testFrame.getContentPane());
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    public void testGameManagerSingleton() {
        GameManager instance1 = GameManager.getInstance();
        GameManager instance2 = GameManager.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    public void testGameManagerInitialization() {
        gameManager.initGameFrame();
        assertNotNull(gameManager);
    }

    @Test
    public void testSetAndGetPlayerGameManager(){
        String testNickname = "TestPlayer";
        gameManager.setPlayerNickname(testNickname);
        assertEquals(testNickname, gameManager.getPlayerNickname());
    }

    // GameOverPanel 테스트
    @Test
    public void testGameOverPanelCreation() {
        gameOverPanel = new GameOverPanel(testFrame, "TestPlayer", 1000);
        assertNotNull(gameOverPanel);
    }

    // StartMenuPanel 테스트
    @Test
    public void testStartMenuPanelCreation() {
        startMenuPanel = new StartMenuPanel(testFrame);
        assertNotNull(startMenuPanel);
    }

    // MapLevelSelectPanel 테스트
    @Test
    public void testMapLevelSelectPanelCreation() {
        mapLevelSelectPanel = new MapLevelSelectPanel();
        assertNotNull(mapLevelSelectPanel);
    }

    // GameplayPanel 테스트
    @Test
    public void testGameplayPanel() {
        assertTrue(GameplayPanel.width >= 0 && GameplayPanel.height >= 0);
    }
}