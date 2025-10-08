package contrib.utils.components.collide;

import contrib.components.CollideComponent;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import java.util.Arrays;
import java.util.List;

/** Utility class for handling various collision detection things. */
public class CollisionUtils {

  /**
   * A small offset to prevent floating-point precision issues when checking collisions at the
   * corner of an entity.
   */
  public static final float TOP_OFFSET = 0.0001f;

  /**
   * Checks if the hitbox specified by the bottom-left and top-right points is colliding with any
   * level tiles that are not accessible.
   *
   * @param bottomLeft the bottom-left position of the entity's hitbox
   * @param topRight the top-right position of the entity's hitbox
   * @param canEnterPits whether the entity can enter pit tiles
   * @param canEnterWalls whether the entity can enter wall tiles
   * @return true if any corner of the hitbox is colliding with a non-accessible tile, false
   *     otherwise
   */
  public static boolean isCollidingWithLevel(
      Point bottomLeft, Point topRight, boolean canEnterPits, boolean canEnterWalls) {
    List<Point> corners =
        Arrays.asList(
            new Point(bottomLeft.x(), bottomLeft.y()), // bottom-left
            new Point(topRight.x() - TOP_OFFSET, bottomLeft.y()), // bottom-right
            new Point(bottomLeft.x(), topRight.y() - TOP_OFFSET), // top-left
            new Point(topRight.x() - TOP_OFFSET, topRight.y() - TOP_OFFSET) // top-right
            );
    return corners.stream()
        .anyMatch(p -> !tileIsAccessible(Game.tileAt(p).orElse(null), canEnterPits, canEnterWalls));
  }

