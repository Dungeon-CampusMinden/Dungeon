package contrib.editor.level.ui;

import contrib.editor.level.mode.LevelEditorMode;
import core.ui.overlay.OverlayManager;
import core.utils.Time;
import core.utils.logging.DungeonLogger;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * Presenter for managing the Level Editor overlay UI.
 *
 * <p>This class handles the presentation logic for the overlay, including displaying
 * status information, mode selection, layer debug status, and temporary feedback messages.
 *
 * <p>It manages the visibility and content of the overlay, as well as feedback display with
 * automatic timeout.
 */
public final class LevelEditorOverlayPresenter {
  private static final DungeonLogger LOGGER =
      DungeonLogger.getLogger(LevelEditorOverlayPresenter.class);

  private static final long FEEDBACK_DURATION_MS = 3000L;

  private final LevelEditorOverlay overlay;
  private final LongSupplier currentTimeMs;

  private String feedbackMessage = "";
  private Color feedbackColor = Color.WHITE;
  private long feedbackUntilMs = 0L;

  /**
   * Creates a new LevelEditorOverlayPresenter with default overlay and current system time.
   */
  public LevelEditorOverlayPresenter() {
    this(new LevelEditorOverlay(), Time::nowMs);
  }

  /**
   * Creates a new LevelEditorOverlayPresenter with a custom overlay and time supplier.
   *
   * @param overlay the overlay to manage
   * @param currentTimeMs supplier for the current time in milliseconds
   * @throws NullPointerException if overlay or currentTimeMs is null
   */
  public LevelEditorOverlayPresenter(LevelEditorOverlay overlay, LongSupplier currentTimeMs) {
    this.overlay = Objects.requireNonNull(overlay, "overlay must not be null");
    this.currentTimeMs = Objects.requireNonNull(currentTimeMs, "currentTimeMs must not be null");
  }

  /**
   * Attaches the overlay to the overlay manager, making it visible.
   *
   * <p>This method ensures the overlay is visible, added to the overlay manager if not already present,
   * and brought to the front.
   */
  public void attach() {
    overlay.visible(true);

    if (!OverlayManager.contains(overlay)) {
      OverlayManager.add(overlay);
    }

    OverlayManager.toFront(overlay);
  }

  /**
   * Detaches the overlay from the overlay manager, making it invisible.
   *
   * <p>This method hides the overlay and removes it from the overlay manager.
   */
  public void detach() {
    overlay.visible(false);
    OverlayManager.remove(overlay);
  }

  /**
   * Updates the overlay content with the current editor state.
   *
   * @param currentMode the current level editor mode
   * @param modeSelectionText display text for available modes
   * @param layerDebugActive whether the layer debug shader is active
   * @throws NullPointerException if currentMode is null
   */
  public void update(LevelEditorMode currentMode, String modeSelectionText, boolean layerDebugActive) {
    Objects.requireNonNull(currentMode, "currentMode must not be null");

    overlay.content(
        "",
        buildStatusLines(currentMode, modeSelectionText, layerDebugActive),
        currentFeedbackMessage(),
         currentFeedbackColor());
  }

  /**
   * Displays a temporary feedback message in the overlay.
   *
   * <p>The feedback message is displayed for a fixed duration (3 seconds) and automatically
   * disappears after this time.
   *
   * <p>The message is also logged according to the specified color:
   * red messages are logged as error, yellow as warning, and others as info.
   *
   * @param message the feedback message to display (null is treated as empty string)
   * @param color the color in which to display the message (null defaults to white)
   */
  public void showFeedback(String message, Color color) {
    feedbackMessage = message == null ? "" : message;
    feedbackColor = color == null ? Color.WHITE : color;
    feedbackUntilMs = currentTimeMs.getAsLong() + FEEDBACK_DURATION_MS;

    if (Color.RED.equals(feedbackColor)) {
      LOGGER.error(feedbackMessage);
    } else if (Color.YELLOW.equals(feedbackColor)) {
      LOGGER.warn(feedbackMessage);
    } else {
      LOGGER.info(feedbackMessage);
    }
  }

  String currentFeedbackMessage() {
    return currentTimeMs.getAsLong() <= feedbackUntilMs ? feedbackMessage : "";
  }

  Color currentFeedbackColor() {
    return currentTimeMs.getAsLong() <= feedbackUntilMs ? feedbackColor : Color.WHITE;
  }

  private List<String> buildStatusLines(
      LevelEditorMode currentMode, String modeSelectionText, boolean layerDebugActive) {
    List<String> lines = new ArrayList<>();
    lines.add("Level Editor v2 | Modes: " + modeSelectionText);
    lines.add("( SPACE to toggle layer debug shader [" + layerDebugActive + "] )");
    lines.add("");
    lines.addAll(currentMode.getFullStatusLines());
    return lines;
  }
}
