package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import contrib.crafting.Crafting;
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
import mushroomDungeon.level.Level01;

/** Starter for the Demo Escaperoom Dungeon. */
public class MushRoom {
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
    Game.initBaseLogger(Level.WARNING);
    configGame();
    onSetup();

    if (DEBUG_MODE) {
      Debugger debugger = new Debugger();
      Game.userOnFrame(() -> debugger.execute());
    }
    Game.windowTitle("Mush-Room");
    Game.run();
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          setupMusic();
          DungeonLoader.addLevel(Tuple.of("mushroom", Level01.class));
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
  }

  private static void setupMusic() {
    Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
    backgroundMusic.setLooping(true);
    backgroundMusic.play();
    backgroundMusic.setVolume(.05f);
  }
}