  /**
   * Checks if an entity, when set on a specific position, is colliding with any level tiles that
   * are not accessible.
   *
   * @param e the entity to check for collision
   * @param pos the position to check for collision
   * @param canEnterPits whether the entity can enter pit tiles
   * @param canEnterWalls whether the entity can enter wall tiles
   * @return true if any corner of the entity's hitbox is colliding with a non-accessible tile,
   *     false otherwise
   */
  public static boolean isCollidingWithLevel(
      Entity e, Point pos, boolean canEnterPits, boolean canEnterWalls) {
    PositionComponent pc =
        e.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, PositionComponent.class));
    CollideComponent cc =
        e.fetch(CollideComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, CollideComponent.class));
    return isCollidingWithLevel(
        cc.bottomLeft(pos, pc.scale()), cc.topRight(pos, pc.scale()), canEnterPits, canEnterWalls);
  }

  /**
   * Checks if an entity is colliding with any level tiles that are not accessible.
   *
   * @param e the entity to check for collision
   * @param canEnterPits whether the entity can enter pit tiles
   * @param canEnterWalls whether the entity can enter wall tiles
   * @return true if any corner of the entity's hitbox is colliding with a non-accessible tile,
   *     false otherwise
   */
  public static boolean isCollidingWithLevel(
      Entity e, boolean canEnterPits, boolean canEnterWalls) {
    CollideComponent cc =
        e.fetch(CollideComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, CollideComponent.class));
    return isCollidingWithLevel(cc.bottomLeft(e), cc.topRight(e), canEnterPits, canEnterWalls);
  }

  /**
   * Checks if a hitbox at a given position and scale is colliding with any level tiles that are not
   * accessible.
   *
   * @param pos the position of the entity
   * @param scale the scale of the entity
   * @param hitbox the hitbox component of the entity (can be null)
   * @param canEnterPits whether the entity can enter pit tiles
   * @param canEnterWalls whether the entity can enter wall tiles
   * @return true if any corner of the hitbox (or position if hitbox is null) is colliding with a
   *     non-accessible tile, false otherwise
   */
  public static boolean isCollidingWithLevel(
      Point pos,
      Vector2 scale,
      CollideComponent hitbox,
      boolean canEnterPits,
      boolean canEnterWalls) {
    if (hitbox == null) {
      // Only check for the actual position
      return !tileIsAccessible(Game.tileAt(pos).orElse(null), canEnterPits, canEnterWalls);
    }
    return isCollidingWithLevel(
        hitbox.bottomLeft(pos, scale), hitbox.topRight(pos, scale), canEnterPits, canEnterWalls);
  }

  /**
   * Checks if an entity at a given position with a specified size and offset is colliding with the
   * given {@link Point}.
   *
   * @param pos the bottom-left position of the entity
   * @param offset the offset of the hitbox
   * @param size the size of the entity
   * @param point the point to check for collision
   * @return true if any corner of the hitbox is colliding with the point, false otherwise
   */
  public static boolean isCollidingWithPoint(Point pos, Vector2 offset, Vector2 size, Point point) {
    float minX = pos.x() + offset.x();
    float minY = pos.y() + offset.y();
    float maxX = minX + size.x();
    float maxY = minY + size.y();

    // Use half-open interval \[min, max) with a small TOP_OFFSET to avoid floating-point/corner
    // issues
    return point.x() >= minX
        && point.x() < maxX - TOP_OFFSET
        && point.y() >= minY
        && point.y() < maxY - TOP_OFFSET;
  }

  /**
   * Helper method to determine if a tile can be entered by the entity.
   *
   * <p>Considers both whether the tile is accessible and whether the entity is allowed to enter pit
   * tiles.
   *
   * @param tile the tile to check for accessibility
   * @param canEnterPitTiles whether the entity can enter pit tiles
   * @param canEnterWalls whether the entity can enter wall tiles
   * @return true if tile is accessible or a pit tile that can be entered, false otherwise
   */
  public static boolean tileIsAccessible(
      Tile tile, boolean canEnterPitTiles, boolean canEnterWalls) {
    return tile != null
        && (tile.isAccessible()
            || (canEnterPitTiles && tile.levelElement().equals(LevelElement.PIT))
            || (canEnterWalls && tile.levelElement().equals(LevelElement.WALL)));
  }

  /**
   * Checks whether the path between two points is completely accessible by stepping along the
   * vector between them in small increments.
   *
   * <p>This method simulates movement from the starting point to the target by walking small steps
   * along the direction vector. At each step, it checks whether the tile is accessible or can be
   * entered (e.g., if it's a pit and the entity is allowed to enter pits).
   *
   * <p>This ensures that no wall or inaccessible tile is skipped due to large velocity steps,
   * especially important when moving diagonally or at high speeds.
   *
   * @param from the starting point
   * @param to the target point
   * @param canEnterPitTiles whether the entity is allowed to walk into pit tiles
   * @param canEnterWalls whether the entity is allowed to walk into wall tiles
   * @return true if the entire path from start to target is clear; false if a tile in between is
   *     blocked
   */
  public static boolean isPathClearByStepping(
      Point from, Point to, boolean canEnterPitTiles, boolean canEnterWalls) {
    Vector2 direction = from.vectorTo(to);
    double distance = direction.length();

    if (distance == 0f) return true;

    // Choose a small step size to ensure all intermediate tiles are checked (including diagonals)
    Vector2 step = direction.normalize().scale(0.1f);
    Point current = from;

    // Step from start to end and check each tile along the way
    for (float traveled = 0; traveled <= distance; traveled += step.length()) {
      Tile tile = Game.tileAt(current).orElse(null);
      if (!tileIsAccessible(tile, canEnterPitTiles, canEnterWalls)) {
        return false;
      }
      current = current.translate(step);
    }

    // Ensure that the final destination tile is also checked
    return tileIsAccessible(Game.tileAt(to).orElse(null), canEnterPitTiles, canEnterWalls);
  }


  // ========== Collisions ==========
  public static boolean rectCollidePoint(float rX, float rY, float rW, float rH, Point point)
  {
    return (double) point.x() >= (double) rX && (double) point.y() >= (double) rY && (double) point.x() < (double) rX + (double) rW && (double) point.y() < (double) rY + (double) rH;
  }

  public static boolean circleCollidePoint(Point position, float radius, Point point)
  {
    return Math.pow(position.distance(point), 2) < (double) radius * (double) radius;
  }

  public static boolean circleCollideLine(
    Point position,
    float radius,
    Point lineFrom,
    Point lineTo)
  {
    Point closestPoint = closestPointOnLine(lineFrom, lineTo, position);
    return circleCollidePoint(position, radius, closestPoint);
  }

  public static Point closestPointOnLine(Point lineA, Point lineB, Point closestTo)
  {
    Vector2 closeToA = lineA.vectorTo(closestTo);
    Vector2 line = lineA.vectorTo(lineB);
    float t = (float) Math.clamp(closeToA.dot(line) / line.dot(line), 0.0f, 1f);
    return lineA.translate(line.scale(t));
  }




  public static boolean rectCollideCircle(
    float rX,
    float rY,
    float rW,
    float rH,
    Point cPosition,
    float cRadius)
  {
    if (rectCollidePoint(rX, rY, rW, rH, cPosition))
      return true;
    PointSectors sector = getSector(rX, rY, rW, rH, cPosition);
    Point lineFrom;
    Point lineTo;
    if ((sector.and(PointSectors.TOP)) != PointSectors.CENTER)
    {
      lineFrom = new Point(rX, rY);
      lineTo = new Point(rX + rW, rY);
      if (circleCollideLine(cPosition, cRadius, lineFrom, lineTo))
        return true;
    }
    if ((sector.and(PointSectors.BOTTOM)) != PointSectors.CENTER)
    {
      lineFrom = new Point(rX, rY + rH);
      lineTo = new Point(rX + rW, rY + rH);
      if (circleCollideLine(cPosition, cRadius, lineFrom, lineTo))
        return true;
    }
    if ((sector.and(PointSectors.LEFT)) != PointSectors.CENTER)
    {
      lineFrom = new Point(rX, rY);
      lineTo = new Point(rX, rY + rH);
      if (circleCollideLine(cPosition, cRadius, lineFrom, lineTo))
        return true;
    }
    if ((sector.and(PointSectors.RIGHT)) != PointSectors.CENTER)
    {
      lineFrom = new Point(rX + rW, rY);
      lineTo = new Point(rX + rW, rY + rH);
      if (circleCollideLine(cPosition, cRadius, lineFrom, lineTo))
        return true;
    }
    return false;
  }

  public static boolean rectCollideLine(
    float rX,
    float rY,
    float rW,
    float rH,
    Point lineFrom,
    Point lineTo)
  {
    PointSectors sector1 = getSector(rX, rY, rW, rH, lineFrom);
    PointSectors sector2 = getSector(rX, rY, rW, rH, lineTo);
    if (sector1 == PointSectors.CENTER || sector2 == PointSectors.CENTER)
      return true;
    if ((sector1.and(sector2)) != PointSectors.CENTER)
      return false;
    PointSectors pointSectors = sector1.or(sector2);
    Point vector2;
    if ((pointSectors.and(PointSectors.TOP)) != PointSectors.CENTER)
    {
      Point a1 = new Point(rX, rY);
      vector2 = new Point(rX + rW, rY);
      Point a2 = vector2;
      Point b1 = lineFrom;
      Point b2 = lineTo;
      if (lineCheck(a1, a2, b1, b2))
        return true;
    }
    if ((pointSectors.and(PointSectors.BOTTOM)) != PointSectors.CENTER)
    {
      Point a1 = new Point(rX, rY + rH);
      vector2 = new Point(rX + rW, rY + rH);
      Point a2 = vector2;
      Point b1 = lineFrom;
      Point b2 = lineTo;
      if (lineCheck(a1, a2, b1, b2))
        return true;
    }
    if ((pointSectors.and(PointSectors.LEFT)) != PointSectors.CENTER)
    {
      Point a1 = new Point(rX, rY);
      vector2 = new Point(rX, rY + rH);
      Point a2 = vector2;
      Point b1 = lineFrom;
      Point b2 = lineTo;
      if (lineCheck(a1, a2, b1, b2))
        return true;
    }
    if ((pointSectors.and(PointSectors.RIGHT)) != PointSectors.CENTER)
    {
      Point a1 = new Point(rX + rW, rY);
      vector2 = new Point(rX + rW, rY + rH);
      Point a2 = vector2;
      Point b1 = lineFrom;
      Point b2 = lineTo;
      if (lineCheck(a1, a2, b1, b2))
        return true;
    }
    return false;
  }


  public static boolean lineCheck(Point a1, Point a2, Point b1, Point b2) {
    Vector2 line1 = a1.vectorTo(a2);
    Vector2 line2 = b1.vectorTo(b2);

    float determinant = line1.x() * line2.y() - line1.y() * line2.x();
    if (determinant == 0.0f)
      return false; // Lines are parallel (no intersection)

    Vector2 aToB = a1.vectorTo(b1);

    // Parametric position along line1 (0..1 means intersection within the segment)
    float t = (aToB.x() * line2.y() - aToB.y() * line2.x()) / determinant;
    if (t < 0.0f || t > 1.0f)
      return false;

    // Parametric position along line2 (0..1 means intersection within the segment)
    float u = (aToB.x() * line1.y() - aToB.y() * line1.x()) / determinant;
    return u >= 0.0f && u <= 1.0f;
  }


  // ===== PointSectors =====
  private static PointSectors getSector(float rX, float rY, float rW, float rH, Point point) {
    int sectorValue = PointSectors.CENTER.value();

    if (point.x() < rX)
      sectorValue |= PointSectors.LEFT.value();
    else if (point.x() >= rX + rW)
      sectorValue |= PointSectors.RIGHT.value();

    if (point.y() < rY)
      sectorValue |= PointSectors.TOP.value();
    else if (point.y() >= rY + rH)
      sectorValue |= PointSectors.BOTTOM.value();

    return PointSectors.fromValue(sectorValue);
  }

  private enum PointSectors {
    CENTER(0),
    TOP(1),
    BOTTOM(2),
    LEFT(8),
    RIGHT(4),
    TOP_LEFT(9),      // LEFT | TOP
    TOP_RIGHT(5),     // RIGHT | TOP
    BOTTOM_LEFT(10),  // LEFT | BOTTOM
    BOTTOM_RIGHT(6);  // RIGHT | BOTTOM

    private final int value;

    PointSectors(int value) {
      this.value = value;
    }

    public int value() {
      return value;
    }

    public PointSectors and(PointSectors other) {
      return fromValue(this.value & other.value);
    }

    public PointSectors or(PointSectors other) {
      return fromValue(this.value | other.value);
    }

    public static PointSectors fromValue(int value) {
      for (PointSectors ps : values()) {
        if (ps.value == value) {
          return ps;
        }
      }
      // If combination isn't predefined, just return CENTER (or create a new instance if desired)
      return CENTER;
    }
  }
}
