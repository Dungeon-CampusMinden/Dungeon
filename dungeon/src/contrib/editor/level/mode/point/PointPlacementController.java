package contrib.editor.level.mode.point;

import contrib.editor.level.LevelEditorSystem;
import core.level.utils.Coordinate;
import core.utils.Point;
import java.awt.Color;
import java.util.Map;
import java.util.Optional;

/**
 * The PointPlacementController class manages the placement, pickup, cloning, and deletion of named
 * points in the level editor.
 *
 * <p>It interacts with the underlying LevelEditorSystem to modify and retrieve named points within
 * the current dungeon level.
 */
final class PointPlacementController {
  private final LevelEditorSystem system;

  private String heldPointName;

  PointPlacementController(LevelEditorSystem system) {
    this.system = system;
  }

  boolean hasHeldPoint() {
    return heldPointName != null;
  }

  String heldPointName() {
    return heldPointName;
  }

  int totalPoints() {
    return system.currentDungeonLevelForModes().map(level -> level.namedPoints().size()).orElse(0);
  }

  void addPoint(String name, Point position) {
    if (name == null || name.isBlank()) {
      return;
    }

    system.currentDungeonLevelForModes().ifPresent(level -> level.addNamedPoint(name, position));
    system.showModeFeedback("Added point: " + name, new Color(120, 220, 120));
  }

  void placeHeldPoint(Point snapPos) {
    if (heldPointName == null) {
      return;
    }

    system
        .currentDungeonLevelForModes()
        .ifPresent(level -> level.addNamedPoint(heldPointName, snapPos));
    system.showModeFeedback("Placed point: " + heldPointName, new Color(120, 220, 120));
    heldPointName = null;
  }

  void pickupOrClonePoint(Point cursorPos, Point snapPos) {
    Optional<String> clickedPoint = findNamedPointAt(cursorPos);
    clickedPoint.ifPresent(point -> heldPointName = point);

    if (heldPointName == null) {
      system.showModeFeedback("No point to pick up on coordinate!", new Color(255, 220, 120));
    } else if (clickedPoint.isEmpty()) {
      system
          .currentDungeonLevelForModes()
          .ifPresent(
              level -> {
                String baseName = heldPointName.replaceAll("\\d+$", "");
                String newPointName = baseName + (level.getHighestPointNumber(baseName) + 1);
                level.addNamedPoint(newPointName, snapPos);
                system.showModeFeedback("Cloned point: " + newPointName, new Color(120, 220, 120));
              });
    } else {
      system.showModeFeedback("Picked point: " + heldPointName, new Color(120, 220, 120));
    }
  }

  void deletePointAt(Point cursorPos) {
    findNamedPointAt(cursorPos)
        .ifPresent(
            pointName ->
                system
                    .currentDungeonLevelForModes()
                    .ifPresent(
                        level -> {
                          level.removeNamedPoint(pointName);
                          system.showModeFeedback(
                              "Removed point: " + pointName, new Color(255, 180, 180));
                        }));
  }

  void clearHeldPoint() {
    heldPointName = null;
  }

  Optional<String> findNamedPointAt(Point worldPos) {
    if (worldPos == null) {
      return Optional.empty();
    }

    Coordinate toCheck = worldPos.toCoordinate();
    return system
        .currentDungeonLevelForModes()
        .flatMap(
            level ->
                level.namedPoints().entrySet().stream()
                    .filter(entry -> entry.getValue().toCoordinate().equals(toCheck))
                    .map(Map.Entry::getKey)
                    .findFirst());
  }
}
