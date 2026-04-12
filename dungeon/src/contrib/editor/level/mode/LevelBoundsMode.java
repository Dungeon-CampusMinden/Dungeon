package contrib.editor.level.mode;

import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.InputManager;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** LITIENGINE level editor mode for resizing the dungeon bounds. */
public final class LevelBoundsMode extends LevelEditorMode {

  public LevelBoundsMode(core.platform.litiengine.systems.LitiengineLevelEditorSystem system) {
    super(system, "Level Bounds Mode");
  }

  @Override
  protected void execute() {
    if (InputManager.isKeyJustPressed(PRIMARY_UP)) {
      addSize(0, 1);
    } else if (InputManager.isKeyJustPressed(PRIMARY_DOWN)) {
      addSize(0, -1);
    }

    if (InputManager.isKeyJustPressed(SECONDARY_UP)) {
      addSize(1, 0);
    } else if (InputManager.isKeyJustPressed(SECONDARY_DOWN)) {
      addSize(-1, 0);
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
            "New cells are filled with SKIP.",
            "Existing tiles keep their current LevelElement."))
      .orElse(List.of("Current size: <no dungeon level>"));
  }

  @Override
  protected Map<Integer, String> getControls() {
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(PRIMARY_UP, "Height +1");
    controls.put(PRIMARY_DOWN, "Height -1");
    controls.put(SECONDARY_UP, "Width +1");
    controls.put(SECONDARY_DOWN, "Width -1");
    return controls;
  }

  private void addSize(int addX, int addY) {
    system()
      .currentDungeonLevelForModes()
      .ifPresent(
        level -> {
          Tile[][] layout = level.layout();
          int rows = layout.length;
          int cols = layout[0].length;

          int newRows = rows + addY;
          int newCols = cols + addX;

          if (newRows < 1 || newCols < 1) {
            system().showModeFeedback("Level size must stay at least 1x1.", Color.YELLOW);
            return;
          }

          String feedback =
            "Resized level by: ("
              + addX
              + ", "
              + addY
              + ")"
              + "\nNew size: ("
              + newCols
              + ", "
              + newRows
              + ")";
          system().showModeFeedback(feedback, Color.WHITE);

          LevelElement[][] newLayout = new LevelElement[newRows][newCols];

          for (int i = 0; i < newRows; i++) {
            for (int j = 0; j < newCols; j++) {
              if (i >= rows || j >= cols) {
                newLayout[i][j] = LevelElement.SKIP;
              } else {
                newLayout[i][j] = layout[i][j].levelElement();
              }
            }
          }

          level.setLayout(newLayout);
        });
  }
}
