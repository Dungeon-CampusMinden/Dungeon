package starter;

import contrib.components.InventoryComponent;
import contrib.entities.EntityFactory;
import contrib.item.concreteItem.ItemResourceMushroomRed;
import contrib.systems.*;
import core.Entity;
import core.Game;
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
  public static void main(String[] args) {
    DungeonLoader.addLevel(Tuple.of("playground", DungeonLevel.class));
    try {
      Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.disableAudio(true);
    Game.userOnSetup(() -> {
      Entity hero = EntityFactory.newHero();
      hero.fetch(InventoryComponent.class).ifPresent(InventoryComponent -> {
        InventoryComponent.add(new ItemResourceMushroomRed());
        InventoryComponent.add(new ItemResourceMushroomRed());
        InventoryComponent.add(new ItemResourceMushroomRed());
        InventoryComponent.add(new ItemResourceMushroomRed());
      });
      Game.add(hero);
      Game.add(new CollisionSystem());
      Game.add(new AISystem());
      Game.add(new ProjectileSystem());
      Game.add(new HealthSystem());
      Game.add(new HudSystem());
      Game.add(new SpikeSystem());
      Game.add(new LevelTickSystem());
      Game.add(new PitSystem());
      Game.add(new EventScheduler());
      Game.add(new LeverSystem());
      Game.add(new PressurePlateSystem());

    });
    Game.frameRate(30);
    Game.windowTitle("Basic Dungeon");
    Game.run();
  }
}
