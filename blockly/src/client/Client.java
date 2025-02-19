package client;

import com.sun.net.httpserver.HttpServer;
import contrib.crafting.Crafting;
import contrib.entities.EntityFactory;
import contrib.hud.DialogUtils;
import contrib.level.generator.graphBased.RoomBasedLevelGenerator;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.game.ECSManagment;
import core.level.TileLevel;
import core.level.elements.ILevel;
import core.level.utils.DesignLabel;
import core.systems.LevelSystem;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import entities.VariableHUD;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import level.MazeLevel;
import level.utils.DungeonLoader;
import server.Server;
import systems.DevHealthSystem;
import systems.HudBlocklySystem;
import systems.LevelTickSystem;

/**
 * This Class must be run to start the dungeon application. Otherwise, the blockly frontend won't
 * have any effect
 */
public class Client {
  private static final ArrayList<TileLevel> levels = new ArrayList<>();
  private static int currentLevel = 0;
  private static HttpServer httpServer;

  /**
   * Setup and run the game. Also start the server that is listening to the requests from blockly
   * frontend.
   *
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    Game.initBaseLogger(Level.WARNING);
    Debugger debugger = new Debugger();
    // start the game
    configGame();
    // Set up components and level
    onSetup();

    onFrame(debugger);

    onLevelLoad();

    // build and start game
    Game.run();

    httpServer.stop(0);
  }

  private static void onFrame(Debugger debugger) {
    Game.userOnFrame(debugger::execute);
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          DungeonLoader.instance().addLevel(new Tuple<>("maze", MazeLevel.class));
          createSystems();
          createHero();
          Crafting.loadRecipes();

          startServer();

          Crafting.loadRecipes();
          LevelSystem levelSystem = (LevelSystem) ECSManagment.systems().get(LevelSystem.class);
          levelSystem.onEndTile(() -> DungeonLoader.instance().loadNextLevel());

          DungeonLoader.instance().loadLevel("maze");
        });
  }

  private static void onLevelLoad() {
    Game.userOnLevelLoad(
        (firstLoad) -> {
          VariableHUD variableHUD = Server.instance().variableHUD;
          if (variableHUD == null
              && Game.stage().isPresent()) { // should only be on first level load
            variableHUD = new VariableHUD(Game.stage().get());
            Server.instance().variableHUD = variableHUD;
          }

          // (Re-)add the variable HUD to the game
          Game.add(variableHUD.createEntity());
        });
  }

  /**
   * Load the next level. This function will be executed when the player enters the exit tile. If
   * the player finished all level generated a random level layout and call it sandbox mode.
   */
  public static void loadNextLevel() {
    Server.instance().interruptExecution = true;
    currentLevel++;
    if (currentLevel >= levels.size()) {
      createRoomBasedLevel(10, 5, 1);
      DialogUtils.showTextPopup(
          "Du hast alle Level erfolgreich gelöst!\nDu bist jetzt im Sandbox Modus.", "Gewonnen");
      return;
    }
    Game.currentLevel(levels.get(currentLevel));
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
      hero.fetch(PlayerComponent.class)
          .flatMap(
              fetch ->
                  fetch.registerCallback(
                      KeyboardConfig.TOGGLE_BLOCKLY_HUD.value(),
                      (e) ->
                          Server.instance()
                              .variableHUD
                              .visible(!Server.instance().variableHUD.visible()),
                      false,
                      true));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.add(hero);
  }

  private static void createSystems() {
    Game.add(new CollisionSystem());
    Game.add(new AISystem());
    Game.add(new DevHealthSystem());
    Game.add(new PathSystem());
    Game.add(new LevelTickSystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthBarSystem());
    Game.add(new HudSystem());
    Game.add(new HudBlocklySystem());
  }

  private static void startServer() {
    try {
      httpServer = Server.instance().start();
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }
}
