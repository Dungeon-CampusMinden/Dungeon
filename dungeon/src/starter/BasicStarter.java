package starter;

import contrib.entities.EntityFactory;
import contrib.entities.MiscFactory;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.configuration.KeyboardConfig;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;

/**
 * Entry point for running a minimal dungeon game instance.
 *
 * <p>This starter initializes the game framework, loads the dungeon configuration, spawns a basic
 * player, and starts the game loop. It is mainly used to verify that the engine runs correctly with
 * a simple setup.
 *
 * <p>Usage: run with the Gradle task {@code runBasicStarter}.
 */
public class BasicStarter {

  /**
   * Main entry point to launch the basic dungeon game.
   *
   * @param args command-line arguments (not used in this starter)
   */
  static void main(String[] args) {
    GdxPlatformBootstrap.init();
    DungeonLoader.addLevel(Tuple.of("playground", DungeonLevel.class));
    try {
      Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.disableAudio(true);
    Game.userOnSetup(() -> Game.add(EntityFactory.newHero()));
    Game.frameRate(30);
    Game.windowTitle("Basic Dungeon");

    Game.userOnLevelLoad(firstLoad -> {
      if (!firstLoad) {
        return;
      }

      var start = Game.startTile().orElseThrow().position();

      Entity chest = MiscFactory.newChest();
      chest.fetch(PositionComponent.class).orElseThrow().position(start.translate(2, 0));
      Game.add(chest);
      Game.add(MiscFactory.cookingPot(start.translate(4, 0), 6));
      Game.add(MiscFactory.newCraftingCauldron(start.translate(6, 0)));
    });

    Game.run();
  }
}
