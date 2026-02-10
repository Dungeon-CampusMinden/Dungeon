package contrib.utils.systems.levelEditor;

import com.badlogic.gdx.graphics.Color;
import contrib.systems.LevelEditorSystem;
import core.level.loader.DungeonSaver;
import core.utils.InputManager;
import java.util.LinkedHashMap;
import java.util.Map;

/** The SaveMode allows the user to save the current dungeon layout to the clipboard. */
public class SaveMode extends LevelEditorMode {

  private boolean saveToFile;
  private String pathToLevels;

  /** Constructs a new SaveMode. */
  public SaveMode(boolean saveToFile, String pathToLevels) {
    super("Save Mode");
    this.saveToFile = saveToFile;
    this.pathToLevels = pathToLevels;
  }

  @Override
  public void execute() {
    if (InputManager.isKeyJustPressed(PRIMARY_UP)) {
      DungeonSaver.saveCurrentDungeon(this.saveToFile, this.pathToLevels);
      LevelEditorSystem.showFeedback("Exported level to clipboard!", Color.GREEN);
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
    controls.put(PRIMARY_UP, "Save level to clipboard");
    return controls;
  }
}
