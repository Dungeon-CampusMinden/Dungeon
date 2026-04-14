package contrib.debug;

import contrib.components.AIComponent;
import contrib.components.CollideComponent;
import contrib.components.HealthComponent;
import contrib.utils.components.ai.fight.AIChaseBehaviour;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.transition.SelfDefendTransition;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.DungeonLogger;

/** Creates debug-only monster entities for runtime testing. */
public final class DebugMonsterSpawner {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(DebugMonsterSpawner.class);

  private DebugMonsterSpawner() {}

  /** Spawns a debug monster at the current cursor position. */
  public static void spawnAtCursor() {
    LOGGER.info("Spawn Monster on Cursor");
    spawnAt(SkillTools.cursorPositionAsPoint());
  }

  /**
   * Spawns a debug monster at the given position if the target tile exists and is accessible.
   *
   * @param position target spawn position
   */
  public static void spawnAt(Point position) {
    Tile tile = null;
    try {
      tile = Game.tileAt(position).orElse(null);
    } catch (NullPointerException ex) {
      LOGGER.info(ex.getMessage());
    }

    if (tile == null || !tile.isAccessible()) {
      LOGGER.info("Cannot spawn monster at non-existent or non-accessible tile");
      return;
    }

    Entity monster = new Entity("Debug Monster");

    monster.add(new PositionComponent(position));
    monster.add(new DrawComponent(new SimpleIPath("character/monster/chort")));
    monster.add(new VelocityComponent(1));
    monster.add(new HealthComponent());
    monster.add(new CollideComponent());
    monster.add(
      new AIComponent(
        new AIChaseBehaviour(1), new RadiusWalk(5, 1), new SelfDefendTransition()));

    Game.add(monster);
    LOGGER.info("Spawned monster at position {}", position);
  }
}
