package contrib.editor.level.mode.deco;

import contrib.editor.level.LevelEditorSystem;
import contrib.editor.level.mode.EditorSnapMode;
import contrib.editor.level.mode.LevelEditorMode;
import contrib.entities.deco.Deco;
import core.input.InputLabelFormatter.InputCode;
import core.input.MouseButtons;
import core.platform.Platform;
import core.utils.InputManager;
import core.utils.Point;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A level editor mode for placing, managing, and manipulating decorations (deco) in a dungeon
 * level.
 *
 * <p>DecoMode provides comprehensive functionality for decorating a level with various deco types.
 * It supports the following operations:
 *
 * <ul>
 *   <li>Placing new decorations at cursor positions
 *   <li>Picking up and moving existing decorations
 *   <li>Deleting decorations from the level
 *   <li>Pipetting deco types from existing decorations
 *   <li>Previewing deco placements with visual feedback
 *   <li>Switching between different deco types and snap modes
 * </ul>
 *
 * <p>The mode features intelligent placement validation, showing blocked (red) vs. valid (white)
 * placements through color-coded visual indicators. It supports multiple snap modes for flexible
 * positioning and remembers original entity tint colors for proper restoration.
 */
public final class DecoMode extends LevelEditorMode {

  private final DecoPlacementController placementController;
  private final DecoTintController tintController;

  private int selectedDecoIndex = 0;
  private EditorSnapMode decoSnapMode = EditorSnapMode.ON_GRID;

  /**
   * Constructs a new instance of DecoMode, representing a specific editing mode within the level
   * editor.
   *
   * @param system the LevelEditorSystem instance that manages the level editor and provides the
   *     necessary context and functionality for this mode
   */
  public DecoMode(LevelEditorSystem system) {
    super(system, "Deco Mode");
    this.placementController = new DecoPlacementController(system);
    this.tintController = new DecoTintController();
  }

  @Override
  protected void execute() {
    if (InputManager.isKeyJustPressed(PRIMARY_UP)) {
      shiftSelectedDeco(1);
    } else if (InputManager.isKeyJustPressed(PRIMARY_DOWN)) {
      shiftSelectedDeco(-1);
    }

    if (InputManager.isKeyJustPressed(SECONDARY_UP)) {
      decoSnapMode = decoSnapMode.nextMode();
      if (!placementController.hasHeldDeco()) {
        refreshPreviewEntity();
      }
      system().showModeFeedback("Snap mode: " + decoSnapMode.displayName(), Color.WHITE);
    }

    Point cursorPos = Platform.cursor().world();
    Point snapPos = currentDecoSnapPosition();

    if (InputManager.isKeyJustPressed(QUATERNARY)) {
      placementController
          .pipetteDecoAt(cursorPos)
          .ifPresent(
              deco -> {
                selectDeco(deco);
                system().showModeFeedback("Picked deco type: " + deco.name(), Color.WHITE);
              });
    } else if (InputManager.isButtonJustPressed(MouseButtons.RIGHT)
        && !placementController.hasHeldDeco()) {
      tintController.clearHoveredDecoIndicator();
      placementController
          .pickupDecoAt(cursorPos, snapPos, tintController)
          .ifPresent(
              pickedName ->
                  system()
                      .showModeFeedback("Picked up deco: " + pickedName, new Color(120, 220, 120)));
    } else if (InputManager.isKeyJustPressed(TERTIARY) && !placementController.hasHeldDeco()) {
      tintController.clearHoveredDecoIndicator();
      placementController
          .deleteDecoAt(cursorPos)
          .ifPresent(
              removedName ->
                  system()
                      .showModeFeedback("Removed deco: " + removedName, new Color(255, 180, 180)));
    } else if (InputManager.isButtonJustPressed(MouseButtons.LEFT)) {
      if (placementController.hasHeldDeco()) {
        placeHeldDeco(snapPos);
      } else {
        placeSelectedDeco(snapPos);
      }
    }

    if (placementController.hasHeldDeco()) {
      tintController.clearHoveredDecoIndicator();
      placementController.updateHeldDecoPosition(snapPos);
      updatePlacementIndicator(snapPos);
      return;
    }

    placementController.ensurePreviewEntity(selectedDeco(), snapPos);
    placementController.updateDecoPreviewPosition(snapPos);
    updatePlacementIndicator(snapPos);
    tintController.updateHoveredDecoIndicator(placementController.findHoverableDecoNear(cursorPos));
  }

