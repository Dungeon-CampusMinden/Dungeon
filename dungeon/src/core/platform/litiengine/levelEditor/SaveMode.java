package core.platform.litiengine.levelEditor;

import core.level.loader.DungeonSaver;
import core.utils.InputManager;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** LITIENGINE save mode for exporting the current dungeon to the clipboard. */
public final class SaveMode extends LevelEditorMode {

  public SaveMode(core.platform.litiengine.systems.LitiengineLevelEditorSystem system) {
    super(system, "Save Mode");
  }

  @Override
  protected void execute() {
    if (InputManager.isKeyJustPressed(PRIMARY_UP)) {
      DungeonSaver.saveCurrentDungeon();
      system().showModeFeedback("Exported level to clipboard!", Color.GREEN);
    }
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
