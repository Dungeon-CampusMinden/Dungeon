package feature.leveleditor;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import engine.level.utils.Coordinate;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.systems.input.InputManager;
import engine.utils.Point;
import feature.hud.dialogs.DialogFactory;
import feature.systems.DebugDrawSystem;
import feature.systems.LevelEditorSystem;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** The PointMode allows the user to place, pick up, and delete named points in the level editor. */
public class PointMode extends LevelEditorMode {

  private static SnapMode snapMode = SnapMode.OnGrid;
  private static SnapMode snapModeBeforePickup;
  private static String heldPointName = null;
  private String hoveredPointName;
  private Point cursorPreviewPosition;

  /**
   * Constructs a new PointMode.
   *
   * @param levelChangedCallback invoked after named points are changed.
   */
  public PointMode(Runnable levelChangedCallback) {
    super("Point Mode", levelChangedCallback);
  }

  @Override
  public void onEnter() {
    hoveredPointName = null;
    cursorPreviewPosition = null;
  }

  @Override
  public void onExit() {
    hoveredPointName = null;
    cursorPreviewPosition = null;
  }

  @Override
  public void onCursorLeaveWorld() {
    hoveredPointName = null;
    cursorPreviewPosition = null;
  }

  @Override
  public void execute() {
    if (InputManager.isKeyJustPressed(SECONDARY_UP)) {
      snapMode = snapMode.nextMode();
    }

    Point cursorPos = getCursorPosition();
    Point snapPos = snapMode.getPosition(cursorPos);
    if (InputManager.isButtonJustPressed(Input.Buttons.LEFT)) {
      if (heldPointName != null) {
        // Place held point
        getLevel().addNamedPoint(heldPointName, snapPos);
        heldPointName = null;
        restoreSnapModeAfterPickup();
        levelChanged();
      } else {
        // Place new point instance
        Point placementPosition = snapPos;
        DialogFactory.showInputDialog(
            "",
            "Add Named Point",
            "",
            "Name of point",
            "Add",
            "Cancel",
            false,
            payload -> {
              if (payload instanceof DialogResponseMessage.StringValue(String value)
                  && !value.isBlank()) {
                getLevel().addNamedPoint(value, placementPosition);
                levelChanged();
              }
            },
            () -> {});
      }
    } else if (InputManager.isButtonJustPressed(Input.Buttons.RIGHT)) {
      Optional<String> clickedPoint = getOnPosition(cursorPos);
      if (clickedPoint.isPresent()) {
        String point = clickedPoint.get();
        if (heldPointName == null) {
          snapModeBeforePickup = snapMode;
        }
        heldPointName = point;
        snapMode = snapModeFor(getLevel().namedPoints().get(point));
        snapPos = snapMode.getPosition(cursorPos);
      }

      if (heldPointName == null) {
        LevelEditorSystem.showFeedback("No point to pickup on coordinate!", Color.YELLOW);
      } else if (clickedPoint.isEmpty()) {
        // Clone and increment held point to cursor
        String baseName = heldPointName.replaceAll("\\d+$", "");
        String newPointName = baseName + (getLevel().getHighestPointNumber(baseName) + 1);
        getLevel().addNamedPoint(newPointName, snapPos);
        levelChanged();
      }
    } else if (InputManager.isKeyPressed(TERTIARY)) {
      // Delete point on cursor
      getOnPosition(cursorPos)
          .ifPresent(
              point -> {
                getLevel().removeNamedPoint(point);
                levelChanged();
              });
    }
    cursorPreviewPosition = snapPos;
    hoveredPointName = getOnPosition(cursorPos).orElse(null);
  }

  @Override
  public void render() {
    String highlightedPoint = hoveredPointName != null ? hoveredPointName : heldPointName;
    DebugDrawSystem.drawNamedPoints(highlightedPoint, true);
    if (cursorPreviewPosition != null) {
      DebugDrawSystem.drawNamedPointPreview(cursorPreviewPosition, heldPointName != null);
    }
  }

  @Override
  public String additionalInformation() {
    StringBuilder status =
        new StringBuilder("Snap Mode: ")
            .append(snapMode.name())
            .append("\nHeld Point: ")
            .append(Objects.requireNonNullElse(heldPointName, "<none>"))
            .append("\nTotal Points: ")
            .append(getLevel().namedPoints().size());

    if (hoveredPointName != null) {
      Point position = getLevel().namedPoints().get(hoveredPointName);
      if (position != null) {
        status
            .append("\nPoint under cursor: ")
            .append(hoveredPointName)
            .append("\nPosition: (")
            .append(position.x())
            .append(", ")
            .append(position.y())
            .append(")");
      }
    }

    return status.toString();
  }

  @Override
  public Map<Integer, String> getControls() {
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(Input.Buttons.LEFT, "Place Point");
    controls.put(Input.Buttons.RIGHT, "Pickup Point / Clone Held Point");
    controls.put(SECONDARY_UP, "Change Snap Mode");
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

  private static SnapMode snapModeFor(Point point) {
    if (alignedToGrid(point, 1)) return SnapMode.OnGrid;
    if (alignedToGrid(point, 4)) return SnapMode.QuarterGrid;
    if (alignedToGrid(point, 16)) return SnapMode.PixelGrid;
    return SnapMode.OffGrid;
  }

  private static boolean alignedToGrid(Point point, int divisions) {
    return alignedToGrid(point.x(), divisions) && alignedToGrid(point.y(), divisions);
  }

  private static boolean alignedToGrid(float value, int divisions) {
    float scaled = value * divisions;
    return Math.abs(scaled - Math.round(scaled)) < 0.0001f;
  }

  private static void restoreSnapModeAfterPickup() {
    if (snapModeBeforePickup != null) {
      snapMode = snapModeBeforePickup;
      snapModeBeforePickup = null;
    }
  }
}
