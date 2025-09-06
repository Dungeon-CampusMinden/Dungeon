package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import contrib.crafting.Crafting;
import contrib.entities.HeroFactory;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import coopDungeon.level.*;
import core.Entity;
import core.Game;
import core.level.loader.DungeonLoader;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Starter for the Coop Dungeon.
 *
 * <p>The Coop Dungeon is designed for two players.
 *
 * <p>The players have to work together to solve small parkour-style riddles, such as jumping over
 * pits or using levers to open gates to reach the end.
 *
 * <p>Usage: run with the Gradle task {@code runCoopDungeon}.
 */
public class CoopDungeon {
  private static final boolean DEBUG_MODE = false;

  private static final String BACKGROUND_MUSIC = "sounds/background.wav";
  private static final int START_LEVEL = 0;

  /**
   * Main method to start the game.
   *
   * @param args The arguments passed to the game.
   * @throws IOException If an I/O error occurs.
   */
  public static void main(String[] args) throws IOException {
    Game.initBaseLogger(Level.WARNING);
    configGame();
    onSetup();

    if (DEBUG_MODE) {
      Debugger debugger = new Debugger();
      Game.userOnFrame(() -> debugger.execute());
    }
    Game.windowTitle("Coop-Dungeon");
    Game.run();
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          setupMusic();
          DungeonLoader.addLevel(Tuple.of("coop1", Level01.class));
          DungeonLoader.addLevel(Tuple.of("coop2", Level02.class));
          createSystems();
          try {
            createHero();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          Crafting.loadRecipes();
          DungeonLoader.loadLevel(START_LEVEL);
        });
  }

  private static void createHero() throws IOException {
    Entity hero = HeroFactory.newHero();
    Game.add(hero);
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.disableAudio(false);
    Game.frameRate(30);
  }

  private static void createSystems() {
    if (DEBUG_MODE) Game.add(new LevelEditorSystem());
    Game.add(new CollisionSystem());
    Game.add(new AISystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthBarSystem());
    Game.add(new HealthSystem());
    Game.add(new HudSystem());
    Game.add(new SpikeSystem());
    if (!DEBUG_MODE) Game.add(new FallingSystem());
    Game.add(new PathSystem());
    Game.add(new LevelTickSystem());
    Game.add(new PitSystem());
    Game.add(new EventScheduler());
    Game.add(new LeverSystem());
    Game.add(new PressurePlateSystem());
    Game.add(new IdleSoundSystem());
    Game.add(new ManaRestoreSystem());
    Game.add(new StaminaRestoreSystem());
    Game.add(new ManaBarSystem());
    Game.add(new StaminaBarSystem());
  }

  private static void setupMusic() {
    Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
    backgroundMusic.setLooping(true);
    backgroundMusic.play();
    backgroundMusic.setVolume(.05f);
  }
}
