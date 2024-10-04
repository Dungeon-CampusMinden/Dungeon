package level;

import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
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
 * This class is used to parse a level file and generate a layout.
 * Usage:
 * Step 1: Load all level files with the function getAllLevelFilePaths. The level files must be placed under
 *         assets/levels. Each level file must be name like this <level-name>_<difficulty>_<int>.level.
 * Step 2: Call the function getRandomVariant for your levels that you want to add to your game.
 * Step 3: Store all levels in a list.
 * Step 4: Define a function that should be called when the hero is on the end tile of a level.
 * Step 5: Add this function to your LevelSystem with the function onEndTile. Check the Client class as an example.
 */
public class LevelParser {
  private static final Logger LOGGER = Logger.getLogger(LevelParser.class.getSimpleName());
  private static final String LEVEL_PATH_PREFIX = "/levels";
  // Top Level key is the level name -> Returns another Map. Key for this map is the difficulty of the level.
  private static final Map<String, Map<String, List<String>>> LEVELS = new HashMap<>();
  protected static final Random RANDOM = new Random();

  /**
   * Retrieve a random layout for the specified level.
   * @param levelName The name of the level.
   * @return Returns a BlocklyLevel instance containing the layout, design label, hero position and custom points.
   */
  public static BlocklyLevel getRandomVariant(String levelName, String difficulty) {
    List<String> levelVariants = LEVELS.get(levelName).get(difficulty);

    if (levelVariants == null || levelVariants.isEmpty()) {
      throw new RuntimeException("Level file not found: " + levelName);
    }

    // Random Level Variant Path
    IPath levelPath = new SimpleIPath(levelVariants.get(RANDOM.nextInt(levelVariants.size())));

    return loadFromPath(levelPath);
  }

  /**
   * Read all level files placed under assets/levels. This functions should always be executed as a first step.
   */
  public static void getAllLevelFilePaths() {
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

  /**
   * Check if application is currently running from a jar.
   * @return Returns true if the application is currently running from a jar. Otherwise false.
   */
  private static boolean isRunningFromJar() {
    return Objects.requireNonNull(
        LevelParser.class.getResource(LevelParser.class.getSimpleName() + ".class"))
      .toString()
      .startsWith("jar:");
  }

  /**
   * Parses all level files from the file system when not running from a jar.
   * @throws IOException
   * @throws URISyntaxException
   */
  private static void getAllLevelFilePathsFromFileSystem() throws IOException, URISyntaxException {
    URI uri = Objects.requireNonNull(LevelParser.class.getResource(LEVEL_PATH_PREFIX)).toURI();
    Path path = Paths.get(uri);
    parseLevelFiles(path, false);
  }

  /**
   * Parses all level files from the jar.
   * @throws IOException
   * @throws URISyntaxException
   */
  private static void getAllLevelFilePathsFromJar() throws IOException, URISyntaxException {
    URI uri = Objects.requireNonNull(LevelParser.class.getResource(LEVEL_PATH_PREFIX)).toURI();
    FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
    Path path = fileSystem.getPath(LEVEL_PATH_PREFIX);
    parseLevelFiles(path, true);
  }

  /**
   * Parse a level file from the given path.
   * @param path Path of the level file.
   * @param isJar Set to true, if currently running from jar. Path needs to be adjusted in this case. If executed from
   *              the file system set to false.
   * @throws IOException
   */
  private static void parseLevelFiles(Path path, boolean isJar) throws IOException {
    try (Stream<Path> paths = Files.walk(path)) {
      paths
        .filter(Files::isRegularFile)
        .forEach(
          file -> {
            String fileName = file.getFileName().toString();
            if (fileName.endsWith(".level")) {
              String[] onlyFileName = fileName.split("/");
              fileName = onlyFileName[onlyFileName.length - 1];
              String[] parts = fileName.split("_");
              if (parts.length == 3) {
                String levelName = parts[0];
                String difficulty = parts[1];
                String levelFilePath = file.toString();
                if (!LEVELS.containsKey(levelName)) {
                  Map<String, List<String>> diffs = new HashMap<>();
                  List<String> files = new ArrayList<>();
                  files.add(isJar ? "jar:" + levelFilePath : levelFilePath);
                  diffs.put(difficulty, files);
                  LEVELS.put(levelName, diffs);
                } else if (!LEVELS.get(levelName).containsKey(difficulty)) {
                  List<String> files = new ArrayList<>();
                  files.add(isJar ? "jar:" + levelFilePath : levelFilePath);
                  LEVELS.get(levelName).put(difficulty, files);
                } else {
                  LEVELS.get(levelName).get(difficulty).add(isJar ? "jar:" + levelFilePath : levelFilePath);
                }
              } else {
                LOGGER.warning("Invalid level file name: " + fileName);
              }
            }
          });
    }
  }

  /**
   * Loads a BlocklyLevel from the specified path.
   * @param path The path to the level file.
   * @return Returns an instance of a BlocklyLevel containing the layout, design label, hero position and custom points.
   */
  public static BlocklyLevel loadFromPath(IPath path) {
    try {
      BufferedReader reader;
      if (path.pathString().startsWith("jar:")) {
        InputStream is = LevelParser.class.getResourceAsStream(path.pathString().substring(4));
        reader = new BufferedReader(new InputStreamReader(is));
      } else {
        File file = new File(path.pathString());
        if (!file.exists()) {
          throw new FileNotFoundException("File not found: " + path);
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

      return new BlocklyLevel(layout, designLabel, heroPos, customPoints);
    } catch (IOException e) {
      throw new RuntimeException("Error reading level file", e);
    }
  }

  /**
   * Read a line with the given reader. This function will ignore lines beginning with # and will also remove all chars
   * after the # if a line contains the # char.
   * @param reader Reader that will be used for reading a new line.
   * @return Returns the line.
   * @throws IOException
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

  /**
   * Parse the position of the hero and returns a point.
   * @param heroPositionLine The line from the level file containing the hero position. The line must not be empty or
   *                         contain an invalid format for the hero position.
   * @return Returns the point with the x and y coordinate that will be used as the start position for the hero.
   */
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

  /**
   * Parse custom points of the level file. Can be used for custom events.
   * @param customPointsLine The line containing the custom points. The line can be empty.
   * @return Returns an array list with all custom points. Return an empty list if the line is empty.
   */
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

  /**
   * Parse the design label from the level file.
   * @param line The line containing the design label. If the line is empty choose a random design label.
   * @return Returns the design label for the level.
   */
  private static DesignLabel parseDesignLabel(String line) {
    if (line.isEmpty()) return DesignLabel.randomDesign();
    try {
      return DesignLabel.valueOf(line);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Invalid DesignLabel: " + line);
    }
  }

  /**
   * Generate a 2D array of Level Elements for a list of lines. The generated Level Elements build the level layout.
   * @param lines Lines of the level file only containing information about the level layout.
   * @return Returns a 2D array of Level Elements which will be used as the level layout.
   */
  private static LevelElement[][] loadLevelLayoutFromString(List<String> lines) {
    LevelElement[][] layout = new LevelElement[lines.size()][lines.getFirst().length()];

    for (int y =0; y <  lines.size(); y++) {
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
}
