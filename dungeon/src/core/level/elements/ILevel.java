package core.level.elements;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;
import core.Entity;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.elements.astar.TileHeuristic;
import core.level.elements.tile.*;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.TileTextureFactory;
import core.utils.Point;
import core.utils.Tuple;
import java.util.*;
import java.util.Optional;
import java.util.function.Function;

/**
 * Defines the API for Levels in the dungeon.
 *
 * <p>This is the datatype used in every API call inside the dungeon framework to define a level.
 * Each level you want to use needs to implement this interface.
 *
 * <p>Also provides the API for the LibGDX Pathfinding.
 *
 * @see DungeonLevel
 */
public interface ILevel extends IndexedGraph<Tile> {

  /** Default random number generator (seeded with current time). */
  Random RANDOM = new Random();

  /**
   * Sets the start tile for the level.
   *
   * @param start The start tile.
   */
  void startTile(final Tile start);

  /**
   * Adds an unspecified tile to the level.
   *
   * @param tile The tile to be added.
   */
  void addTile(final Tile tile);

  /**
   * Removes a tile from the level.
   *
   * @param tile The tile to be removed.
   */
  void removeTile(final Tile tile);

  /**
   * Returns a list of all floor tiles in the level.
   *
   * @return List of floor tiles.
   */
  List<FloorTile> floorTiles();

  /**
   * Returns a list of all wall tiles in the level.
   *
   * @return List of wall tiles.
   */
  List<WallTile> wallTiles();

  /**
   * Returns a list of all hole tiles in the level.
   *
   * @return List of hole tiles.
   */
  List<HoleTile> holeTiles();

  /**
   * Returns a list of all door tiles in the level.
   *
   * @return List of door tiles.
   */
  List<DoorTile> doorTiles();

  /**
   * Returns a list of all exit tiles in the level.
   *
   * @return List of exit tiles.
   */
  List<ExitTile> exitTiles();

  /**
   * Returns a list of all skip tiles in the level.
   *
   * @return List of skip tiles.
   */
  List<SkipTile> skipTiles();

  /**
   * Returns a list of all pit tiles in the level.
   *
   * @return List of pit tiles.
   */
  List<PitTile> pitTiles();

  /**
   * Adds connections to neighboring tiles for a specified tile.
   *
   * @param checkTile The tile for which connections to neighbors are added.
   */
  void addConnectionsToNeighbours(final Tile checkTile);

  /**
   * Generates a String representation of the level layout using specific symbols for different tile
   * types.
   *
   * <p>The symbols used are: - 'F' for Floor tiles, - 'W' for Wall tiles, - 'E' for Exit tiles, -
   * 'S' for Skip/Blank tiles.
   *
   * <p>The layout is formatted with each row represented on a new line in the resulting String.
   *
   * @return A String representation of the level layout.
   */
  default String printLevel() {
    StringBuilder output = new StringBuilder();
    for (int y = 0; y < layout().length; y++) {
      for (int x = 0; x < layout()[0].length; x++) {
        if (layout()[y][x].levelElement() == LevelElement.FLOOR) {
          output.append("F");
        } else if (layout()[y][x].levelElement() == LevelElement.WALL) {
          output.append("W");
        } else if (layout()[y][x].levelElement() == LevelElement.EXIT) {
          output.append("E");
        } else if (layout()[y][x].levelElement() == LevelElement.SKIP) {
          output.append("S");
        } else if (layout()[y][x].levelElement() == LevelElement.HOLE) {
          output.append("H");
        } else if (layout()[y][x].levelElement() == LevelElement.DOOR) {
          output.append("D");
        } else if (layout()[y][x].levelElement() == LevelElement.PIT) {
          output.append("P");
        } else {
          throw new RuntimeException(
              "Invalid LevelElement in level layout: " + layout()[y][x].levelElement());
        }
      }
      output.append("\n");
    }
    return output.toString();
  }

