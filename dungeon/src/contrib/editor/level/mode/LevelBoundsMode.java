package contrib.editor.level.mode;

import contrib.editor.level.LevelEditorSystem;
import core.input.InputLabelFormatter.InputCode;
import core.level.Tile;
import core.level.utils.LevelTransformations;
import core.utils.InputManager;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A level editor mode for modifying the dimensions of a dungeon level.
 *
 * <p>LevelBoundsMode allows dynamic resizing of the level layout by adjusting its width and height.
 * The mode provides the following capabilities:
 * <ul>
 *   <li>Increasing or decreasing the height of the level
 *   <li>Increasing or decreasing the width of the level
 *   <li>Automatic filling of new cells with SKIP elements
 *   <li>Preservation of existing tiles and their level elements
 * </ul>
 *
 * <p>The level must maintain a minimum size of 1x1. When expanding the level, new cells are
 * initialized with the SKIP level element, while existing tiles retain their current values.
 */
public final class LevelBoundsMode extends LevelEditorMode {

  /**
   * Creates a new level-bounds editor mode.
   *
   * @param system the owning level editor system
   */
  public LevelBoundsMode(LevelEditorSystem system) {
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
      .map(
        level ->
          List.of(
            "Current size: " + level.layout()[0].length + "x" + level.layout().length,
            "New cells are filled with SKIP.",
            "Existing tiles keep their current LevelElement."))
      .orElse(List.of("Current size: <no dungeon level>"));
  }

  @Override
  protected Map<InputCode, String> getControls() {
    Map<InputCode, String> controls = new LinkedHashMap<>();
    controls.put(key(PRIMARY_UP), "Height +1");
    controls.put(key(PRIMARY_DOWN), "Height -1");
    controls.put(key(SECONDARY_UP), "Width +1");
    controls.put(key(SECONDARY_DOWN), "Width -1");
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

          level.setLayout(LevelTransformations.resizedLayout(layout, newRows, newCols));
        });
  }
}
