package rooms.demo;

import engine.Entity;
import engine.Game;
import engine.level.DungeonLevel;
import engine.level.loader.DungeonLoader;
import engine.utils.Point;
import engine.utils.Tuple;
import engine.utils.components.path.SimpleIPath;
import feature.entities.DungeonMonster;
import feature.entities.EntityFactory;
import feature.entities.LeverFactory;
import feature.entities.MiscFactory;
import feature.systems.AISystem;
import feature.systems.CollisionSystem;
import feature.systems.HealthSystem;
import feature.systems.ManaRestoreSystem;
import feature.systems.PathSystem;
import feature.systems.ProjectileSystem;
import java.io.IOException;
import java.util.Set;

/**
 * Starter for a small dungeon that shows the atmosphere renderer: a pillared hall, a library, a
 * crypt and an east chamber with torches, wall fountains and wandering monsters. The level file
 * chooses the dungeon atmosphere.
 *
 * <p>Usage: run with the Gradle task {@code runDungeonShowcase}.
 */
public class DungeonShowcase {

  /**
   * Starts the showcase.
   *
   * @param args not used
   * @throws IOException if the game configuration cannot be loaded
   */
  public static void main(String[] args) throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        feature.input.configuration.KeyboardConfig.class,
        engine.configuration.KeyboardConfig.class);
    Game.disableAudio(true);
    Game.frameRate(60);
    Game.userOnSetup(
        () -> {
          DungeonLoader.addLevel(Tuple.of("showcase", DungeonLevel.class));
          Game.add(new CollisionSystem());
          Game.add(new AISystem());
          Game.add(new PathSystem());
          Game.add(new HealthSystem());
          Game.add(new ProjectileSystem());
          Game.add(new ManaRestoreSystem());
          Game.add(EntityFactory.newHero());
          DungeonLoader.loadLevel(0);
        });
    Game.userOnLevelLoad(firstLoad -> populate());
    Game.windowTitle("Dungeon Showcase");
    Game.run();
  }

  private static void populate() {
    for (Point p :
        new Point[] {new Point(14, 14), new Point(33, 14), new Point(14, 25), new Point(33, 25)}) {
      Game.add(LeverFactory.createTorch(new Entity("torch"), p, true, false));
    }
    Game.add(MiscFactory.newChest(Set.of(), new Point(29, 3)));
    Game.add(MiscFactory.newChest(Set.of(), new Point(46, 17)));

    spawn(DungeonMonster.GOBLIN, 16, 19);
    spawn(DungeonMonster.IMP, 31, 20);
    spawn(DungeonMonster.ORC_SHAMAN, 24, 15);
    spawn(DungeonMonster.OGRE, 44, 21);
    spawn(DungeonMonster.ZOMBIE, 4, 21);
    spawn(DungeonMonster.ICE_ZOMBIE, 7, 17);
  }

  private static void spawn(DungeonMonster monster, float x, float y) {
    Game.add(monster.builder().build(new Point(x, y)));
  }
}
