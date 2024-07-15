package client;

import contrib.crafting.Crafting;
import contrib.entities.EntityFactory;
import contrib.level.generator.graphBased.RoomBasedLevelGenerator;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.level.elements.ILevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.MissingHeroException;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import server.Server;

/** WTF? . */
public class Client {
  /**
   * WTF? .
   *
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    // toggle this to off, if you want to use the default level generator
    boolean useRoomBasedLevel = false;

    Game.initBaseLogger(Level.WARNING);
    Debugger debugger = new Debugger();
    // start the game
    configGame();

    if (useRoomBasedLevel) onSetupRoomBasedLevel(10, 5, 1);
    else {
      onSetup();
      onLevelLoad(5, 1);
    }

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
          createHero();
          Crafting.loadRecipes();
          startServer();
          Crafting.loadRecipes();
        });
  }

  private static void onSetupRoomBasedLevel(int roomcount, int monstercount, int chestcount) {
    Game.userOnSetup(
        () -> {
          createSystems();
          createHero();
          Crafting.loadRecipes();
          createRoomBasedLevel(roomcount, monstercount, chestcount);
          startServer();
        });
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.frameRate(30);
    Game.disableAudio(true);
    Game.windowTitle("My Dungeon");
  }

  private static void createRoomBasedLevel(int roomcount, int monstercount, int chestcount) {
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
        } catch (IOException ignored) {

        }
      }
    }
    ILevel level = RoomBasedLevelGenerator.level(entities, DesignLabel.randomDesign());
    Game.currentLevel(level);
  }

  private static void createHero() {
    Entity hero;
    try {
      hero = (EntityFactory.newHero());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.add(hero);
  }

  private static void createSystems() {
    Game.add(new CollisionSystem());
    Game.add(new AISystem());
    Game.add(new HealthSystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthBarSystem());
    Game.add(new HudSystem());
  }

  private static void startServer() {

    Server blocklyServer = new Server(Game.hero().orElseThrow(MissingHeroException::new));
    try {
      blocklyServer.start();
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }
}
