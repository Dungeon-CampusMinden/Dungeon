package core.level;

import contrib.utils.level.ITickable;
import contrib.utils.level.MissingLevelException;
import core.level.elements.ILevel;
import core.level.elements.astar.TileConnection;
import core.level.elements.astar.TileHeuristic;
import core.level.elements.tile.*;
import core.level.loader.DungeonLoader;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.TileTextureFactory;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

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
public class TileLevel implements ILevel, ITickable {

  protected final List<Coordinate> customPoints = new ArrayList<>();
  protected String levelName = "Dungeon Level";
  protected String description = "A Level";
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
   * Constructs a new DevDungeonLevel with the given layout, design label, and custom points.
   *
   * @param layout The layout of the level, represented as a 2D array of LevelElements.
   * @param designLabel The design label of the level.
   * @param customPoints A list of custom points to be added to the level.
   * @param levelName The name of the level. (can be empty)
   * @param description The description of the level. (only set if levelName is not empty)
   */
  public TileLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      List<Coordinate> customPoints,
      String levelName,
      String description) {
    this(layout, designLabel);
    this.customPoints.addAll(customPoints);
    this.levelName = levelName;
    this.description = description;
  }

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
  public Optional<Tile> startTile() {
    return Optional.ofNullable(startTile);
  }

  @Override
  public void startTile(Tile start) {
    startTile = start;
  }

  @Override
  public Optional<Tile> endTile() {
    return Optional.ofNullable(exitTiles.size() > 0 ? exitTiles.get(0) : null);
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
  ;

  /**
   * Called when the level is ticked.
   *
   * @see #onFirstTick()
   * @see ITickable
   */
  protected void onTick() {}
  ;

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) onFirstTick();
    onTick();
  }

  /**
   * Gets the custom points that are within the given bounds.
   *
   * @param start The start index of the custom points list.
   * @param end The end index of the custom points list. (inclusive)
   * @return An array of custom points within the given bounds.
   */
  protected Coordinate[] getCoordinates(int start, int end) {
    return IntStream.rangeClosed(start, end)
        .mapToObj(customPoints()::get)
        .toArray(Coordinate[]::new);
  }

  @Override
  public List<Coordinate> customPoints() {
    return customPoints;
  }

  @Override
  public void addCustomPoint(Coordinate point) {
    customPoints.add(point);
  }

  @Override
  public void removeCustomPoint(Coordinate point) {
    customPoints.remove(point);
  }

  // ======== LOADING IN ==========//

  /**
   * Loads a DevDungeonLevel from the given path.
   *
   * @param path The path to the level file.
   * @return The loaded DevDungeonLevel.
   */
  public static TileLevel loadFromPath(IPath path) {
    try {
      BufferedReader reader;
      if (path.pathString().startsWith("jar:")) {
        InputStream is = TileLevel.class.getResourceAsStream(path.pathString().substring(4));
        reader = new BufferedReader(new InputStreamReader(is));
      } else {
        File file = new File(path.pathString());
        if (!file.exists()) {
          throw new MissingLevelException(path.toString());
        }
        reader = new BufferedReader(new FileReader(file));
      }

      // Parse DesignLabel
      String designLabelLine = readLine(reader);
      DesignLabel designLabel = parseDesignLabel(designLabelLine);

      // Parse Hero Position
      String heroPosLine = readLine(reader);
      Point heroPos = parseHeroPosition(heroPosLine);

      // Custom Points
      String customPointsLine = readLine(reader);
      List<Coordinate> customPoints = parseCustomPoints(customPointsLine);

      // Parse LAYOUT
      List<String> layoutLines = new ArrayList<>();
      String line;
      while (!(line = readLine(reader)).isEmpty()) {
        layoutLines.add(line);
      }
      LevelElement[][] layout = loadLevelLayoutFromString(layoutLines);

      TileLevel newLevel;
      newLevel = getDevLevel(DungeonLoader.currentLevel(), layout, designLabel, customPoints);

      // Set Hero Position
      Tile heroTile = newLevel.tileAt(heroPos);
      if (heroTile == null) {
        throw new RuntimeException("Invalid Hero Position: " + heroPos);
      }
      newLevel.startTile(heroTile);

      return newLevel;
    } catch (IOException e) {
      throw new RuntimeException("Error reading level file", e);
    }
  }

  /**
   * Read a line from the reader, ignoring comments. It skips lines that start with a '#' (comments)
   * and returns the next non-empty line.
   *
   * @param reader The reader to read from
   * @return The next non-empty, non-comment line without any comments
   * @throws IOException If an error occurs while reading from the reader
   */
  private static String readLine(BufferedReader reader) throws IOException {
    String line = reader.readLine();
    if (line == null) return "";
    while (line.trim().startsWith("#")) {
      line = reader.readLine();
    }
    line = line.trim().split("#")[0].trim();

    return line;
  }

  private static Point parseHeroPosition(String heroPositionLine) {
    if (heroPositionLine.isEmpty()) throw new RuntimeException("Missing Hero Position");
    String[] parts = heroPositionLine.split(",");
    if (parts.length != 2) throw new RuntimeException("Invalid Hero Position: " + heroPositionLine);
    try {
      float x = Float.parseFloat(parts[0]);
      float y = Float.parseFloat(parts[1]);
      return new Point(x, y);
    } catch (NumberFormatException e) {
      throw new RuntimeException("Invalid Hero Position: " + heroPositionLine);
    }
  }

  private static List<Coordinate> parseCustomPoints(String customPointsLine) {
    List<Coordinate> customPoints = new ArrayList<>();
    if (customPointsLine.isEmpty()) return customPoints;
    String[] points = customPointsLine.split(";");
    for (String point : points) {
      if (point.isEmpty()) continue;
      String[] parts = point.split(",");
      if (parts.length != 2) throw new RuntimeException("Invalid Custom Point: " + point);
      try {
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        customPoints.add(new Coordinate(x, y));
      } catch (NumberFormatException e) {
        throw new RuntimeException("Invalid Custom Point: " + point);
      }
    }
    return customPoints;
  }

  private static DesignLabel parseDesignLabel(String line) {
    if (line.isEmpty()) return DesignLabel.DEFAULT;
    try {
      return DesignLabel.valueOf(line);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Invalid DesignLabel: " + line);
    }
  }

  private static LevelElement[][] loadLevelLayoutFromString(List<String> lines) {
    LevelElement[][] layout = new LevelElement[lines.size()][lines.getFirst().length()];

    for (int y = 0; y < lines.size(); y++) {
      for (int x = 0; x < lines.getFirst().length(); x++) {
        char c = lines.get(y).charAt(x);
        switch (c) {
          case 'F' -> layout[y][x] = LevelElement.FLOOR;
          case 'W' -> layout[y][x] = LevelElement.WALL;
          case 'E' -> layout[y][x] = LevelElement.EXIT;
          case 'S' -> layout[y][x] = LevelElement.SKIP;
          case 'P' -> layout[y][x] = LevelElement.PIT;
          case 'H' -> layout[y][x] = LevelElement.HOLE;
          case 'D' -> layout[y][x] = LevelElement.DOOR;
          default -> throw new IllegalArgumentException("Invalid character in level layout: " + c);
        }
      }
    }

    return layout;
  }

  private static TileLevel getDevLevel(
      String levelName,
      LevelElement[][] layout,
      DesignLabel designLabel,
      List<Coordinate> customPoints) {
    Class<? extends TileLevel> levelHandler = DungeonLoader.levelHandler(levelName);
    if (levelHandler != null) {
      try {
        return levelHandler
            .getConstructor(LevelElement[][].class, DesignLabel.class, List.class)
            .newInstance(layout, designLabel, customPoints);
      } catch (Exception e) {
        throw new RuntimeException("Error creating level handler", e);
      }
    }
    throw new RuntimeException("No level handler found for level: " + levelName);
  }
}