  /**
   * Retrieves a random tile of the specified type from the level.
   *
   * <p>The method uses a switch statement to determine the type of tile requested and retrieves a
   * random tile of that type from the corresponding list in the level. If the list for the
   * specified type is empty, the method returns null.
   *
   * @param elementType Type of the tile to retrieve.
   * @return A random tile of the specified type, or empty if the list for that type is empty.
   */
  default Optional<Tile> randomTile(final LevelElement elementType) {
    Function<List<? extends Tile>, Optional<Tile>> returnVal =
        (list) ->
            Optional.ofNullable(list.isEmpty() ? null : list.get(RANDOM.nextInt(list.size())));

    return returnVal.apply(
        switch (elementType) {
          case SKIP -> skipTiles();
          case FLOOR -> floorTiles();
          case WALL -> wallTiles();
          case HOLE -> holeTiles();
          case EXIT -> exitTiles();
          case DOOR -> doorTiles();
          case PIT -> pitTiles();
          default -> throw new NoSuchElementException("No such tile type: '" + elementType + "'");
        });
  }

  /**
   * Retrieves the count of nodes in the level for use in libGDX pathfinding algorithms.
   *
   * @return The number of nodes in the level.
   */
  int getNodeCount();

  /**
   * Starts the indexed A* pathfinding algorithm and returns a path between the specified start and
   * end tiles.
   *
   * <p>Throws an IllegalArgumentException if either the start or end tile is non-accessible.
   *
   * @param start The starting tile for pathfinding.
   * @param end The destination tile for pathfinding.
   * @return The generated path between the start and end tiles.
   * @throws IllegalArgumentException If the start or end tile is non-accessible.
   */
  default GraphPath<Tile> findPath(final Tile start, final Tile end)
      throws IllegalArgumentException {
    if (!start.isAccessible())
      throw new IllegalArgumentException(
          "Cannot calculate path because the start point is non-accessible.");
    if (!end.isAccessible())
      throw new IllegalArgumentException(
          "Cannot calculate path because the end point is non-accessible.");

    GraphPath<Tile> path = new DefaultGraphPath<>();
    new IndexedAStarPathFinder<>(this).searchNodePath(start, end, tileHeuristic(), path);
    return path;
  }

  @Override
  default int getIndex(final Tile tile) {
    return tile.index();
  }

  @Override
  default Array<Connection<Tile>> getConnections(final Tile fromNode) {
    return fromNode.connections();
  }

  /**
   * Retrieves the TileHeuristic associated with the Level.
   *
   * @return The TileHeuristic for the Level.
   */
  TileHeuristic tileHeuristic();

  /**
   * Retrieves the layout of the level, represented as a 2D array of tiles.
   *
   * <p>Note that the layout is stored [y][x], so the first index defines the y-coordinate, and the
   * second index the x-coordinate.
   *
   * @return The layout of the level as a 2D array of tiles.
   */
  Tile[][] layout();

  /**
   * Get the size (row x col) of the level as a Tuple.
   *
   * <p>{@link Tuple#a()} contains the row size (starting at 1).
   *
   * <p>{@link Tuple#b()} contains the column size (starting at 1).
   *
   * <p>Note that the layout is stored [y][x], so the first index defines the y-coordinate, and the
   * second index the x-coordinate.
   *
   * @return The size of the level as a Tuple.
   */
  default Tuple<Integer, Integer> size() {
    Tile[][] layout = layout();
    return new Tuple<>(layout[0].length, layout.length);
  }

  /**
   * Retrieves the tile at the specified position within the level.
   *
   * <p>The method uses the provided coordinate to access the tile in the level layout. If the
   * coordinate is out of bounds, the method returns an empty Optional.
   *
   * @param coordinate The position from which to retrieve the tile.
   * @return An {@link Optional} containing the tile at the specified coordinate, or {@link
   *     Optional#empty()} if there is no tile or the coordinate is out of bounds.
   */
  default Optional<Tile> tileAt(final Coordinate coordinate) {
    Tile[][] layout = layout();
    if (coordinate.y() < 0
        || coordinate.y() >= layout.length
        || coordinate.x() < 0
        || coordinate.x() >= layout[0].length) {
      return Optional.empty();
    }
    return Optional.ofNullable(layout[coordinate.y()][coordinate.x()]);
  }

  /**
   * Retrieves the tile at the specified position within the level.
   *
   * <p>The {@link Point} is converted to a {@link Coordinate} via {@link Point#toCoordinate()}. If
   * the resulting coordinate is out of bounds or there is no tile, an empty {@link Optional} is
   * returned.
   *
   * @param point The position from which to retrieve the tile, converted to a coordinate.
   * @return An {@link Optional} containing the tile at the given point, or {@link
   *     Optional#empty()}.
   */
  default Optional<Tile> tileAt(final Point point) {
    return tileAt(point.toCoordinate());
  }

