package core.level.utils;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.util.*;

/** Offers some utility functions to work on and with {@link core.level.elements.ILevel}. */
public final class LevelUtils {

  private static final Random RANDOM = new Random();

  /**
   * Finds the path from the given point to another given point.
   *
   * <p>Throws an IllegalArgumentException if the tile at 'from' or 'to' is non-accessible.
   *
   * @param from The start point.
   * @param to The end point.
   * @return Path from the start point to the end point.
   */
  public static GraphPath<Tile> calculatePath(final Point from, final Point to) {
    return calculatePath(from.toCoordinate(), to.toCoordinate());
  }

  /**
   * Finds the path from the given coordinate to another given coordinate.
   *
   * <p>Throws an IllegalArgumentException if the tile at the start or end is non-accessible.
   *
   * @param from The start coordinate.
   * @param to The end coordinate.
   * @return Path from the start coordinate to the end coordinate.
   */
  public static GraphPath<Tile> calculatePath(final Coordinate from, final Coordinate to) {
    Tile fromTile = Game.tileAT(from);
    Tile toTile = Game.tileAT(to);
    if (fromTile == null || !fromTile.isAccessible()) return new DefaultGraphPath<>();
    if (toTile == null || !toTile.isAccessible()) return new DefaultGraphPath<>();
    return Game.findPath(Game.tileAT(from), Game.tileAT(to));
  }

  /**
   * Finds the path to a random (accessible) tile in the given radius, starting from the given
   * point.
   *
   * <p>If there is no accessible tile in the range, the path will be calculated from the given
   * start point to the given start point.
   *
   * <p>Throws an IllegalArgumentException if the tile at the start point is non-accessible.
   *
   * @param point The start point.
   * @param radius Radius in which the tiles are to be considered.
   * @return Path from the center point to the randomly selected tile.
   */
  public static GraphPath<Tile> calculatePathToRandomTileInRange(final Point point, float radius) {
    Coordinate newPosition =
        randomAccessibleTileCoordinateInRange(point, radius).orElse(point.toCoordinate());
    return calculatePath(point.toCoordinate(), newPosition);
  }

