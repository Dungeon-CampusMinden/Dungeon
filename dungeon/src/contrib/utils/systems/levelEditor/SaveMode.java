package contrib.utils.systems.levelEditor;

import com.badlogic.gdx.Gdx;
import core.level.loader.DungeonSaver;

public class SaveMode extends LevelEditorMode {

  public SaveMode() {
    super("Save Mode");
  }

  @Override
  public void execute() {
    if (Gdx.input.isKeyJustPressed(PRIMARY_UP)) {
      DungeonSaver.saveCurrentDungeon();
    }
  }

  @Override
  public void onEnter() {}

  @Override
  public void onExit() {}

  @Override
  public String getStatusText() {
    StringBuilder status = new StringBuilder("--- Save Level Mode ---");
    status.append("\nControls:");
    status.append("\n- E: Save level to clipboard");
    return status.toString();
  }
}
