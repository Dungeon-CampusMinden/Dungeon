package starter;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static logging.LoggerConfig.initBaseLogger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import configuration.Configuration;
import configuration.KeyboardConfig;
import controller.AbstractController;
import controller.SystemController;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.entities.Teleportsystem;
import ecs.entities.monsters.Chort;
import ecs.entities.monsters.Goblin;
import ecs.entities.monsters.LittleChort;
import ecs.systems.*;
import graphic.DungeonCamera;
import graphic.Painter;
import graphic.hud.PauseMenu;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import level.IOnLevelLoader;
import level.LevelAPI;
import level.elements.ILevel;
import level.elements.tile.Tile;
import level.generator.IGenerator;
import level.generator.postGeneration.WallGenerator;
import level.generator.randomwalk.RandomWalkGenerator;
import level.tools.LevelSize;
import tools.Constants;
import tools.Point;

/**
 * The heart of the framework. From here all strings are pulled.
 */
public class Game extends ScreenAdapter implements IOnLevelLoader {

    private final LevelSize LEVELSIZE = LevelSize.SMALL;

    /**
     * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
     * batch.
     */
    protected SpriteBatch batch;

    /**
     * Contains all Controller of the Dungeon
     */
    protected List<AbstractController<?>> controller;

    public static DungeonCamera camera;
    /**
     * Draws objects
     */
    protected Painter painter;

    protected LevelAPI levelAPI;
    /**
     * Generates the level
     */
    protected IGenerator generator;

    private boolean doSetup = true;
    private static boolean paused = false;

    /**
     * All entities that are currently active in the dungeon
     */
    private static final Set<Entity> entities = new HashSet<>();
    /**
     * All entities to be removed from the dungeon in the next frame
     */
    private static final Set<Entity> entitiesToRemove = new HashSet<>();
    /**
     * All entities to be added from the dungeon in the next frame
     */
    private static final Set<Entity> entitiesToAdd = new HashSet<>();

    /**
     * List of all Systems in the ECS
     */
    public static SystemController systems;

    public static ILevel currentLevel;
    private static PauseMenu<Actor> pauseMenu;
    private static Entity hero;



    private int currentDepth = 1;
    public Teleportsystem teleportsystem = new Teleportsystem();

    private Logger gameLogger;

