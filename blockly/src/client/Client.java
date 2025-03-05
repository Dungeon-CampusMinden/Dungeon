package client;

import com.sun.net.httpserver.HttpServer;
import contrib.crafting.Crafting;
import contrib.devDungeon.level.DungeonLoader;
import contrib.entities.EntityFactory;
import contrib.hud.DialogUtils;
import contrib.level.generator.GeneratorUtils;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.game.ECSManagment;
import core.systems.LevelSystem;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.logging.Level;
import level.MazeLevel;
import server.Server;

/**
 * This Class must be run to start the dungeon application. Otherwise, the blockly frontend won't
 * have any effect
 */
public class Client {
  private static HttpServer httpServer;

  /**
   * Main method to start the game.
   *
   * @param args The arguments passed to the game.
   * @throws IOException If an I/O error occurs.
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
          DungeonLoader.addLevel(new Tuple<>("maze", MazeLevel.class));
          createSystems();
          createHero();
          Crafting.loadRecipes();

          startServer();

          Crafting.loadRecipes();
          LevelSystem levelSystem = (LevelSystem) ECSManagment.systems().get(LevelSystem.class);
          levelSystem.onEndTile(DungeonLoader::loadNextLevel);
          DungeonLoader.afterAllLevels(Client::startRoomBasedLevel);

          DungeonLoader.loadLevel("maze");
        });
  }

  private static void onLevelLoad() {
    Game.userOnLevelLoad(
        (firstLoad) -> {
          Server.instance().interruptExecution = true;
        });
  }

  private static void startRoomBasedLevel() {
    GeneratorUtils.createRoomBasedLevel(10, 5, 1);
    DialogUtils.showTextPopup(
        "Du hast alle Level erfolgreich gelöst!\nDu bist jetzt im Sandbox Modus.", "Gewonnen");

    LevelSystem levelSystem = (LevelSystem) ECSManagment.systems().get(LevelSystem.class);
    levelSystem.onEndTile(Client::startRoomBasedLevel); // restart the level -> endless loop
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.frameRate(30);
    Game.disableAudio(true);
    Game.windowTitle("Blockly Dungeon");
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
    Game.add(new HealthSystem());
    Game.add(new PathSystem());
    Game.add(new LevelTickSystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthBarSystem());
    Game.add(new HudSystem());
    Game.add(new SpikeSystem());
    Game.add(new IdleSoundSystem());
  }

  private static void startServer() {
    try {
      httpServer = Server.instance().start();
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }
}
