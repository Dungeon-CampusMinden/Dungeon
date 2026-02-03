package contrib.utils.systems.levelEditor;

import com.badlogic.gdx.graphics.Color;
import contrib.systems.LevelEditorSystem;
import contrib.systems.PositionSync;
import core.Game;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.InputManager;
import core.utils.Point;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The ShiftLevelMode allows the user to shift the entire level layout in one of four directions
 * (up, down, left, right) by one tile, provided that the shift does not overwrite any non-SKIP
 * tiles.
 */
public class ShiftLevelMode extends LevelEditorMode {

  /** Constructs a new ShiftLevelMode. */
  public ShiftLevelMode() {
    super("Level Bounds Mode");
  }

  @Override
  public void execute() {
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
  public void onEnter() {}

  @Override
  public void onExit() {}

  @Override
  public String getStatusText() {
    return "";
  }

  @Override
  public Map<Integer, String> getControls() {
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(PRIMARY_UP, "Shift Level Up");
    controls.put(PRIMARY_DOWN, "Shift Level Down");
    controls.put(SECONDARY_UP, "Shift Level Right");
    controls.put(SECONDARY_DOWN, "Shift Level Left");
    return controls;
  }

  private void shiftLevel(int x, int y) {
    if (x == 0 && y == 0) return;

    String directionName = x == 1 ? "RIGHT" : x == -1 ? "LEFT" : y == 1 ? "UP" : "DOWN";
    LevelEditorSystem.showFeedback("Shifting level " + directionName, Color.WHITE);

    DungeonLevel level = getLevel();
    Tile[][] layout = level.layout();

    if (!canShift(layout, x, y)) {
      LevelEditorSystem.showFeedback("Cannot shift level: overwriting non-SKIP tiles!", Color.RED);
      return;
    }

    // ALGORITHM: shift all tiles in the layout by x and y, which are either 1, 0 or -1
    int rows = layout.length;
    int cols = layout[0].length;

    LevelElement[][] newLayout = new LevelElement[rows][cols];

    // Iterate through the current layout and shift tiles accordingly
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        int newI = i - y;
        int newJ = j - x;

        // Check if the new position is within bounds
        if (newI >= 0 && newI < rows && newJ >= 0 && newJ < cols) {
          newLayout[i][j] = layout[newI][newJ].levelElement();
        } else {
          newLayout[i][j] = LevelElement.SKIP; // Empty space for out-of-bounds tiles
        }
      }
    }

    // Copy the shifted layout back to the original layout
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        level.changeTileElementType(layout[i][j], newLayout[i][j]);
      }
    }

    // Set all tiles again to fix the sprites
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        level.changeTileElementType(layout[i][j], layout[i][j].levelElement());
      }
    }

    // Shift all named points
    Map<String, Point> namedPoints = Game.currentLevel().orElseThrow().namedPoints();
    namedPoints.replaceAll((s, p) -> new Point(p.x() + x, p.y() + y));

    // Shift all entities
    Game.levelEntities(Set.of(PositionComponent.class))
        .forEach(
            e -> {
              PositionComponent pc = e.fetch(PositionComponent.class).orElseThrow();
              Point old = pc.position();
              pc.position(new Point(old.x() + x, old.y() + y));
              PositionSync.syncPosition(e);
            });
  }

  /**
   * Check if all tiles that would be deleted by the shift are empty (SKIP).
   *
   * @param layout the current layout
   * @param x the shift in x direction
   * @param y the shift in y direction
   * @return true if the shift is possible, false otherwise
   */
  private boolean canShift(Tile[][] layout, int x, int y) {
    int rows = layout.length;
    int cols = layout[0].length;

    if (x == 1) { // Shift right
      for (int i = 0; i < rows; i++) {
        if (layout[i][cols - 1].levelElement() != LevelElement.SKIP) {
          return false;
        }
      }
    } else if (x == -1) { // Shift left
      for (int i = 0; i < rows; i++) {
        if (layout[i][0].levelElement() != LevelElement.SKIP) {
          return false;
        }
      }
    }

    if (y == 1) { // Shift up
      for (int j = 0; j < cols; j++) {
        if (layout[rows - 1][j].levelElement() != LevelElement.SKIP) {
          return false;
        }
      }
    } else if (y == -1) { // Shift down
      for (int j = 0; j < cols; j++) {
        if (layout[0][j].levelElement() != LevelElement.SKIP) {
          return false;
        }
      }
    }

    return true;
  }
}
