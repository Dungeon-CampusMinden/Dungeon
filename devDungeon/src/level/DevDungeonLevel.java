package level;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.TileLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.MissingHeroException;
import core.utils.Point;
import core.utils.components.MissingComponentException;
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
      Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
      PositionComponent heroPosComp =
          hero.fetch(PositionComponent.class)
              .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
      heroPosComp.position(heroPos);

      // Parse Hostile Entities
      String monsterLine = readLine(reader);
      List<Entity> hostileEntities = parseHostileMobs(monsterLine);
      hostileEntities.forEach(Game::add);

      // Parse Misc Entities
      String miscEntitiesLine = readLine(reader);
      List<Entity> miscEntities = parseMiscEntities(miscEntitiesLine);
      miscEntities.forEach(Game::add);

      // Parse LAYOUT
      List<String> layoutLines = new ArrayList<>();
      String line;
      while (!(line = readLine(reader)).isEmpty()) {
        layoutLines.add(line);
      }
      LevelElement[][] layout = loadLevelLayoutFromString(layoutLines);

      return new DevDungeonLevel(layout, designLabel);
    } catch (IOException e) {
      throw new RuntimeException("Error reading level file", e);
    }
  }

  /**
   * Read a line from the reader, ignoring comments and empty lines.
   *
   * @param reader The reader to read from
   * @return The next non-empty, non-comment line
   * @throws IOException If an error occurs while reading from the reader
   */
  private static String readLine(BufferedReader reader) throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      line = line.trim().split("#")[0];
      if (!line.isEmpty()) {
        return line;
      }
    }
    return "";
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

  private static List<Entity> parseHostileMobs(String line) {
    List<Entity> entities = new ArrayList<>();
    if (line.isEmpty()) return entities;
    // TODO: Implement this method
    return entities;
  }

  private static List<Entity> parseMiscEntities(String line) {
    List<Entity> entities = new ArrayList<>();
    if (line.isEmpty()) return entities;
    // TODO: Implement this method
    return entities;
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
