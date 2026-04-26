package core.level.utils;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.path.ListTilePath;
import core.level.path.TilePath;
import core.utils.*;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import java.util.*;

/** Offers some utility functions to work on and with {@link core.level.elements.ILevel}. */
public final class LevelUtils {

  private LevelUtils() {}

  private static final Random RANDOM = new Random();

  /** These vectors can be used to calculate neighbor coordinates. */
  private static final Vector2[] DELTA_VECTORS =
      new Vector2[] {
        Vector2.of(-1, 0), Vector2.of(1, 0), Vector2.of(0, -1), Vector2.of(0, 1),
      };

  /**
   * Returns the last {@link Tile} of the given {@link TilePath}.
   *
   * <p>If the provided path is {@code null} or empty, this method returns {@code null}.
   *
   * @param path the path whose last tile should be returned
   * @return the last tile of the path, or {@code null} if {@code path} is {@code null} or empty
   */
  public static Tile lastTile(final TilePath path) {
    return path == null ? null : path.last();
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
    while (!tileQueue.isEmpty()) {
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
   *     given entity. If there is no player, return false.
   */
  public static boolean playerInRange(final Entity entity, float range) {
    return Game.player().filter(value -> entityInRange(entity, value, range)).isPresent();
  }

  /**
   * Get a random free tile from the current level. A free tile is a tile that is of type FLOOR and
   * is not occupied by any entity and is accessible.
   *
   * @return An Optional containing a random free tile if available, otherwise an empty Optional.
   */
  public static Optional<Tile> freeTile() {
    if (Game.currentLevel().isEmpty()) {
      return Optional.empty();
    }
    Tuple<Integer, Integer> levelSize = Game.currentLevel().get().size();
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
   * Checks if the player is in a given area.
   *
   * @param topLeft The top left coordinate of the area.
   * @param bottomRight The bottom right coordinate of the area.
   * @return true if the player is in the area, false if not.
   * @see #isTileWithinArea(Tile, Coordinate, Coordinate)
   */
  public static boolean isPlayerInArea(Coordinate topLeft, Coordinate bottomRight) {
    Entity player = Game.player().orElse(null);
    if (player == null) {
      return false;
    }
    PositionComponent pc =
        player
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(player, PositionComponent.class));
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

  /**
   * Calculates a list of tiles representing the path from one point to another.
   *
   * <p>Throws an IllegalArgumentException if the tile at 'from' or 'to' is non-accessible.
   *
   * @param from The start point.
   * @param to The end point.
   * @return List of tiles representing the path from the start point to the end point.
   */
  public static List<Tile> calculatePathTiles(final Point from, final Point to) {
    return calculatePathTiles(from.toCoordinate(), to.toCoordinate());
  }

  /**
   * Calculates a list of tiles representing the path from one coordinate to another.
   *
   * <p>Throws an IllegalArgumentException if the tile at the start or end is non-accessible.
   *
   * @param from The start coordinate.
   * @param to The end coordinate.
   * @return List of tiles representing the path from the start coordinate to the end coordinate.
   */
  public static List<Tile> calculatePathTiles(final Coordinate from, final Coordinate to) {
    final Tile fromTile = Game.tileAt(from).orElse(null);
    final Tile toTile = Game.tileAt(to).orElse(null);

    if (fromTile == null || toTile == null) return List.of();
    if (!fromTile.isAccessible() || !toTile.isAccessible()) return List.of();

    return Game.findPath(fromTile, toTile).map(LevelUtils::toTileList).orElse(List.of());
  }

  /**
   * Calculates a {@link TilePath} from one point to another.
   *
   * <p>Throws an IllegalArgumentException if the tile at 'from' or 'to' is non-accessible.
   *
   * @param from The start point.
   * @param to The end point.
   * @return A {@link TilePath} representing the path from the start point to the end point.
   */
  public static TilePath calculateTilePath(final Point from, final Point to) {
    return calculateTilePath(from.toCoordinate(), to.toCoordinate());
  }

  /**
   * Calculates a {@link TilePath} from one coordinate to another.
   *
   * <p>Throws an IllegalArgumentException if the tile at the start or end is non-accessible.
   *
   * @param from The start coordinate.
   * @param to The end coordinate.
   * @return A {@link TilePath} representing the path from the start coordinate to the end
   *     coordinate.
   */
  public static TilePath calculateTilePath(final Coordinate from, final Coordinate to) {
    final Tile fromTile = Game.tileAt(from).orElse(null);
    final Tile toTile = Game.tileAt(to).orElse(null);

    if (fromTile == null || toTile == null) return new ListTilePath();
    if (!fromTile.isAccessible() || !toTile.isAccessible()) return new ListTilePath();

    return Game.findPath(fromTile, toTile).orElseGet(ListTilePath::new);
  }

  private static List<Tile> toTileList(final TilePath path) {
    final List<Tile> tiles = new ArrayList<>(path.size());
    for (Tile tile : path) {
      tiles.add(tile);
    }
    return List.copyOf(tiles);
  }

  /**
   * Calculates a {@link TilePath} from the position of the given entity to the position of the
   * player.
   *
   * <p>If no player exists in the game, the path will be calculated from the given entity to the
   * given entity.
   *
   * <p>Throws a {@link core.utils.components.MissingComponentException} if the entity has no {@link
   * core.components.PositionComponent}.
   *
   * @param entity the entity from which the path to the player is calculated
   * @return a {@link TilePath} from the entity to the player, or from the entity to itself if no
   *     player exists
   */
  public static TilePath calculateTilePathToPlayer(final Entity entity) {
    final Point from =
        entity
            .fetch(PositionComponent.class)
            .map(PositionComponent::position)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));

    final Point to = Game.player().flatMap(Game::positionOf).orElse(from);
    return calculateTilePath(from, to);
  }

  /**
   * Calculates a {@link TilePath} between two points, assuming they lie on a straight horizontal or
   * vertical line.
   *
   * <p>If the two points do not share the same x- or y-coordinate (i.e., they are neither
   * horizontally nor vertically aligned), an empty {@link TilePath} is returned.
   *
   * <p>Unlike {@link #calculateTilePath(Point, Point)}, this method does not require the tiles
   * along the path to be accessible, making it suitable for traversing wall tiles.
   *
   * <p>If a tile along the path does not exist in the level, the path is truncated at that point.
   *
   * @param from the start point
   * @param to the end point
   * @return a {@link TilePath} containing all tiles from {@code from} to {@code to}, or an empty
   *     path if the points are not axis-aligned or a tile is missing
   */
  public static TilePath calculateTilePathInsideWall(final Point from, final Point to) {
    final Coordinate a = from.toCoordinate();
    final Coordinate b = to.toCoordinate();

    // only horizontal OR vertical
    if (a.x() != b.x() && a.y() != b.y()) return new ListTilePath();

    final int dx = Integer.compare(b.x(), a.x());
    final int dy = Integer.compare(b.y(), a.y());

    final List<Tile> tiles = new ArrayList<>();
    Coordinate cur = a;

    while (true) {
      final Optional<Tile> t = Game.tileAt(cur);
      if (t.isEmpty()) break; // out of level -> stop defensively
      tiles.add(t.get());

      if (cur.equals(b)) break;
      cur = new Coordinate(cur.x() + dx, cur.y() + dy);
    }

    return new ListTilePath(tiles);
  }

  /**
   * Calculates a {@link TilePath} from the given entity to a randomly chosen accessible tile within
   * the specified radius.
   *
   * <p>If no accessible tile exists within the radius, the resulting path targets the entity's
   * current position (i.e., a path from the entity to itself).
   *
   * <p>This method requires the entity to have a {@link PositionComponent}.
   *
   * @param entity the entity whose position is used as the center/start
   * @param radius the radius in which an accessible target tile is selected
   * @return a {@link TilePath} to a random accessible tile in range, or to the entity itself if no
   *     such tile exists
   * @throws MissingComponentException if the entity has no {@link PositionComponent}
   */
  public static TilePath calculateTilePathToRandomTileInRange(final Entity entity, float radius) {
    final Point from =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class))
            .position();

    final Point target = randomAccessibleTileInRangeAsPoint(from, radius).orElse(from);
    return calculateTilePath(from, target);
  }
}
