package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import contrib.crafting.Crafting;
import contrib.entities.EntityFactory;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.level.utils.*;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.logging.Level;

/** WTF? . */
public class RandomDungeon {

  private static final String BACKGROUND_MUSIC = "sounds/background.wav";

  /**
   * WTF? .
   *
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    Game.initBaseLogger(Level.WARNING);
    Debugger debugger = new Debugger();
    configGame();
    onSetup();
    onLevelLoad(5, 1);
    onFrame(debugger);

    // build and start game
    Game.run();
  }

  private static void onLevelLoad(int monstercount, int chestcount) {
    Game.userOnLevelLoad(
        (firstTime) -> {
          if (firstTime) {
            try {
              for (int i = 0; i < monstercount; i++) Game.add(EntityFactory.randomMonster());
              for (int i = 0; i < chestcount; i++) Game.add(EntityFactory.newChest());
              Game.add(EntityFactory.newCraftingCauldron());
            } catch (IOException e) {
              throw new RuntimeException();
            }
            Game.levelSize(LevelSize.randomSize());
          }
        });
  }

  private static void onFrame(Debugger debugger) {
    Game.userOnFrame(debugger::execute);
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          createSystems();
          try {
            createHero();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          setupMusic();
          Crafting.loadRecipes();
        });
  }

  private static void createHero() throws IOException {
    Entity hero = EntityFactory.newHero();
    Game.add(hero);
  }

  private static void setupMusic() {
    Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
    backgroundMusic.setLooping(true);
    backgroundMusic.play();
    backgroundMusic.setVolume(.1f);
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.frameRate(30);
    Game.disableAudio(false);
    Game.windowTitle("My Dungeon");
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
  }
}
