package contrib.editor.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.editor.level.mode.LevelEditorMode;
import core.ui.overlay.OverlayManager;
import java.awt.Color;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

/** Tests for {@link LevelEditorOverlayPresenter}. */
public class LevelEditorOverlayPresenterTest {

  /** Ensures presenter output contains the shared status block and current feedback. */
  @Test
  public void updateWritesOverlayContent() {
    AtomicLong nowMs = new AtomicLong(1_000L);
    LevelEditorOverlay overlay = new LevelEditorOverlay();
    LevelEditorOverlayPresenter presenter = new LevelEditorOverlayPresenter(overlay, nowMs::get);

    presenter.showFeedback("Placed tile", Color.GREEN);
    presenter.update(new TestMode(), "1 | [2] | 3", true);

    assertEquals("", overlay.title());
    assertIterableEquals(
      List.of(
        "Level Editor v2 | Modes: 1 | [2] | 3",
        "( SPACE to toggle layer debug shader [true] )",
        "",
        "--- Test Mode ---",
        "",
        "Settings:",
        " - brush: floor"),
      overlay.lines());
    assertEquals("Placed tile", overlay.feedback());
    assertEquals(Color.GREEN, overlay.feedbackColor());
  }

  /** Ensures feedback visibility expires after the configured timeout window. */
  @Test
  public void feedbackExpiresAfterTimeout() {
    AtomicLong nowMs = new AtomicLong(1_000L);
    LevelEditorOverlayPresenter presenter =
      new LevelEditorOverlayPresenter(new LevelEditorOverlay(), nowMs::get);

    presenter.showFeedback("Expired soon", Color.YELLOW);
    assertEquals("Expired soon", presenter.currentFeedbackMessage());
    assertEquals(Color.YELLOW, presenter.currentFeedbackColor());

    nowMs.set(4_001L);

    assertEquals("", presenter.currentFeedbackMessage());
    assertEquals(Color.WHITE, presenter.currentFeedbackColor());
  }

  /** Ensures attach and detach keep the overlay manager in sync with presenter visibility. */
  @Test
  public void attachAndDetachManageOverlayLifecycle() {
    LevelEditorOverlay overlay = new LevelEditorOverlay();
    LevelEditorOverlayPresenter presenter =
      new LevelEditorOverlayPresenter(overlay, () -> 0L);

    presenter.detach();

    try {
      assertFalse(OverlayManager.contains(overlay));

      presenter.attach();
      assertTrue(overlay.visible());
      assertTrue(OverlayManager.contains(overlay));

      presenter.detach();
      assertFalse(overlay.visible());
      assertFalse(OverlayManager.contains(overlay));
    } finally {
      presenter.detach();
    }
  }

  private static final class TestMode extends LevelEditorMode {
    private TestMode() {
      super(new LevelEditorSystem(), "Test Mode");
    }

    @Override
    protected void execute() {
      // test mode does not execute editor actions
    }

    @Override
    protected List<String> getStatusLines() {
      return List.of(" - brush: floor");
    }
  }
}
