package contrib.editor.level.mode.point;

import contrib.editor.level.LevelEditorSystem;
import contrib.editor.level.mode.EditorSnapMode;
import contrib.editor.level.mode.LevelEditorMode;
import core.input.InputLabelFormatter.InputCode;
import core.input.MouseButtons;
import core.platform.Platform;
import core.utils.InputManager;
import core.utils.Point;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A level editor mode for creating and managing named points within a dungeon level.
 *
 * <p>PointMode enables editors to place, manipulate, and visualize named reference points in the
 * level. These points serve as markers for important locations, spawn points, objectives, or other
 * level-specific references.
 *
 * <p>Supported operations:
 *
 * <ul>
 *   <li>Placing new named points at cursor positions
 *   <li>Picking up and moving existing points
 *   <li>Cloning points with automatic numbering
 *   <li>Deleting points from the level
 *   <li>Switching between different snap modes for flexible positioning
 *   <li>Visual rendering of point markers with labels
 * </ul>
 *
 * <p>The mode provides visual feedback with color-coded markers:
 *
 * <ul>
 *   <li>Orange markers for existing points
 *   <li>Green markers for held/moved points
 *   <li>White labels for point names
 * </ul>
 *
 * <p>When placing a new point, the editor is prompted to enter a name via a dialog. Held points can
 * be cloned by right-clicking, with automatic numbering applied to the clone.
 */
public final class PointMode extends LevelEditorMode {

  private final PointPlacementController placementController;
  private final PointDialogController dialogController;
  private final PointRenderer renderer;

  private EditorSnapMode snapMode = EditorSnapMode.ON_GRID;

  /**
   * Constructs a new PointMode for managing and rendering point markers in the level editor.
   *
   * @param system The LevelEditorSystem instance to which this mode belongs, providing necessary
   *     context and functionality for the mode's operations.
   */
  public PointMode(LevelEditorSystem system) {
    super(system, "Point Mode");
    this.placementController = new PointPlacementController(system);
    this.dialogController = new PointDialogController(placementController);
    this.renderer = new PointRenderer(system);
  }

  @Override
  protected void execute() {
    if (dialogController.isOpen()) {
      return;
    }

    if (InputManager.isKeyJustPressed(SECONDARY_UP)) {
      snapMode = snapMode.nextMode();
      system().showModeFeedback("Snap mode: " + snapMode.displayName(), Color.WHITE);
    }

    Point cursorPos = Platform.cursor().world();
    Point snapPos = currentSnapPosition();

    if (InputManager.isButtonJustPressed(MouseButtons.LEFT)) {
      if (placementController.hasHeldPoint()) {
        placementController.placeHeldPoint(snapPos);
      } else {
        dialogController.openAddNamedPointDialog(snapPos);
      }
      return;
    }

    if (InputManager.isButtonJustPressed(MouseButtons.RIGHT)) {
      placementController.pickupOrClonePoint(cursorPos, snapPos);
      return;
    }

    if (InputManager.isKeyPressed(TERTIARY)) {
      placementController.deletePointAt(cursorPos);
    }
  }

  @Override
  public void render(Graphics2D g, float deltaSeconds) {
    renderer.render(g, placementController.heldPointName(), currentSnapPosition());
  }

  @Override
  public void onExit() {
    placementController.clearHeldPoint();
    dialogController.close();
  }

  @Override
  protected List<String> getStatusLines() {
    Point cursor = currentSnapPosition();

    return List.of(
        "Cursor point: (" + cursor.x() + ", " + cursor.y() + ")",
        "Snap mode: " + snapMode.displayName(),
        "Held point: "
            + (placementController.heldPointName() == null
                ? "<none>"
                : placementController.heldPointName()),
        "Total points: " + placementController.totalPoints());
  }

  @Override
  protected Map<InputCode, String> getControls() {
    Map<InputCode, String> controls = new LinkedHashMap<>();
    controls.put(key(SECONDARY_UP), "Change snap mode");
    controls.put(mouseButton(MouseButtons.LEFT), "Place point / open name dialog");
    controls.put(mouseButton(MouseButtons.RIGHT), "Pick point / clone held point");
    controls.put(key(TERTIARY), "Delete point");
    return controls;
  }

  private Point currentSnapPosition() {
    return snapMode.getPosition(Platform.cursor().world());
  }
}
