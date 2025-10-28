package contrib.utils.systems.levelEditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import contrib.systems.LevelEditorSystem;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.utils.LevelElement;

public class LevelBoundsMode extends LevelEditorMode {

  public LevelBoundsMode() {
    super("Level Bounds Mode");
  }

  @Override
  public void execute() {
    if (Gdx.input.isKeyJustPressed(PRIMARY_UP)) {
      addSize(0, 1);
    } else if (Gdx.input.isKeyJustPressed(PRIMARY_DOWN)) {
      addSize(0, -1);
    }

    if (Gdx.input.isKeyJustPressed(SECONDARY_DOWN)) {
      addSize(1, 0);
    } else if (Gdx.input.isKeyJustPressed(SECONDARY_UP)) {
      addSize(-1, 0);
    }
  }

  @Override
  public void onEnter() {}

  @Override
  public void onExit() {}

  @Override
  public String getStatusText() {
    StringBuilder status = new StringBuilder("--- Edit Level Bounds Mode ---");
    status.append("\nControls:");
    status.append("\n- E: increase height");
    status.append("\n- Q: decrease height");
    status.append("\n- Right: increase width");
    status.append("\n- Left: decrease width");
    return status.toString();
  }

  private void addSize(int addX, int addY) {
    LevelEditorSystem.showFeedback("Resizing level by: x + " + addX + ", y + " + addY, Color.WHITE);

    DungeonLevel level = getLevel();
    Tile[][] layout = level.layout();

    int rows = layout.length;
    int cols = layout[0].length;

    int newRows = rows + addY;
    int newCols = cols + addX;

    LevelElement[][] newLayout = new LevelElement[newRows][newCols];

    for (int i = 0; i < newRows; i++) {
      for (int j = 0; j < newCols; j++) {
        if (i == rows || j == cols) {
          newLayout[i][j] = LevelElement.SKIP;
        } else {
          newLayout[i][j] = layout[i][j].levelElement();
        }
      }
    }

    level.setLayout(newLayout);
  }
}
