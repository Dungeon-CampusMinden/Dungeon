package core.level;

import core.level.elements.ILevel;
import core.level.elements.astar.TileConnection;
import core.level.elements.astar.TileHeuristic;
import core.level.elements.tile.*;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.TileTextureFactory;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Basic 2D-Matrix Tile-based level.
 *
 * <p>The level is represented by a 2D-Matrix where each entry is a {@link Tile}.
 *
 * <p>The coordinate of the tile defines the place in the 2D-Matrix. The matrix is saved as a
 * 2D-Array. Note that the layout is stored [y][x], so the first index defines the y-coordinate, and
 * the second index the x-coordinate.
 *
 * @see core.level.elements.ILevel
 */
public class TileLevel implements ILevel {

  private static final Vector2[] CONNECTION_OFFSETS = {
    Vector2.of(0, 1), Vector2.of(0, -1), Vector2.of(1, 0), Vector2.of(-1, 0),
  };
  protected final TileHeuristic tileHeuristic = new TileHeuristic();
  protected Tile startTile;
  protected int nodeCount = 0;
  protected Tile[][] layout;
  protected ArrayList<FloorTile> floorTiles = new ArrayList<>();
  protected ArrayList<WallTile> wallTiles = new ArrayList<>();
  protected ArrayList<HoleTile> holeTiles = new ArrayList<>();
  protected ArrayList<DoorTile> doorTiles = new ArrayList<>();
  protected ArrayList<ExitTile> exitTiles = new ArrayList<>();
  protected ArrayList<SkipTile> skipTiles = new ArrayList<>();
  protected ArrayList<PitTile> pitTiles = new ArrayList<>();

  /**
   * Create a new level.
   *
   * @param layout The layout of the level.
   */
  public TileLevel(Tile[][] layout) {
    this.layout = layout;
    putTilesInLists();
  }

  /**
   * Create a new Level.
   *
   * @param layout The layout of the Level
   * @param designLabel The design the level should have
   */
  public TileLevel(LevelElement[][] layout, DesignLabel designLabel) {
    this(convertLevelElementToTile(layout, designLabel));
  }

  /**
   * Converts the given LevelElement[][] in a corresponding Tile[][].
   *
   * @param layout The LevelElement[][]
   * @param designLabel The selected Design for the Tiles
   * @return The converted Tile[][]
   */
  private static Tile[][] convertLevelElementToTile(
      LevelElement[][] layout, DesignLabel designLabel) {
    Tile[][] tileLayout = new Tile[layout.length][layout[0].length];
    for (int y = 0; y < layout.length; y++) {
      for (int x = 0; x < layout[0].length; x++) {
        Coordinate coordinate = new Coordinate(x, y);
        IPath texturePath =
            TileTextureFactory.findTexturePath(
                new TileTextureFactory.LevelPart(layout[y][x], designLabel, layout, coordinate));
        tileLayout[y][x] =
            TileFactory.createTile(texturePath, coordinate, layout[y][x], designLabel);
      }
    }
    return tileLayout;
  }

  private void putTilesInLists() {
    for (Tile[] tiles : layout) {
      for (int x = 0; x < layout[0].length; x++) {
        addTile(tiles[x]);
      }
    }
  }

  @Override
  public int getNodeCount() {
    return nodeCount;
  }

  @Override
  public TileHeuristic tileHeuristic() {
    return tileHeuristic;
  }

  /**
   * Check each tile around the tile, if it is accessible add it to the connectionList.
   *
   * @param checkTile Tile to check for.
   */
  @Override
  public void addConnectionsToNeighbours(Tile checkTile) {
    for (Vector2 v : CONNECTION_OFFSETS) {
      Coordinate c = checkTile.coordinate().translate(v);
      Tile t = tileAt(c);
      if (t != null
          && t.isAccessible()
          && !checkTile.connections().contains(new TileConnection(checkTile, t), false)) {
        checkTile.addConnection(t);
      }
    }
  }


  @Override
  public List<FloorTile> floorTiles() {
    return floorTiles;
  }

  @Override
  public List<WallTile> wallTiles() {
    return wallTiles;
  }

  @Override
  public List<HoleTile> holeTiles() {
    return holeTiles;
  }

  @Override
  public List<DoorTile> doorTiles() {
    return doorTiles;
  }

  @Override
  public List<ExitTile> exitTiles() {
    return exitTiles;
  }

  @Override
  public List<SkipTile> skipTiles() {
    return skipTiles;
  }

  @Override
  public List<PitTile> pitTiles() {
    return pitTiles;
  }

  @Override
  public void removeTile(Tile tile) {
    switch (tile.levelElement()) {
      case SKIP -> skipTiles.remove((SkipTile) tile);
      case FLOOR -> floorTiles.remove((FloorTile) tile);
      case WALL -> wallTiles.remove((WallTile) tile);
      case HOLE -> holeTiles.remove((HoleTile) tile);
      case DOOR -> doorTiles.remove((DoorTile) tile);
      case EXIT -> exitTiles.remove((ExitTile) tile);
      case PIT -> pitTiles.remove((PitTile) tile);
    }
    this.removeFromPathfinding(tile);
  }

  /**
   * Removes the given tile from the pathfinding. By removing all neighbours connections to this
   * tile and its index.
   *
   * @param tile Tile to remove from pathfinding.
   */
  public void removeFromPathfinding(Tile tile) {
    tile.connections()
        .forEach(
            x ->
                x.getToNode()
                    .connections()
                    .removeValue(new TileConnection(x.getToNode(), tile), false));
    if (tile.isAccessible()) removeIndex(tile.index());
  }

  /**
   * Adds the given tile to the pathfinding. By adding all neighbours connections to this tile and
   * giving it an index. If the tile is not accessible, it will not be added to the pathfinding.
   *
   * @param tile Tile to add to pathfinding.
   */
  public void addToPathfinding(Tile tile) {
    if (tile == null || !tile.isAccessible()) return;
    this.addConnectionsToNeighbours(tile);
    tile.connections()
        .forEach(
            x -> {
              if (!x.getToNode()
                  .connections()
                  .contains(new TileConnection(x.getToNode(), tile), false))
                x.getToNode().addConnection(tile);
            });
    tile.index(nodeCount++);
  }

  private void removeIndex(int index) {
    Arrays.stream(layout)
        .flatMap(x -> Arrays.stream(x).filter(y -> y.index() > index))
        .forEach(x -> x.index(x.index() - 1));
    nodeCount--;
  }

  @Override
  public void addTile(Tile tile) {
    switch (tile.levelElement()) {
      case SKIP -> skipTiles.add((SkipTile) tile);
      case FLOOR -> floorTiles.add((FloorTile) tile);
      case WALL -> wallTiles.add((WallTile) tile);
      case HOLE -> holeTiles.add((HoleTile) tile);
      case EXIT -> exitTiles.add((ExitTile) tile);
      case DOOR -> doorTiles.add((DoorTile) tile);
      case PIT -> pitTiles.add((PitTile) tile);
    }
    this.addToPathfinding(tile);
    tile.level(this);
  }

  @Override
  public Tile[][] layout() {
    return layout;
  }

  @Override
  public Tile startTile() {
    return startTile;
  }

  @Override
  public void startTile(Tile start) {
    startTile = start;
  }

  @Override
  public Tile endTile() {
    return exitTiles.size() > 0 ? exitTiles.get(0) : null;
  }

  @Override
  public Set<ExitTile> endTiles() {
    return endTiles();
  }
}
