package contrib.utils.systems.levelEditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import contrib.systems.LevelEditorSystem;
import core.Game;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.Point;
import java.util.Map;

public class ShiftLevelMode extends LevelEditorMode {

  public ShiftLevelMode() {
    super("Level Bounds Mode");
  }

  @Override
  public void execute() {
    if (Gdx.input.isKeyJustPressed(PRIMARY_UP)) {
      shiftLevel(0, 1);
    } else if (Gdx.input.isKeyJustPressed(PRIMARY_DOWN)) {
      shiftLevel(0, -1);
    }

    if (Gdx.input.isKeyJustPressed(SECONDARY_DOWN)) {
      shiftLevel(1, 0);
    } else if (Gdx.input.isKeyJustPressed(SECONDARY_UP)) {
      shiftLevel(-1, 0);
    }
  }

  @Override
  public void onEnter() {}

  @Override
  public void onExit() {}

  @Override
  public String getStatusText() {
    StringBuilder status = new StringBuilder("--- Edit Level Shift Mode ---");
    status.append("\nControls:");
    status.append("\n- E: shift level up");
    status.append("\n- Q: shift level down");
    status.append("\n- Right: shift level right");
    status.append("\n- Left: shift level left");
    return status.toString();
  }

  private void shiftLevel(int x, int y) {
    if (x == 0 && y == 0) return;

    LevelEditorSystem.showFeedback("Shifting level by: x=" + x + ", y=" + y, Color.WHITE);

    DungeonLevel level = getLevel();
    Tile[][] layout = level.layout();

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
  }
}
