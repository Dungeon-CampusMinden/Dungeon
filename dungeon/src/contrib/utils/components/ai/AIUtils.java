package contrib.utils.components.ai;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.path.TilePath;
import core.utils.Direction;
import core.utils.Vector2;

public class AIUtils {

  public static void followPath(final Entity entity, final TilePath path) {
    if (pathFinishedOrLeft(entity, path)) return;

    Tile currentTile = Game.tileAtEntity(entity).orElse(null);
    Tile nextTile = findNextTile(path, currentTile);

    if (nextTile == null) return;

    Vector2 direction = calculateDirection(currentTile, nextTile);

    entity
      .fetch(VelocityComponent.class)
      .ifPresent(vc -> vc.applyForce("MOVEMENT", direction.normalize().scale(vc.maxSpeed())));
  }

  public static boolean pathFinishedOrLeft(final Entity entity, final TilePath path) {
    return pathFinished(entity, path) || pathLeft(entity, path);
  }

  public static boolean pathFinished(final Entity entity, final TilePath path) {
    if (path == null || path.isEmpty()) return true;

    return entity
      .fetch(PositionComponent.class)
      .map(pc -> {
        Tile current = Game.tileAt(pc.position()).orElse(null);
        Tile last = path.last();
        return last != null && last.equals(current);
      })
      .orElse(false);
  }

  public static boolean pathLeft(final Entity entity, final TilePath path) {
    if (path == null || path.isEmpty()) return true;

    return entity
      .fetch(PositionComponent.class)
      .map(pc -> {
        Tile current = Game.tileAt(pc.position()).orElse(null);
        return !onPath(path, current);
      })
      .orElse(true);
  }

  private static Tile findNextTile(final TilePath path, final Tile currentTile) {
    if (path == null || currentTile == null || path.size() < 2) return null;

    for (int i = 0; i < path.size() - 1; i++) {
      if (currentTile.equals(path.get(i))) {
        return path.get(i + 1);
      }
    }
    return null;
  }

  private static boolean onPath(final TilePath path, final Tile currentTile) {
    if (path == null || currentTile == null) return false;
    for (Tile t : path) {
      if (currentTile.equals(t)) return true;
    }
    return false;
  }

  private static Vector2 calculateDirection(final Tile currentTile, final Tile nextTile) {
    Vector2 direction = Vector2.ZERO;
    for (Direction dir : currentTile.directionTo(nextTile)) {
      direction = direction.add(dir);
    }
    return direction;
  }
}
