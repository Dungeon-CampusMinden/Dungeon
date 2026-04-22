package contrib.editor.level.mode;

import contrib.editor.level.LevelEditorSystem;
import core.level.loader.DungeonSaver;
import core.platform.Platform;
import core.utils.InputManager;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * A level editor mode for saving and exporting dungeon levels.
 *
 * <p>SaveMode provides functionality to serialize and export the current dungeon level to the
 * system clipboard. This allows editors to share level configurations or back up their work in a
 * portable format.
 *
 * <p>The mode uses DungeonSaver for serialization, which converts the entire DungeonLevel into
 * a format suitable for clipboard storage and later reimport.
 *
 * <p>Supported operations:
 * <ul>
 *   <li>Exporting the current level to clipboard via DungeonSaver
 * </ul>
 */
public final class SaveMode extends LevelEditorMode {

  /**
   * Constructor for the SaveMode class, which initializes the save mode for the level editor system.
   *
   * @param system The LevelEditorSystem instance that this mode operates on. This parameter
   *               represents the central system managing the level editor functionality.
   */
  public SaveMode(LevelEditorSystem system) {
    super(system, "Save Mode");
  }

  @Override
  protected void execute() {
    if (!InputManager.isKeyJustPressed(PRIMARY_UP)) {
      return;
    }

    if (!Platform.clipboard().isSupported()) {
      system().showModeFeedback("Clipboard export is not supported on this runtime.", Color.YELLOW);
      return;
    }

    DungeonSaver.saveCurrentDungeon();
    system().showModeFeedback("Exported level to clipboard!", Color.GREEN);
  }

  @Override
  protected List<String> getStatusLines() {
    return List.of("Uses DungeonSaver serialization of the current DungeonLevel.");
  }

  @Override
  protected Map<Integer, String> getControls() {
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(PRIMARY_UP, "Save level to clipboard");
    return controls;
  }
}
