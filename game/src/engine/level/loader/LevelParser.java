package engine.level.loader;

import engine.level.DungeonLevel;
import engine.level.loader.parsers.LevelFormatParser;
import engine.level.loader.parsers.V1FormatParser;
import engine.level.loader.parsers.V2FormatParser;
import engine.level.loader.parsers.V3FormatParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Logger;

/**
 * The LevelParser class is responsible for parsing dungeon level data from various formats and
 * versions. It's backwards compatible with all old level data, migrating them to the up-to-date
 * format.
 */
public class LevelParser {

  private static final Logger LOGGER = Logger.getLogger(LevelParser.class.getName());
  private static final String VERSION_PREFIX = "Version: ";
  private static final LevelFormatParser DEFAULT_PARSER = new V3FormatParser();
  private static final LevelFormatParser V2_PARSER = new V2FormatParser();
  private static final LevelFormatParser LEGACY_PARSER = new V1FormatParser();

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
    String levelData;
    try {
      StringBuilder data = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        if (!data.isEmpty()) data.append('\n');
        data.append(line);
      }
      levelData = data.toString();
    } catch (IOException e) {
      LOGGER.severe("Error reading level data: " + e.getMessage());
      throw new IllegalArgumentException("Error reading level data", e);
    }

    String trimmed = levelData.stripLeading();
    if (trimmed.startsWith("{")) {
      try {
        return DEFAULT_PARSER.parseLevel(
            new BufferedReader(new StringReader(levelData)), levelHandlerName);
      } catch (IOException e) {
        throw new IllegalArgumentException("Error parsing V3 level data", e);
      }
    }

    BufferedReader versionReader = new BufferedReader(new StringReader(levelData));
    String versionLine;
    try {
      versionLine = LevelFormatParser.readLine(versionReader);
    } catch (IOException e) {
      throw new IllegalArgumentException("Error reading level version", e);
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
          yield LEGACY_PARSER.parseLevel(
              new BufferedReader(new StringReader(levelData)), levelHandlerName);
        }
        case 2 -> V2_PARSER.parseLevel(versionReader, levelHandlerName);
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
    return DEFAULT_PARSER.serializeLevel(level);
  }

  /**
   * Get the version string for a given version number.
   *
   * @param version The version number
   * @return The version string
   */
  public static String getVersion(int version) {
    return VERSION_PREFIX + version;
  }
}
