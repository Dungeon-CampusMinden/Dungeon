package contrib.utils.components.collide;

import contrib.components.CollideComponent;
import core.Game;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Vector2;
import java.util.List;
import java.util.Set;

/** Utility class for handling various collision detection things. */
public class CollisionUtils {

  /**
   * A small offset to prevent floating-point precision issues when checking collisions at the
   * corner of an entity.
   */
  public static final float TOP_OFFSET = 0.0001f;

  public static boolean isCollidingWithOtherSolids(Collider collider, Point newPos) {
    Point oldPos = collider.position();
    collider.position(newPos);

    boolean colliding =
        Game.levelEntities(Set.of(CollideComponent.class))
            .anyMatch(
                other -> {
                  CollideComponent ccOther = other.fetch(CollideComponent.class).orElseThrow();
                  if (!ccOther.isSolid()) return false;

                  Collider colliderOther = ccOther.collider();
                  if (collider == colliderOther) return false; // Don't check against itself

                  boolean isColliding = collider.collide(colliderOther);
                  return isColliding;
                });

    collider.position(oldPos);
    return colliding;
  }

  /**
   * Checks if a collider, when set on a specific position, is colliding with any level tiles that
   * are not accessible.
   *
   * @param collider the collider
   * @param pos the position to check for collision
   * @param canEnterPits whether the collider can enter pit tiles
   * @param canEnterWalls whether the collider can enter wall tiles
   * @param canEnterGitter whether the entity can enter gitter tiles
   * @param canEnterGlassWalls whether the entity can enter glasswall tile
   * @return true if any corner of the collider is colliding with a non-accessible tile, false
   *     otherwise
   */
  public static boolean isCollidingWithLevel(
      Collider collider,
      Point pos,
      boolean canEnterPits,
      boolean canEnterWalls,
      boolean canEnterGitter,
      boolean canEnterGlassWalls) {
    List<Vector2> corners = collider.cornersScaled();
    return corners.stream()
        .anyMatch(
            v ->
                !tileIsAccessible(
                    Game.tileAt(pos.translate(v)).orElse(null),
                    canEnterPits,
                    canEnterWalls,
                    canEnterGitter,
                    canEnterGlassWalls));
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
   * @param canEnterGitter whether the entity can enter gitter tiles
   * @param canEnterGlassWalls whether the entity can enter glasswall tile
   * @return true if tile is accessible or a pit tile that can be entered, false otherwise
   */
  public static boolean tileIsAccessible(
      Tile tile,
      boolean canEnterPitTiles,
      boolean canEnterWalls,
      boolean canEnterGitter,
      boolean canEnterGlassWalls) {
    return tile != null
        && (tile.isAccessible()
            || (canEnterPitTiles && tile.levelElement().equals(LevelElement.PIT))
            || (canEnterGitter && tile.levelElement().equals(LevelElement.GITTER))
            || (canEnterGlassWalls && tile.levelElement().equals(LevelElement.GLASSWALL))
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
   * @param canEnterGitter whether the entity can enter gitter tiles
   * @param canEnterGlassWall whether the entity can enter glasswall tile
   * @return true if the entire path from start to target is clear; false if a tile in between is
   *     blocked
   */
  public static boolean isPathClearByStepping(
      Point from,
      Point to,
      boolean canEnterPitTiles,
      boolean canEnterWalls,
      boolean canEnterGitter,
      boolean canEnterGlassWall) {
    Vector2 direction = from.vectorTo(to);
    double distance = direction.length();

    if (distance == 0f) return true;

    // Choose a small step size to ensure all intermediate tiles are checked (including diagonals)
    Vector2 step = direction.normalize().scale(0.1f);
    Point current = from;

    // Step from start to end and check each tile along the way
    for (float traveled = 0; traveled <= distance; traveled += step.length()) {
      Tile tile = Game.tileAt(current).orElse(null);
      if (!tileIsAccessible(
          tile, canEnterPitTiles, canEnterWalls, canEnterGitter, canEnterGlassWall)) {
        return false;
      }
      current = current.translate(step);
    }

    // Ensure that the final destination tile is also checked
    return tileIsAccessible(
        Game.tileAt(to).orElse(null),
        canEnterPitTiles,
        canEnterWalls,
        canEnterGitter,
        canEnterGlassWall);
  }

  // ========== Collision Math ==========

  /**
   * Checks whether a point lies within a rectangle.
   *
   * @param rectX the X coordinate of the rectangle’s bottom-left corner
   * @param rectY the Y coordinate of the rectangle’s bottom-left corner
   * @param rectWidth the width of the rectangle
   * @param rectHeight the height of the rectangle
   * @param point the point to test
   * @return {@code true} if the point lies within the rectangle; {@code false} otherwise
   */
  public static boolean rectCollidesPoint(
      float rectX, float rectY, float rectWidth, float rectHeight, Point point) {
    return point.x() >= rectX
        && point.x() < rectX + rectWidth
        && point.y() >= rectY
        && point.y() < rectY + rectHeight;
  }

  /**
   * Checks whether a circle contains a given point.
   *
   * @param position the center position of the circle
   * @param radius the radius of the circle
   * @param point the point to test
   * @return {@code true} if the point lies inside the circle; {@code false} otherwise
   */
  public static boolean circleCollidesPoint(Point position, float radius, Point point) {
    return Math.pow(position.distance(point), 2) < (double) radius * (double) radius;
  }

  /**
   * Checks whether a circle intersects with a line segment.
   *
   * <p>A collision occurs if any point on the line segment lies within the circle.
   *
   * @param position the center position of the circle
   * @param radius the radius of the circle
   * @param lineFrom the starting point of the line segment
   * @param lineTo the ending point of the line segment
   * @return {@code true} if the circle intersects the line segment; {@code false} otherwise
   */
  public static boolean circleCollidesLine(
      Point position, float radius, Point lineFrom, Point lineTo) {
    Point closestPoint = closestPointOnLine(lineFrom, lineTo, position);
    return circleCollidesPoint(position, radius, closestPoint);
  }

  /**
   * Finds the closest point on a line segment to a given reference point.
   *
   * <p>If the perpendicular projection of the reference point onto the line falls outside the
   * segment, the nearest endpoint is returned instead.
   *
   * @param lineA the starting point of the line segment
   * @param lineB the ending point of the line segment
   * @param closestTo the point for which to find the nearest location on the line
   * @return the closest point on the line segment to {@code closestTo}
   */
  public static Point closestPointOnLine(Point lineA, Point lineB, Point closestTo) {
    Vector2 closeToA = lineA.vectorTo(closestTo);
    Vector2 line = lineA.vectorTo(lineB);
    float t = (float) Math.clamp(closeToA.dot(line) / line.dot(line), 0.0f, 1f);
    return lineA.translate(line.scale(t));
  }

  /**
   * Determines whether a circle intersects or overlaps with a rectangle.
   *
   * <p>A collision is reported if the circle’s center lies inside the rectangle or if any part of
   * the circle’s boundary touches or crosses any edge of the rectangle. Circles that are entirely
   * outside the rectangle without touching it do not count as collisions.
   *
   * @param rectX the X coordinate of the rectangle’s bottom-left corner
   * @param rectY the Y coordinate of the rectangle’s bottom-left corner
   * @param rectWidth the width of the rectangle
   * @param rectHeight the height of the rectangle
   * @param circlePos the center position of the circle
   * @param circleRadius the radius of the circle
   * @return {@code true} if the circle intersects or overlaps the rectangle; {@code false}
   *     otherwise
   */
  public static boolean rectCollidesCircle(
      float rectX,
      float rectY,
      float rectWidth,
      float rectHeight,
      Point circlePos,
      float circleRadius) {
    // If circle center is inside the rectangle, collision occurs
    if (rectCollidesPoint(rectX, rectY, rectWidth, rectHeight, circlePos)) return true;

    PointSector sector = getSector(rectX, rectY, rectWidth, rectHeight, circlePos);

    // --- TOP edge (y = rectY + rectHeight) ---
    if (sector.and(PointSector.TOP) != PointSector.CENTER) {
      Point edgeStart = new Point(rectX, rectY + rectHeight);
      Point edgeEnd = new Point(rectX + rectWidth, rectY + rectHeight);
      if (circleCollidesLine(circlePos, circleRadius, edgeStart, edgeEnd)) return true;
    }

    // --- BOTTOM edge (y = rectY) ---
    if (sector.and(PointSector.BOTTOM) != PointSector.CENTER) {
      Point edgeStart = new Point(rectX, rectY);
      Point edgeEnd = new Point(rectX + rectWidth, rectY);
      if (circleCollidesLine(circlePos, circleRadius, edgeStart, edgeEnd)) return true;
    }

    // --- LEFT edge (x = rectX) ---
    if (sector.and(PointSector.LEFT) != PointSector.CENTER) {
      Point edgeStart = new Point(rectX, rectY);
      Point edgeEnd = new Point(rectX, rectY + rectHeight);
      if (circleCollidesLine(circlePos, circleRadius, edgeStart, edgeEnd)) return true;
    }

    // --- RIGHT edge (x = rectX + rectWidth) ---
    if (sector.and(PointSector.RIGHT) != PointSector.CENTER) {
      Point edgeStart = new Point(rectX + rectWidth, rectY);
      Point edgeEnd = new Point(rectX + rectWidth, rectY + rectHeight);
      if (circleCollidesLine(circlePos, circleRadius, edgeStart, edgeEnd)) return true;
    }

    return false;
  }

  /**
   * Determines whether a line segment intersects or passes through a rectangle.
   *
   * <p>A collision is reported if any part of the line lies inside the rectangle or if the line
   * crosses any of its edges. Lines that are completely outside and on the same side of the
   * rectangle do not count as collisions.
   *
   * @param rectX the X coordinate of the rectangle’s bottom-left corner
   * @param rectY the Y coordinate of the rectangle’s bottom-left corner
   * @param rectWidth the width of the rectangle
   * @param rectHeight the height of the rectangle
   * @param lineStart the starting point of the line segment
   * @param lineEnd the ending point of the line segment
   * @return {@code true} if the line intersects or passes through the rectangle; {@code false}
   *     otherwise
   */
  public static boolean rectCollidesLine(
      float rectX, float rectY, float rectWidth, float rectHeight, Point lineStart, Point lineEnd) {
    PointSector startSector = getSector(rectX, rectY, rectWidth, rectHeight, lineStart);
    PointSector endSector = getSector(rectX, rectY, rectWidth, rectHeight, lineEnd);

    // If either endpoint is inside the rectangle, we have a collision
    if (startSector == PointSector.CENTER || endSector == PointSector.CENTER) return true;

    // If both points are outside on the same side, no collision
    if (startSector.and(endSector) != PointSector.CENTER) return false;

    PointSector combinedSectors = startSector.or(endSector);

    // --- TOP edge (y = rectY + rectHeight) ---
    if (combinedSectors.and(PointSector.TOP) != PointSector.CENTER) {
      Point edgeStart = new Point(rectX, rectY + rectHeight);
      Point edgeEnd = new Point(rectX + rectWidth, rectY + rectHeight);
      if (lineCheck(edgeStart, edgeEnd, lineStart, lineEnd)) return true;
    }

    // --- BOTTOM edge (y = rectY) ---
    if (combinedSectors.and(PointSector.BOTTOM) != PointSector.CENTER) {
      Point edgeStart = new Point(rectX, rectY);
      Point edgeEnd = new Point(rectX + rectWidth, rectY);
      if (lineCheck(edgeStart, edgeEnd, lineStart, lineEnd)) return true;
    }

    // --- LEFT edge (x = rectX) ---
    if (combinedSectors.and(PointSector.LEFT) != PointSector.CENTER) {
      Point edgeStart = new Point(rectX, rectY);
      Point edgeEnd = new Point(rectX, rectY + rectHeight);
      if (lineCheck(edgeStart, edgeEnd, lineStart, lineEnd)) return true;
    }

    // --- RIGHT edge (x = rectX + rectWidth) ---
    if (combinedSectors.and(PointSector.RIGHT) != PointSector.CENTER) {
      Point edgeStart = new Point(rectX + rectWidth, rectY);
      Point edgeEnd = new Point(rectX + rectWidth, rectY + rectHeight);
      if (lineCheck(edgeStart, edgeEnd, lineStart, lineEnd)) return true;
    }

    return false;
  }

  /**
   * Determines whether two line segments intersect.
   *
   * <p>The method treats each line as a finite segment defined by its start and end points: {@code
   * a1 → a2} and {@code b1 → b2}. It checks if these two segments cross at any point.
   *
   * @param a1 the starting point of the first line segment
   * @param a2 the ending point of the first line segment
   * @param b1 the starting point of the second line segment
   * @param b2 the ending point of the second line segment
   * @return {@code true} if the line segments intersect at any point within their finite bounds;
   *     {@code false} otherwise (parallel, disjoint, or only meeting outside segment ranges)
   */
  private static boolean lineCheck(Point a1, Point a2, Point b1, Point b2) {
    Vector2 line1 = a1.vectorTo(a2);
    Vector2 line2 = b1.vectorTo(b2);

    float determinant = line1.x() * line2.y() - line1.y() * line2.x();
    if (determinant == 0.0f) return false; // Lines are parallel (no intersection)

    Vector2 aToB = a1.vectorTo(b1);

    // Parametric position along line1 (0..1 means intersection within the segment)
    float t = (aToB.x() * line2.y() - aToB.y() * line2.x()) / determinant;
    if (t < 0.0f || t > 1.0f) return false;

    // Parametric position along line2 (0..1 means intersection within the segment)
    float u = (aToB.x() * line1.y() - aToB.y() * line1.x()) / determinant;
    return u >= 0.0f && u <= 1.0f;
  }

  // ===== PointSector =====
  /**
   * Determines which region (sector) a point lies in relative to a rectangle.
   *
   * <p>The method returns a {@link PointSector} that describes whether the point lies inside the
   * rectangle ( {@link PointSector#CENTER} ), strictly to one of the four cardinal sides ({@link
   * PointSector#LEFT}, {@link PointSector#RIGHT}, {@link PointSector#TOP}, {@link
   * PointSector#BOTTOM}), or in one of the four diagonal corner regions (e.g. {@link
   * PointSector#TOP_LEFT}).
   *
   * <p>Important boundary semantics:
   *
   * <ul>
   *   <li>The rectangle is treated as a half-open region along both axes: {@code [rectX, rectX +
   *       rectWidth) × [rectY, rectY + rectHeight)}.
   *   <li>Points with {@code x == rectX} or {@code y == rectY} are considered inside
   *       horizontally/vertically (i.e. not LEFT/BOTTOM).
   *   <li>Points with {@code x == rectX + rectWidth} or {@code y == rectY + rectHeight} are
   *       considered outside on the RIGHT/TOP side respectively (those flags are set).
   * </ul>
   *
   * <p>Implementation note: the method composes a small bitmask from horizontal and vertical
   * comparisons and converts that mask to a {@link PointSector} constant.
   *
   * @param rectX X coordinate of the rectangle's bottom-left corner
   * @param rectY Y coordinate of the rectangle's bottom-left corner
   * @param rectWidth rectangle width (non-negative)
   * @param rectHeight rectangle height (non-negative)
   * @param point the point to classify
   * @return the {@link PointSector} describing the point's region relative to the rectangle
   */
  private static PointSector getSector(
      float rectX, float rectY, float rectWidth, float rectHeight, Point point) {
    int sectorValue = PointSector.CENTER.value();

    // Horizontal check
    if (point.x() < rectX) sectorValue |= PointSector.LEFT.value();
    else if (point.x() >= rectX + rectWidth) sectorValue |= PointSector.RIGHT.value();

    // Vertical check
    if (point.y() < rectY) sectorValue |= PointSector.BOTTOM.value();
    else if (point.y() >= rectY + rectHeight) sectorValue |= PointSector.TOP.value();

    return PointSector.fromValue(sectorValue);
  }

  /**
   * Represents the relative sector of a point in relation to a rectangle.
   *
   * <p>Each constant corresponds to a position either inside, along an edge, or diagonally outside
   * a rectangle. The enum uses bitmask values so that combinations can be represented and resolved
   * programmatically.
   *
   * <p>The bit values are combined as follows:
   *
   * <ul>
   *   <li>{@code TOP = 1}
   *   <li>{@code BOTTOM = 2}
   *   <li>{@code RIGHT = 4}
   *   <li>{@code LEFT = 8}
   * </ul>
   *
   * <p>These can be ORed together to produce compound sectors, for example:
   *
   * <ul>
   *   <li>{@code TOP_LEFT = TOP | LEFT}
   *   <li>{@code BOTTOM_RIGHT = BOTTOM | RIGHT}
   * </ul>
   */
  private enum PointSector {
    CENTER(0),
    TOP(1),
    BOTTOM(2),
    LEFT(8),
    RIGHT(4),
    TOP_LEFT(9), // LEFT | TOP
    TOP_RIGHT(5), // RIGHT | TOP
    BOTTOM_LEFT(10), // LEFT | BOTTOM
    BOTTOM_RIGHT(6); // RIGHT | BOTTOM

    private final int value;

    PointSector(int value) {
      this.value = value;
    }

    public int value() {
      return value;
    }

    public PointSector and(PointSector other) {
      return fromValue(this.value & other.value);
    }

    public PointSector or(PointSector other) {
      return fromValue(this.value | other.value);
    }

    public static PointSector fromValue(int value) {
      for (PointSector ps : values()) {
        if (ps.value == value) {
          return ps;
        }
      }
      // If combination isn't predefined, just return CENTER (or create a new instance if desired)
      return CENTER;
    }
  }
}
