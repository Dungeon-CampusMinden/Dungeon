package contrib.utils.systems.levelEditor;

import com.badlogic.gdx.Input;
import java.util.LinkedHashMap;
import java.util.Map;

public class PointMode extends LevelEditorMode {

  public PointMode() {
    super("Point Mode");
  }

  @Override
  public void execute() {}

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
    controls.put(PRIMARY_UP, "Next Deco");
    controls.put(PRIMARY_DOWN, "Prev Deco");
    controls.put(SECONDARY_UP, "Change Grid Snap");
    controls.put(SECONDARY_DOWN, "Delete on Cursor");
    controls.put(Input.Buttons.LEFT, "Place Deco");
    controls.put(Input.Buttons.RIGHT, "Pickup Deco");
    controls.put(TERTIARY, "Pick Deco on Cursor");
    return controls;
  }
}
