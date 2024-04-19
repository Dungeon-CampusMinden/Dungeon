package core.level.elements;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;
import core.Entity;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.astar.TileHeuristic;
import core.level.elements.tile.*;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.level.utils.TileTextureFactory;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.util.List;
import java.util.Random;

/**
 * Defines the API for Levels in the dungeon.
 *
 * <p>This is the datatype used in every API call inside the dungeon framework to define a level.
 * Each level you want to use needs to implement this interface.
 *
 * <p>Also provides the API for the LibGDX Pathfinding.
 *
 * @see core.level.TileLevel
 */
public interface ILevel extends IndexedGraph<Tile> {

  /** Default random number generator (seeded with current time). */
  Random RANDOM = new Random();

  /**
   * Marks a random tile as the start of the level.
   *
   * <p>This method selects a random tile with the element type {@link LevelElement#FLOOR} and sets
   * it as the start tile for the level.
   */
  default void randomStart() {
    startTile(randomTile(LevelElement.FLOOR));
  }

  /**
   * Sets the start tile for the level.
   *
   * @param start The start tile.
   */
  void startTile(final Tile start);

  /**
   * Marks a random tile as the end of the level.
   *
   * <p>This method selects a random floor tile and sets it as the exit tile for the level.
   *
   * <p>If there are not enough floor tiles for both the start and exit tiles, no action is taken.
   */
  default void randomEnd() {
    List<FloorTile> floorTiles = floorTiles();
    if (floorTiles.size() <= 1) {
      // not enough Tiles for startTile and ExitTile
      return;
    }
    int startTileIndex = floorTiles.indexOf((FloorTile) startTile());
    int index = RANDOM.nextInt(floorTiles.size() - 1);
    changeTileElementType(
        floorTiles.get(index < startTileIndex ? index : index + 1), LevelElement.EXIT);
  }

  /**
   * Adds a floor tile to the level.
   *
   * @param tile The new floor tile to be added.
   */
  void addFloorTile(final FloorTile tile);

  /**
   * Adds a wall tile to the level.
   *
   * @param tile The new wall tile to be added.
   */
  void addWallTile(final WallTile tile);

  /**
   * Adds a hole tile to the level.
   *
   * @param tile The new hole tile to be added.
   */
  void addHoleTile(final HoleTile tile);

  /**
   * Adds a door tile to the level.
   *
   * @param tile The new door tile to be added.
   */
  void addDoorTile(final DoorTile tile);

  /**
   * Adds an exit tile to the level.
   *
   * @param tile The new exit tile to be added.
   */
  void addExitTile(final ExitTile tile);

