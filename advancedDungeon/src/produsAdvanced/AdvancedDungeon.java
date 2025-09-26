package produsAdvanced;

import contrib.components.SkillComponent;
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
=======
>>>>>>> e27b0a78 (added dummy portal skill)
=======
>>>>>>> 75c6fee5 (added dummy portal skill)
import contrib.crafting.Crafting;
>>>>>>> ca4f8ed2 (added dummy portal skill)
=======
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
>>>>>>> 2178e611 (added green and blue portal variants)
=======
>>>>>>> ef71cb29 (added green and blue portal variants)
import contrib.entities.EntityFactory;
import contrib.entities.HeroFactory;
import contrib.hud.DialogUtils;
import contrib.systems.*;
import contrib.utils.DynamicCompiler;
import contrib.utils.components.Debugger;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.level.loader.DungeonLoader;
import core.utils.JsonHandler;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import produsAdvanced.abstraction.Hero;
import produsAdvanced.abstraction.PlayerController;
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
import produsAdvanced.abstraction.portalSkills.BluePortalSkill;
import produsAdvanced.abstraction.portalSkills.GreenPortalSkill;
import produsAdvanced.abstraction.portalSkills.PortalSkill;
=======
import produsAdvanced.abstraction.PortalSkill;
>>>>>>> ca4f8ed2 (added dummy portal skill)
=======
import produsAdvanced.abstraction.portalSkills.BluePortalSkill;
import produsAdvanced.abstraction.portalSkills.GreenPortalSkill;
import produsAdvanced.abstraction.portalSkills.PortalSkill;
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
import produsAdvanced.abstraction.portals.portalSkills.BluePortalSkill;
import produsAdvanced.abstraction.portals.portalSkills.GreenPortalSkill;
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> 20f3a7f9 (restructed portal related files)
=======
import produsAdvanced.abstraction.PortalSkill;
>>>>>>> e27b0a78 (added dummy portal skill)
=======
import produsAdvanced.abstraction.portalSkills.BluePortalSkill;
import produsAdvanced.abstraction.portalSkills.GreenPortalSkill;
import produsAdvanced.abstraction.portalSkills.PortalSkill;
>>>>>>> 2178e611 (added green and blue portal variants)
=======
import produsAdvanced.abstraction.PortalSkill;
>>>>>>> 75c6fee5 (added dummy portal skill)
=======
import produsAdvanced.abstraction.portalSkills.BluePortalSkill;
import produsAdvanced.abstraction.portalSkills.GreenPortalSkill;
import produsAdvanced.abstraction.portalSkills.PortalSkill;
>>>>>>> ef71cb29 (added green and blue portal variants)
=======
import produsAdvanced.abstraction.portals.portalSkills.BluePortalSkill;
import produsAdvanced.abstraction.portals.portalSkills.GreenPortalSkill;
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
import produsAdvanced.abstraction.portals.systems.PortalCollideSystem;
import produsAdvanced.abstraction.portals.systems.TestingSystem;
>>>>>>> 8dba5349 (commit for help)
=======
>>>>>>> f8ecc099 (fixed pÃrojectiles and rotation)
import produsAdvanced.level.*;

