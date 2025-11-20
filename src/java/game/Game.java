package game;

import game.entities.*;
import game.entities.levelStrategies.Level1Strategy;
import game.entities.levelStrategies.Level2Strategy;
import game.entities.levelStrategies.Level3Strategy;
import game.entities.levelStrategies.LevelStrategy;
import game.entities.superPacGums.*;
import game.entities.ghostDecorator.*;
import game.entities.pacmanDecorator.*;
import game.entities.ghosts.Blinky;
import game.entities.ghosts.Ghost;
import game.ghostFactory.*;
import game.ghostStates.EatenMode;
import game.ghostStates.FrightenedMode;
import game.utils.CollisionDetector;
import game.utils.CsvReader;
import game.utils.KeyHandler;

import java.awt.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

//Classe gérant le jeu en lui même
public class Game implements Observer {
    //Pour lister les différentes entités présentes sur la fenêtre
    private List<Entity> objects = new ArrayList();
    private List<Ghost> ghosts = new ArrayList();
    private static List<Wall> walls = new ArrayList();

    private static Pacman pacman;
    private static Blinky blinky;

    private static boolean firstInput = false;

    private static int gameLevel = 3; // 테스트용 하드코딩 (1, 2, 3)
    private static LevelStrategy levelStrategy;

    public Game(){
        //Initialisation du jeu

        // 레벨 Strategy 초기화 (Strategy 패턴)
        initializeLevelStrategy();

        //Chargement du fichier csv du niveau
        List<List<String>> data = null;
        try {
            data = new CsvReader().parseCsv(getClass().getClassLoader().getResource("level/level.csv").toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        int cellsPerRow = data.get(0).size();
        int cellsPerColumn = data.size();
        int cellSize = 8;

        CollisionDetector collisionDetector = new CollisionDetector(this);
        AbstractGhostFactory abstractGhostFactory = null;

        //Le niveau a une "grille", et pour chaque case du fichier csv, on affiche une entité parculière sur une case de la grille selon le caracère présent
        for(int xx = 0 ; xx < cellsPerRow ; xx++) {
            for(int yy = 0 ; yy < cellsPerColumn ; yy++) {
                String dataChar = data.get(yy).get(xx);
                if (dataChar.equals("x")) { //Création des murs
                    objects.add(new Wall(xx * cellSize, yy * cellSize));
                }else if (dataChar.equals("P")) { //Création de Pacman
                    pacman = new Pacman(xx * cellSize, yy * cellSize);
                    pacman.setCollisionDetector(collisionDetector);

                    //Enregistrement des différents observers de Pacman
                    pacman.registerObserver(GameLauncher.getUIPanel());
                    pacman.registerObserver(this);

                    pacman = new SheildPacmanDecorator(pacman, collisionDetector);
                    pacman = new FastPacmanDecorator(pacman, 3);

                }else if (dataChar.equals("b") || dataChar.equals("p") || dataChar.equals("i") || dataChar.equals("c")) { //Création des fantômes en utilisant les différentes factories
                    switch (dataChar) {
                        case "b":
                            abstractGhostFactory = new BlinkyFactory();
                            break;
                        case "p":
                            abstractGhostFactory = new PinkyFactory();
                            break;
                        case "i":
                            abstractGhostFactory = new InkyFactory();
                            break;
                        case "c":
                            abstractGhostFactory = new ClydeFactory();
                            break;
                    }

                    Ghost ghost = abstractGhostFactory.makeGhost(xx * cellSize, yy * cellSize);
                    if (dataChar.equals("b")) {
                        blinky = (Blinky) ghost;
                    }

                    // 레벨별 Decorator 적용
                    ghost = applyGhostDecorators(ghost);

                    ghost = new SlowGhostDecorator(ghost, 2);
                    ghosts.add(ghost);
                }else if (dataChar.equals(".")) { //Création des PacGums
                    objects.add(new PacGum(xx * cellSize, yy * cellSize));
                }else if (dataChar.equals("o")) { //Création des SuperPacGums
                    int rand = (int)(Math.random() * 4); // 0~3 난수 생성
                    int px = xx * cellSize;
                    int py = yy * cellSize;
                    switch (rand) {
                        case 0:
                            objects.add(new FrightenedGhostSuperPacGum(px, py));
                            break;
                        case 1:
                            objects.add(new SlowGhostSuperPacGum(px, py));
                            break;
                        case 2:
                            objects.add(new SheildPacmanSuperPacGum(px, py));
                            break;
                        case 3:
                            objects.add(new FastPacmanSuperPacGum(px, py));
                            break;
                    }
                }else if (dataChar.equals("-")) { //Création des murs de la maison des fantômes
                    objects.add(new GhostHouse(xx * cellSize, yy * cellSize));
                }
            }
        }
        objects.add(pacman);
        objects.addAll(ghosts);

        for (Entity o : objects) {
            if (o instanceof Wall) {
                walls.add((Wall) o);
            }
        }
    }

    /**
     * 레벨 Strategy 초기화 (Strategy 패턴)
     *
     * gameLevel 값에 따라 적절한 LevelStrategy 구현체 생성
     * 각 Strategy는 레벨별 순간이동/투명화 규칙과 속도/해산시간 설정 정의
     */
    private void initializeLevelStrategy() {
        switch (gameLevel) {
            case 1:
                levelStrategy = new Level1Strategy();
                break;
            case 2:
                levelStrategy = new Level2Strategy();
                break;
            case 3:
                levelStrategy = new Level3Strategy();
                break;
            default:
                levelStrategy = new Level1Strategy();
        }

        // 콘솔에 현재 레벨 출력
        System.out.println("🎮 Game Started: " + levelStrategy.getLevelName());
        System.out.println("   ├─ 속도 증가율: " + (int)(levelStrategy.getSpeedIncreaseRate() * 100) + "%");
        System.out.println("   └─ 해산시간 감소율: " + (int)(levelStrategy.getFrightenedTimerReduction() * 100) + "%");
    }

    /**
     * 고스트에 Decorator 적용 (Decorator 패턴 + Strategy 패턴)
     *
     * 레벨 Strategy에 따라 적절한 Decorator 조합 적용
     * - 모든 레벨: SpeedBoostGhostDecorator (속도 증가, 레벨별 다른 증가율)
     * - 모든 레벨: SlowGhostDecorator (기본)
     * - 레벨 2+: TeleportGhostDecorator (순간이동)
     * - 레벨 3: InvisibleGhostDecorator (투명화)
     *
     * Decorator 적용 순서:
     * 1. SpeedBoostGhostDecorator (가장 안쪽 - 기본 속도 변경)
     * 2. SlowGhostDecorator (중간 - 아이템 효과)
     * 3. TeleportGhostDecorator (바깥 - 순간이동)
     * 4. InvisibleGhostDecorator (가장 바깥 - 투명화)
     *
     * @param ghost 원본 고스트
     * @return Decorator가 적용된 고스트
     */
    private Ghost applyGhostDecorators(Ghost ghost) {
        // 속도 증가 Decorator (모든 레벨에 적용, 레벨별 다른 증가율)
        ghost = new SpeedBoostGhostDecorator(ghost, levelStrategy);

        // 기본 Decorator: 속도 감소
        ghost = new SlowGhostDecorator(ghost, 2);

        // 순간이동 Decorator (레벨 2 이상)
        if (levelStrategy.getTeleportInterval() > 0) {
            ghost = new TeleportGhostDecorator(ghost, levelStrategy);
        }

        // 투명화 Decorator (레벨 3)
        if (levelStrategy.getInvisibleInterval() > 0) {
            ghost = new InvisibleGhostDecorator(ghost, levelStrategy);
        }

        return ghost;
    }

    // 레벨 설정 (나중에 시작 패널에서 호출)
    public static void setGameLevel(int level) {
        gameLevel = level;
    }

    public static int getGameLevel() {
        return gameLevel;
    }

    public static LevelStrategy getLevelStrategy() {
        return levelStrategy;
    }

    public static List<Wall> getWalls() {
        return walls;
    }

    public List<Entity> getEntities() {
        return objects;
    }

    //Mise à jour de toutes les entités
    public void update() {
        for (Entity o: objects) {
            if (!o.isDestroyed()) o.update();
        }
    }

    //Gestion des inputs
    public void input(KeyHandler k) {
        pacman.input(k);
    }

    //Rendu de toutes les entités
    public void render(Graphics2D g) {
        for (Entity o: objects) {
            if (!o.isDestroyed()) o.render(g);
        }
    }

    public static Pacman getPacman() {
        return pacman;
    }
    public static Blinky getBlinky() {
        return blinky;
    }

    //Le jeu est notifiée lorsque Pacman est en contact avec une PacGum, une SuperPacGum ou un fantôme
    @Override
    public void updatePacGumEaten(PacGum pg) {
        pg.destroy(); //La PacGum est détruite quand Pacman la mange
    }

    @Override
    public void updateSuperPacGumEaten(SuperPacGum spg) {
        spg.destroy(); //La SuperPacGum est détruite quand Pacman la mange
        if(spg instanceof GhostSuperPacGum){
            for (Ghost gh : ghosts) {
                gh.superPacGumEaten(spg);
            }
        }
        else if(spg instanceof PacmanSuperPacGum){
            pacman.superPacGumEaten(spg);
        }
    }

    @Override
    public void updateGhostCollision(Ghost gh) {
        if (gh.getState() instanceof FrightenedMode) {
            gh.getState().eaten(); //S'il existe une transition particulière quand le fantôme est mangé, son état change en conséquence
        }else if (!(gh.getState() instanceof EatenMode)) {
            System.out.println("Game over !\nScore : " + GameLauncher.getUIPanel().getScore()); //Quand Pacman rentre en contact avec un Fantôme qui n'est ni effrayé, ni mangé, c'est game over !
            System.exit(0); //TODO
        }
    }

    public static void setFirstInput(boolean b) {
        firstInput = b;
    }

    public static boolean getFirstInput() {
        return firstInput;
    }
}