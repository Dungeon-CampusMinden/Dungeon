package starter;

import contrib.entities.CharacterClass;
import contrib.entities.EntityFactory;
import contrib.entities.HeroBuilder;
import contrib.entities.HeroController;
import contrib.modules.keypad.KeypadSystem;
import contrib.systems.CollisionSystem;
import contrib.systems.DebugDrawSystem;
import contrib.systems.LevelEditorSystem;
import contrib.utils.components.Debugger;
import core.Game;
import core.configuration.KeyboardConfig;
import core.game.ECSManagement;
import core.game.GameLoop;
import core.game.PreRunConfiguration;
import core.level.loader.DungeonLoader;
import core.network.messages.s2c.LevelChangeEvent;
import core.systems.*;
import core.utils.CursorUtil;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import level.LastHourLevel1;

/**
 * Entry point for running a minimal dungeon game instance.
 *
 * <p>This starter initializes the game framework, loads the dungeon configuration, spawns a basic
 * player, and starts the game loop. It is mainly used to verify that the engine runs correctly with
 * a simple setup.
 *
 * <p>Usage: run with the Gradle task {@code runBasicStarter}.
 */
public class TheLastHour {

  private static boolean DEBUG_MODE = true;
  private static boolean RUN_MP_SERVER = false;

  /**
   * Main entry point to launch the basic dungeon game.
   *
   * @param args command-line arguments (not used in this starter)
   */
  public static void main(String[] args) {
    if (RUN_MP_SERVER) {
      Game.userOnFrame(TheLastHour::onFrame);
      PreRunConfiguration.multiplayerEnabled(true);
      PreRunConfiguration.isNetworkServer(true);
    }

    DungeonLoader.addLevel(Tuple.of("lasthour", LastHourLevel1.class));
    try {
      Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.disableAudio(false);
    Game.userOnSetup(TheLastHour::onUserSetup);
    Game.frameRate(60);
    Game.windowTitle("The Last Hour");
    Game.run();
  }

  private static void onUserSetup() {
    if (RUN_MP_SERVER) {
      ECSManagement.add(new PositionSystem());
      ECSManagement.add(new VelocitySystem());
      ECSManagement.add(new FrictionSystem());
      ECSManagement.add(new MoveSystem());

      ECSManagement.system(
          LevelSystem.class,
          levelSystem ->
              levelSystem.onLevelLoad(
                  () -> {
                    GameLoop.onLevelLoad.execute();
                    Game.network().broadcast(LevelChangeEvent.currentLevel(), true);
                  }));
    } else {
      Game.add(HeroBuilder.builder().characterClass(CharacterClass.WIZARD).build());
      Game.stage().ifPresent(CursorUtil::initListener);
    }

    ECSManagement.add(new CollisionSystem());
    ECSManagement.add(new KeypadSystem());

    if (DEBUG_MODE && !Game.isHeadless()) {
      ECSManagement.add(new Debugger());
      ECSManagement.add(new DebugDrawSystem());
      ECSManagement.add(new LevelEditorSystem());
    }
  }

  private static void onFrame() {
    HeroController.drainAndApplyInputs();
  }
}
