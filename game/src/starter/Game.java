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
import ecs.components.InventoryComponent;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.entities.*;
import ecs.entities.Entity;
import ecs.entities.FriendlyGhost;
import ecs.entities.Hero;
import ecs.entities.Monsters.Demon;
import ecs.entities.Monsters.Imp;
import ecs.entities.Monsters.Slime;
import ecs.graphic.DungeonCamera;
import ecs.graphic.Painter;
import ecs.graphic.hud.*;
import ecs.items.ItemData;
import ecs.items.ItemType;
import ecs.items.newItems.Bag;
import ecs.items.newItems.BookOfRa;
import ecs.items.newItems.Greatsword;
import ecs.items.newItems.InvinciblePotion;
import ecs.systems.*;
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

/** The heart of the framework. From here all strings are pulled. */
public class Game extends ScreenAdapter implements IOnLevelLoader {

    private LevelSize levelSize = LevelSize.SMALL;

    /**
     * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
     * batch.
     */
    protected SpriteBatch batch;

    /** Contains all Controller of the Dungeon */
    protected List<AbstractController<?>> controller;

    public static DungeonCamera camera;
    /** Draws objects */
    protected Painter painter;

    protected LevelAPI levelAPI;
    /** Generates the level */
    protected IGenerator generator;

    private boolean doSetup = true;
    private static boolean paused = false;

    /** All entities that are currently active in the dungeon */
    private static final Set<Entity> entities = new HashSet<>();
    /** All entities to be removed from the dungeon in the next frame */
    private static final Set<Entity> entitiesToRemove = new HashSet<>();
    /** All entities to be added from the dungeon in the next frame */
    private static final Set<Entity> entitiesToAdd = new HashSet<>();

    /** List of all Systems in the ECS */
    public static SystemController systems;

    public static ILevel currentLevel;
    private static PauseMenu<Actor> pauseMenu;

    // new
    private static InventoryHUD<Actor> inventoryHUD;
    private static GameOverHUD<Actor> gameOverHUD;

    private static boolean inventoryOpen = false;

    private static Entity hero;
    private static Hero playHero;
    private Logger gameLogger;

    private int currentLvl;

    private FriendlyGhost friendlyGhost;

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

    /** Called once at the beginning of the game. */
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

        // new
        gameOverHUD = new GameOverHUD<>();
        controller.add(gameOverHUD);
        inventoryHUD = new InventoryHUD<>();
        controller.add(inventoryHUD);
        pauseMenu = new PauseMenu<>();
        controller.add(pauseMenu);
        playHero = new Hero();
        hero = playHero;
        levelAPI = new LevelAPI(batch, painter, new WallGenerator(new RandomWalkGenerator()), this);
        levelAPI.loadLevel(levelSize);
        createSystems();
    }

    /** Called at the beginning of each frame. Before the controllers call <code>update</code>. */
    protected void frame() {
        setCameraFocus();
        manageEntitiesSets();
        getHero().ifPresent(this::loadNextLevelIfEntityIsOnEndTile);
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) togglePause();
        if (Hero.isDead()){
            openGameOver();
        };
    }

    @Override
    public void onLevelLoad() {
        currentLevel = levelAPI.getCurrentLevel();
        entities.clear();
        getHero().ifPresent(this::placeOnLevelStart);
        loadGhost();
        spawnMonster();
        spawnItems();
        new Mine();
        new BearTrap();
        currentLvl++;
        bookCheck();
        System.out.println("Current Level:" + currentLvl);

        // Test
        Hero hero1 = (Hero) Game.hero;
        hero1.getXpCmp().addXP(hero1.getXpCmp().getXPToNextLevel());
        System.out.println(
                hero1.getXpCmp().getCurrentLevel()
                        + ". level with "
                        + hero1.getXpCmp().getCurrentXP()
                        + " XP.");
    }

    /** Spawn ghost, there is a 10% chance it doesn't spawn */
    private void loadGhost() {
        Random random = new Random();
        if (random.nextInt(0, 100) > 10) friendlyGhost = new FriendlyGhost(playHero);
    }

    private void setLevelSize(int currentLvl) {
        if (currentLvl >= 5) levelSize = LevelSize.MEDIUM;
        if (currentLvl >= 10) levelSize = LevelSize.LARGE;
    }

    /** Chance of Randomly spawn a Item */
    private void spawnItems() {
        int random = (int) (Math.random() * (0 - 100));
        if (random < 0) {
            new Bag(ItemType.Active);
            new Greatsword();
            new BookOfRa();
            new InvinciblePotion();
        }
    }

    /** Spawns monster in relation to current level progress */
    private void spawnMonster() {
        Random random = new Random();

        int monster = 0;

        for (int i = 0; i < ((currentLvl) + 1); i++) {

            int rng = random.nextInt(0, 3);

            if (rng == 0) new Imp(currentLvl);
            if (rng == 1) new Demon(currentLvl);
            if (rng == 2) new Slime(currentLvl);

            monster++;
        }

        System.out.println("Amount of monsters: " + monster);
    }

    public void bookCheck() {
        Hero worker = (Hero) hero;
        InventoryComponent inv = worker.getInv();
        BookOfRa books;

        for (ItemData item : inv.getItems()) {

            // Check for book bags
            if (item instanceof Bag) {
                for (ItemData book : ((Bag) item).getItems()) {
                    books = (BookOfRa) book;
                    books.grantXP();
                }
            }

            // Check for books
            if (item instanceof BookOfRa) {
                ((BookOfRa) item).grantXP();
            }
        }
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
        if (isOnEndTile(hero)) {
            setLevelSize(currentLvl);
            levelAPI.loadLevel(levelSize);
        }
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

    /** Toggle between pause and run */
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

    /** Open Inventory */
    public static void openInventory() {
        inventoryOpen = !inventoryOpen;
        if (inventoryHUD != null) {
            if (inventoryOpen) inventoryHUD.showMenu();
            else inventoryHUD.hideMenu();
        }
    }

    public static void openGameOver(){
        systems.forEach(ECS_System::toggleRun);
        gameOverHUD.showMenu();

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
