package core.level.loader;

import contrib.entities.deco.Deco;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Tuple;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The LevelParser class is responsible for parsing dungeon level data from various formats and
 * versions. It's backwards compatible with all old level data, migrating them to the up-to-date
 * format.
 */
public class LevelParser {

  private static final Logger LOGGER = Logger.getLogger(LevelParser.class.getName());

  /**
   * Parse level data from a string.
   *
   * @param levelData The level data as a string
   * @param levelHandlerName The name of the level handler to use
   * @return The parsed DungeonLevel
   */
  public static DungeonLevel parseLevel(String levelData, String levelHandlerName) {
    BufferedReader reader = new BufferedReader(new java.io.StringReader(levelData));
    return parseLevel(reader, levelHandlerName);
  }

  /**
   * Parse level data from a BufferedReader.
   *
   * @param reader The BufferedReader to read level data from
   * @param levelHandlerName The name of the level handler to use
   * @return The parsed DungeonLevel
   */
  public static DungeonLevel parseLevel(BufferedReader reader, String levelHandlerName) {
    // Make a buffered reader for easier parsing:
    String versionLine = "";

    try {
      reader.mark(8192); // Mark the current position, since we need to reset for v1
      versionLine = readLine(reader);
    } catch (IOException e) {
      LOGGER.severe("Error reading level data: " + e.getMessage());
      return null;
    }

    // Line 1 should be the version, in the format 'Version: X'
    // Actual version numbers start at 2. If the first line doesnt match this format, it is version
    // 1.
    int version = 1;
    if (versionLine.startsWith("Version: ")) {
      try {
        version = Integer.parseInt(versionLine.substring(9).trim());
      } catch (NumberFormatException ignored) {
      }
    }

    try {
      return switch (version) {
        case 1 -> {
          reader.reset();
          yield parseLevelV1(reader, levelHandlerName);
        }
        case 2 -> parseLevelV2(reader, levelHandlerName);
        default -> {
          LOGGER.severe("Unsupported level version: " + version);
          throw new IllegalArgumentException("Unsupported level version: " + version);
        }
      };
    } catch (IOException e) {
      LOGGER.severe("Error parsing level data: " + e.getMessage());
      throw new IllegalArgumentException("Error parsing level data", e);
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

  // #region Version-Independent Methods
  private static DungeonLevel getLevel(
      String levelName,
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      List<Tuple<Deco, Point>> decorations) {
    Class<? extends DungeonLevel> levelHandler = DungeonLoader.levelHandler(levelName);
    if (levelHandler != null) {
      // Try normal constructor first
      try {
        return levelHandler
            .getConstructor(LevelElement[][].class, DesignLabel.class, Map.class, List.class)
            .newInstance(layout, designLabel, namedPoints, decorations);
      } catch (Exception ignored) {
      }

      // Legacy support: Try old constructor without decorations
      try {
        return levelHandler
            .getConstructor(LevelElement[][].class, DesignLabel.class, Map.class)
            .newInstance(layout, designLabel, namedPoints);
      } catch (Exception e) {
        throw new RuntimeException("Error creating level handler", e);
      }
    }
    throw new RuntimeException("No level handler found for level: " + levelName);
  }

  private static Point parseHeroPosition(String line) {
    if (line.isEmpty()) throw new RuntimeException("Missing Hero Position");
    String[] parts = line.split(",");
    if (parts.length != 2) throw new RuntimeException("Invalid Hero Position: " + line);
    try {
      float x = Float.parseFloat(parts[0]);
      float y = Float.parseFloat(parts[1]);
      return new Point(x, y);
    } catch (NumberFormatException e) {
      throw new RuntimeException("Invalid Hero Position: " + line);
    }
  }

  private static DesignLabel parseDesignLabel(String line) {
    if (line.isEmpty()) return DesignLabel.DEFAULT;
    try {
      return DesignLabel.valueOf(line);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Invalid DesignLabel: " + line);
    }
  }

  /**
   * Get LevelElement from character representation.
   *
   * @param c The character representing the LevelElement
   * @return The corresponding LevelElement
   */
  public static LevelElement getLevelElementFromChar(char c) {
    return switch (c) {
      case 'F' -> LevelElement.FLOOR;
      case 'W' -> LevelElement.WALL;
      case 'E' -> LevelElement.EXIT;
      case 'S' -> LevelElement.SKIP;
      case 'P' -> LevelElement.PIT;
      case 'H' -> LevelElement.HOLE;
      case 'D' -> LevelElement.DOOR;
      default -> throw new IllegalArgumentException("Invalid character in level layout: " + c);
    };
  }

  /**
   * Get character representation from LevelElement.
   *
   * @param element The LevelElement to convert
   * @return The corresponding character
   */
  public static char getCharFromLevelElement(LevelElement element) {
    return switch (element) {
      case FLOOR -> 'F';
      case WALL -> 'W';
      case EXIT -> 'E';
      case SKIP -> 'S';
      case PIT -> 'P';
      case HOLE -> 'H';
      case DOOR -> 'D';
    };
  }

  /**
   * The compressDungeonLayout method takes a multi-line string as input and returns a string where
   * all lines containing only 'S' are removed. It does this by using the replaceAll method with a
   * regular expression that matches lines containing only 'S' and replaces them with an empty
   * string.
   *
   * @param layout The dungeon layout to compress.
   * @return The compressed dungeon layout.
   */
  private static String compressDungeonLayout(String layout) {
    return layout.replaceAll("(?m)^S+$\\n", "");
  }

  // #endregion

  // #region V2 Parsing
  private static DungeonLevel parseLevelV2(BufferedReader reader, String levelHandlerName)
      throws IOException {
    DesignLabel designLabel = parseDesignLabel(readLine(reader));
    Point heroPos = parseHeroPosition(readLine(reader));
    Map<String, Point> namedPoints = v2ParseNamedPoints(readLine(reader));
    List<Tuple<Deco, Point>> decorations = v2ParseDecorationList(readLine(reader));

    // Rest of the file is the layout
    List<String> layoutLines = new ArrayList<>();
    String line;
    while (!(line = readLine(reader)).isEmpty()) {
      layoutLines.add(line);
    }
    LevelElement[][] layout = v2LoadLevelLayout(layoutLines);

    DungeonLevel newLevel =
        getLevel(levelHandlerName, layout, designLabel, namedPoints, decorations);

    // Set Hero Position
    newLevel
        .tileAt(heroPos)
        .ifPresentOrElse(
            newLevel::startTile,
            () -> {
              throw new RuntimeException("Invalid Hero Position: " + heroPos);
            });

    return newLevel;
  }

  /**
   * Parse a decoration list from a string in version 2 format. The format is assumed to be:
   * DecoType1:1.5,6.0;DecoType2:3.0,4.5;...
   *
   * @param input The input string containing the decoration list.
   * @return A list of tuples, each containing a Deco and its corresponding Point.
   */
  private static List<Tuple<Deco, Point>> v2ParseDecorationList(String input) {
    List<Tuple<Deco, Point>> decorations = new ArrayList<>();

    String[] entries = input.split(";");
    for (String entry : entries) {
      String[] parts = entry.split(":");
      if (parts.length == 2) {
        // Name of the deco in the Deco enum
        String decoType = parts[0].trim();
        String[] coordinates = parts[1].split(",");
        if (coordinates.length == 2) {
          try {
            float x = Float.parseFloat(coordinates[0].trim());
            float y = Float.parseFloat(coordinates[1].trim());
            Deco deco = Deco.valueOf(decoType);
            decorations.add(new Tuple<>(deco, new Point(x, y)));
          } catch (NumberFormatException e) {
            System.err.println("Invalid number format in entry: " + entry);
          } catch (IllegalArgumentException e) {
            System.err.println("Invalid Deco type in entry: " + entry);
          }
        }
      }
    }
    return decorations;
  }

  /**
   * Serialize a decoration list to a string in version 2 format.
   *
   * @param decorations The list of decorations to serialize.
   * @return The serialized decoration list as a string.
   */
  public static String v2SerializeDecorationList(List<Tuple<Deco, Point>> decorations) {
    return decorations.stream()
        .map(tuple -> tuple.a().name() + ":" + tuple.b().x() + "," + tuple.b().y())
        .collect(Collectors.joining(";"));
  }

  private static Map<String, Point> v2ParseNamedPoints(String input) {
    Map<String, Point> pointsMap = new HashMap<>();

    String[] entries = input.split(";"); // Split by semicolon
    for (String entry : entries) {
      String[] parts = entry.split(":"); // Split by colon
      if (parts.length == 2) {
        String name = parts[0].trim();
        String[] coordinates = parts[1].split(","); // Split coordinates

        if (coordinates.length == 2) {
          try {
            float x = Float.parseFloat(coordinates[0].trim());
            float y = Float.parseFloat(coordinates[1].trim());
            pointsMap.put(name, new Point(x, y));
          } catch (NumberFormatException e) {
            System.err.println("Invalid number format in entry: " + entry);
          }
        }
      }
    }
    return pointsMap;
  }

  /**
   * Serialize named points to a string in version 2 format.
   *
   * @param namedPoints The map of named points to serialize.
   * @return The serialized named points as a string.
   */
  public static String v2SerializeNamedPoints(Map<String, Point> namedPoints) {
    return namedPoints.entrySet().stream()
        .map(entry -> entry.getKey() + ":" + entry.getValue().x() + "," + entry.getValue().y())
        .collect(Collectors.joining(";"));
  }

  /**
   * Load level layout from a list of strings in version 2 format. In version 2, the layout flipped,
   * since in the game +y is up and the layout in the file should be more intuitive to read from top
   * to bottom.
   *
   * @param lines The list of strings representing the level layout.
   * @return A 2D array of LevelElement representing the level layout.
   */
  private static LevelElement[][] v2LoadLevelLayout(List<String> lines) {
    LevelElement[][] layout = new LevelElement[lines.size()][lines.getFirst().length()];
    for (int y = lines.size() - 1; y >= 0; y--) {
      for (int x = 0; x < lines.getFirst().length(); x++) {
        char c = lines.get(y).charAt(x);
        layout[y][x] = getLevelElementFromChar(c);
      }
    }
    return layout;
  }

  /**
   * Serialize level layout to a list of strings in version 2 format. The first line in the output
   * will represent the top row of the layout.
   *
   * @param layout The 2D array of LevelElement representing the level layout.
   * @return The string representation of the level layout.
   */
  public static String v2SerializeLevelLayout(Tile[][] layout) {
    StringBuilder builder = new StringBuilder();
    for (int y = layout.length - 1; y >= 0; y--) {
      for (int x = 0; x < layout[0].length; x++) {
        builder.append(getCharFromLevelElement(layout[y][x].levelElement()));
      }
      builder.append(System.lineSeparator());
    }
    return compressDungeonLayout(builder.toString());
  }

  // #endregion

  // #region V1 Parsing
  private static DungeonLevel parseLevelV1(BufferedReader reader, String levelHandlerName)
      throws IOException {
    DesignLabel designLabel = parseDesignLabel(readLine(reader));
    Point heroPos = parseHeroPosition(readLine(reader));
    List<Coordinate> customPoints = v1ParseCustomPoints(readLine(reader));
    Map<String, Point> namedPoints = v1MigrateCustomPointsToNamedPoints(customPoints);

    // Rest of the file is the layout
    List<String> layoutLines = new ArrayList<>();
    String line;
    while (!(line = readLine(reader)).isEmpty()) {
      layoutLines.add(line);
    }
    LevelElement[][] layout = v1LoadLevelLayout(layoutLines);

    DungeonLevel newLevel =
        getLevel(levelHandlerName, layout, designLabel, namedPoints, new ArrayList<>());

    // Set Hero Position
    Tile heroTile = newLevel.tileAt(heroPos).orElse(null);
    if (heroTile == null) {
      throw new RuntimeException("Invalid Hero Position: " + heroPos);
    }
    newLevel.startTile(heroTile);

    return newLevel;
  }

  private static LevelElement[][] v1LoadLevelLayout(List<String> lines) {
    LevelElement[][] layout = new LevelElement[lines.size()][lines.getFirst().length()];

    for (int y = 0; y < lines.size(); y++) {
      for (int x = 0; x < lines.getFirst().length(); x++) {
        char c = lines.get(y).charAt(x);
        layout[y][x] = getLevelElementFromChar(c);
      }
    }

    return layout;
  }

  private static List<Coordinate> v1ParseCustomPoints(String customPointsLine) {
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

  private static Map<String, Point> v1MigrateCustomPointsToNamedPoints(
      List<Coordinate> customPoints) {
    Map<String, Point> namedPoints = new HashMap<>();
    for (int i = 0; i < customPoints.size(); i++) {
      Coordinate coord = customPoints.get(i);
      namedPoints.put("Point" + i, new Point(coord.x(), coord.y()));
    }
    return namedPoints;
  }
  // #endregion
}
