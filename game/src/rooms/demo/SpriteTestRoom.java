package rooms.demo;

import engine.Entity;
import engine.Game;
import engine.level.loader.DungeonLoader;
import engine.utils.Tuple;
import engine.utils.components.path.SimpleIPath;
import feature.components.Debugger;
import feature.entities.CharacterClass;
import feature.entities.EntityFactory;
import feature.systems.AISystem;
import feature.systems.CollisionSystem;
import feature.systems.DecoTestSystem;
import feature.systems.FallingSystem;
import feature.systems.HealthSystem;
import feature.systems.IdleSoundSystem;
import feature.systems.LevelEditorSystem;
import feature.systems.LevelTickSystem;
import feature.systems.LeverSystem;
import feature.systems.ManaRestoreSystem;
import feature.systems.PathSystem;
import feature.systems.PitSystem;
import feature.systems.PressurePlateSystem;
import feature.systems.ProjectileSystem;
import feature.systems.SpikeSystem;
import feature.systems.StaminaRestoreSystem;
import java.io.IOException;

/**
 * Starter for the Demo Escaperoom Dungeon.
 *
 * <p>Usage: run with the Gradle task {@code runDemoRoom}.
 */
public class SpriteTestRoom {
  private static final boolean DEBUG_MODE = true;
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";
  private static final int START_LEVEL = 0;

  /**
   * Main method to start the game.
   *
   * @param args The arguments passed to the game.
   * @throws IOException If an I/O error occurs.
   */
  public static void main(String[] args) throws IOException {
    configGame();
    onSetup();

    Game.windowTitle("Sprite Test Room");
    Game.run();
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          DungeonLoader.addLevel(Tuple.of("spritetest", Level01.class));
          createSystems();
          try {
            createHero();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          DungeonLoader.loadLevel(START_LEVEL);
        });
  }

  private static void createHero() throws IOException {
    Entity hero = EntityFactory.newHero(CharacterClass.WIZARD);
    Game.add(hero);
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        feature.input.configuration.KeyboardConfig.class,
        engine.configuration.KeyboardConfig.class);
    Game.disableAudio(true);
    Game.frameRate(60);
  }

  private static void createSystems() {
    if (DEBUG_MODE) Game.add(new LevelEditorSystem());
    if (DEBUG_MODE) Game.add(new DecoTestSystem());
    Game.add(new CollisionSystem());
    Game.add(new ManaRestoreSystem());
    Game.add(new StaminaRestoreSystem());
    Game.add(new AISystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthSystem());
    Game.add(new SpikeSystem());
    if (!DEBUG_MODE) Game.add(new FallingSystem());
    Game.add(new PathSystem());
    Game.add(new LevelTickSystem());
    Game.add(new PitSystem());
    Game.add(new LeverSystem());
    Game.add(new PressurePlateSystem());
    Game.add(new IdleSoundSystem());
    if (DEBUG_MODE) Game.add(new Debugger());
  }
}
