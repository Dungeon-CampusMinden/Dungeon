package core.level.loader.parsers;

import contrib.entities.deco.Deco;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.loader.LevelParser;
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
import java.util.stream.Collectors;

/**
 * Parser for version 1 level format. This version changes customPoints to namedPoints, adds static
 * decorations and flips the layout vertically.
 */
public class V2FormatParser extends LevelFormatParser {

  @Override
  public DungeonLevel parseLevel(BufferedReader reader, String levelName) throws IOException {
    DesignLabel designLabel = parseDesignLabel(readLine(reader));
    List<Point> startTiles = parseStartTiles(readLine(reader));
    Map<String, Point> namedPoints = parseNamedPoints(readLine(reader));
    List<Tuple<Deco, Point>> decorations = parseDecorationList(readLine(reader));

    // Rest of the file is the layout
    List<String> layoutLines = new ArrayList<>();
    String line;
    while (!(line = readLine(reader)).trim().isEmpty()) {
      layoutLines.add(line.trim());
    }
    LevelElement[][] layout = loadLevelLayout(layoutLines);

    DungeonLevel newLevel = getLevel(levelName, layout, designLabel, namedPoints, decorations);

    startTiles.forEach(
        pos -> newLevel.tileAt(pos).ifPresent(tile -> newLevel.startTiles().add(tile)));
    return newLevel;
  }

  @Override
  public String serializeLevel(DungeonLevel level) {
    if (level == null) {
      LOGGER.error("Trying to serialize a null level!");
      throw new IllegalArgumentException("Level to serialize cannot be null");
    }

    String designLabel =
        level.designLabel().map(DesignLabel::name).orElse(DesignLabel.DEFAULT.name());

    String startingPositions = V2FormatParser.serializeStartingPositions(level.startTiles());
    String customPointsString = V2FormatParser.serializeNamedPoints(level.namedPoints());
    String decorations = V2FormatParser.serializeDecorationList(level.decorations());
    String dunLayout = V2FormatParser.serializeLevelLayout(level.layout());

    StringBuilder result = new StringBuilder();
    result.append(LevelParser.getVersion(2)).append("\n");
    result.append(designLabel).append("\n");
    result.append(startingPositions).append("\n");
    result.append(customPointsString).append("\n");
    result.append(decorations).append("\n");
    result.append(dunLayout);

    return result.toString();
  }

  /**
   * Parse a decoration list from a string in version 2 format. The format is assumed to be:
   * DecoType1:1.5,6.0;DecoType2:3.0,4.5;...
   *
   * @param input The input string containing the decoration list.
   * @return A list of tuples, each containing a Deco and its corresponding Point.
   */
  private static List<Tuple<Deco, Point>> parseDecorationList(String input) {
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
  public static String serializeDecorationList(List<Tuple<Deco, Point>> decorations) {
    return decorations.stream()
        .map(tuple -> tuple.a().name() + ":" + tuple.b().x() + "," + tuple.b().y())
        .collect(Collectors.joining(";"));
  }

  private static Map<String, Point> parseNamedPoints(String input) {
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
  public static String serializeNamedPoints(Map<String, Point> namedPoints) {
    return namedPoints.entrySet().stream()
        .map(entry -> entry.getKey() + ":" + entry.getValue().x() + "," + entry.getValue().y())
        .collect(Collectors.joining(";"));
  }

  private static List<Point> parseStartTiles(String input) {
    List<Point> startTiles = new ArrayList<>();

    String[] entries = input.split(";");
    for (String entry : entries) {
      String[] coordinates = entry.split(",");
      if (coordinates.length == 2) {
        try {
          float x = Float.parseFloat(coordinates[0].trim());
          float y = Float.parseFloat(coordinates[1].trim());
          startTiles.add(new Point(x, y));
        } catch (NumberFormatException e) {
          System.err.println("Invalid number format in entry: " + entry);
        }
      }
    }
    return startTiles;
  }

  /**
   * Serialize starting positions to a string in version 2 format.
   *
   * @param startTiles The list of starting tiles.
   * @return The serialized starting positions as a string.
   */
  public static String serializeStartingPositions(List<Tile> startTiles) {
    return startTiles.stream()
        .map(tile -> tile.position().x() + "," + tile.position().y())
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
  public static LevelElement[][] loadLevelLayout(List<String> lines) {
    int height = lines.size();
    int width = lines.getFirst().length();
    LevelElement[][] layout = new LevelElement[height][width];

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        char c = lines.get(y).charAt(x);
        layout[(height - 1) - y][x] = getLevelElementFromChar(c);
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
  public static String serializeLevelLayout(Tile[][] layout) {
    StringBuilder builder = new StringBuilder();
    for (int y = layout.length - 1; y >= 0; y--) {
      for (int x = 0; x < layout[0].length; x++) {
        builder.append(getCharFromLevelElement(layout[y][x].levelElement()));
      }
      if (y != 0) {
        builder.append(System.lineSeparator());
      }
    }
    return compressDungeonLayout(builder.toString());
  }
}
