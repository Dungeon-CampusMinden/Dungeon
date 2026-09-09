package rooms.programming.level;

import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.level.utils.Coordinate;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.components.path.SimpleIPath;
import java.util.List;

/** Creates identical gate tiles on host and clients so ordinary door snapshots synchronize them. */
final class ProgrammingGates {
  private ProgrammingGates() {}

  static void initialize(DungeonLevel level) {
    List<String> gates =
        level.namedPoints().keySet().stream()
            .filter(name -> name.endsWith("-gate-start"))
            .filter(name -> name.startsWith("act"))
            .map(name -> name.substring(0, name.length() - "-gate-start".length()))
            .sorted()
            .toList();
    gates.forEach(gate -> visit(level, gate, true));
    // Neighbour refreshes during tile creation finish before custom door art is installed.
    for (String gate : gates) {
      Point start = level.namedPoints().get(gate + "-gate-start");
      Point end = level.namedPoints().get(gate + "-gate-end");
      if (start == null || end == null) continue;
      int minX = (int) Math.min(start.x(), end.x());
      int maxX = (int) Math.max(start.x(), end.x());
      int minY = (int) Math.min(start.y(), end.y());
      int maxY = (int) Math.max(start.y(), end.y());
      for (int y = minY; y <= maxY; y++) {
        for (int x = minX; x <= maxX; x++) {
          level
              .tileAt(new Coordinate(x, y))
              .ifPresent(tile -> tile.texturePath(new SimpleIPath("rooms/programming/gate.png")));
        }
      }
    }
  }

  static void open(DungeonLevel level, int act) {
    visit(level, "act" + act, false);
  }

  private static void visit(DungeonLevel level, String gate, boolean initialize) {
    Point start = level.namedPoints().get(gate + "-gate-start");
    Point end = level.namedPoints().get(gate + "-gate-end");
    if (start == null || end == null) return;
    for (int y = (int) Math.min(start.y(), end.y()); y <= Math.max(start.y(), end.y()); y++) {
      for (int x = (int) Math.min(start.x(), end.x()); x <= Math.max(start.x(), end.x()); x++) {
        Coordinate coordinate = new Coordinate(x, y);
        if (initialize) {
          level
              .tileAt(coordinate)
              .ifPresent(tile -> level.changeTileElementType(tile, LevelElement.DOOR));
        }
        level
            .tileAt(coordinate)
            .filter(DoorTile.class::isInstance)
            .map(DoorTile.class::cast)
            .ifPresent(
                door -> {
                  if (initialize) door.close();
                  else door.open();
                });
      }
    }
  }
}
