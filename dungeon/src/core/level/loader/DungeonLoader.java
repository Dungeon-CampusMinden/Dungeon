package core.level.loader;

import contrib.utils.level.MissingLevelException;
import core.Game;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * The DevDungeonLoader class is used to load {@link DungeonLevel} in the game. It is used to load
 * levels in a specific order or to load a specific level. The DungeonLoader class is used to load
 * levels from the file system or from a jar file.
 *
 * @see DungeonLevel
 */
public class DungeonLoader {

  private static final Logger LOGGER = Logger.getLogger(DungeonLoader.class.getSimpleName());
  private static final Random RANDOM = new Random();
  private static final String LEVEL_PATH_PREFIX = "/levels";
  private static final Map<String, List<String>> LEVELS = new HashMap<>();

  static {
    getAllLevelFilePaths();
  }

  private static final List<Tuple<String, Class<? extends DungeonLevel>>> levelOrder =
      new ArrayList<>();
  private static int currentLevel = -1;
  private static int currentVariant = 0;
  private static IVoidFunction afterAllLevels =
      () -> {
        System.out.println("Game Over!");
        System.out.println("You have passed all " + currentLevel + " levels!");
        Game.exit();
      };

  // Private constructor to prevent instantiation, as this class is a static utility class.
  private DungeonLoader() {}

  private static void getAllLevelFilePaths() {
    if (isRunningFromJar()) {
      try {
        getAllLevelFilePathsFromJar();
      } catch (IOException | URISyntaxException e) {
        LOGGER.warning("Failed to load level files from jar: " + e.getMessage());
      }
    } else {
      try {
        getAllLevelFilePathsFromFileSystem();
      } catch (IOException | URISyntaxException e) {
        LOGGER.warning("Failed to load level files from file system: " + e.getMessage());
      }
    }
  }

  private static boolean isRunningFromJar() {
    return Objects.requireNonNull(DungeonLoader.class.getResource(LEVEL_PATH_PREFIX))
        .toString()
        .startsWith("jar:");
  }

  private static void getAllLevelFilePathsFromFileSystem() throws IOException, URISyntaxException {
    URI uri = Objects.requireNonNull(DungeonLoader.class.getResource(LEVEL_PATH_PREFIX)).toURI();
    Path path = Paths.get(uri);
    parseLevelFiles(path, false);
  }

  private static void getAllLevelFilePathsFromJar() throws IOException, URISyntaxException {
    URI uri = Objects.requireNonNull(DungeonLoader.class.getResource(LEVEL_PATH_PREFIX)).toURI();
    FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
    Path path = fileSystem.getPath(LEVEL_PATH_PREFIX);
    parseLevelFiles(path, true);
  }

