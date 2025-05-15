package produsAdvanced;

import contrib.crafting.Crafting;
import contrib.entities.EntityFactory;
import contrib.entities.HeroFactory;
import contrib.hud.DialogUtils;
import contrib.level.DevDungeonLoader;
import contrib.level.generator.GeneratorUtils;
import contrib.systems.*;
import contrib.utils.DynamicCompiler;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.game.ECSManagment;
import core.game.WindowEventManager;
import core.systems.LevelSystem;
import core.utils.IVoidFunction;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import produsAdvanced.abstraction.Fireball;
import produsAdvanced.abstraction.FireballSkill;
import produsAdvanced.abstraction.Hero;
import produsAdvanced.abstraction.PlayerController;
import produsAdvanced.level.*;
import systems.BlockSystem;
import systems.TintTilesSystem;

/**
 * Entry point for the "Advanced Dungeon" game setup.
 *
 * <p>This class is responsible for initializing and launching the game. It configures the game
 * environment, sets up levels, adds systems, creates the player hero, and integrates dynamic
 * recompilation of user control code for live testing of custom hero controllers.
 */
public class AdvancedDungeon {
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

  private static final Debugger DEBUGGER = new Debugger();

  /** Global reference to the {@link Hero} instance used in the game. */
  public static Hero hero;

  /** Global reference to the {@link FireballSkill} instance used in the game. */
  public static FireballSkill fireballSkill = new FireballSkill();

  /** If true, the {@link produsAdvanced.riddles.MyPlayerController} will not be recompiled. */
  private static boolean recompilePaused = false;

  private static final String ERROR_MSG_CONTROLLER =
      "Da scheint etwas mit meinem Steuerrungscode nicht zu stimmen.";

  /** Path to the Java source file of the custom player controller. */
  private static final SimpleIPath HERO_CONTROLLER_PATH =
      new SimpleIPath("src/produsAdvanced/riddles/MyPlayerController.java");

  private static final SimpleIPath FIREBALL_PATH =
      new SimpleIPath("src/produsAdvanced/riddles/MyFireball.java");

  /** Fully qualified class name of the custom player controller. */
  private static final String CONTROLLER_CLASSNAME = "produsAdvanced.riddles.MyPlayerController";

  private static final String FIREBALL_CLASSNAME = "produsAdvanced.riddles.MyFireball";

  /**
   * Function called every frame to check for manual recompilation trigger.
   *
   * <p>Pressing the 'M' key triggers recompilation of the user-defined player controller class.
   */
  private static final IVoidFunction onFrame =
      () -> {
        if (DEBUG_MODE) DEBUGGER.execute();
      };

  /**
   * Attempts to dynamically recompile and load the custom player controller class.
   *
   * <p>If compilation is successful, the hero's controller is updated at runtime. Otherwise, a
   * dialog is shown to indicate an error.
   */
  private static void recompileHeroControl() {
    if (recompilePaused) return;
    try {
      Object o =
          DynamicCompiler.loadUserInstance(
              FIREBALL_PATH, FIREBALL_CLASSNAME, new Tuple<>(FireballSkill.class, fireballSkill));
      fireballSkill.setController((Fireball) o);

      o =
          DynamicCompiler.loadUserInstance(
              HERO_CONTROLLER_PATH, CONTROLLER_CLASSNAME, new Tuple<>(Hero.class, hero));
      hero.setController((PlayerController) o);
    } catch (Exception e) {
      recompilePaused = true;
      if (DEBUG_MODE) e.printStackTrace();
      DialogUtils.showTextPopup(ERROR_MSG_CONTROLLER, "Code Error", () -> recompilePaused = false);
    }
  }

  /**
   * Main method to launch the game.
   *
   * @param args Command-line arguments (not used).
   * @throws IOException If game configuration or hero creation fails.
   */
  public static void main(String[] args) throws IOException {
    Game.initBaseLogger(Level.WARNING);
    configGame();
    onSetup();
    Game.userOnFrame(onFrame);
    Game.run();
  }

