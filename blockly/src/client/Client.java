package client;

import com.sun.net.httpserver.HttpServer;
import components.AmmunitionComponent;
import contrib.crafting.Crafting;
import contrib.entities.HeroFactory;
import contrib.hud.DialogUtils;
import contrib.level.DevDungeonLoader;
import contrib.level.generator.GeneratorUtils;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.game.ECSManagment;
import core.systems.LevelSystem;
import core.systems.PlayerSystem;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import editor.LevelEditorSystem;
import entities.HeroTankControlledFactory;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import level.produs.chapter1.*;
import server.Server;
import systems.BlockSystem;
import systems.TintTilesSystem;
import utils.CheckPatternPainter;

/**
 * This Class must be run to start the dungeon application. Otherwise, the blockly frontend won't
 * have any effect
 */
public class Client {
  private static final boolean KEYBOARD_DEACTIVATION = false;
  private static final boolean DRAW_CHECKER_PATTERN = true;

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
          DevDungeonLoader.addLevel(Tuple.of("chapter11", Chapter11Level.class));
          DevDungeonLoader.addLevel(Tuple.of("chapter12", Chapter12Level.class));
          DevDungeonLoader.addLevel(Tuple.of("chapter13", Chapter13Level.class));
          DevDungeonLoader.addLevel(Tuple.of("chapter14", Chapter14Level.class));
          DevDungeonLoader.addLevel(Tuple.of("chapter15", Chapter15Level.class));
          DevDungeonLoader.addLevel(Tuple.of("chapter16", Chapter16Level.class));
          DevDungeonLoader.addLevel(Tuple.of("chapter17", Chapter17Level.class));
          //DevDungeonLoader.addLevel(Tuple.of("chapter18", Chapter18Level.class));
          //DevDungeonLoader.addLevel(Tuple.of("chapter19", Chapter19Level.class));
          //DevDungeonLoader.addLevel(Tuple.of("chapter110", Chapter110Level.class));
          createSystems();

          HeroFactory.heroDeath(
              entity -> {
                restart();
              });

          createHero();
          Crafting.loadRecipes();

          startServer();

          Crafting.loadRecipes();

          if (KEYBOARD_DEACTIVATION) {
            Game.remove(PlayerSystem.class);
          }

          LevelSystem levelSystem = (LevelSystem) ECSManagment.systems().get(LevelSystem.class);
          levelSystem.onEndTile(DevDungeonLoader::loadNextLevel);
          DevDungeonLoader.afterAllLevels(Client::startRoomBasedLevel);

          DevDungeonLoader.loadLevel(0);
        });
  }

  private static void onLevelLoad() {
    Game.userOnLevelLoad(
        (firstLoad) -> {
          if (DRAW_CHECKER_PATTERN)
            CheckPatternPainter.paintCheckerPattern(Game.currentLevel().layout());
          Server.instance().interruptExecution = true;
          Game.hero()
              .flatMap(e -> e.fetch(AmmunitionComponent.class))
              .map(AmmunitionComponent::resetCurrentAmmunition);
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

  private static void createSystems() {
    Game.add(new LevelEditorSystem());
    Game.add(new CollisionSystem());
    Game.add(new AISystem());
    Game.add(new HealthSystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthBarSystem());
    Game.add(new HudSystem());
    Game.add(new SpikeSystem());
    Game.add(new IdleSoundSystem());
    Game.add(new PathSystem());
    Game.add(new LevelTickSystem());
    Game.add(new LeverSystem());
    Game.add(new BlockSystem());
    Game.add(new FallingSystem());
    Game.add(new PitSystem());
    Game.add(new TintTilesSystem());
  }

  private static void startServer() {
    try {
      httpServer = Server.instance().start();
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  /**
   * Creates and adds a new hero entity to the game.
   *
   * <p>Any existing entities with a {@link PlayerComponent} will first be removed. The new hero is
   * generated using the {@link HeroTankControlledFactory} and is equipped with an {@link
   * AmmunitionComponent}.
   *
   * @throws RuntimeException if an {@link IOException} occurs during hero creation
   */
  public static void createHero() {
    Game.entityStream(Set.of(PlayerComponent.class)).forEach(e -> Game.remove(e));
    Entity hero;
    try {
      hero = HeroTankControlledFactory.newTankControlledHero();
      hero.add(new AmmunitionComponent());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.add(hero);
  }

  /**
   * Restarts the game by removing all entities, recreating the hero, and reloading the current
   * level.
   *
   * <p>This effectively resets the game state to its initial configuration.
   */
  public static void restart() {
    Game.removeAllEntities();
    createHero();
    DevDungeonLoader.reloadCurrentLevel();
  }
}
