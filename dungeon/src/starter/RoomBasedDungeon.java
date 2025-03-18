package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import contrib.components.LeverComponent;
import contrib.crafting.Crafting;
import contrib.entities.DoorManagerFactory;
import contrib.entities.EntityFactory;
import contrib.entities.LeverFactory;
import contrib.level.generator.GeneratorUtils;
import contrib.systems.*;
import contrib.utils.Predicate;
import contrib.utils.PredicateFactory;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.utils.LevelElement;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.function.Consumer;
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
    onSetup(10, 0, 1);
    onFrame(debugger);

    Game.userOnLevelLoad(new Consumer<Boolean>() {
      @Override
      public void accept(Boolean aBoolean) {
        if (aBoolean){
          DoorTile door = Game.currentLevel().doorTiles().stream().findFirst().get();
          Entity l1 = LeverFactory.createLever(Game.randomTilePoint(LevelElement.FLOOR).get());
          Entity l2 = LeverFactory.createLever(Game.randomTilePoint(LevelElement.FLOOR).get());
          Entity l3 = LeverFactory.createLever(Game.randomTilePoint(LevelElement.FLOOR).get());
          LeverComponent l1c=l1.fetch(LeverComponent.class).get();
          LeverComponent l2c=l2.fetch(LeverComponent.class).get();
          LeverComponent l3c=l3.fetch(LeverComponent.class).get();
          Game.add(l1);
          Game.add(l2);
          Game.add(l3);


          Predicate p1 = PredicateFactory.and(l1c,l2c);
          Predicate p2 = PredicateFactory.not(l3c);
          Predicate combi = PredicateFactory.and(p1,p2);
          Game.add(DoorManagerFactory.doorOpener(door,combi));
        }
      }
    });
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
          GeneratorUtils.createRoomBasedLevel(roomcount, monstercount, chestcount);
        });
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
    Game.add(new PredicateSystem());
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
