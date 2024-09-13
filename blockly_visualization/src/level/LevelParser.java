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

public class LevelParser {
  private static final Logger LOGGER = Logger.getLogger(LevelParser.class.getSimpleName());
  private static final String LEVEL_PATH_PREFIX = "/levels";
  private static final Map<String, List<String>> LEVELS = new HashMap<>();
  protected static final Random RANDOM = new Random();

  public static BlocklyLevel getRandomVariant(String levelName) {
    List<String> levelVariants = LEVELS.get(levelName);

    if (levelVariants == null || levelVariants.isEmpty()) {
      throw new RuntimeException("Level file not found: " + levelName);
    }

    // Random Level Variant Path
    IPath levelPath = new SimpleIPath(levelVariants.get(RANDOM.nextInt(levelVariants.size())));

    return loadFromPath(levelPath);
  }

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

  private static boolean isRunningFromJar() {
    return Objects.requireNonNull(
        LevelParser.class.getResource(LevelParser.class.getSimpleName() + ".class"))
      .toString()
      .startsWith("jar:");
  }

  private static void getAllLevelFilePathsFromFileSystem() throws IOException, URISyntaxException {
    URI uri = Objects.requireNonNull(LevelParser.class.getResource(LEVEL_PATH_PREFIX)).toURI();
    Path path = Paths.get(uri);
    parseLevelFiles(path, false);
  }

  private static void getAllLevelFilePathsFromJar() throws IOException, URISyntaxException {
    URI uri = Objects.requireNonNull(LevelParser.class.getResource(LEVEL_PATH_PREFIX)).toURI();
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
              String[] onlyFileName = fileName.split("/");
              fileName = onlyFileName[onlyFileName.length - 1];
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
   * Loads a DevDungeonLevel from the given path.
   *
   * @param path The path to the level file.
   * @return The loaded DevDungeonLevel.
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
    if (line.isEmpty()) return DesignLabel.randomDesign();
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
}
