package core.level.loader.parsers;

import core.level.DungeonLevel;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Parser for version 1 level format. */
public class V1FormatParser extends LevelFormatParser {

  @Override
  public DungeonLevel parseLevel(BufferedReader reader, String levelName) throws IOException {
    DesignLabel designLabel = parseDesignLabel(readLine(reader));
    Point playerPos = parsePlayerPosition(readLine(reader));
    List<Coordinate> customPoints = parseCustomPoints(readLine(reader));
    Map<String, Point> namedPoints = migrateCustomPointsToNamedPoints(customPoints);

    // Rest of the file is the layout
    List<String> layoutLines = new ArrayList<>();
    String line;
    while (!(line = readLine(reader)).isEmpty()) {
      layoutLines.add(line);
    }
    LevelElement[][] layout = loadLevelLayout(layoutLines);

    DungeonLevel newLevel =
        getLevel(levelName, layout, designLabel, namedPoints, new ArrayList<>());

    // Set Player Position
    Tile playerTile = newLevel.tileAt(playerPos).orElse(null);
    if (playerTile == null) {
      throw new RuntimeException("Invalid Player Position: " + playerPos);
    }
    newLevel.startTiles().add(playerTile);

    return newLevel;
  }

  @Override
  public String serializeLevel(DungeonLevel level) {
    throw new RuntimeException("Do not use this method. V1 is deprecated.");
  }

  private static LevelElement[][] loadLevelLayout(List<String> lines) {
    LevelElement[][] layout = new LevelElement[lines.size()][lines.getFirst().length()];

    for (int y = 0; y < lines.size(); y++) {
      for (int x = 0; x < lines.getFirst().length(); x++) {
        char c = lines.get(y).charAt(x);
        layout[y][x] = getLevelElementFromChar(c);
      }
    }

    return layout;
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

  private static Map<String, Point> migrateCustomPointsToNamedPoints(
      List<Coordinate> customPoints) {
    Map<String, Point> namedPoints = new HashMap<>();
    for (int i = 0; i < customPoints.size(); i++) {
      Coordinate coord = customPoints.get(i);
      namedPoints.put("Point" + i, new Point(coord.x(), coord.y()));
    }
    return namedPoints;
  }
}