  /**
   * Retrieves a random tile from the level.
   *
   * <p>The method generates random indices within the bounds of the level layout and returns the
   * tile at the corresponding position.
   *
   * @return A randomly selected tile from the level.
   */
  default Optional<Tile> randomTile() {
    return Optional.ofNullable(
        layout()[RANDOM.nextInt(layout().length)][RANDOM.nextInt(layout()[0].length)]);
  }

  /**
   * Retrieves one end tile of the level, if present.
   *
   * <p>If multiple end tiles exist in the level, only one of them will be returned.
   *
   * @return an {@link Optional} containing one end tile if present, or an empty {@link Optional} if
   *     none exist
   * @deprecated use {@link #endTiles()} to retrieve all end tiles
   */
  @Deprecated
  Optional<Tile> endTile();

  /**
   * Retrieves a Set containing the end tiles of the level.
   *
   * @return Set containing the end tiles of the level.
   */
  Set<ExitTile> endTiles();

  /**
   * Retrieves the start tile of the level.
   *
   * @return The start tile of the level.
   */
  Optional<Tile> startTile();

  /**
   * Retrieves the tile on which the given entity is standing.
   *
   * <p>The method fetches the position component of the entity using {@link Entity#fetch(Class)}
   * and looks up the tile at that position. If the entity has no {@link PositionComponent} or the
   * position is out of bounds / has no tile, an empty {@link Optional} is returned.
   *
   * @param entity The entity for which to retrieve the tile.
   * @return An {@link Optional} containing the tile at the entity's position, or empty if
   *     unavailable.
   */
  default Optional<Tile> tileAtEntity(final Entity entity) {
    return entity
        .fetch(PositionComponent.class)
        .map(PositionComponent::position)
        .flatMap(this::tileAt);
  }

  /**
   * Retrieves the position of a randomly selected tile in the level as a {@link Point}.
   *
   * <p>The method internally calls {@link #randomTile()} to obtain a random tile and then retrieves
   * its position.
   *
   * @return The position of a randomly selected tile in the level as a {@link Point}.
   */
  default Optional<Point> randomTilePoint() {
    return randomTile().map(Tile::position);
  }

  /**
   * Retrieves the position of a randomly selected tile of the specified type in the level as a
   * {@link Point}.
   *
   * <p>The method internally calls {@link #randomTile(LevelElement)} to obtain a random tile of the
   * specified type and then retrieves its position.
   *
   * @param elementType Type of the tile for which to retrieve the position.
   * @return The position of a randomly selected tile of the specified type in the level as a {@link
   *     Point}.
   */
  default Optional<Point> randomTilePoint(final LevelElement elementType) {
    return randomTile(elementType).map(Tile::position);
  }

  /**
   * Changes the type of specified tile, including updating its texture and level representation.
   *
   * <p>The method first checks if the tile is associated with a level. If not, the method returns
   * without making any changes. If the tile is associated with a level, it is removed from the
   * level, and a new tile is created with the specified level element, texture path, coordinates,
   * and design label. The new tile is then added back to the level at the same coordinates as the
   * original tile.
   *
   * @param tile The tile to be changed.
   * @param changeInto The LevelElement to change the tile into.
   */
  default void changeTileElementType(final Tile tile, final LevelElement changeInto) {
    ILevel level = tile.level();
    if (level == null) {
      return;
    }
    level.removeTile(tile);
    Tile newTile =
        TileFactory.createTile(
            TileTextureFactory.findTexturePath(tile, layout(), changeInto),
            tile.coordinate(),
            changeInto,
            tile.designLabel());
    level.layout()[tile.coordinate().y()][tile.coordinate().x()] = newTile;
    newTile.index(tile.index());
    newTile.tintColor(tile.tintColor());
    newTile.visible(tile.visible());
    level.addTile(newTile);
  }

  /**
   * Get the {@link DesignLabel} of the level.
   *
   * @return The DesignLabel of the level
   */
  default Optional<DesignLabel> designLabel() {
    return randomTile().map(Tile::designLabel);
  }

  /**
   * Returns the list of custom points.
   *
   * @return A list of custom points.
   */
  List<Coordinate> customPoints();

  /**
   * Adds a new custom point to the list.
   *
   * @param point The custom point to be added.
   */
  void addCustomPoint(Coordinate point);

  /**
   * Removes a custom point from the list.
   *
   * @param point The custom point to be removed.
   */
  void removeCustomPoint(Coordinate point);
}
