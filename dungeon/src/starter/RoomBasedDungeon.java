package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import contrib.crafting.Crafting;
import contrib.entities.EntityFactory;
import contrib.level.generator.graphBased.RoomBasedLevelGenerator;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.level.elements.ILevel;
import core.level.utils.DesignLabel;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/** WTF? . */
public class RoomBasedDungeon {
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";

  /**
   * WTF? .
   *
   * @param args the array of command-line arguments
   * @throws IOException if an I/O error occurs
   */
  public static void main(String[] args) throws IOException {
    Game.initBaseLogger(Level.WARNING);
    Debugger debugger = new Debugger();
    // start the game
    configGame();
    onSetup(10, 5, 1);
    onFrame(debugger);

    // build and start game
    Game.run();
  }

  private static void onFrame(Debugger debugger) {
    Game.userOnFrame(debugger::execute);
  }

  private static void onSetup(int roomcount, int monstercount, int chestcount) {
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
          createRoomBasedLevel(roomcount, monstercount, chestcount);
        });
  }

  public static void createRoomBasedLevel(int roomcount, int monstercount, int chestcount) {
    // create entity sets
    Set<Set<Entity>> entities = new HashSet<>();
    for (int i = 0; i < roomcount; i++) {
      Set<Entity> set = new HashSet<>();
      entities.add(set);
      if (i == roomcount / 2) {
        try {
          set.add(EntityFactory.newCraftingCauldron());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      for (int j = 0; j < monstercount; j++) {
        try {
          set.add(EntityFactory.randomMonster());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      for (int k = 0; k < chestcount; k++) {
        try {
          set.add(EntityFactory.newChest());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
    ILevel level = RoomBasedLevelGenerator.level(entities, DesignLabel.randomDesign());
    Game.currentLevel(level);
  }

  private static void createHero() throws IOException {
    Entity hero = EntityFactory.newHero();
    Game.add(hero);
  }

  private static void createSystems() {
    Game.add(new CollisionSystem());
    Game.add(new AISystem());
    Game.add(new HealthSystem());
    Game.add(new PathSystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthBarSystem());
    Game.add(new HudSystem());
    Game.add(new SpikeSystem());
    Game.add(new IdleSoundSystem());
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
}