  /**
   * Adds a skip tile to the level.
   *
   * @param tile The new skip tile to be added.
   */
  void addSkipTile(final SkipTile tile);

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
   * Adds a pit tile to the level.
   *
   * @param tile The new pit tile to be added.
   */
  void addPitTile(final PitTile tile);

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
    level.layout()[tile.coordinate().y][tile.coordinate().x] = newTile;
    newTile.index(tile.index());
    newTile.tintColor(tile.tintColor());
    newTile.visible(tile.visible());
    level.addTile(newTile);
  }

  /**
   * Retrieves a random tile of the specified type from the level.
   *
   * <p>The method uses a switch statement to determine the type of tile requested and retrieves a
   * random tile of that type from the corresponding list in the level. If the list for the
   * specified type is empty, the method returns null.
   *
   * @param elementType Type of the tile to retrieve.
   * @return A random tile of the specified type, or null if the list for that type is empty.
   */
  default Tile randomTile(final LevelElement elementType) {
    return switch (elementType) {
      case SKIP ->
          skipTiles().size() > 0 ? skipTiles().get(RANDOM.nextInt(skipTiles().size())) : null;
      case FLOOR ->
          floorTiles().size() > 0 ? floorTiles().get(RANDOM.nextInt(floorTiles().size())) : null;
      case WALL ->
          wallTiles().size() > 0 ? wallTiles().get(RANDOM.nextInt(wallTiles().size())) : null;
      case HOLE ->
          holeTiles().size() > 0 ? holeTiles().get(RANDOM.nextInt(holeTiles().size())) : null;
      case EXIT ->
          exitTiles().size() > 0 ? exitTiles().get(RANDOM.nextInt(exitTiles().size())) : null;
      case DOOR ->
          doorTiles().size() > 0 ? doorTiles().get(RANDOM.nextInt(doorTiles().size())) : null;
      case PIT -> pitTiles().size() > 0 ? pitTiles().get(RANDOM.nextInt(pitTiles().size())) : null;
    };
  }

  /**
   * Sets the function that should be executed when this level is loaded as the current level for
   * the first time.
   *
   * <p>The specified function will be executed only once when the level is loaded for the first
   * time.
   *
   * @param function The function to be executed when the level is loaded for the first time.
   */
  void onFirstLoad(final IVoidFunction function);

  /**
   * Notifies the level that it has been loaded as the current level.
   *
   * <p>This function should be called when the level is loaded. It checks if the level was loaded
   * for the first time and then executes the registered function from {@link
   * #onFirstLoad(IVoidFunction)}.
   */
  void onLoad();

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
   * Retrieves the position of the specified entity within the level.
   *
   * <p>This method requires the specified entity to have a {@link PositionComponent}.
   *
   * @param entity The entity from which to retrieve the position.
   * @return The position of the given entity.
   * @throws MissingComponentException If the entity lacks a required PositionComponent.
   */
  default Point positionOf(final Entity entity) throws MissingComponentException {
    return entity
        .fetch(PositionComponent.class)
        .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class))
        .position();
  }

  /**
   * Retrieves the layout of the level, represented as a 2D array of tiles.
   *
   * @return The layout of the level as a 2D array of tiles.
   */
  Tile[][] layout();

  /**
   * Retrieves the tile at the specified position within the level.
   *
   * <p>The method uses the provided coordinate to access the tile in the level layout. If the
   * coordinate is out of bounds, the method returns null.
   *
   * @param coordinate The position from which to retrieve the tile.
   * @return The tile at the specified coordinate, or null if there is no tile or the coordinate is
   *     out of bounds.
   */
  default Tile tileAt(final Coordinate coordinate) {
    try {
      return layout()[coordinate.y][coordinate.x];
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  /**
   * Retrieves the tile at the specified position within the level.
   *
   * <p>The method uses the provided point and converts it to a coordinate using {@link
   * Point#toCoordinate}, then retrieves the corresponding tile in the level layout. If the
   * resulting coordinate is out of bounds, the method returns null.
   *
   * @param point The position from which to retrieve the tile, converted to a coordinate.
   * @return The tile at the specified point, or null if there is no tile or the coordinate is out
   *     of bounds.
   */
  default Tile tileAt(final Point point) {
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
  default Tile randomTile() {
    return layout()[RANDOM.nextInt(layout().length)][RANDOM.nextInt(layout()[0].length)];
  }

  /**
   * Retrieves the end tile of the level.
   *
   * @return The end tile of the level.
   */
  Tile endTile();

  /**
   * Retrieves the start tile of the level.
   *
   * @return The start tile of the level.
   */
  Tile startTile();

  /**
   * Retrieves the tile on which the given entity is standing.
   *
   * <p>The method fetches the position component of the entity using {@link Entity#fetch(Class)}
   * and retrieves the corresponding tile using the position's coordinate. If the entity does not
   * have a position component, a {@link MissingComponentException} is thrown.
   *
   * @param entity The entity for which to retrieve the tile.
   * @return The tile at the coordinate of the entity's position.
   * @throws MissingComponentException If the entity does not have a required {@link
   *     PositionComponent}.
   */
  default Tile tileAtEntity(final Entity entity) {
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    return tileAt(pc.position().toCoordinate());
  }

  /**
   * Retrieves the position of a randomly selected tile in the level as a {@link Point}.
   *
   * <p>The method internally calls {@link #randomTile()} to obtain a random tile and then retrieves
   * its position.
   *
   * @return The position of a randomly selected tile in the level as a {@link Point}.
   */
  default Point randomTilePoint() {
    return randomTile().position();
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
  default Point randomTilePoint(final LevelElement elementType) {
    return randomTile(elementType).position();
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
  default List<Tile> tilesInArea(Coordinate topLeft, Coordinate bottomRight) {
    List<Tile> tiles = new java.util.ArrayList<>();
    for (int x = topLeft.x; x <= bottomRight.x; x++) {
      for (int y = bottomRight.y; y <= topLeft.y; y++) {
        tiles.add(this.layout()[y][x]);
      }
    }
    return tiles;
  }
}
