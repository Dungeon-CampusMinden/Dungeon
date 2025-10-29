package contrib.utils.systems.levelEditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import contrib.components.DecoComponent;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.hud.dialogs.FreeInputDialog;
import contrib.systems.DebugDrawSystem;
import contrib.utils.EntityUtils;
import core.Entity;
import core.Game;
import core.level.utils.Coordinate;
import core.utils.Point;
import core.utils.Vector2;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class PointMode extends LevelEditorMode {

  private static SnapMode snapMode = SnapMode.OnGrid;
  private static String heldPointName = null;

  public PointMode() {
    super("Point Mode");
  }

  @Override
  public void onEnter() {}

  @Override
  public void onExit() {}

  @Override
  public void execute() {
    DebugDrawSystem.drawNamedPoints(heldPointName);

    if (Gdx.input.isKeyJustPressed(SECONDARY_UP)) {
      snapMode = snapMode.nextMode();
    }

    Point cursorPos = getCursorPosition();
    Point snapPos = snapMode.getPosition(cursorPos);
    if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
      if (heldPointName != null) {
        // Place held deco
        getLevel().addNamedPoint(heldPointName, snapPos);
        heldPointName = null;
      } else {
        // Place new point instance
        FreeInputDialog.showTextInputDialog("Add Named Point", "Name of new point", (string) -> {
          getLevel().addNamedPoint(string, snapPos);
        });
      }
    } else if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT) && heldPointName == null) {
      // Pickup deco on cursor
      Optional<String> clickedPoint = getOnPosition(cursorPos);
      clickedPoint.ifPresent(point -> {
        heldPointName = point;
      });
    } else if (Gdx.input.isKeyPressed(TERTIARY)) {
      // Delete deco on cursor
      getOnPosition(cursorPos).ifPresent(getLevel()::removeNamedPoint);
    }
  }

  @Override
  public String getStatusText() {
    StringBuilder status = new StringBuilder();
    status.append("Snap Mode: ").append(snapMode.name());
    status.append("\nHeld Point: ");
    status.append(Objects.requireNonNullElse(heldPointName, "<none>"));
    status.append("\nTotal Points: ").append(getLevel().namedPoints().size());

    return status.toString();
  }

  @Override
  public Map<Integer, String> getControls() {
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(SECONDARY_UP, "Change Snap Mode");
    controls.put(Input.Buttons.LEFT, "Place Point");
    controls.put(Input.Buttons.RIGHT, "Pickup Point");
    controls.put(TERTIARY, "Delete Point");
    return controls;
  }

  private Optional<String> getOnPosition(Point position) {
    Coordinate toCheck = position.toCoordinate();
    return getLevel().namedPoints().entrySet().stream()
        .filter(entry -> entry.getValue().toCoordinate().equals(toCheck))
        .map(Map.Entry::getKey)
        .findFirst();
  }
}
