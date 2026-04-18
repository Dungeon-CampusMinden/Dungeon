package contrib.debug;

import contrib.entities.DungeonMonster;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.utils.Point;
import core.utils.logging.DungeonLogger;
import contrib.utils.components.skill.SkillTools;

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

    Entity monster = DungeonMonster.randomMonster().builder().build(position);
    monster.name("Debug Monster");
    Game.add(monster);

    LOGGER.info("Spawned monster at position {}", position);
  }
}
