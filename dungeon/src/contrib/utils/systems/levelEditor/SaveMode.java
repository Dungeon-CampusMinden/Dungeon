package contrib.utils.systems.levelEditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import contrib.systems.LevelEditorSystem;
import core.level.loader.DungeonSaver;
import java.util.LinkedHashMap;
import java.util.Map;

/** The SaveMode allows the user to save the current dungeon layout to the clipboard. */
public class SaveMode extends LevelEditorMode {

  /** Constructs a new SaveMode. */
  public SaveMode() {
    super("Save Mode");
  }

  @Override
  public void execute() {
    if (Gdx.input.isKeyJustPressed(PRIMARY_UP)) {
      DungeonSaver.saveCurrentDungeon();
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