  /**
   * Finds the path to a random (accessible) tile in the given radius, starting from the position of
   * the given entity.
   *
   * <p>If there is no accessible tile in the range, the path will be calculated from the given
   * start point to the given start point.
   *
   * <p>Throws an IllegalArgumentException if the entities position is on a non-accessible tile.
   *
   * @param entity Entity whose position is the center point.
   * @param radius Radius in which the tiles are to be considered.
   * @return Path from the position of the entity to the randomly selected tile.
   */
  public static GraphPath<Tile> calculatePathToRandomTileInRange(
      final Entity entity, float radius) {
    Point point =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class))
            .position();
    return calculatePathToRandomTileInRange(point, radius);
  }

  /**
   * Finds the path from the position of one entity to the position of another entity.
   *
   * <p>Throws an IllegalArgumentException if one of the entities position is non-accessible.
   *
   * @param from Entity whose position is the start point.
   * @param to Entity whose position is the goal point.
   * @return Path from one entity to the other entity.
   */
  public static GraphPath<Tile> calculatePath(final Entity from, final Entity to) {
    PositionComponent fromPositionComponent =
        from.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(from, PositionComponent.class));
    PositionComponent positionComponent =
        to.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(to, PositionComponent.class));
    return calculatePath(fromPositionComponent.position(), positionComponent.position());
  }

  /**
   * Finds the path from the position of one entity to the position of the hero.
   *
   * <p>If no hero exists in the game, the path will be calculated from the given entity to the
   * given entity.
   *
   * <p>Throws an IllegalArgumentException if one of the entities position is non-accessible.
   *
   * @param entity Entity from which the path to the hero is calculated.
   * @return Path from the entity to the hero, if there is no hero, the path from the entity to
   *     itself.
   */
  public static GraphPath<Tile> calculatePathToHero(final Entity entity) {
    Optional<Entity> hero = Game.hero();
    if (hero.isPresent()) return calculatePath(entity, hero.get());
    else return calculatePath(entity, entity);
  }

  /**
   * Get the last Tile in the given GraphPath.
   *
   * @param path Considered GraphPath.
   * @return Last Tile in the given path.
   * @see GraphPath
   */
  public static Tile lastTile(final GraphPath<Tile> path) {
    return path.get(path.getCount() - 1);
  }

  /**
   * Get all tiles within a specified range around a given center point.
   *
   * <p>The range is determined by the provided radius.
   *
   * <p>The tile at the given point will be part of the list as well.
   *
   * @param center The center point around which the tiles are considered.
   * @param radius The radius within which the tiles should be located.
   * @return List of tiles in the given radius around the center point.
   */
  public static List<Tile> tilesInRange(final Point center, float radius) {
    // offset of neighbour Tiles which may not be accessible
    Coordinate[] offsets =
        new Coordinate[] {
          new Coordinate(-1, -1),
          new Coordinate(0, -1),
          new Coordinate(1, -1),
          new Coordinate(-1, 0),
          new Coordinate(1, 0),
          new Coordinate(-1, 1),
          new Coordinate(0, 1),
          new Coordinate(1, 1),
        };
    // all found tiles
    Set<Tile> tiles = new HashSet<>();
    // BFS queue
    Queue<Tile> tileQueue = new ArrayDeque<>();
    Tile start = Game.tileAT(center);
    if (start != null) tileQueue.add(start);
    while (tileQueue.size() > 0) {
      Tile current = tileQueue.remove();
      boolean added = tiles.add(current);
      if (added) {
        // Tile is a new Tile so add the neighbours to be checked
        for (Coordinate offset : offsets) {
          if (current.level() == null) continue;
          Tile tile = current.level().tileAt(current.coordinate().add(offset));
          if (tile != null && isInRange(center, radius, tile)) tileQueue.add(tile);
        }
      }
    }
    tiles.removeIf(Objects::isNull);
    return new ArrayList<>(tiles);
  }

  private static boolean isInRange(final Point center, float radius, final Tile tile) {
    return isAnyCornerOfTileInRadius(center, radius, tile)
        || isPointBarelyInTile(center, radius, tile);
  }

  private static boolean isPointBarelyInTile(final Point center, float radius, final Tile tile) {
    // left max distance
    Point xMin = new Point(-radius, 0).add(center);
    // right max distance
    Point xMax = new Point(radius, 0).add(center);
    // up max distance
    Point yMin = new Point(0, -radius).add(center);
    // down max distance
    Point yMax = new Point(0, radius).add(center);
    return isPointInTile(xMin, tile)
        || isPointInTile(xMax, tile)
        || isPointInTile(yMin, tile)
        || isPointInTile(yMax, tile);
  }

  private static boolean isPointInTile(final Point point, final Tile tile) {
    return tile.coordinate().toPoint().x < point.x
        && point.x < (tile.coordinate().toPoint().x + 1)
        && tile.coordinate().toPoint().y < point.y
        && point.y < (tile.coordinate().toPoint().y + 1);
  }

  private static boolean isAnyCornerOfTileInRadius(
      final Point center, float radius, final Tile tile) {
    return Point.inRange(
            center, new Point(tile.coordinate().toPoint().x, tile.coordinate().toPoint().y), radius)
        || Point.inRange(
            center,
            new Point(tile.coordinate().toPoint().x + 1, tile.coordinate().toPoint().y),
            radius)
        || Point.inRange(
            center,
            new Point(tile.coordinate().toPoint().x, tile.coordinate().toPoint().y + 1),
            radius)
        || Point.inRange(
            center,
            new Point(tile.coordinate().toPoint().x + 1, tile.coordinate().toPoint().y + 1),
            radius);
  }

  /**
   * Get all accessible tiles within a specified range around a given center point.
   *
   * <p>The range is determined by the provided radius.
   *
   * <p>The tile at the given point will be part of the list as well, if it is accessible.
   *
   * @param center The center point around which the tiles are considered.
   * @param radius The radius within which the accessible tiles should be located.
   * @return List of accessible tiles in the given radius around the center point.
   */
  public static List<Tile> accessibleTilesInRange(final Point center, float radius) {
    List<Tile> tiles = tilesInRange(center, radius);
    tiles.removeIf(tile -> !tile.isAccessible() && !(tile instanceof DoorTile));
    return tiles;
  }

  /**
   * Get a random accessible tile coordinate within a specified range around a given center point.
   *
   * <p>The range is determined by the provided radius.
   *
   * <p>The tile at the given point can be the return value as well if it is accessible.
   *
   * @param center The center point around which the tiles are considered.
   * @param radius The radius within which the accessible tiles should be located.
   * @return An Optional containing a random Coordinate object representing an accessible tile
   *     within the range, or an empty Optional if no accessible tiles were found.
   */
  public static Optional<Coordinate> randomAccessibleTileCoordinateInRange(
      final Point center, float radius) {
    List<Tile> tiles = accessibleTilesInRange(center, radius);
    if (tiles.isEmpty()) return Optional.empty();
    Coordinate newPosition = tiles.get(RANDOM.nextInt(tiles.size())).coordinate();
    return Optional.of(newPosition);
  }

  /**
   * Check if two entities are positioned in a specified range from each other.
   *
   * @param entity1 The first entity which is considered.
   * @param entity2 The second entity which is to be searched for in the given range.
   * @param range The range in which the two entities are positioned from each other.
   * @return True if the position of the two entities is within the given range, else false.
   */
  public static boolean entityInRange(final Entity entity1, final Entity entity2, float range) {
    Point entity1Position =
        entity1
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity1, PositionComponent.class))
            .position();
    Point entity2Position =
        entity2
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity2, PositionComponent.class))
            .position();
    return Point.inRange(entity1Position, entity2Position, range);
  }

  /**
   * Check if the player is in the given range of an entity.
   *
   * @param entity Entity whose position specifies the center point.
   * @param range The range within which the player should be located.
   * @return True if the position of the player is within the given radius of the position of the
   *     given entity. If there is no hero, return false.
   */
  public static boolean playerInRange(final Entity entity, float range) {
    Optional<Entity> hero = Game.hero();
    return hero.filter(value -> entityInRange(entity, value, range)).isPresent();
  }

  /**
   * Get the tiles in the line of sight between two points.
   *
   * @param startPoint The start point.
   * @param endPoint The end point.
   * @param sampleSize The number of tiles to skip before adding the next tile to the list. A higher
   *     sample size will result in a faster calculation, but may miss some tiles.
   * @param maxIterations The maximum number of iterations before the calculation is stopped.
   *     (Distance in Tiles)
   * @return List of tiles in the line of sight between the two points.
   */
  public static List<Tile> ray(
      Point startPoint, Point endPoint, int sampleSize, int maxIterations) {
    List<Tile> tilesInRay = new ArrayList<>();
    float startX = startPoint.x;
    float startY = startPoint.y;
    float endX = endPoint.x;
    float endY = endPoint.y;
    int deltaX = Math.round(Math.abs(endX - startX));
    int deltaY = Math.round(Math.abs(endY - startY));
    int stepX = startX < endX ? 1 : -1;
    int stepY = startY < endY ? 1 : -1;
    int error = deltaX - deltaY;
    int iterationCount = 0;
    while (iterationCount < maxIterations) {
      if (iterationCount % sampleSize == 0) {
        Tile tile = Game.tileAT(new Point(startX, startY));
        if (tile != null) tilesInRay.add(tile);
      }
      if (startX == endX && startY == endY) break;
      int error2 = 2 * error;
      if (error2 > -deltaY) {
        error -= deltaY;
        startX += stepX;
      }
      if (error2 < deltaX) {
        error += deltaX;
        startY += stepY;
      }
      iterationCount++;
    }
    return tilesInRay;
  }
}