/**
 * Entry point for the "Advanced Dungeon" game setup.
 *
 * <p>This class is responsible for initializing and launching the game. It configures the game
 * environment, sets up levels, adds systems, creates the player hero, and integrates dynamic
 * recompilation of user control code for live testing of custom hero controllers.
 *
 * <p>Usage: run with the Gradle task {@code runAdvancedDungeon}.
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
  public static final boolean DEBUG_MODE = true;

  private static final String SAVE_LEVEL_KEY = "LEVEL";

  private static final String SAVE_FILE = "currentAdvancedLevel.json";

  /** Global reference to the {@link Hero} instance used in the game. */
  public static Hero hero;

  /** If true, the {@link produsAdvanced.riddles.MyPlayerController} will not be recompiled. */
  private static boolean recompilePaused = false;

  private static final String ERROR_MSG_CONTROLLER =
      "Da scheint etwas mit meinem Steuerrungscode nicht zu stimmen.";

  /** Path to the Java source file of the custom player controller. */
  private static final SimpleIPath HERO_CONTROLLER_PATH =
      new SimpleIPath("advancedDungeon/src/produsAdvanced/riddles/MyPlayerController.java");

  private static final SimpleIPath FIREBALL_PATH =
      new SimpleIPath("advancedDungeon/src/produsAdvanced/riddles/MyFireballSkill.java");

  /** Fully qualified class name of the custom player controller. */
  private static final String CONTROLLER_CLASSNAME = "produsAdvanced.riddles.MyPlayerController";

  private static final String FIREBALL_CLASSNAME = "produsAdvanced.riddles.MyFireballSkill";

  /**
   * Attempts to dynamically recompile and load the custom player controller class.
   *
   * <p>If compilation is successful, the hero's controller is updated at runtime. Otherwise, a
   * dialog is shown to indicate an error.
   */
  private static void recompileHeroControl() {
    if (recompilePaused) return;
    try {

      Object o = DynamicCompiler.loadUserInstance(FIREBALL_PATH, FIREBALL_CLASSNAME);
      hero.addSkill((Skill) o);
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
   */
  public static void main(String[] args) {
    Game.initBaseLogger(Level.WARNING);
    configGame();
    onSetup();
    Game.run();
  }

  /** Configures game settings like frame rate, window title, and audio. */
  private static void configGame() {
    Game.frameRate(30);
    Game.disableAudio(true);
    Game.userOnLevelLoad(
        aBoolean -> {
          if (aBoolean) {
            writeLevelIndex(DungeonLoader.currentLevelIndex());
          }
        });
    Game.resizeable(true);
    Game.windowTitle("Advanced Dungeon");
  }

  /** Initializes the game by setting up levels, systems, the player character, and crafting. */
  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          DungeonLoader.addLevel(Tuple.of("portal", PlayGroundLevel.class));

          try {
            Game.add(HeroFactory.newHero());
            SkillComponent sc = Game.hero().get().fetch(SkillComponent.class).get();
            sc.removeAll();
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
            sc.addSkill(new BluePortalSkill(new Tuple<>(Resource.MANA, 0)));
            sc.addSkill(new GreenPortalSkill(new Tuple<>(Resource.MANA, 0)));
<<<<<<< HEAD
<<<<<<< HEAD
=======
            sc.addSkill(new PortalSkill(new Tuple<>(Resource.MANA, 10)));
>>>>>>> ca4f8ed2 (added dummy portal skill)
=======
            sc.addSkill(new BluePortalSkill(new Tuple<>(Resource.MANA, 0)));
            sc.addSkill(new GreenPortalSkill(new Tuple<>(Resource.MANA, 0)));
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
            sc.addSkill(new FireballSkill(SkillTools::cursorPositionAsPoint, new Tuple<>(Resource.MANA, 0)));
>>>>>>> cefa46bc (added PortalComponent to avoid unwanted portal on portal interactions)
=======
            sc.addSkill(new PortalSkill(new Tuple<>(Resource.MANA, 10)));
>>>>>>> e27b0a78 (added dummy portal skill)
=======
            sc.addSkill(new BluePortalSkill(new Tuple<>(Resource.MANA, 0)));
            sc.addSkill(new GreenPortalSkill(new Tuple<>(Resource.MANA, 0)));
>>>>>>> 2178e611 (added green and blue portal variants)
=======
            sc.addSkill(new PortalSkill(new Tuple<>(Resource.MANA, 10)));
>>>>>>> 75c6fee5 (added dummy portal skill)
=======
            sc.addSkill(new BluePortalSkill(new Tuple<>(Resource.MANA, 0)));
            sc.addSkill(new GreenPortalSkill(new Tuple<>(Resource.MANA, 0)));
>>>>>>> ef71cb29 (added green and blue portal variants)
=======
            sc.addSkill(new FireballSkill(SkillTools::cursorPositionAsPoint, new Tuple<>(Resource.MANA, 0)));
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          /*
          DungeonLoader.addLevel(Tuple.of("control1", AdvancedControlLevel1.class));
          DungeonLoader.addLevel(Tuple.of("control2", AdvancedControlLevel2.class));
          DungeonLoader.addLevel(Tuple.of("control3", AdvancedControlLevel3.class));
          DungeonLoader.addLevel(Tuple.of("control4", AdvancedControlLevel4.class));
          DungeonLoader.addLevel(Tuple.of("interact", AdvancedBerryLevel.class));
          DungeonLoader.addLevel(Tuple.of("arraycreate", ArrayCreateLevel.class));
          DungeonLoader.addLevel(Tuple.of("arrayremove", ArrayRemoveLevel.class));
          DungeonLoader.addLevel(Tuple.of("arrayiterate", ArrayIterateLevel.class));
          DungeonLoader.addLevel(Tuple.of("sort", AdvancedSortLevel.class));
          */
          createSystems();

         /*WindowEventManager.registerFocusChangeListener(
              isInFocus -> {
                if (isInFocus) recompileHeroControl();
              });

          HeroFactory.heroDeath(entity -> restart());

          */
        //Crafting.loadRecipes();
        //DungeonLoader.loadLevel(loadLevelIndex());
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
    Game.add(new EventScheduler());
    Game.add(new ManaRestoreSystem());
    Game.add(new StaminaRestoreSystem());
    Game.add(new ManaBarSystem());
    Game.add(new StaminaBarSystem());
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
    Game.add(new PortalCollideSystem());
//    Game.add(new TestingSystem());
>>>>>>> 8dba5349 (commit for help)
=======
>>>>>>> f8ecc099 (fixed pÃrojectiles and rotation)
    if (DEBUG_MODE) Game.add(new Debugger());
=======
    Game.add(new DebugDrawSystem());
>>>>>>> 5d963fb8 (fixed portal creating bug and added directions to the portals to smoothen the transition)
=======
    Game.add(new DebugDrawSystem());
>>>>>>> 355d8064 (fixed portal creating bug and added directions to the portals to smoothen the transition)
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
    Game.levelEntities(Set.of(PlayerComponent.class)).forEach(Game::remove);
    Entity heroEntity = EntityFactory.newHero();
    Game.add(heroEntity);
    hero = new Hero(heroEntity);

    if (!DEBUG_MODE) recompileHeroControl();
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
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    DungeonLoader.reloadCurrentLevel();
  }

  private static int loadLevelIndex() {
    File file = new File(AdvancedDungeon.SAVE_FILE);

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
      File file = new File(AdvancedDungeon.SAVE_FILE);
      // Ensure parent directory exists
      try (OutputStreamWriter osw =
             new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8)) {
        osw.write(content);
      }
    } catch (Exception ignored) {
    }
  }
}
