package rooms.systemRecovery.network;

import engine.Entity;
import engine.Game;
import engine.utils.Point;
import java.util.HashMap;
import java.util.Map;
import rooms.systemRecovery.riddles.SearchRobotRiddle;

/** Applies the server-authoritative tint for each search robot's active matrix cell. */
final class SearchRobotVisualSync {
  private final Map<Integer, Point> highlightedCells = new HashMap<>();
  private final Map<Integer, Integer> highlightedCellOriginalTints = new HashMap<>();

  /**
   * Updates the client-side matrix highlight from the robot metadata.
   *
   * @param entity synchronized search robot
   * @param metadata server-authoritative robot state
   */
  void apply(Entity entity, Map<String, String> metadata) {
    if (!"search_robot".equals(entity.name())) return;

    String cellValue =
        metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_SEARCH_ROBOT_CELL);
    Point nextCell = parsePoint(cellValue);
    if (nextCell == null) {
      clearHighlight(entity.id());
      return;
    }
    if (nextCell.equals(highlightedCells.get(entity.id()))) return;

    clearHighlight(entity.id());
    Game.tileAt(nextCell)
        .ifPresent(
            tile -> {
              highlightedCells.put(entity.id(), nextCell);
              highlightedCellOriginalTints.put(entity.id(), tile.tintColor());
              tile.tintColor(SearchRobotRiddle.SCAN_TINT);
            });
  }

  /** Clears the client-side highlight when the level or robot is removed. */
  void reset() {
    for (Integer robotId : highlightedCells.keySet().toArray(Integer[]::new)) {
      clearHighlight(robotId);
    }
  }

  private void clearHighlight(int robotId) {
    Point cell = highlightedCells.remove(robotId);
    Integer originalTint = highlightedCellOriginalTints.remove(robotId);
    if (cell == null || originalTint == null) return;
    Game.tileAt(cell).ifPresent(tile -> tile.tintColor(originalTint));
  }

  private static Point parsePoint(String value) {
    if (value == null || value.isBlank()) return null;
    String[] coordinates = value.split(",", -1);
    if (coordinates.length != 2) return null;
    try {
      return new Point(Float.parseFloat(coordinates[0]), Float.parseFloat(coordinates[1]));
    } catch (NumberFormatException ignored) {
      return null;
    }
  }
}
