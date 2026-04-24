package contrib.editor.level;

import contrib.editor.level.mode.LevelEditorMode;
import core.ui.overlay.OverlayManager;
import core.utils.Time;
import core.utils.logging.DungeonLogger;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.LongSupplier;

/** Presenter that keeps the level editor overlay and transient feedback in sync. */
final class LevelEditorOverlayPresenter {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(LevelEditorOverlayPresenter.class);

  private static final long FEEDBACK_DURATION_MS = 3000L;

  private final LevelEditorOverlay overlay;
  private final LongSupplier currentTimeMs;

  private String feedbackMessage = "";
  private Color feedbackColor = Color.WHITE;
  private long feedbackUntilMs = 0L;

  LevelEditorOverlayPresenter() {
    this(new LevelEditorOverlay(), Time::nowMs);
  }

  LevelEditorOverlayPresenter(LevelEditorOverlay overlay, LongSupplier currentTimeMs) {
    this.overlay = Objects.requireNonNull(overlay, "overlay must not be null");
    this.currentTimeMs = Objects.requireNonNull(currentTimeMs, "currentTimeMs must not be null");
  }

  void attach() {
    overlay.visible(true);

    if (!OverlayManager.contains(overlay)) {
      OverlayManager.add(overlay);
    }

    OverlayManager.toFront(overlay);
  }

  void detach() {
    overlay.visible(false);
    OverlayManager.remove(overlay);
  }

  void update(LevelEditorMode currentMode, String modeSelectionText, boolean layerDebugActive) {
    Objects.requireNonNull(currentMode, "currentMode must not be null");

    overlay.content(
      "",
      buildStatusLines(currentMode, modeSelectionText, layerDebugActive),
      currentFeedbackMessage(),
      currentFeedbackColor());
  }

  void showFeedback(String message, Color color) {
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

  LevelEditorOverlay overlay() {
    return overlay;
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
