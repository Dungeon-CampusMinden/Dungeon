package engine.collision;

import engine.level.Tile;
import engine.level.elements.ILevel;
import engine.level.utils.LevelElement;
import engine.resources.Res;
import engine.utils.Point;
import engine.utils.Vector2;
import java.util.Objects;

/**
 * Answers whether positions in the current level are blocked for an entity.
 *
 * <p>This is a <b>resource view</b>: it keeps a {@link Res handle} to the level and offers
 * questions about it. Like a handle, it is created in the constructor of a system and kept in a
 * field. Systems use it for collision checks against the level instead of reading the level through
 * {@code Game}.
 *
 * <p>A position is blocked if there is no level, if there is no tile at the position, or if the
 * tile cannot be entered. Walls can additionally be entered by entities that are allowed to, see
 * {@link engine.components.VelocityComponent#canEnterWalls()}.
 */
public final class LevelCollisionView {

  private final Res<ILevel> level;

  /**
   * Creates a new view on the level of the given handle.
   *
   * @param level handle to the level to check against
   */
  public LevelCollisionView(final Res<ILevel> level) {
    this.level = Objects.requireNonNull(level, "level");
  }

  /**
   * Checks whether the given position is blocked.
   *
   * @param position the position to check
   * @param canEnterWalls whether the entity may enter wall tiles
   * @return {@code true} if the tile at the position cannot be entered, otherwise {@code false}
   */
  public boolean isBlocked(final Point position, final boolean canEnterWalls) {
    ILevel current = level.get().orElse(null);
    return !canEnter(tileAt(current, position), canEnterWalls);
  }

  /**
   * Checks whether any corner of the given collider is blocked when the collider is placed at the
   * given position.
   *
   * @param collider the collider whose corners are checked
   * @param position the position the collider is placed at for the check
   * @param canEnterWalls whether the entity may enter wall tiles
   * @return {@code true} if at least one corner is on a tile that cannot be entered, otherwise
   *     {@code false}
   */
  public boolean isBlocked(
      final Collider collider, final Point position, final boolean canEnterWalls) {
    ILevel current = level.get().orElse(null);
    for (Vector2 corner : collider.cornersScaled()) {
      if (!canEnter(tileAt(current, position.translate(corner)), canEnterWalls)) {
        return true;
      }
    }
    return false;
  }

  private static Tile tileAt(final ILevel level, final Point position) {
    return level == null ? null : level.tileAt(position).orElse(null);
  }

  /*
   * Kept here instead of in Tile on purpose: tests mock tiles and only stub isAccessible(). A new
   * method on Tile would return false on such mocks and silently block every position.
   */
  private static boolean canEnter(final Tile tile, final boolean canEnterWalls) {
    return tile != null
        && (tile.isAccessible() || (canEnterWalls && tile.levelElement() == LevelElement.WALL));
  }
}
