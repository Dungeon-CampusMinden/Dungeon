package contrib.editor.level.mode;

import contrib.editor.level.LevelEditorSystem;
import core.camera.CameraViewportState;
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
  public static final int QUARTERNARY = Keys.V;

  private final LevelEditorSystem system;
  private final String name;

  protected LevelEditorMode(LevelEditorSystem system, String name) {
    this.system = Objects.requireNonNull(system, "system must not be null");
    this.name = Objects.requireNonNull(name, "name must not be null");
  }

  public final LevelEditorSystem system() {
    return this.system;
  }

  public final String name() {
    return this.name;
  }

  /** Decorator method to execute the mode logic. */
  public final void doExecute() {
    execute();
  }

  /** Executes the logic for this mode. Called every frame while active. */
  protected abstract void execute();

  /** Render hook for this mode. Default: no-op. */
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
   * @return ordered map of control keys to action descriptions
   */
  protected Map<Integer, String> getControls() {
    return new LinkedHashMap<>();
  }

  protected final Optional<CameraViewportState.Viewport> activeCameraView() {
    return CameraViewportState.activeViewport();
  }

  /**
   * Builds the full overlay lines for this mode using the old compact editor structure.
   *
   * @return combined lines with title, controls and settings
   */
  public final List<String> getFullStatusLines() {
    List<String> lines = new ArrayList<>();
    lines.add("--- " + name() + " ---");

    Map<Integer, String> controls = getControls();
    if (!controls.isEmpty()) {
      lines.add("Controls:");
      controls.forEach((key, description) -> lines.add(" - " + keyLabel(key) + ": " + description));
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

  private String keyLabel(int keycode) {
    return switch (keycode) {
      case Keys.UP -> "UP";
      case Keys.DOWN -> "DOWN";
      case Keys.LEFT -> "LEFT";
      case Keys.RIGHT -> "RIGHT";
      case PRIMARY_UP -> "E";
      case PRIMARY_DOWN -> "Q";
      case SECONDARY_UP -> "C";
      case SECONDARY_DOWN -> "Z";
      case TERTIARY -> "X";
      case QUARTERNARY -> "V";
      case MouseButtons.LEFT -> "LMB";
      case MouseButtons.RIGHT -> "RMB";
      default -> Integer.toString(keycode);
    };
  }
}
