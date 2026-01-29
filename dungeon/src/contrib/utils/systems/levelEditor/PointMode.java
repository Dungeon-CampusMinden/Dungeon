package contrib.utils.systems.levelEditor;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.DialogType;
import contrib.systems.DebugDrawSystem;
import contrib.systems.LevelEditorSystem;
import core.level.utils.Coordinate;
import core.network.messages.c2s.DialogResponseMessage;
import core.utils.InputManager;
import core.utils.Point;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** The PointMode allows the user to place, pick up, and delete named points in the level editor. */
public class PointMode extends LevelEditorMode {

  private static SnapMode snapMode = SnapMode.OnGrid;
  private static String heldPointName = null;

  /** Constructs a new PointMode. */
  public PointMode() {
    super("Point Mode");
  }

  @Override
  public void onEnter() {}

  @Override
  public void onExit() {}

  @Override
  public void execute() {

    if (InputManager.isKeyJustPressed(SECONDARY_UP)) {
      snapMode = snapMode.nextMode();
    }

    Point cursorPos = getCursorPosition();
    Point snapPos = snapMode.getPosition(cursorPos);
    if (InputManager.isButtonJustPressed(Input.Buttons.LEFT)) {
      if (heldPointName != null) {
        // Place held deco
        getLevel().addNamedPoint(heldPointName, snapPos);
        heldPointName = null;
      } else {
        // Place new point instance
        UIComponent dialogUI =
            DialogFactory.show(
                DialogContext.builder()
                    .type(DialogType.DefaultTypes.FREE_INPUT)
                    .put(DialogContextKeys.TITLE, "Add Named Point")
                    .put(DialogContextKeys.QUESTION, "Name of new point")
                    .build());
        dialogUI.registerCallback(
            DialogContextKeys.INPUT_CALLBACK,
            payload -> {
              if (payload instanceof DialogResponseMessage.StringValue(String value)
                  && !value.isBlank()) {
                getLevel().addNamedPoint(value, snapPos);
              }
              UIUtils.closeDialog(dialogUI);
            });
      }
    } else if (InputManager.isButtonJustPressed(Input.Buttons.RIGHT)) {
      Optional<String> clickedPoint = getOnPosition(cursorPos);
      clickedPoint.ifPresent(point -> heldPointName = point);

      if (heldPointName == null) {
        LevelEditorSystem.showFeedback("No point to pickup on coordinate!", Color.YELLOW);
      } else if (clickedPoint.isEmpty()) {
        // Clone and increment held point to cursor
        String baseName = heldPointName.replaceAll("\\d+$", "");
        String newPointName = baseName + (getLevel().getHighestPointNumber(baseName) + 1);
        getLevel().addNamedPoint(newPointName, snapPos);
      }
    } else if (InputManager.isKeyPressed(TERTIARY)) {
      // Delete deco on cursor
      getOnPosition(cursorPos).ifPresent(getLevel()::removeNamedPoint);
    }
  }

  @Override
  public void render() {
    DebugDrawSystem.drawNamedPoints(heldPointName, true);
  }

  @Override
  public String getStatusText() {
    String status =
        "Snap Mode: "
            + snapMode.name()
            + "\nHeld Point: "
            + Objects.requireNonNullElse(heldPointName, "<none>")
            + "\nTotal Points: "
            + getLevel().namedPoints().size();

    return status;
  }

  @Override
  public Map<Integer, String> getControls() {
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(SECONDARY_UP, "Change Snap Mode");
    controls.put(TERTIARY, "Delete Point");
    controls.put(Input.Buttons.LEFT, "Place Point");
    controls.put(Input.Buttons.RIGHT, "Pickup Point / Clone Held Point");
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
