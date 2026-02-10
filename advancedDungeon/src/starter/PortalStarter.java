package starter;

import com.badlogic.gdx.Input;
import contrib.components.*;
import contrib.entities.EntityFactory;
import contrib.hud.DialogUtils;
import contrib.systems.*;
import contrib.utils.DynamicCompiler;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.components.*;
import core.game.WindowEventManager;
import core.level.elements.ILevel;
import core.level.loader.DungeonLoader;
import core.systems.CameraSystem;
import core.utils.*;
import core.utils.components.MissingComponentException;
import core.utils.components.path.SimpleIPath;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import portal.antiMaterialBarrier.AntiMaterialBarrierSystem;
import portal.controlls.Hero;
import portal.controlls.PlayerController;
import portal.laserGrid.LasergridSystem;
import portal.level.*;
import portal.portals.PortalColor;
import portal.portals.PortalExtendSystem;
import portal.portals.PortalSkill;
import portal.portals.abstraction.PortalConfig;

/**
 * Starter for the Portal Dungeon.
 *
 * <p>Usage: run with the Gradle task {@code runPortal}.
 */
public class PortalStarter {

  /**
   * Activate this to enable Debug mode.
   *
   * <p>Creates a classic Hero with standard controls; no custom implementation needed.
   *
   * <p>Activates the {@link Debugger}.
   *
   * <p>Also disables recompilation for player control.
   */
  public static final boolean DEBUG_MODE = false;

  private static final boolean LEVELEDITOR_MODE = true;

  private static final String SAVE_LEVEL_KEY = "LEVEL";
  private static final String SAVE_FILE = "currentPortalLevel.json";
  private static final SimpleIPath PORTAL_CONFIG_PATH =
      new SimpleIPath("advancedDungeon/src/portal/riddles/MyPortalConfig.java");
  private static final String CONFIG_CLASSNAME = "portal.riddles.MyPortalConfig";
  private static final float ZOOM = .3f;
  private static final int FPS = 30;
  private static final int WIDTH = 640;
  private static final int HEIGHT = 480;

  /** Global reference to the {@link Hero} instance used in the game. */
  public static Hero hero;

  /** If true, the {@link MyPlayerController} will not be recompiled. */
  private static boolean recompilePaused = false;

  private static final String ERROR_MSG_CONTROLLER =
      "Da scheint etwas mit meinem Steuerrungscode nicht zu stimmen.";

  /** Path to the Java source file of the custom player controller. */
  private static final SimpleIPath PLAYER_CONTROLLER_PATH =
      new SimpleIPath("advancedDungeon/src/portal/riddles/MyPlayerController.java");

  /** Fully qualified class name of the custom player controller. */
  private static final String CONTROLLER_CLASSNAME = "portal.riddles.MyPlayerController";

  private static final Consumer<Entity> DEATH_CALLBACK =
      (hero) ->
          DialogUtils.showTextPopup(
              "Du bist gestorben.",
              "Game Over",
              () -> {
                // Just respawn at Start Tile instead of reloading the level
                hero.fetch(PositionComponent.class)
                    .ifPresent(
                        pc -> {
                          pc.position(Game.currentLevel().flatMap(ILevel::startTile).orElseThrow());
                          pc.viewDirection(Direction.DOWN);
                          PositionSync.syncPosition(hero);
                        });

                hero.fetch(VelocityComponent.class)
                    .ifPresent(
                        vc -> {
                          vc.clearForces();
                          vc.currentVelocity(Vector2.ZERO);
                        });

                hero.fetch(HealthComponent.class)
                    .ifPresent(
                        hc -> {
                          hc.currentHealthpoints(hc.maximalHealthpoints());
                          hc.clearDamage();
                          hc.alreadyDead(false);
                        });

                hero.fetch(ManaComponent.class).ifPresent(hc -> hc.currentAmount(hc.maxAmount()));
                hero.fetch(StaminaComponent.class)
                    .ifPresent(hc -> hc.currentAmount(hc.maxAmount()));

                // reset inventory
                hero.fetch(CharacterClassComponent.class)
                    .ifPresent(
                        characterClassComponent -> {
                          InventoryComponent invComp =
                              new InventoryComponent(
                                  characterClassComponent.characterClass().inventorySize());
                          characterClassComponent
                              .characterClass()
                              .startItems()
                              .forEach(invComp::add);
                          hero.add(invComp);
                        });

                // reset the animation queue
                hero.fetch(DrawComponent.class).ifPresent(DrawComponent::resetState);

                DungeonLoader.reloadCurrentLevel();
              });

