import contrib.crafting.Crafting;
import contrib.entities.EntityFactory;
import contrib.entities.HeroFactory;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.level.loader.DungeonLoader;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.logging.Level;
import level.Level001;
import produsAdvanced.abstraction.portals.systems.PortalExtendSystem;

/**
 * This Class must be run to start the dungeon application. Otherwise, the blockly frontend won't
 * have any effect
 *
 * <p>Usage: run with the Gradle task {@code runBlockly}.
 *
 * <p>For the Web-Ui you have to start the frontend yourself.
 */
public class Starter {

  /** Force to apply for movement of all entities. */
  private static final boolean DEBUG_MODE = true;

  /**
   * Setup and run the game. Also start the server that is listening to the requests from blockly
   * frontend.
   *
   * @param args CLI arguments
   * @throws IOException if textures can not be loaded.
   */
  public static void main(String[] args) throws IOException {
    Game.initBaseLogger(Level.WARNING);
    Debugger debugger = new Debugger();
    // start the game
    configGame();
    // Set up components and level
    onSetup();
    // build and start game
    try {
      Game.run();
    } finally {

    }
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          // chapter 1
          DungeonLoader.addLevel(Tuple.of("level001", Level001.class));
          HeroFactory.heroDeath(entity -> restart());
          try {
            createHero();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          createSystems();
          Crafting.loadRecipes();
          Crafting.loadRecipes();

          DungeonLoader.loadLevel(0);
        });
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.frameRate(30);
    Game.disableAudio(true);
    Game.resizeable(true);
    Game.windowTitle("Blockly Dungeon");
  }

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
    Game.add(new PressurePlateSystem());
    Game.add(new PortalExtendSystem());
    if (DEBUG_MODE) Game.add(new Debugger());
    if (DEBUG_MODE) {
      Game.add(new DebugDrawSystem());
      Game.add(new LevelEditorSystem());
    }
  }

  public static void restart() {
    Game.removeAllEntities();
    try {
      createHero();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    DungeonLoader.reloadCurrentLevel();
  }

  private static void createHero() throws IOException {
    Entity heroEntity = EntityFactory.newHero();
    Game.add(heroEntity);
  }
}
