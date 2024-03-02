package level;

import core.Entity;
import core.level.TileLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.path.IPath;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DevDungeonLevel extends TileLevel {

  public DevDungeonLevel(LevelElement[][] layout, DesignLabel designLabel) {
    super(layout, designLabel);
  }

  public static DevDungeonLevel loadFromPath(IPath path) {
    // Load file from the path
    File file = new File(path.toString());
    if (!file.exists()) {
      throw new MissingLevelException(path.toString());
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      // Parse DesignLabel
      String designLabelLine = reader.readLine().trim();
      DesignLabel designLabel = parseDesignLabel(designLabelLine);

      // Parse Hostile Entities
      String monsterLine = reader.readLine().trim();
      Entity[] hostileEntities = parseHostileMobs(monsterLine);

      // Parse Misc Entities
      String miscEntitiesLine = reader.readLine().trim();
      Entity[] miscEntities = parseMiscEntities(miscEntitiesLine);

      // Parse LAYOUT
      List<String> layoutLines = new ArrayList<>();
      String line;
      while ((line = reader.readLine()) != null) {
        layoutLines.add(line);
      }
      LevelElement[][] layout = loadLevelLayoutFromString(layoutLines.toArray(new String[0]));

      return new DevDungeonLevel(layout, designLabel);
    } catch (IOException e) {
      throw new RuntimeException("Error reading level file", e);
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

  private static Entity[] parseHostileMobs(String line) {
    if (line.isEmpty()) return new Entity[0];
    List<Entity> entities = new ArrayList<>();
    // TODO: Implement this method
    return null;
  }

  private static Entity[] parseMiscEntities(String line) {
    if (line.isEmpty()) return new Entity[0];
    List<Entity> entities = new ArrayList<>();
    // TODO: Implement this method
    return null;
  }

  private static LevelElement[][] loadLevelLayoutFromString(String[] lines) {
    LevelElement[][] layout = new LevelElement[lines.length][lines[0].length()];

    for (int y = 0; y < lines.length; y++) {
      for (int x = 0; x < lines[y].length(); x++) {
        char c = lines[y].charAt(x);
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
          default:
            throw new IllegalArgumentException("Invalid character in level layout: " + c);
        }
      }
    }

    return layout;
  }
}
