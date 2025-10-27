package core.level;

import contrib.entities.deco.Deco;
import contrib.utils.level.ITickable;
import core.level.elements.ILevel;
import core.level.elements.astar.TileConnection;
import core.level.elements.astar.TileHeuristic;
import core.level.elements.tile.*;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.TileTextureFactory;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import java.util.*;
import java.util.stream.Collectors;

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
public class DungeonLevel implements ILevel, ITickable {

  protected final Map<String, Point> namedPoints = new HashMap<>();
  protected final List<Tuple<Deco, Point>> decorations = new ArrayList<>();

  private static int levelNameSuffix = 1;
  protected String levelName;
  private static final Vector2[] CONNECTION_OFFSETS = {
    Vector2.of(0, 1), Vector2.of(0, -1), Vector2.of(1, 0), Vector2.of(-1, 0),
  };
  protected final TileHeuristic tileHeuristic = new TileHeuristic();
  protected final ArrayList<Tile> startTiles = new ArrayList<>();
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
  public DungeonLevel(Tile[][] layout) {
    this.layout = layout;
    putTilesInLists();
    levelName = "level_" + levelNameSuffix++;
  }

  /**
   * Create a new Level.
   *
   * @param layout The layout of the Level
   * @param designLabel The design the level should have
   */
  public DungeonLevel(LevelElement[][] layout, DesignLabel designLabel) {
    this(convertLevelElementToTile(layout, designLabel));
  }

