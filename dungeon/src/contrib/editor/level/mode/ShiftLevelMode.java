package contrib.editor.level.mode;

import contrib.editor.level.systems.LevelEditorSystem;
import contrib.systems.PositionSync;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.InputManager;
import core.utils.Point;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** LITIENGINE level editor mode for shifting the whole level by one tile. */
public final class ShiftLevelMode extends LevelEditorMode {

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
      .<List<String>>map(
        level ->
          List.of(
            "Current size: " + level.layout()[0].length + "x" + level.layout().length,
            "Named points: " + level.namedPoints().size(),
            "Blocked if a non-SKIP border tile would be overwritten.",
            "Also shifts named points and entities with PositionComponent."))
      .orElse(List.of("Current size: <no dungeon level>"));
  }

  @Override
  protected Map<Integer, String> getControls() {
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(PRIMARY_UP, "Shift level up");
    controls.put(PRIMARY_DOWN, "Shift level down");
    controls.put(SECONDARY_UP, "Shift level right");
    controls.put(SECONDARY_DOWN, "Shift level left");
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

          int rows = layout.length;
          int cols = layout[0].length;

          LevelElement[][] newLayout = new LevelElement[rows][cols];

          for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
              int newI = i - y;
              int newJ = j - x;

              if (newI >= 0 && newI < rows && newJ >= 0 && newJ < cols) {
                newLayout[i][j] = layout[newI][newJ].levelElement();
              } else {
                newLayout[i][j] = LevelElement.SKIP;
              }
            }
          }

          for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
              level.changeTileElementType(layout[i][j], newLayout[i][j]);
            }
          }

          // Keep the second refresh pass from the old implementation.
          for (Tile[] tiles : layout) {
            for (int j = 0; j < cols; j++) {
              level.changeTileElementType(tiles[j], tiles[j].levelElement());
            }
          }

          level.namedPoints().replaceAll((name, point) -> new Point(point.x() + x, point.y() + y));

          Game.levelEntities(Set.of(PositionComponent.class))
            .forEach(
              entity -> {
                PositionComponent pc = entity.fetch(PositionComponent.class).orElseThrow();
                Point old = pc.position();
                pc.position(new Point(old.x() + x, old.y() + y));
                PositionSync.syncPosition(entity);
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
