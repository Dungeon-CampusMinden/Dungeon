package contrib.hud.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.game.loop.GameLoop;
import core.game.loop.GameLoopHost;
import core.input.MouseButtons;
import core.platform.Platform;
import core.ui.StageHandle;
import core.utils.InputManager;
import java.awt.Rectangle;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link DialogButtonInputHandler}. */
public class DialogButtonInputHandlerTest {

  /** Resets static input and platform state after stage-based tests. */
  @AfterEach
  public void tearDown() {
    InputManager.reset();
    Platform.loopHost(null);
  }

  /** Clicks are only fired when press and release happen on the same button. */
  @Test
  public void clickRequiresReleaseOnPressedButton() {
    DialogButtonInputHandler handler = new DialogButtonInputHandler(2);
    AtomicInteger clickedIndex = new AtomicInteger(-1);

    handler.onClick(0, () -> clickedIndex.set(0));
    handler.onClick(1, () -> clickedIndex.set(1));
    handler.updateBounds(
      List.of(
        new Rectangle(0, 0, 10, 10),
        new Rectangle(20, 0, 10, 10)));

    handler.update(5, 5, true);
    assertTrue(handler.isPressed(0));
    assertFalse(handler.isPressed(1));

    handler.update(25, 5, false);
    assertEquals(-1, clickedIndex.get());
    assertFalse(handler.isPressed(0));
    assertFalse(handler.isPressed(1));

    handler.update(25, 5, true);
    handler.update(25, 5, false);
    assertEquals(1, clickedIndex.get());
  }

  /** Resetting interaction state cancels a pending press. */
  @Test
  public void resetCancelsPendingPress() {
    DialogButtonInputHandler handler = new DialogButtonInputHandler(1);
    AtomicInteger clicks = new AtomicInteger();

    handler.onClick(0, clicks::incrementAndGet);
    handler.updateBounds(List.of(new Rectangle(0, 0, 10, 10)));

    handler.update(5, 5, true);
    assertTrue(handler.isPressed(0));

    handler.resetInteractionState();
    assertFalse(handler.isPressed(0));

    handler.update(5, 5, false);
    assertEquals(0, clicks.get());
  }

  /** The stage shortcut reads mouse position and primary button state from the current game stage. */
  @Test
  public void updateFromStageUsesCurrentStageMouseInput() {
    DialogButtonInputHandler handler = new DialogButtonInputHandler(1);
    AtomicInteger clicks = new AtomicInteger();

    handler.onClick(0, clicks::incrementAndGet);
    handler.updateBounds(List.of(new Rectangle(0, 0, 10, 10)));
    Platform.loopHost(new StubLoopHost(new StubStageHandle(5, 5)));

    InputManager.notifyButtonDown(MouseButtons.LEFT);
    assertTrue(handler.updateFromStage());
    assertTrue(handler.isPressed(0));

    InputManager.update();
    InputManager.notifyButtonUp(MouseButtons.LEFT);
    assertTrue(handler.updateFromStage());

    assertFalse(handler.isPressed(0));
    assertEquals(1, clicks.get());
  }

  /** Missing stages reset pending button presses instead of leaving stale state behind. */
  @Test
  public void updateFromStageResetsWhenStageIsMissing() {
    DialogButtonInputHandler handler = new DialogButtonInputHandler(1);

    handler.updateBounds(List.of(new Rectangle(0, 0, 10, 10)));
    handler.update(5, 5, true);
    assertTrue(handler.isPressed(0));

    assertFalse(handler.updateFromStage());
    assertFalse(handler.isPressed(0));
  }

  private record StubLoopHost(StageHandle stageHandle) implements GameLoopHost {
    @Override
    public void run(String[] args, GameLoop core) {}

    @Override
    public Optional<StageHandle> stage() {
      return Optional.of(stageHandle);
    }
  }

  private record StubStageHandle(int mouseX, int mouseY) implements StageHandle {
    @Override
    public Object raw() {
      return this;
    }

    @Override
    public <T> Optional<T> unwrap(Class<T> type) {
      return Optional.empty();
    }

    @Override
    public float getWidth() {
      return 100;
    }

    @Override
    public float getHeight() {
      return 100;
    }

    @Override
    public void addActor(Object actor) {}

    @Override
    public void setKeyboardFocus(Object actor) {}
  }
}
