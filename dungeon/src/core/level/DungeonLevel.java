package core.level;

import contrib.entities.deco.Deco;
import contrib.utils.level.ITickable;
import core.level.elements.ILevel;
import core.level.elements.tile.*;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.TileTextureFactory;
import core.utils.Point;
import core.utils.Tuple;
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
  protected final ArrayList<Tile> startTiles = new ArrayList<>();
  protected Tile[][] layout;
  protected ArrayList<FloorTile> floorTiles = new ArrayList<>();
  protected ArrayList<WallTile> wallTiles = new ArrayList<>();
  protected ArrayList<HoleTile> holeTiles = new ArrayList<>();
  protected ArrayList<DoorTile> doorTiles = new ArrayList<>();
  protected ArrayList<ExitTile> exitTiles = new ArrayList<>();
  protected ArrayList<SkipTile> skipTiles = new ArrayList<>();
  protected ArrayList<PitTile> pitTiles = new ArrayList<>();
  protected ArrayList<PortalTile> portalTiles = new ArrayList<>();
  protected ArrayList<GitterTile> gitterTiles = new ArrayList<>();
  protected ArrayList<GlasswandTile> glassWallTiles = new ArrayList<>();

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
   * Constructs a new DungeonLevel with the given layout, design label, and custom points.
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
   * Constructs a new DungeonLevel with the given layout, design label, and custom points.
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
   * Constructs a new DungeonLevel with the given layout, design label, and custom points.
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
        Tile t = tiles[x];
        if (t == null) continue;
        addTile(t);
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
    if (tile == null) return;

    switch (tile.levelElement()) {
      case SKIP -> skipTiles.remove((SkipTile) tile);
      case FLOOR -> floorTiles.remove((FloorTile) tile);
      case WALL -> wallTiles.remove((WallTile) tile);
      case HOLE -> holeTiles.remove((HoleTile) tile);
      case DOOR -> doorTiles.remove((DoorTile) tile);
      case EXIT -> exitTiles.remove((ExitTile) tile);
      case PIT -> pitTiles.remove((PitTile) tile);
      case PORTAL -> portalTiles.remove((PortalTile) tile);
      case GITTER -> gitterTiles.remove((GitterTile) tile);
      case GLASSWALL -> glassWallTiles.remove((GlasswandTile) tile);
    }

    layout[tile.coordinate().y()][tile.coordinate().x()] = null;
  }

  @Override
  public void addTile(Tile tile) {
    if (tile == null) return;

    switch (tile.levelElement()) {
      case SKIP -> skipTiles.add((SkipTile) tile);
      case FLOOR -> floorTiles.add((FloorTile) tile);
      case WALL -> wallTiles.add((WallTile) tile);
      case HOLE -> holeTiles.add((HoleTile) tile);
      case EXIT -> exitTiles.add((ExitTile) tile);
      case DOOR -> doorTiles.add((DoorTile) tile);
      case PIT -> pitTiles.add((PitTile) tile);
      case PORTAL -> portalTiles.add((PortalTile) tile);
      case GITTER -> gitterTiles.add((GitterTile) tile);
      case GLASSWALL -> glassWallTiles.add((GlasswandTile) tile);
    }

    layout[tile.coordinate().y()][tile.coordinate().x()] = tile;
    tile.level(this);
    assignIndex(tile);
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
  @Override
  public List<Tuple<Deco, Point>> decorations() {
    return decorations;
  }

  /**
   * Adds a decoration to the level at the specified position.
   *
   * @param deco the decoration to add
   * @param position the position to place the decoration
   */
  public void addDecoration(Deco deco, Point position) {
    decorations.add(new Tuple<>(deco, position));
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

  private void assignIndex(final Tile tile) {
    if (tile == null || layout == null || layout.length == 0 || layout[0].length == 0) return;
    final int w = layout[0].length;
    final int x = tile.coordinate().x();
    final int y = tile.coordinate().y();
    tile.index(y * w + x);
  }
}
