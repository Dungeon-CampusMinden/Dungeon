package core.level.loader;

import core.level.DungeonLevel;
import core.level.loader.parsers.LevelFormatParser;
import core.level.loader.parsers.V1FormatParser;
import core.level.loader.parsers.V2FormatParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * The LevelParser class is responsible for parsing dungeon level data from various formats and
 * versions. It's backwards compatible with all old level data, migrating them to the up-to-date
 * format.
 */
public class LevelParser {

  private static final Logger LOGGER = Logger.getLogger(LevelParser.class.getName());
  private static final String VERSION_PREFIX = "Version: ";

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
      versionLine = LevelFormatParser.readLine(reader);
    } catch (IOException e) {
      LOGGER.severe("Error reading level data: " + e.getMessage());
      return null;
    }

    // Line 1 should be the version, in the format 'Version: X'
    // Actual version numbers start at 2. If the first line doesnt match this format, it is version
    // 1.
    int version = 1;
    if (versionLine.startsWith(VERSION_PREFIX)) {
      try {
        version = Integer.parseInt(versionLine.substring(9).trim());
      } catch (NumberFormatException ignored) {
      }
    }

    try {
      return switch (version) {
        case 1 -> {
          reader.reset();
          yield new V1FormatParser().parseLevel(reader, levelHandlerName);
        }
        case 2 -> new V2FormatParser().parseLevel(reader, levelHandlerName);
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
   * Serialize a DungeonLevel to a string.
   *
   * @param level The DungeonLevel to serialize
   * @return The serialized level data as a string
   */
  public static String serializeLevel(DungeonLevel level) {
    return new V2FormatParser().serializeLevel(level);
  }

  public static String getVersion(int version) {
    return VERSION_PREFIX + version;
  }
}
