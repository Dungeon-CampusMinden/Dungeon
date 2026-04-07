package core.platform.litiengine.levelEditor;

import contrib.entities.deco.Deco;
import core.input.MouseButtons;
import core.platform.Platform;
import core.utils.InputManager;
import core.utils.Point;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * LITIENGINE level editor mode for placing, moving, deleting and pipetting deco entities.
 *
 * <p>This first extraction step moves mode orchestration, lifecycle and overlay text out of the
 * system while the low-level preview/hover/tint helpers remain backend-local in the system.
 */
public final class DecoMode extends LevelEditorMode {

  public DecoMode(core.platform.litiengine.systems.LitiengineLevelEditorSystem system) {
    super(system, "Deco Mode");
  }

  @Override
  protected void execute() {
    if (InputManager.isKeyJustPressed(PRIMARY_UP)) {
      system().changeSelectedDecoByForModes(1);
      if (!system().isHoldingDecoForModes()) {
        system().previewDecoEntityChangedForModes();
      }
      system().showModeFeedback("Selected deco: " + system().selectedDecoForModes().name(), Color.WHITE);
    } else if (InputManager.isKeyJustPressed(PRIMARY_DOWN)) {
      system().changeSelectedDecoByForModes(-1);
      if (!system().isHoldingDecoForModes()) {
        system().previewDecoEntityChangedForModes();
      }
      system().showModeFeedback("Selected deco: " + system().selectedDecoForModes().name(), Color.WHITE);
    }

    if (InputManager.isKeyJustPressed(SECONDARY_UP)) {
      system().cycleDecoSnapModeForModes();
      if (!system().isHoldingDecoForModes()) {
        system().previewDecoEntityChangedForModes();
      }
      system().showModeFeedback(
        "Snap mode: " + system().decoSnapModeDisplayNameForModes(), Color.WHITE);
    }

    Point cursorPos = Platform.cursor().world();
    Point snapPos = system().currentDecoSnapPositionForModes();

    if (InputManager.isKeyJustPressed(QUARTERNARY)) {
      system().pipetteDecoAtCursorForModes();
    } else if (InputManager.isButtonJustPressed(MouseButtons.RIGHT)
      && !system().isHoldingDecoForModes()) {
      system().pickupDecoAtCursorForModes();
    } else if (InputManager.isKeyJustPressed(TERTIARY)
      && !system().isHoldingDecoForModes()) {
      system().deleteDecoAtCursorForModes();
    } else if (InputManager.isButtonJustPressed(MouseButtons.LEFT)) {
      if (system().isHoldingDecoForModes()) {
        system().placeHeldDecoForModes(snapPos);
      } else {
        system().placeSelectedDecoForModes(snapPos);
      }
    }

    if (system().isHoldingDecoForModes()) {
      system().updateHeldDecoPlacementForModes(snapPos);
      return;
    }

    system().refreshDecoPreviewForModes(snapPos);
    system().updateHoveredDecoForModes(cursorPos);
  }

  @Override
  public void onEnter() {
    if (!system().isHoldingDecoForModes()) {
      system().refreshDecoPreviewForModes(system().currentDecoSnapPositionForModes());
    }
  }

  @Override
  public void onExit() {
    system().clearDecoEditingArtifactsForModes();
  }

  @Override
  protected List<String> getStatusLines() {
    Point cursor = system().snappedCursorTileForModes();
    Deco currentDeco = system().selectedDecoForModes();

    return List.of(
      "Cursor tile: (" + (int) cursor.x() + ", " + (int) cursor.y() + ")",
      "Current deco: "
        + system().selectedDecoDisplayIndexForModes()
        + "/"
        + system().availableDecoCountForModes()
        + " ("
        + currentDeco.name()
        + ")",
      "Snap mode: " + system().decoSnapModeDisplayNameForModes(),
      "Placement: " + (system().isCurrentDecoPlacementBlockedForModes() ? "blocked" : "valid"),
      "Hover: " + system().currentHoveredDecoNameForModes(),
      "Preview tint: white = valid, red = blocked",
      system().isHoldingDecoForModes()
        ? "State: holding placed deco"
        : "State: preview ghost active");
  }

  @Override
  protected Map<Integer, String> getControls() {
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(PRIMARY_UP, "Next deco");
    controls.put(PRIMARY_DOWN, "Previous deco");
    controls.put(SECONDARY_UP, "Next snap mode");
    controls.put(MouseButtons.LEFT, "Place new deco or place held deco");
    controls.put(MouseButtons.RIGHT, "Pick up placed deco near cursor");
    controls.put(TERTIARY, "Delete placed deco near cursor");
    controls.put(QUARTERNARY, "Pipette deco type near cursor");
    return controls;
  }
}