  private static void parseLevelFiles(Path path, boolean isJar) throws IOException {
    try (Stream<Path> paths = Files.walk(path)) {
      paths
          .filter(Files::isRegularFile)
          .forEach(
              file -> {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(".level")) {
                  String[] parts = fileName.split("_");
                  if (parts.length == 2) {
                    String levelName = parts[0];
                    String levelFilePath = file.toString();
                    LEVELS
                        .computeIfAbsent(levelName, k -> new ArrayList<>())
                        .add(isJar ? "jar:" + levelFilePath : levelFilePath);
                  } else {
                    LOGGER.warning("Invalid level file name: " + fileName);
                  }
                }
              });
    }
  }

  /**
   * Adds a level to the level order.
   *
   * @param level A {@link Tuple} containing the level name and the level handler class. The level
   *     name is converted to lowercase.
   * @see #levelOrder()
   * @see #loadNextLevel()
   */
  @SafeVarargs
  public static void addLevel(Tuple<String, Class<? extends DungeonLevel>>... level) {
    for (Tuple<String, Class<? extends DungeonLevel>> t : level) {
      levelOrder.add(new Tuple<>(t.a().toLowerCase(), t.b()));
    }
  }

  /**
   * Returns the current level order.
   *
   * @return The current level order.
   */
  public static List<String> levelOrder() {
    return levelOrder.stream().map(Tuple::a).toList();
  }

  /**
   * Returns the name of the current level (in lowercase).
   *
   * @return The name of the current level.
   * @throws IndexOutOfBoundsException If the current level index is out of bounds.
   */
  public static String currentLevel() {
    return levelOrder.get(currentLevel).a().toLowerCase();
  }

  /**
   * Returns the level handler for the given level name.
   *
   * @param levelName The name of the level.
   * @return The level handler for the given level name. (null if not found)
   * @see DungeonLevel
   */
  public static Class<? extends DungeonLevel> levelHandler(String levelName) {
    for (Tuple<String, Class<? extends DungeonLevel>> level : levelOrder) {
      if (level.a().equalsIgnoreCase(levelName)) {
        return level.b();
      }
    }
    return null;
  }

  private static ILevel getRandomVariant(String levelName) {
    List<String> levelVariants = LEVELS.get(levelName);

    if (levelVariants == null || levelVariants.isEmpty()) {
      throw new MissingLevelException(levelName);
    }

    // Random Level Variant Path
    currentVariant = RANDOM.nextInt(levelVariants.size());
    IPath levelPath = new SimpleIPath(levelVariants.get(currentVariant));

    return DungeonLoader.loadFromPath(levelPath);
  }

  /**
   * Loads the next level in the level order.
   *
   * <p>If the current level is the last level in the level order, it will execute the callback
   * function set by {@link #afterAllLevels(IVoidFunction)}. Default is to close the game with a
   * "Game Over!" message.
   *
   * <p>It chooses a random variant of the next level.
   */
  public static void loadNextLevel() {
    DungeonLoader.currentLevel++;
    try {
      Game.currentLevel(getRandomVariant(currentLevel()));
    } catch (MissingLevelException | IndexOutOfBoundsException e) {
      afterAllLevels.execute();
    }
  }

  /**
   * Sets the callback function that will be executed after all levels are completed.
   *
   * <p>Default is to close the game with a "Game Over!" message.
   *
   * @param afterAllLevels The callback function to execute after all levels are completed.
   * @see IVoidFunction
   * @see #loadNextLevel()
   */
  public static void afterAllLevels(IVoidFunction afterAllLevels) {
    DungeonLoader.afterAllLevels = afterAllLevels;
  }

  /**
   * Loads a specific level (with a random variant).
   *
   * @param levelName The name of the level.
   */
  public static void loadLevel(String levelName) {
    setCurrentLevelByLevelName(levelName);
    Game.currentLevel(getRandomVariant(levelName));
  }

  /**
   * Loads a specific level (with a random variant).
   *
   * @param levelIndex The index of the level.
   * @throws IndexOutOfBoundsException If the level index is out of bounds.
   */
  public static void loadLevel(int levelIndex) {
    if (levelIndex < 0 || levelIndex >= levelOrder.size()) {
      throw new IndexOutOfBoundsException("Level index is out of bounds: " + levelIndex);
    }
    loadLevel(levelOrder.get(levelIndex).a());
  }

  /**
   * Loads a specific level variant.
   *
   * @param levelName The name of the level.
   * @param variant The index of the level variant.
   */
  public static void loadLevel(String levelName, int variant) {
    setCurrentLevelByLevelName(levelName);
    List<String> levelVariants = LEVELS.get(levelName);
    if (levelVariants == null || levelVariants.isEmpty() || variant >= levelVariants.size()) {
      throw new MissingLevelException(levelName);
    }

    currentVariant = variant;
    IPath levelPath = new SimpleIPath(levelVariants.get(variant));
    Game.currentLevel(DungeonLoader.loadFromPath(levelPath));
  }

  private static void setCurrentLevelByLevelName(String levelName) {
    DungeonLoader.currentLevel = -1;
    for (int i = 0; i < levelOrder.size(); i++) {
      if (levelOrder.get(i).a().equals(levelName)) {
        DungeonLoader.currentLevel = i;
        break;
      }
    }

    if (currentLevel == -1) {
      throw new MissingLevelException(levelName);
    }
  }

  /**
   * Reloads the current level with the current variant.
   *
   * <p>This method is useful for resetting the level state without changing the level itself.
   */
  public static void reloadCurrentLevel() {
    if (currentLevel < 0 || currentLevel >= levelOrder.size()) {
      throw new IndexOutOfBoundsException("Current level index is out of bounds: " + currentLevel);
    }
    loadLevel(levelOrder.get(currentLevel).a(), currentVariant);
  }

  /**
   * Returns the index of the current level.
   *
   * @return The index of the current level.
   */
  public static int currentLevelIndex() {
    return currentLevel;
  }

  /**
   * Returns the current variant index of the level.
   *
   * @return The current variant index of the level.
   */
  public static int currentVariantIndex() {
    return currentVariant;
  }

  /**
   * Loads a DungeonLevel from the given path.
   *
   * @param path The path to the level file.
   * @return The loaded DevDungeonLevel.
   */
  public static DungeonLevel loadFromPath(IPath path) {
    try {
      BufferedReader reader;
      if (path.pathString().startsWith("jar:")) {
        InputStream is = DungeonLevel.class.getResourceAsStream(path.pathString().substring(4));
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

      DungeonLevel newLevel;
      newLevel = getLevel(DungeonLoader.currentLevel(), layout, designLabel, customPoints);

      // Set Hero Position
      Tile heroTile = newLevel.tileAt(heroPos).orElse(null);
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

  private static DungeonLevel getLevel(
      String levelName,
      LevelElement[][] layout,
      DesignLabel designLabel,
      List<Coordinate> customPoints) {
    Class<? extends DungeonLevel> levelHandler = DungeonLoader.levelHandler(levelName);
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