    public static void main(String[] args) {
        // start the game
        try {
            Configuration.loadAndGetConfiguration("dungeon_config.json", KeyboardConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        DesktopLauncher.run(new Game());
    }

    /**
     * Main game loop. Redraws the dungeon and calls the own implementation (beginFrame, endFrame
     * and onLevelLoad).
     *
     * @param delta Time since last loop.
     */
    @Override
    public void render(float delta) {
        if (doSetup) setup();
        batch.setProjectionMatrix(camera.combined);
        frame();
        clearScreen();
        levelAPI.update();
        controller.forEach(AbstractController::update);
        camera.update();
    }

    /**
     * Called once at the beginning of the game.
     */
    protected void setup() {
        doSetup = false;
        controller = new ArrayList<>();
        setupCameras();
        painter = new Painter(batch, camera);
        generator = new RandomWalkGenerator();
        levelAPI = new LevelAPI(batch, painter, generator, this);
        initBaseLogger();
        gameLogger = Logger.getLogger(this.getClass().getName());
        systems = new SystemController();
        controller.add(systems);
        pauseMenu = new PauseMenu<>();
        controller.add(pauseMenu);
        hero = new Hero();
        levelAPI = new LevelAPI(batch, painter, new WallGenerator(new RandomWalkGenerator()), this);
        levelAPI.loadLevel(LEVELSIZE);
        createSystems();
    }

    /**
     * Called at the beginning of each frame. Before the controllers call <code>update</code>.
     */
    protected void frame() {
        setCameraFocus();
        manageEntitiesSets();
        getHero().ifPresent(this::loadNextLevelIfEntityIsOnEndTile);
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) togglePause();
        teleportsystem.updateTeleportSystem();  //telportsystem updating counting//

    }

    @Override
    public void onLevelLoad() {
        currentLevel = levelAPI.getCurrentLevel();
        entities.clear();
        getHero().ifPresent(this::placeOnLevelStart);
        teleportsystem.makePads();             // bei jeden neuen level aufrufen der Methode//

        // Erstellen und platzieren Sie eine zufällige Anzahl von Monstern basierend auf der aktuellen Tiefe
        int numberOfMonsters = calculateNumberOfMonsters();
        for (int i = 0; i < numberOfMonsters; i++) {
            Entity monster = createRandomMonster(currentDepth);
            placeOnLevel(monster);
        }

        // Inkrementiere die Tiefe des Dungeons, nachdem das Level geladen wurde
        currentDepth++;
    }

        /**
         * Berechnet die Gesamtzahl der Monster, die in der aktuellen Ebene erstellt werden sollen.
         * Die Anzahl der schwachen und starken Monster wird basierend auf der aktuellen Tiefe berechnet.
         * Wenn starke Monster generiert werden sollen, wird eine zufällige Anzahl von ihnen zur Gesamtanzahl der Monster hinzugefügt.
         * @return Die Gesamtzahl der Monster, die in der aktuellen Ebene erstellt werden sollen.
         */
        private int calculateNumberOfMonsters() {

            int numMonsters = currentDepth + 1;
            int numStrongMonsters = 0;

            for (int i = 2; i <= currentDepth; i++) {
                numMonsters += i + 1;
                numStrongMonsters += i;
            }

            Random random = new Random();
            boolean strongMonsters = random.nextBoolean();

            if (strongMonsters && numStrongMonsters > 0) {
                int strongMonsterCount = random.nextInt(numStrongMonsters) + 1;
                numMonsters += strongMonsterCount;
            }

            return numMonsters;
        }


        /**
         * Erstellt eine zufällige Monsterentität basierend auf einer zufällig generierten Zahl.
         * Es gibt drei Arten von Monstern zur Auswahl: Chort, Goblin und LittleChort.
         * Jeder Monster-Typ hat eine spezielle Konfiguration, die von der aktuellen Tiefe abhängt.
         * Wenn die aktuelle Tiefe größer als 3 ist, wird eine stärkere Version des Chorts mit höheren Geschwindigkeiten und mehr Gesundheitspunkten erstellt.
         * @param depth Die aktuelle Tiefeebene des Spiels.
         * @return Eine zufällig generierte Monsterentität.
         * @throws IllegalStateException Wenn die zufällig generierte Zahl keinen gültigen Monster-Typ ergibt.
         */
        private Entity createRandomMonster(int depth) {
            int monsterType = new Random().nextInt(3);
            Entity monster;
            monsterType = 2;
            switch (monsterType) {
               /* case 0:
                    monster = new Chort(0.1f, 0.1f, 10);
                    if(currentDepth > 3) {
                        monster = new Chort(0.3f, 0.3f, 15);
                    }
                    break;
                case 1:
                    //monster = new Goblin();
                    break;
                    +/
                */
                case 2:
                    monster = new LittleChort();
                    break;
                default:
                    throw new IllegalStateException("Unerwarteter Wert: " + monsterType);
            }

            return monster;
        }



    private void manageEntitiesSets() {
        entities.removeAll(entitiesToRemove);
        entities.addAll(entitiesToAdd);
        for (Entity entity : entitiesToRemove) {
            gameLogger.info("Entity '" + entity.getClass().getSimpleName() + "' was deleted.");
        }
        for (Entity entity : entitiesToAdd) {
            gameLogger.info("Entity '" + entity.getClass().getSimpleName() + "' was added.");
        }
        entitiesToRemove.clear();
        entitiesToAdd.clear();
    }

    private void setCameraFocus() {
        if (getHero().isPresent()) {
            PositionComponent pc =
                (PositionComponent)
                    getHero()
                        .get()
                        .getComponent(PositionComponent.class)
                        .orElseThrow(
                            () ->
                                new MissingComponentException(
                                    "PositionComponent"));
            camera.setFocusPoint(pc.getPosition());

        } else camera.setFocusPoint(new Point(0, 0));
    }

    private void loadNextLevelIfEntityIsOnEndTile(Entity hero) {
        if (isOnEndTile(hero)) levelAPI.loadLevel(LEVELSIZE);
    }

    private boolean isOnEndTile(Entity entity) {
        PositionComponent pc =
            (PositionComponent)
                entity.getComponent(PositionComponent.class)
                    .orElseThrow(
                        () -> new MissingComponentException("PositionComponent"));
        Tile currentTile = currentLevel.getTileAt(pc.getPosition().toCoordinate());
        return currentTile.equals(currentLevel.getEndTile());
    }

    private void placeOnLevelStart(Entity hero) {
        entities.add(hero);
        PositionComponent pc =
            (PositionComponent)
                hero.getComponent(PositionComponent.class)
                    .orElseThrow(
                        () -> new MissingComponentException("PositionComponent"));
        pc.setPosition(currentLevel.getStartTile().getCoordinate().toPoint());
    }

    private void placeOnLevel(Entity entity) {
        entities.add(entity);
        PositionComponent pc =
            (PositionComponent)
                entity.getComponent(PositionComponent.class)
                    .orElseThrow(
                        () -> new MissingComponentException("PositionComponent"));
        pc.setPosition(currentLevel.getRandomFloorTile().getCoordinate().toPoint());
    }




    /**
     * Toggle between pause and run
     */
    public static void togglePause() {
        paused = !paused;
        if (systems != null) {
            systems.forEach(ECS_System::toggleRun);
        }
        if (pauseMenu != null) {
            if (paused) pauseMenu.showMenu();
            else pauseMenu.hideMenu();
        }
    }

    /**
     * Given entity will be added to the game in the next frame
     *
     * @param entity will be added to the game next frame
     */
    public static void addEntity(Entity entity) {
        entitiesToAdd.add(entity);
    }

    /**
     * Given entity will be removed from the game in the next frame
     *
     * @param entity will be removed from the game next frame
     */
    public static void removeEntity(Entity entity) {
        entitiesToRemove.add(entity);
    }

    /**
     * @return Set with all entities currently in game
     */
    public static Set<Entity> getEntities() {
        return entities;
    }

    /**
     * @return Set with all entities that will be added to the game next frame
     */
    public static Set<Entity> getEntitiesToAdd() {
        return entitiesToAdd;
    }

    /**
     * @return Set with all entities that will be removed from the game next frame
     */
    public static Set<Entity> getEntitiesToRemove() {
        return entitiesToRemove;
    }

    /**
     * @return the player character, can be null if not initialized
     */
    public static Optional<Entity> getHero() {
        return Optional.ofNullable(hero);
    }



    /**
     * set the reference of the playable character careful: old hero will not be removed from the
     * game
     *
     * @param hero new reference of hero
     */
    public static void setHero(Entity hero) {
        Game.hero = hero;
    }

    public void setSpriteBatch(SpriteBatch batch) {
        this.batch = batch;
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
    }

    private void setupCameras() {
        camera = new DungeonCamera(null, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        camera.zoom = Constants.DEFAULT_ZOOM_FACTOR;

        // See also:
        // https://stackoverflow.com/questions/52011592/libgdx-set-ortho-camera
    }

    private void createSystems() {
        new VelocitySystem();
        new DrawSystem(painter);
        new PlayerSystem();
        new AISystem();
        new CollisionSystem();
        new HealthSystem();
        new XPSystem();
        new SkillSystem();
        new ProjectileSystem();
    }
}
