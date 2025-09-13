package core.level.utils;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.utils.*;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import java.util.*;

/** Offers some utility functions to work on and with {@link core.level.elements.ILevel}. */
public final class LevelUtils {

  private static final Random RANDOM = new Random();

  /** These vectors can be used to calculate neighbor coordinates. */
  private static final Vector2[] DELTA_VECTORS =
      new Vector2[] {
        Vector2.of(-1, 0), Vector2.of(1, 0), Vector2.of(0, -1), Vector2.of(0, 1),
      };

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
    Tile fromTile = Game.tileAt(from).orElse(null);
    Tile toTile = Game.tileAt(to).orElse(null);
    if (fromTile == null || !fromTile.isAccessible()) return new DefaultGraphPath<>();
    if (toTile == null || !toTile.isAccessible()) return new DefaultGraphPath<>();
    return Game.findPath(fromTile, toTile).orElse(new DefaultGraphPath<>());
  }

  /**
   * Calculates a path of tiles between two points, assuming they lie on a straight horizontal or
   * vertical line.
   *
   * @param from the start point
   * @param to the end point
   * @return a path containing all tiles from {@code from} to {@code to}
   */
  public static GraphPath<Tile> calculatePathInsideWall(final Point from, final Point to) {
    return calculatePathInsideWall(from.toCoordinate(), to.toCoordinate());
  }

  /**
   * Calculates a path of tiles between two coordinates, assuming they lie on a straight horizontal
   * or vertical line.
   *
   * @param from the start coordinate
   * @param to the end coordinate
   * @return a path containing all tiles from {@code from} to {@code to}, or an empty path if
   *     invalid
   */
  public static GraphPath<Tile> calculatePathInsideWall(
      final Coordinate from, final Coordinate to) {
    GraphPath<Tile> path = new DefaultGraphPath<>();

    Tile fromTile = Game.tileAt(from).orElse(null);
    Tile toTile = Game.tileAt(to).orElse(null);

    if (fromTile == null || toTile == null) {
      return path;
    }

    Coordinate current = new Coordinate(from);

    while (!current.equals(to)) {
      Tile currentTile = Game.tileAt(current).orElse(null);
      if (currentTile == null) break;
      path.add(currentTile);

      // one step towards the end coordinate
      if (current.x() < to.x()) {
        current = new Coordinate(current.x() + 1, current.y());
      } else if (current.x() > to.x()) {
        current = new Coordinate(current.x() - 1, current.y());
      } else if (current.y() < to.y()) {
        current = new Coordinate(current.x(), current.y() + 1);
      } else if (current.y() > to.y()) {
        current = new Coordinate(current.x(), current.y() - 1);
      }
    }

    path.add(toTile);

    return path;
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
    return calculatePath(point, randomAccessibleTileInRangeAsPoint(point, radius).orElse(point));
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
    Vector2[] offsets =
        new Vector2[] {
          Vector2.of(-1, -1),
          Vector2.of(0, -1),
          Vector2.of(1, -1),
          Vector2.of(-1, 0),
          Vector2.of(1, 0),
          Vector2.of(-1, 1),
          Vector2.of(0, 1),
          Vector2.of(1, 1),
        };
    // all found tiles
    Set<Tile> tiles = new HashSet<>();
    // BFS queue
    Queue<Tile> tileQueue = new ArrayDeque<>();
    Game.tileAt(center).ifPresent(tileQueue::add);
    while (tileQueue.size() > 0) {
      Tile current = tileQueue.remove();
      boolean added = tiles.add(current);
      if (added) {
        // Tile is a new Tile so add the neighbours to be checked
        for (Vector2 offset : offsets) {
          current
              .level()
              .tileAt(current.coordinate().translate(offset))
              .filter(tile -> isInRange(center, radius, tile))
              .ifPresent(tileQueue::add);
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
    Point xMin = center.translate(Vector2.of(-radius, 0));
    // right max distance
    Point xMax = center.translate(Vector2.of(radius, 0));
    // up max distance
    Point yMin = center.translate(Vector2.of(0, -radius));
    // down max distance
    Point yMax = center.translate(Vector2.of(0, radius));
    return isPointInTile(xMin, tile)
        || isPointInTile(xMax, tile)
        || isPointInTile(yMin, tile)
        || isPointInTile(yMax, tile);
  }

  private static boolean isPointInTile(final Point point, final Tile tile) {
    Point tileposition = tile.position();

    return tileposition.x() < point.x()
        && point.x() < (tileposition.x() + 1)
        && tileposition.y() < point.y()
        && point.y() < (tileposition.y() + 1);
  }

  private static boolean isAnyCornerOfTileInRadius(
      final Point center, float radius, final Tile tile) {
    Point origin = tile.position();
    Vector2[] cornerOffsets = {
      Direction.NONE, Direction.RIGHT, Direction.UP, Direction.RIGHT.add(Direction.UP)
    };
    for (Vector2 offset : cornerOffsets) {
      if (Point.inRange(center, origin.translate(offset), radius)) {
        return true;
      }
    }
    return false;
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
  public static Optional<Point> randomAccessibleTileInRangeAsPoint(
      final Point center, float radius) {
    List<Tile> accessible = accessibleTilesInRange(center, radius);
    if (accessible.isEmpty()) return Optional.empty();

    // Warning: .findAny() seems not to work here. Therefore: RANDOM
    return accessible.stream()
        .map(Tile::position)
        .skip(RANDOM.nextInt(accessible.size()))
        .findFirst();
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
    return Game.hero().filter(value -> entityInRange(entity, value, range)).isPresent();
  }

  /**
   * Get a random free tile from the current level. A free tile is a tile that is of type FLOOR and
   * is not occupied by any entity and is accessible.
   *
   * @return An Optional containing a random free tile if available, otherwise an empty Optional.
   */
  public static Optional<Tile> freeTile() {
    Tuple<Integer, Integer> levelSize = Game.currentLevel().orElse(null).size();
    int startX = RANDOM.nextInt(0, levelSize.a());
    int startY = RANDOM.nextInt(0, levelSize.b());
    boolean[][] queued = new boolean[levelSize.b()][levelSize.a()];

    // Queue to hold the cells to be explored in the form of (row, col)
    Queue<Tile> queue = new LinkedList<>();
    // Start BFS from the given start position
    Game.tileAt(new Coordinate(startX, startY)).ifPresent(queue::add);
    queued[startY][startX] = true;

    while (!queue.isEmpty()) {
      // Dequeue the front cell
      Tile cell = queue.poll();

      // We have found a free field, abort the search
      if (isFreeTile(cell)) return Optional.of(cell);

      // Explore all 4 possible directions
      for (Tile tile : neighbours(cell)) {
        Coordinate coordinate = tile.coordinate();
        // Check if the new cell is within bounds and not yet visited
        if (!queued[coordinate.y()][coordinate.x()]) {
          queue.add(tile);
          queued[coordinate.y()][coordinate.x()] = true;
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Get the neighbors of the given Tile.
   *
   * <p>Neighbors are the tiles directly above, below, left, and right of the given Tile.
   *
   * @param tile Tile to get the neighbors for
   * @return Set with the neighbor tiles.
   */
  public static Set<Tile> neighbours(final Tile tile) {
    // Direction vectors for moving up, down, left, and right+
    Set<Tile> returnSet = new HashSet<>();
    Tuple<Integer, Integer> levelSize = Game.currentLevel().orElse(null).size();
    Tile[][] layout = Game.currentLevel().orElse(null).layout();
    Coordinate coordinate = tile.coordinate();
    for (Vector2 deltaVector : DELTA_VECTORS) {
      Coordinate newCoordinate = coordinate.translate(deltaVector);
      // Check if the new cell is within bounds and not yet visited
      if (newCoordinate.x() >= 0
          && newCoordinate.x() < levelSize.a()
          && newCoordinate.y() >= 0
          && newCoordinate.y() < levelSize.b())
        returnSet.add(layout[newCoordinate.y()][newCoordinate.x()]);
    }
    return returnSet;
  }

  /**
   * Checks if the given Tile is accessible and no entity is placed on that tile.
   *
   * @param tile Tile to check.
   * @return True if the Tile is free, false if not
   */
  public static boolean isFreeTile(final Tile tile) {
    return tile.isAccessible() && Game.entityAtTile(tile).findAny().isEmpty();
  }

  /**
   * Changes the visibility of a rectangular area within the level. The area is defined by the top
   * left and bottom right coordinates. If a tile within the specified area is null, it is skipped.
   *
   * @param topLeft The top left coordinate of the area.
   * @param bottomRight The bottom right coordinate of the area.
   * @param visible The visibility status to be set for the area.
   */
  public static void changeVisibilityForArea(
      Coordinate topLeft, Coordinate bottomRight, boolean visible) {
    for (int x = topLeft.x(); x <= bottomRight.x(); x++) {
      for (int y = bottomRight.y(); y <= topLeft.y(); y++) {
        Game.tileAt(new Coordinate(x, y)).ifPresent(tile -> tile.visible(visible));
      }
    }
  }

  /**
   * Checks if a given Tile is within a given area.
   *
   * @param tile The tile to check.
   * @param topLeft The top left coordinate of the area.
   * @param bottomRight The bottom right coordinate of the area.
   * @return true if the tile is within the area, false if not.
   */
  public static boolean isTileWithinArea(Tile tile, Coordinate topLeft, Coordinate bottomRight) {
    return tile.coordinate().x() >= topLeft.x()
        && tile.coordinate().x() <= bottomRight.x()
        && tile.coordinate().y() >= bottomRight.y()
        && tile.coordinate().y() <= topLeft.y();
  }

  /**
   * Checks if the hero is in a given area.
   *
   * @param topLeft The top left coordinate of the area.
   * @param bottomRight The bottom right coordinate of the area.
   * @return true if the hero is in the area, false if not.
   * @see #isTileWithinArea(Tile, Coordinate, Coordinate)
   */
  public static boolean isHeroInArea(Coordinate topLeft, Coordinate bottomRight) {
    Entity hero = Game.hero().orElse(null);
    if (hero == null) {
      return false;
    }
    PositionComponent pc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    return Game.tileAt(pc.position())
        .map(tile -> LevelUtils.isTileWithinArea(tile, topLeft, bottomRight))
        .orElse(false);
  }

  /**
   * Returns a list of tiles within a specified rectangular area in the level.
   *
   * <p>The method iterates over the tiles in the level layout within the bounds specified by the
   * top-left and bottom-right coordinates. It adds each tile within these bounds to a list, which
   * is then returned.
   *
   * @param topLeft The top-left coordinate of the rectangular area.
   * @param bottomRight The bottom-right coordinate of the rectangular area.
   * @return A list of tiles within the specified rectangular area in the level.
   */
  public static List<Tile> tilesInArea(Coordinate topLeft, Coordinate bottomRight) {
    Tile[][] layout = Game.currentLevel().orElse(null).layout();
    List<Tile> tiles = new java.util.ArrayList<>();
    for (int x = topLeft.x(); x <= bottomRight.x(); x++) {
      for (int y = bottomRight.y(); y <= topLeft.y(); y++) {
        tiles.add(layout[y][x]);
      }
    }
    return tiles;
  }

  /**
   * Tints a given area with a specified color.
   *
   * @param start The starting coordinate of the area to tint. (top left)
   * @param end The ending coordinate of the area to tint. (bottom right)
   * @param color The color to tint the area with.
   */
  public static void tintArea(Coordinate start, Coordinate end, int color) {
    int minX = Math.min(start.x(), end.x());
    int maxX = Math.max(start.x(), end.x());
    int minY = Math.min(start.y(), end.y());
    int maxY = Math.max(start.y(), end.y());

    for (int x = minX; x <= maxX; x++) {
      for (int y = minY; y <= maxY; y++) {
        Game.tileAt(new Coordinate(x, y)).ifPresent(t -> t.tintColor(color));
      }
    }
  }
}