  /**
   * Attempts to dynamically recompile and load the custom player controller class.
   *
   * <p>If compilation is successful, the player's controller is updated at runtime. Otherwise, a
   * dialog is shown to indicate an error.
   */
  private static void recompilePlayerControl() {
    if (recompilePaused) return;
    try {

      // Player Controller
      Object o =
          DynamicCompiler.loadUserInstance(
              PLAYER_CONTROLLER_PATH, CONTROLLER_CLASSNAME, new Tuple<>(Hero.class, hero));
      hero.setController((PlayerController) o);

      // Portal Config

      o =
          DynamicCompiler.loadUserInstance(
              PORTAL_CONFIG_PATH, CONFIG_CLASSNAME, new Tuple<>(Hero.class, hero));
      SkillComponent sc =
          hero.hero()
              .fetch(SkillComponent.class)
              .orElseThrow(
                  () -> MissingComponentException.build(hero.hero(), SkillComponent.class));
      sc.removeAll();
      sc.addSkill(new PortalSkill(PortalColor.GREEN, ((PortalConfig) o)));
      sc.addSkill(new PortalSkill(PortalColor.BLUE, ((PortalConfig) o)));

    } catch (Exception e) {
      recompilePaused = true;
      if (DEBUG_MODE) e.printStackTrace();
      DialogUtils.showTextPopup(ERROR_MSG_CONTROLLER, "Code Error", () -> recompilePaused = false);
    }
  }

