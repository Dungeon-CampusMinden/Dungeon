package contrib.editor.level.mode;

import contrib.editor.level.LevelEditorSystem;
import contrib.utils.components.collide.ColliderSync;
import core.Game;
import core.components.PositionComponent;
import core.input.InputLabel.InputCode;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.level.utils.LevelTransformations;
import core.utils.InputManager;
import core.utils.Point;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A level editor mode for shifting the entire dungeon level layout in cardinal directions.
 *
 * <p>ShiftLevelMode allows editors to translate all level tiles, named points, and entities in a
 * direction while maintaining the level structure. This is useful for repositioning level content
 * without resizing the level or manually moving individual elements.
 *
 * <p>Supported operations:
 * <ul>
 *   <li>Shifting the level up (increasing Y)
 *   <li>Shifting the level down (decreasing Y)
 *   <li>Shifting the level right (increasing X)
 *   <li>Shifting the level left (decreasing X)
 * </ul>
 *
 * <p>Shift behavior:
 * <ul>
 *   <li>All tiles in the level layout are shifted in the specified direction
 *   <li>New border tiles are filled with SKIP elements
 *   <li>All named points are translated by the shift offset
 *   <li>All entities with PositionComponent are repositioned and synchronized
 *   <li>Shifts are blocked if non-SKIP tiles are overwritten at the borders
 * </ul>
 *
 * <p>The mode prevents data loss by validating that only SKIP elements exist at the border that
 * would be overwritten before allowing a shift operation.
 */
public final class ShiftLevelMode extends LevelEditorMode {

  /**
   * Creates a new level-shift editor mode.
   *
   * @param system the owning level editor system
   */
  public ShiftLevelMode(LevelEditorSystem system) {
    super(system, "Shift Level Mode");
  }

  @Override
  protected void execute() {
    if (InputManager.isKeyJustPressed(PRIMARY_UP)) {
      shiftLevel(0, 1);
    } else if (InputManager.isKeyJustPressed(PRIMARY_DOWN)) {
      shiftLevel(0, -1);
    }

    if (InputManager.isKeyJustPressed(SECONDARY_UP)) {
      shiftLevel(1, 0);
    } else if (InputManager.isKeyJustPressed(SECONDARY_DOWN)) {
      shiftLevel(-1, 0);
    }
  }

  @Override
  protected List<String> getStatusLines() {
    return system()
      .currentDungeonLevelForModes()
      .map(
        level ->
          List.of(
            "Current size: " + level.layout()[0].length + "x" + level.layout().length,
            "Named points: " + level.namedPoints().size(),
            "Blocked if a non-SKIP border tile would be overwritten.",
            "Also shifts named points and entities with PositionComponent."))
      .orElse(List.of("Current size: <no dungeon level>"));
  }

  @Override
  protected Map<InputCode, String> getControls() {
    Map<InputCode, String> controls = new LinkedHashMap<>();
    controls.put(key(PRIMARY_UP), "Shift level up");
    controls.put(key(PRIMARY_DOWN), "Shift level down");
    controls.put(key(SECONDARY_UP), "Shift level right");
    controls.put(key(SECONDARY_DOWN), "Shift level left");
    return controls;
  }

  private void shiftLevel(int x, int y) {
    if (x == 0 && y == 0) {
      return;
    }

    String directionName = x == 1 ? "RIGHT" : x == -1 ? "LEFT" : y == 1 ? "UP" : "DOWN";
    system().showModeFeedback("Shifting level " + directionName, Color.WHITE);

    system()
      .currentDungeonLevelForModes()
      .ifPresent(
        level -> {
          Tile[][] layout = level.layout();

          if (!canShift(layout, x, y)) {
            system().showModeFeedback(
              "Cannot shift level: overwriting non-SKIP tiles!", Color.RED);
            return;
          }

          level.setLayout(LevelTransformations.shiftedLayout(layout, x, y));
          LevelTransformations.translateStartTiles(level, x, y);
          LevelTransformations.translateNamedPoints(level, x, y);

          Game.levelEntities(Set.of(PositionComponent.class))
            .forEach(
              entity -> {
                PositionComponent pc = entity.fetch(PositionComponent.class).orElseThrow();
                Point old = pc.position();
                pc.position(new Point(old.x() + x, old.y() + y));
                ColliderSync.sync(entity);
              });
        });
  }

  private boolean canShift(Tile[][] layout, int x, int y) {
    int rows = layout.length;
    int cols = layout[0].length;

    if (x == 1) {
      for (Tile[] tiles : layout) {
        if (tiles[cols - 1].levelElement() != LevelElement.SKIP) {
          return false;
        }
      }
    } else if (x == -1) {
      for (Tile[] tiles : layout) {
        if (tiles[0].levelElement() != LevelElement.SKIP) {
          return false;
        }
      }
    }

    if (y == 1) {
      for (int j = 0; j < cols; j++) {
        if (layout[rows - 1][j].levelElement() != LevelElement.SKIP) {
          return false;
        }
      }
    } else if (y == -1) {
      for (int j = 0; j < cols; j++) {
        if (layout[0][j].levelElement() != LevelElement.SKIP) {
          return false;
        }
      }
    }

    return true;
  }
}
