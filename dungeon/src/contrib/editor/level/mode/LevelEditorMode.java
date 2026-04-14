package contrib.editor.level.mode;

import core.input.Keys;
import core.input.MouseButtons;
import core.camera.CameraViewportState;
import contrib.editor.level.LevelEditorSystem;
import java.awt.Graphics2D;
import java.util.*;


/**
 * Abstract base class for level editor modes.
 *
 * <p>LevelEditorMode defines the framework for implementing different editing modes in the level
 * editor. Each mode represents a distinct set of editing tools and operations, such as placing
 * decorations, modifying level bounds, or other level editing tasks.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Execute mode-specific logic each frame
 *   <li>Provide visual feedback through status lines and controls
 *   <li>Handle mode lifecycle (entering and exiting)
 *   <li>Render mode-specific graphics
 * </ul>
 *
 * <p>Subclasses must implement:
 * <ul>
 *   <li>{@code execute()} - Core logic executed every frame
 *   <li>{@code getStatusLines()} - Mode-specific status information for the overlay
 * </ul>
 *
 * <p>Standard input bindings are provided for common editor actions:
 * <ul>
 *   <li>E/Q - Primary up/down actions
 *   <li>C/Z - Secondary up/down actions
 *   <li>X - Tertiary action
 *   <li>V - Quaternary action
 *   <li>Arrow keys, mouse buttons - Additional inputs
 * </ul>
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
   * Builds the full overlay lines for this mode.
   *
   * @return combined lines with title, controls and settings
   */
  public final List<String> getFullStatusLines() {
    List<String> lines = new ArrayList<>();
    lines.add("--- " + name() + " ---");

    Map<Integer, String> controls = getControls();
    if (!controls.isEmpty()) {
      lines.add("Controls:");
      controls.forEach((key, description) -> lines.add(keyLabel(key) + ": " + description));
    }

    List<String> statusLines = getStatusLines();
    if (!statusLines.isEmpty()) {
      lines.add("");
      lines.add("Settings:");
      lines.addAll(statusLines);
    }

    return lines;
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