  @Override
  public void onEnter() {
    if (!placementController.hasHeldDeco()) {
      Point snapPos = currentDecoSnapPosition();
      placementController.ensurePreviewEntity(selectedDeco(), snapPos);
      placementController.updateDecoPreviewPosition(snapPos);
      updatePlacementIndicator(snapPos);
    }
  }

  @Override
  public void onExit() {
    tintController.clearHoveredDecoIndicator();
    placementController.releaseHeldDecoIfNecessary(tintController);
    placementController.removeDecoPreviewEntity(tintController);
    tintController.restoreAllRememberedEditorTints();
  }

  @Override
  protected List<String> getStatusLines() {
    Point cursor = system().snappedCursorTileForModes();
    Deco currentDeco = selectedDeco();

    return List.of(
        "Cursor tile: (" + (int) cursor.x() + ", " + (int) cursor.y() + ")",
        "Current deco: "
            + (Math.floorMod(selectedDecoIndex, Deco.values().length) + 1)
            + "/"
            + Deco.values().length
            + " ("
            + currentDeco.name()
            + ")",
        "Snap mode: " + decoSnapMode.displayName(),
        "Placement: "
            + (placementController.isCurrentDecoPlacementBlocked(
                    decoSnapMode, currentDecoSnapPosition())
                ? "blocked"
                : "valid"),
        "Hover: " + tintController.currentHoveredDecoName(),
        "Preview tint: white = valid, red = blocked",
        placementController.hasHeldDeco()
            ? "State: holding placed deco"
            : "State: preview ghost active");
  }

  @Override
  protected Map<InputCode, String> getControls() {
    Map<InputCode, String> controls = new LinkedHashMap<>();
    controls.put(key(PRIMARY_UP), "Next deco");
    controls.put(key(PRIMARY_DOWN), "Previous deco");
    controls.put(key(SECONDARY_UP), "Next snap mode");
    controls.put(mouseButton(MouseButtons.LEFT), "Place new deco or place held deco");
    controls.put(mouseButton(MouseButtons.RIGHT), "Pick up placed deco near cursor");
    controls.put(key(TERTIARY), "Delete placed deco near cursor");
    controls.put(key(QUATERNARY), "Pipette deco type near cursor");
    return controls;
  }

  private void shiftSelectedDeco(int delta) {
    selectedDecoIndex = Math.floorMod(selectedDecoIndex + delta, Deco.values().length);
    if (!placementController.hasHeldDeco()) {
      refreshPreviewEntity();
    }
    system().showModeFeedback("Selected deco: " + selectedDeco().name(), Color.WHITE);
  }

  private void selectDeco(Deco deco) {
    Deco[] values = Deco.values();
    for (int i = 0; i < values.length; i++) {
      if (values[i] == deco) {
        selectedDecoIndex = i;
        if (!placementController.hasHeldDeco()) {
          refreshPreviewEntity();
        }
        return;
      }
    }
  }

  private void refreshPreviewEntity() {
    placementController.refreshPreviewEntity(
        selectedDeco(), currentDecoSnapPosition(), tintController);
  }

  private void placeHeldDeco(Point snapPos) {
    if (placementController.isCurrentDecoPlacementBlocked(
        decoSnapMode, currentDecoSnapPosition())) {
      system().showModeFeedback("Cannot place held deco: target blocked", new Color(255, 210, 120));
      return;
    }

    String placedName = placementController.placeHeldDeco(snapPos, tintController);
    placementController.ensurePreviewEntity(selectedDeco(), snapPos);
    updatePlacementIndicator(snapPos);
    system().showModeFeedback("Placed held deco: " + placedName, new Color(120, 220, 120));
  }

  private void placeSelectedDeco(Point snapPos) {
    if (placementController.isCurrentDecoPlacementBlocked(
        decoSnapMode, currentDecoSnapPosition())) {
      system().showModeFeedback("Cannot place deco: target blocked", new Color(255, 210, 120));
      return;
    }

    placementController.placeSelectedDeco(selectedDeco(), snapPos);
    system().showModeFeedback("Placed deco: " + selectedDeco().name(), new Color(120, 220, 120));
  }

  private void updatePlacementIndicator(Point snapPos) {
    tintController.updatePlacementIndicator(
        placementController.indicatorEntity(),
        placementController.isCurrentDecoPlacementBlocked(decoSnapMode, snapPos));
  }

  private Point currentDecoSnapPosition() {
    return decoSnapMode.getPosition(Platform.cursor().world());
  }

  private Deco selectedDeco() {
    Deco[] values = Deco.values();
    return values[Math.floorMod(selectedDecoIndex, values.length)];
  }
}
