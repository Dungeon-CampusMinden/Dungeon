package contrib.editor.level.mode;

import contrib.editor.level.LevelEditorSystem;
import core.camera.CameraViewportState;
import core.input.InputLabelFormatter;
import core.input.InputLabelFormatter.InputCode;
import core.input.Keys;
import core.input.MouseButtons;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents an abstract mode within the level editor system.
 *
 * <p>Each mode implements its own behavior and actions while interacting with the editor.
 *
 * <p>The mode defines specific input controls, rendering logic, and status information to provide
 * functionality in a modular and extensible way. Subclasses must implement the abstract methods
 * to specify their unique logic and state.
 */
public abstract class LevelEditorMode {

  /** Primary action button. Direction UP */
  public static final int PRIMARY_UP = Keys.E;

  /** Primary action button. Direction DOWN */
  public static final int PRIMARY_DOWN = Keys.Q;

  /** Secondary action button. Direction UP */
  public static final int SECONDARY_UP = Keys.C;

  /** Secondary action button. Direction DOWN. */
  public static final int SECONDARY_DOWN = Keys.Z;

  /** Tertiary action button. */
  public static final int TERTIARY = Keys.X;

  /** Quaternary action button. */
  public static final int QUATERNARY = Keys.V;

  private static final List<Integer> EDITOR_INPUTS =
    List.of(
      PRIMARY_UP,
      PRIMARY_DOWN,
      SECONDARY_UP,
      SECONDARY_DOWN,
      TERTIARY,
      QUATERNARY,
      MouseButtons.LEFT,
      MouseButtons.RIGHT);

  private final LevelEditorSystem system;
  private final String name;

  protected LevelEditorMode(LevelEditorSystem system, String name) {
    this.system = Objects.requireNonNull(system, "system must not be null");
    this.name = Objects.requireNonNull(name, "name must not be null");
  }

  /**
   * Provides access to the LevelEditorSystem instance associated with this LevelEditorMode.
   *
   * @return the LevelEditorSystem instance for this mode
   */
  public final LevelEditorSystem system() {
    return this.system;
  }

  /**
   * Retrieves the name of this LevelEditorMode instance.
   *
   * @return the name of this mode as a String
   */
  public final String name() {
    return this.name;
  }

  /**
   * Returns all input codes the level editor uses and must reserve while active.
   *
   * @return immutable list of editor input codes
   */
  public static List<Integer> editorInputs() {
    return EDITOR_INPUTS;
  }

  /** Decorator method to execute the mode logic. */
  public final void doExecute() {
    execute();
  }

  /** Executes the logic for this mode. Called every frame while active. */
  protected abstract void execute();

  /**
   * Renders the graphical content associated with this mode.
   *
   * <p>This method is responsible for drawing the necessary visuals on the provided graphics context during each frame.
   *
   * @param g the {@code Graphics2D} context used for rendering
   * @param deltaSeconds the time in seconds since the last frame update
   */
  public void render(Graphics2D g, float deltaSeconds) {
    // default: no-op
  }

  /** Called when entering this mode. Default: no-op. */
  public void onEnter() {
    // default: no-op
  }

  /** Called when exiting this mode. Default: no-op. */
  public void onExit() {
    // default: no-op
  }

  /**
   * Returns the mode-specific settings/status lines.
   *
   * @return mode-specific lines for the overlay
   */
  protected abstract List<String> getStatusLines();

  /**
   * Returns the controls of this mode.
   *
   * @return ordered map of typed control inputs to action descriptions
   */
  protected Map<InputCode, String> getControls() {
    return new LinkedHashMap<>();
  }

  protected final Optional<CameraViewportState.Viewport> activeCameraView() {
    return CameraViewportState.activeViewport();
  }

  /**
   * Builds the full overlay lines for this mode using the old compact editor structure.
   *
   * @return combined lines with title, controls, and settings
   */
  public final List<String> getFullStatusLines() {
    List<String> lines = new ArrayList<>();
    lines.add("--- " + name() + " ---");

    Map<InputCode, String> controls = getControls();
    if (!controls.isEmpty()) {
      lines.add("Controls:");
      controls.forEach((input, description) -> lines.add(" - " + input.label() + ": " + description));
    }

    lines.add("");
    lines.add("Settings:");

    List<String> statusLines = getStatusLines();
    if (statusLines == null || statusLines.isEmpty()) {
      lines.add(" -");
    } else {
      lines.addAll(statusLines);
    }

    return List.copyOf(lines);
  }

  protected static InputCode key(int keycode) {
    return InputLabelFormatter.keyboard(keycode);
  }

  protected static InputCode mouseButton(int button) {
    return InputLabelFormatter.mouseButton(button);
  }
}
