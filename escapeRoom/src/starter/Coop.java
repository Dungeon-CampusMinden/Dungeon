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
import core.game.ECSManagment;
import core.level.loader.DungeonLoader;
import core.systems.LevelSystem;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.logging.Level;

public class Coop {
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";
  private static final boolean DISABLE_AUDIO = true;
  private static final boolean DEBUG_MODE = true;

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
          DungeonLoader.addLevel(Tuple.of("coop1", Level01.class));
          DungeonLoader.addLevel(Tuple.of("coop2", Level02.class));
          DungeonLoader.addLevel(Tuple.of("coop3", Level03.class));
          DungeonLoader.addLevel(Tuple.of("coop4", Level04.class));
          LevelSystem levelSystem = (LevelSystem) ECSManagment.systems().get(LevelSystem.class);

          createSystems();
          try {
            createHero();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          setupMusic();
          Crafting.loadRecipes();
          DungeonLoader.loadLevel(0); // Tutorial
        });
  }

  private static void createHero() throws IOException {
    Entity hero = HeroFactory.newHero();
    Game.add(hero);
  }

  private static void setupMusic() {
    Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
    backgroundMusic.setLooping(true);
    backgroundMusic.play();
    backgroundMusic.setVolume(.05f);
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.frameRate(30);
    Game.disableAudio(DISABLE_AUDIO);
  }

  private static void createSystems() {
    Game.add(new LevelEditorSystem());
    Game.add(new CollisionSystem());
    Game.add(new AISystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthBarSystem());
    Game.add(new HealthSystem());
    Game.add(new HudSystem());
    Game.add(new SpikeSystem());
    Game.add(new IdleSoundSystem());
    Game.add(new FallingSystem());
    Game.add(new PathSystem());
    Game.add(new LevelTickSystem());
    Game.add(new PitSystem());
    Game.add(new EventScheduler());
    Game.add(new LeverSystem());
    Game.add(new PressurePlateSystem());
  }
}
