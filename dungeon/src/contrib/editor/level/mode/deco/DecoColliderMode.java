package contrib.editor.level.mode.deco;

import contrib.editor.level.LevelEditorSystem;
import contrib.editor.level.mode.LevelEditorMode;
import core.input.InputLabel.InputCode;
import core.input.Keys;
import core.platform.Platform;
import core.utils.InputManager;
import core.utils.Point;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Level editor mode for steering collider tuning input for decorative entities.
 *
 * <p>The mode keeps the editor-facing input flow and overlay wiring thin while
 * {@link DecoColliderController} manages test-entity lifecycle, collider mutation,
 * and clipboard export behavior.
 */
public final class DecoColliderMode extends LevelEditorMode {

  private static final int CHANGE_MODE = Keys.UP;
  private static final int MODE_MODIFY_PLUS = Keys.RIGHT;
  private static final int MODE_MODIFY_MINUS = Keys.LEFT;
  private static final int MOVE_DECO = Keys.DOWN;

  private static final int RAPID_FIRE_THRESHOLD = 5;

  private final DecoColliderController controller;

  private int rapidFireCounter = 0;

  /**
   * Constructs a new instance of the DecoColliderMode class, an editing mode
   * for managing decoration colliders in the Level Editor system.
   *
   * @param system the LevelEditorSystem instance to which this mode belongs.
   */
  public DecoColliderMode(LevelEditorSystem system) {
    super(system, "Deco Collider Mode");
    this.controller = new DecoColliderController(system);
  }

  @Override
  protected void execute() {
    if (InputManager.isKeyJustPressed(CHANGE_MODE)) {
      controller.cycleEditMode();
      return;
    }

    if (InputManager.isKeyJustPressed(MODE_MODIFY_PLUS)) {
      rapidFireCounter = RAPID_FIRE_THRESHOLD;
      controller.applyCurrentEdit(1, cursorWorld());
    }

    if (InputManager.isKeyJustPressed(MODE_MODIFY_MINUS)) {
      rapidFireCounter = RAPID_FIRE_THRESHOLD;
      controller.applyCurrentEdit(-1, cursorWorld());
    }

    if (InputManager.isKeyPressed(MODE_MODIFY_MINUS)
      || InputManager.isKeyPressed(MODE_MODIFY_PLUS)) {
      if (rapidFireCounter <= 0) {
        rapidFireCounter = RAPID_FIRE_THRESHOLD;
        controller.applyCurrentEdit(InputManager.isKeyPressed(MODE_MODIFY_PLUS) ? 1 : -1, cursorWorld());
      }
      rapidFireCounter--;
    }

    if (InputManager.isKeyPressed(MOVE_DECO)) {
      controller.moveTestEntity(cursorWorld());
    }
  }

  @Override
  public void onEnter() {
    controller.onEnter(cursorWorld());
  }

  @Override
  public void onExit() {
    controller.onExit();
  }

  @Override
  protected List<String> getStatusLines() {
    return List.of(
      "Edit mode: " + controller.state().editMode().displayName(),
      "Current deco: " + controller.state().selectedDeco().name(),
      "Test entity: " + (controller.hasTestEntity() ? "active" : "<none>"),
      "Collider: " + controller.colliderString().orElse(controller.hasTestEntity()
        ? "<missing collide component>"
        : "<none>"),
      "Clipboard copy: automatic after collider edits");
  }

  @Override
  protected Map<InputCode, String> getControls() {
    Map<InputCode, String> controls = new LinkedHashMap<>();
    controls.put(key(CHANGE_MODE), "Next collider edit mode");
    controls.put(key(MODE_MODIFY_MINUS), "Modify current value -");
    controls.put(key(MODE_MODIFY_PLUS), "Modify current value +");
    controls.put(key(MOVE_DECO), "Move test deco to cursor");
    return controls;
  }

  private Point cursorWorld() {
    return Platform.cursor().world();
  }
}
