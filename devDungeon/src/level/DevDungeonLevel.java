package level;

import core.level.Tile;
import core.level.TileLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.components.path.IPath;
import level.utils.DungeonLoader;
import level.utils.MissingLevelException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DevDungeonLevel extends TileLevel {

  public DevDungeonLevel(LevelElement[][] layout, DesignLabel designLabel) {
    super(layout, designLabel);
  }

  /**
   * Load a DevDungeonLevel from a file path
   *
   * @param path The path to the file to load
   * @return The loaded DevDungeonLevel
   */
  public static DevDungeonLevel loadFromPath(IPath path) throws ReflectiveOperationException {
    // Load file from the path
    File file = new File(path.pathString());
    if (!file.exists()) {
      throw new MissingLevelException(path.toString());
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      // Parse DesignLabel
      String designLabelLine = readLine(reader);
      DesignLabel designLabel = parseDesignLabel(designLabelLine);

      // Parse Hero Position
      String heroPosLine = readLine(reader);
      Point heroPos = parseHeroPosition(heroPosLine);

      // Parse LAYOUT
      List<String> layoutLines = new ArrayList<>();
      String line;
      while (!(line = readLine(reader)).isEmpty()) {
        layoutLines.add(line);
      }
      LevelElement[][] layout = loadLevelLayoutFromString(layoutLines);

      // Load reflectively the "level-handler" class (level.DevLevelXY) for the current level
      // For custom logic
      Class<?> clazz =
          DevDungeonLevel.class
              .getClassLoader()
              .loadClass(String.format("level.DevLevel%02d", DungeonLoader.CURRENT_LEVEL));
      if (!DevDungeonLevel.class.isAssignableFrom(clazz))
        throw new RuntimeException("Invalid DevDungeonLevel Class: " + clazz);

      DevDungeonLevel newLevel =
          (DevDungeonLevel) clazz.getDeclaredConstructors()[0].newInstance(layout, designLabel);

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
          case 'F':
            layout[y][x] = LevelElement.FLOOR;
            break;
          case 'W':
            layout[y][x] = LevelElement.WALL;
            break;
          case 'E':
            layout[y][x] = LevelElement.EXIT;
            break;
          case 'S':
            layout[y][x] = LevelElement.SKIP;
            break;
          case 'H':
            layout[y][x] = LevelElement.HOLE;
            break;
          case 'D':
            layout[y][x] = LevelElement.DOOR;
            break;
          default:
            throw new IllegalArgumentException("Invalid character in level layout: " + c);
        }
      }
    }

    return layout;
  }
}