  /**
   * Constructs a new DevDungeonLevel with the given layout, design label, and custom points.
   *
   * @param layout The layout of the level, represented as a 2D array of LevelElements.
   * @param designLabel The design label of the level.
   * @param customPoints A list of custom points to be added to the level.
   * @param decorations A list of decorations to be added to the level.
   * @param levelName The name of the level. (can be empty)
   */
  public DungeonLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> customPoints,
      List<Tuple<Deco, Point>> decorations,
      String levelName) {
    this(layout, designLabel, customPoints, decorations);
    this.levelName = levelName;
  }

  /**
   * Constructs a new DevDungeonLevel with the given layout, design label, and custom points.
   *
   * @param layout The layout of the level, represented as a 2D array of LevelElements.
   * @param designLabel The design label of the level.
   * @param namedPoints A list of custom points to be added to the level.
   * @param decorations A list of decorations to be added to the level.
   */
  public DungeonLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      List<Tuple<Deco, Point>> decorations) {
    this(layout, designLabel);
    this.namedPoints.putAll(namedPoints);
    this.decorations.addAll(decorations);
  }

  /**
   * Constructs a new DevDungeonLevel with the given layout, design label, and custom points.
   *
   * @param layout The layout of the level, represented as a 2D array of LevelElements.
   * @param designLabel The design label of the level.
   * @param namedPoints A list of custom points to be added to the level.
   * @param levelName The name of the level. (can be empty)
   */
  public DungeonLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      String levelName) {
    this(layout, designLabel);
    this.namedPoints.putAll(namedPoints);
    this.levelName = levelName;
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
      this.tileAt(c)
          .filter(Tile::isAccessible)
          .filter(t -> !checkTile.connections().contains(new TileConnection(checkTile, t), false))
          .ifPresent(checkTile::addConnection);
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
  public Optional<Tile> startTile() {
    if (startTiles.isEmpty()) return Optional.empty();
    return Optional.ofNullable(startTiles.getFirst());
  }

  @Override
  public void startTile(Tile start) {
    if (startTiles.isEmpty()) startTiles.add(start);
    else startTiles.set(0, start);
  }

  public List<Tile> startTiles() {
    return startTiles;
  }

  @Override
  public void setLayout(LevelElement[][] layout) {
    DesignLabel design = designLabel().orElseThrow();
    this.layout = convertLevelElementToTile(layout, design);
    floorTiles.clear();
    wallTiles.clear();
    holeTiles.clear();
    doorTiles.clear();
    exitTiles.clear();
    skipTiles.clear();
    pitTiles.clear();
    putTilesInLists();
  }

  @Override
  public Optional<Tile> endTile() {
    return Optional.ofNullable(!exitTiles.isEmpty() ? exitTiles.getFirst() : null);
  }

  @Override
  public Set<ExitTile> endTiles() {
    return new HashSet<>(exitTiles);
  }

  /**
   * Called when the level is first ticked.
   *
   * @see #onTick()
   * @see ITickable
   */
  protected void onFirstTick() {}

  /**
   * Called when the level is ticked.
   *
   * @see #onFirstTick()
   * @see ITickable
   */
  protected void onTick() {}

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) onFirstTick();
    onTick();
  }

  /**
   * Returns a map of named points in this level.
   *
   * @return a map of point names and their coordinates
   */
  @Override
  public Map<String, Point> namedPoints() {
    return namedPoints;
  }

  /**
   * Returns a list of decorative elements placed in the level.
   *
   * @return a list of decoration tuples
   */
  public List<Tuple<Deco, Point>> decorations() {
    return decorations;
  }

  /**
   * Returns a specific point by its legacy numeric index (e.g., "Point0", "Point1").
   *
   * @param legacyIndex the numeric index of the point
   * @return the point associated with the legacy index, or null if not found
   */
  public Point getPoint(int legacyIndex) {
    return namedPoints.get("Point" + legacyIndex);
  }

  /**
   * Returns a specific named point by its key.
   *
   * @param name the name of the point
   * @return the point associated with the given name, or null if not found
   */
  public Point getPoint(String name) {
    return namedPoints.get(name);
  }

  /**
   * Get an array of points with the given base name from start to end (inclusive).
   *
   * @param baseName the base name of the points
   * @param start the starting index
   * @param end the ending index
   * @return the array of points
   */
  public Point[] getPoints(String baseName, int start, int end) {
    Point[] points = new Point[end - start + 1];
    for (int i = start; i <= end; i++) {
      points[i - start] = getPoint(baseName + i);
    }
    return points;
  }

  /**
   * Get an array of points using legacy numbering (e.g., Point0, Point1).
   *
   * @param legacyStart the starting index
   * @param legacyEnd the ending index
   * @return the array of points
   */
  public Point[] getPoints(int legacyStart, int legacyEnd) {
    return getPoints("Point", legacyStart, legacyEnd);
  }

  /**
   * Returns the highest existing numbered point index for the given base name.
   *
   * @param baseName the base name to search for
   * @return the highest numbered index, or -1 if none exist
   */
  public int getHighestPointNumber(String baseName) {
    int highestNumber = -1;
    while (namedPoints.containsKey(baseName + (highestNumber + 1))) {
      highestNumber++;
    }
    return highestNumber;
  }

  /**
   * Returns a list of all points with their corresponding numeric indices for the given base name.
   *
   * @param baseName the base name to search for
   * @return a list of point-index tuples
   */
  public List<Tuple<Point, Integer>> listPointsIndexed(String baseName) {
    ArrayList<Tuple<Point, Integer>> toRet = new ArrayList<>();
    int i = 0;
    while (namedPoints.containsKey(baseName + i)) {
      toRet.add(new Tuple(getPoint(baseName + i), i));
      i++;
    }
    return toRet;
  }

  /**
   * Returns a list of all points associated with the given base name.
   *
   * @param baseName the base name to search for
   * @return a list of matching points
   */
  public List<Point> listPoints(String baseName) {
    return listPointsIndexed(baseName).stream().map(Tuple::a).collect(Collectors.toList());
  }

  /**
   * Adds a named point to the level.
   *
   * @param name the name of the point
   * @param position the position of the point
   * @return the previous point associated with the name, or null if none existed
   */
  public Point addNamedPoint(String name, Point position) {
    return namedPoints.put(name, position);
  }

  /**
   * Removes a named point from the level.
   *
   * @param name the name of the point to remove
   * @return the removed point, or null if none existed
   */
  public Point removeNamedPoint(String name) {
    return namedPoints.remove(name);
  }
}