  /**
   * Main method to start the game.
   *
   * @param args The arguments passed to the game.
   * @throws IOException If an I/O error occurs.
   */
  public static void main(String[] args) throws IOException {
    configGame();
    onSetup();
    Game.run();
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          WindowEventManager.registerFocusChangeListener(
              isInFocus -> {
                if (isInFocus && !DEBUG_MODE) recompilePlayerControl();
              });
          CameraSystem.camera().zoom = Math.max(0.1f, CameraSystem.camera().zoom + ZOOM);

          DungeonLoader.addLevel(Tuple.of("control1", AdvancedControlLevel1.class));
          DungeonLoader.addLevel(Tuple.of("control2", AdvancedControlLevel2.class));
          DungeonLoader.addLevel(Tuple.of("interaction1", InteractionLevel_1.class));
          DungeonLoader.addLevel(Tuple.of("cube1", CubeLevel_1.class));
          DungeonLoader.addLevel(Tuple.of("sphere1", SphereLevel_1.class));
          DungeonLoader.addLevel(Tuple.of("portal1", PortalLevel_1.class));
          DungeonLoader.addLevel(Tuple.of("objectsportal1", ObjectsPortalLevel_1.class));
          DungeonLoader.addLevel(Tuple.of("portalskill1", PortalSkillLevel_1.class));
          DungeonLoader.addLevel(Tuple.of("portalskill2", PortalSkillLevel_2.class));
          DungeonLoader.addLevel(Tuple.of("antimaterial1", AntiMaterialLevel_1.class));
          DungeonLoader.addLevel(Tuple.of("energypellet1", EnergyPelletLevel_1.class));
          DungeonLoader.addLevel(Tuple.of("lightbridge1", LightBridgeLevel_1.class));
          DungeonLoader.addLevel(Tuple.of("lightwall1", LightWallLevel_1.class));
          DungeonLoader.addLevel(Tuple.of("tractorbeam1", TractorBeamLevel_1.class));
          createSystems();
          createHero();

          DungeonLoader.loadLevel(loadLevelIndex());
        });
  }

  private static void createHero() {
    Game.levelEntities(Set.of(PlayerComponent.class)).forEach(Game::remove);
    Entity heroEntity = EntityFactory.newHero(DEATH_CALLBACK);
    Game.add(heroEntity);
    hero = new Hero(heroEntity);
    if (LEVELEDITOR_MODE) {
      heroEntity.fetch(InputComponent.class).get().removeCallbacks();
      heroEntity
          .fetch(InputComponent.class)
          .get()
          .registerCallback(
              Input.Keys.F12,
              entity -> DungeonLoader.loadLevel(DungeonLoader.currentLevelIndex() + 1),
              false);
      heroEntity
          .fetch(InputComponent.class)
          .get()
          .registerCallback(
              Input.Keys.F1,
              entity -> DungeonLoader.loadLevel(Math.max(0, DungeonLoader.currentLevelIndex() - 1)),
              false);
    } else {
      if (!DEBUG_MODE) recompilePlayerControl();
      else debugPortalSkills(heroEntity);
    }
  }

  private static void debugPortalSkills(Entity heroEntity) {
    SkillComponent sc =
        heroEntity
            .fetch(SkillComponent.class)
            .orElseThrow(() -> MissingComponentException.build(heroEntity, SkillComponent.class));
    sc.removeAll();
    PortalConfig debugConfig =
        new PortalConfig(hero) {
          @Override
          public long cooldown() {
            return 500;
          }

          @Override
          public float speed() {
            return 10;
          }

          @Override
          public float range() {
            return Integer.MAX_VALUE;
          }

          @Override
          public Supplier<Point> target() {
            return () -> hero.getMousePosition();
          }
        };
    sc.addSkill(new PortalSkill(PortalColor.BLUE, debugConfig));
    sc.addSkill(new PortalSkill(PortalColor.GREEN, debugConfig));
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.disableAudio(true);
    Game.frameRate(FPS);
    Game.windowWidth(WIDTH);
    Game.windowHeight(HEIGHT);
    Game.userOnLevelLoad(
        aBoolean -> {
          if (aBoolean) {
            writeLevelIndex(DungeonLoader.currentLevelIndex());
          }
        });
    Game.resizeable(true);
    Game.windowTitle("Portal Dungeon");
  }

  private static void createSystems() {
    if (LEVELEDITOR_MODE)
      Game.add(new LevelEditorSystem(true, "./advancedDungeon/assets/levels/portal/"));
    Game.add(new CollisionSystem());
    Game.add(new AISystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthSystem());
    Game.add(new HudSystem());
    Game.add(new SpikeSystem());
    Game.add(new LevelTickSystem());
    Game.add(new PitSystem());
    Game.add(new EventScheduler());
    Game.add(new LeverSystem());
    Game.add(new PressurePlateSystem());
    if (!DEBUG_MODE) Game.add(new FallingSystem());
    else Game.add(new Debugger());
    // Portal specific systems
    Game.add(new PortalExtendSystem());
    Game.add(new AntiMaterialBarrierSystem());
    Game.add(new LasergridSystem());
    Game.add(new AttachmentSystem());
  }

  private static int loadLevelIndex() {
    File file = new File(SAVE_FILE);

    if (!file.exists()) {
      return 0;
    }
    try {
      String json = readFileContent(file);
      Map<String, Object> map = JsonHandler.readJson(json);
      return ((Long) map.getOrDefault(SAVE_LEVEL_KEY, 0)).intValue();
    } catch (IOException e) {
      return 0;
    }
  }

  private static String readFileContent(File file) throws IOException {
    try (InputStream fis = new FileInputStream(file)) {
      return new String(fis.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IOException("Failed to read game configuration file: " + file.getPath(), e);
    }
  }

  private static void writeLevelIndex(int index) {
    HashMap<String, Object> map = new HashMap<>();
    map.put(SAVE_LEVEL_KEY, index);
    String content = JsonHandler.writeJson(map, true);
    try {
      File file = new File(SAVE_FILE);
      // Ensure parent directory exists
      try (OutputStreamWriter osw =
          new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8)) {
        osw.write(content);
      }
    } catch (Exception ignored) {
    }
  }
}
