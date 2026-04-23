package contrib.hud.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Rectangle;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/** Tests for {@link DialogButtonInputHandler}. */
public class DialogButtonInputHandlerTest {

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
}