  /**
   * Configures game settings like frame rate, window title, and audio.
   *
   * @throws IOException If configuration fails.
   */
  private static void configGame() throws IOException {
    Game.frameRate(30);
    Game.disableAudio(true);
    Game.resizeable(true);
    Game.windowTitle("Advanced Dungeon");
  }

  /** Initializes the game by setting up levels, systems, the player character, and crafting. */
  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          DevDungeonLoader.addLevel(Tuple.of("control1", AdvancedControlLevel1.class));
          DevDungeonLoader.addLevel(Tuple.of("control2", AdvancedControlLevel2.class));
          DevDungeonLoader.addLevel(Tuple.of("control3", AdvancedControlLevel3.class));
          DevDungeonLoader.addLevel(Tuple.of("control4", AdvancedControlLevel4.class));
          DevDungeonLoader.addLevel(Tuple.of("interact", AdvancedBerryLevel.class));
          DevDungeonLoader.addLevel(Tuple.of("arraycreate", ArrayCreateLevel.class));
          DevDungeonLoader.addLevel(Tuple.of("arrayremove", ArrayRemoveLevel.class));
          DevDungeonLoader.addLevel(Tuple.of("arrayiterate", ArrayIterateLevel.class));
          DevDungeonLoader.addLevel(Tuple.of("sort", AdvancedSortLevel.class));
          createSystems();
          WindowEventManager.registerFocusChangeListener(
              isInFocus -> {
                if (isInFocus) recompileHeroControl();
              });

          HeroFactory.heroDeath(entity -> restart());
          try {
            createHero();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          Crafting.loadRecipes();
          LevelSystem levelSystem = (LevelSystem) ECSManagment.systems().get(LevelSystem.class);
          levelSystem.onEndTile(DevDungeonLoader::loadNextLevel);
          DevDungeonLoader.afterAllLevels(AdvancedDungeon::startRoomBasedLevel);
          DevDungeonLoader.loadLevel(0);
        });
  }

  /**
   * Adds all game-relevant systems to the ECS framework.
   *
   * <p>These include logic for AI, health, projectiles, level events, and more.
   */
  private static void createSystems() {
    Game.add(new CollisionSystem());
    Game.add(new AISystem());
    Game.add(new HealthSystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthBarSystem());
    Game.add(new HudSystem());
    Game.add(new SpikeSystem());
    Game.add(new IdleSoundSystem());
    Game.add(new PathSystem());
    Game.add(new LevelTickSystem());
    Game.add(new LeverSystem());
    Game.add(new BlockSystem());
    Game.add(new FallingSystem());
    Game.add(new PitSystem());
    Game.add(new TintTilesSystem());
    Game.add(new EventScheduler());
    if (DEBUG_MODE) Game.add(new LevelEditorSystem());
  }

  /**
   * Creates the player hero entity and adds it to the game.
   *
   * <p>If a previous hero entity exists, it is removed.
   *
   * @throws IOException If hero creation fails.
   */
  private static void createHero() throws IOException {
    Game.entityStream(Set.of(PlayerComponent.class)).forEach(Game::remove);
    Entity heroEntity = EntityFactory.newHero();
    Game.add(heroEntity);
    hero = new Hero(heroEntity, fireballSkill);
  }

  /**
   * Restarts the game by removing all entities, recreating the hero, and reloading the current
   * level.
   *
   * <p>This effectively resets the game state to its initial configuration.
   */
  public static void restart() {
    Game.removeAllEntities();
    try {
      createHero();
      if (!DEBUG_MODE) recompileHeroControl();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    DevDungeonLoader.reloadCurrentLevel();
  }

  private static void startRoomBasedLevel() {
    GeneratorUtils.createRoomBasedLevel(10, 5, 1);
    DialogUtils.showTextPopup(
        "Du hast alle Level erfolgreich gelöst!\nDu bist jetzt im Sandbox Modus.", "Gewonnen");

    LevelSystem levelSystem = (LevelSystem) ECSManagment.systems().get(LevelSystem.class);
    levelSystem.onEndTile(
        AdvancedDungeon::startRoomBasedLevel); // restart the level -> endless loop
  }
}
