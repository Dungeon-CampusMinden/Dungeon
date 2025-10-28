package core.level.loader.parsers;

import contrib.entities.deco.Deco;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.logging.DungeonLogger;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Abstract base class for level format parsers. */
public abstract class LevelFormatParser {

  protected static DungeonLogger LOGGER =
      DungeonLogger.getLogger(LevelFormatParser.class.getName());

  /**
   * Parse a level from the given BufferedReader.
   *
   * @param reader the reader to parse from
   * @param levelName the name of the level
   * @return the parsed DungeonLevel
   * @throws IOException if an I/O error occurs
   */
  public abstract DungeonLevel parseLevel(BufferedReader reader, String levelName)
      throws IOException;

  /**
   * Serialize the given DungeonLevel to a string.
   *
   * @param level the level to serialize
   * @return the serialized level as a string
   */
  public abstract String serializeLevel(DungeonLevel level);

  /**
   * Read a line from the reader, ignoring comments. It skips lines that start with a '#' (comments)
   * and returns the next non-comment line, stripping any inline comments as well.
   *
   * @param reader The reader to read from
   * @return The next non-empty, non-comment line without any comments
   * @throws IOException If an error occurs while reading from the reader
   */
  public static String readLine(BufferedReader reader) throws IOException {
    String line = reader.readLine();
    if (line == null) return "";
    while (line.trim().startsWith("#")) {
      line = reader.readLine();
    }
    line = line.trim().split("#")[0].trim();

    return line;
  }

  protected static DungeonLevel getLevel(
      String levelName,
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      List<Tuple<Deco, Point>> decorations) {
    Optional<Class<? extends DungeonLevel>> levelHandlerOptional =
        DungeonLoader.levelHandler(levelName);
    if (levelHandlerOptional.isPresent()) {
      var levelHandler = levelHandlerOptional.get();
      try {
        return levelHandler
            .getConstructor(LevelElement[][].class, DesignLabel.class, Map.class, List.class)
            .newInstance(layout, designLabel, namedPoints, decorations);
      } catch (Exception ignored) {
      }

      try {
        return levelHandler
            .getConstructor(LevelElement[][].class, DesignLabel.class, Map.class)
            .newInstance(layout, designLabel, namedPoints);
      } catch (Exception e) {
        throw new RuntimeException("Error creating level handler", e);
      }
    }
    LOGGER.info("No level handler found for level '{}', using default DungeonLevel.", levelName);
    return new DungeonLevel(layout, designLabel, namedPoints, decorations);
  }

  protected static Point parseHeroPosition(String line) {
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

  protected static DesignLabel parseDesignLabel(String line) {
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
  protected static String compressDungeonLayout(String layout) {
    return layout.replaceAll("(?m)^S+$\\n", "");
  }
}
