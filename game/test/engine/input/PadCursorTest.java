package engine.input;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.scenes.scene2d.Stage;
import engine.input.Binding.Pad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests how the pad's click becomes touch events, with and without a drag. */
class PadCursorTest {
  private static final Binding A = Binding.pad(Pad.A);

  private final Stage stage = mock(Stage.class);
  private InputResolver resolver;
  private InputContext inventory;

  @BeforeEach
  void setUp() {
    GameInput.reset();
    resolver = GameInput.resolver();
    resolver.device(InputDevice.PAD);
    inventory = new InputContext(UiActions.INVENTORY);
    resolver.activate(inventory);
  }

  private void frame() {
    resolver.advance(0.016f);
    PadCursor.update(resolver, stage);
  }

  private void press() {
    resolver.padDown(A);
    frame();
  }

  private void release() {
    resolver.padUp(A);
    frame();
  }

  private void pressOnDraggable() {
    when(stage.touchDown(anyInt(), anyInt(), anyInt(), anyInt()))
        .thenAnswer(
            call -> {
              GameInput.keepPadPress();
              return true;
            });
    press();
  }

  private void verifyTouchUps(int count) {
    verify(stage, times(count)).touchUp(anyInt(), anyInt(), anyInt(), anyInt());
  }

  @Test
  void aPressOnSomethingElseIsAClick() {
    press();
    release();

    verify(stage).touchDown(anyInt(), anyInt(), anyInt(), anyInt());
    verifyTouchUps(1);
  }

  @Test
  void aDragStaysAfterTheReleaseAndTheNextPressDrops() {
    pressOnDraggable();
    release();
    verifyTouchUps(0);

    resolver.pointer(0, 50, 60);
    frame();
    verify(stage).touchDragged(50, 60, 0);

    press();
    verifyTouchUps(1);
    release();

    verify(stage).touchDown(anyInt(), anyInt(), anyInt(), anyInt());
    verifyTouchUps(1);
  }

  @Test
  void closingTheUiCancelsADrag() {
    pressOnDraggable();
    release();

    resolver.deactivate(inventory);
    frame();

    verify(stage).touchCancelled(anyInt(), anyInt(), anyInt(), anyInt());
    verifyTouchUps(0);
  }

  @Test
  void aUiAboveCancelsADrag() {
    pressOnDraggable();
    release();

    resolver.activate(new InputContext(UiActions.PAUSE));
    frame();

    verify(stage).touchCancelled(anyInt(), anyInt(), anyInt(), anyInt());
  }

  @Test
  void leavingThePadCancelsADrag() {
    pressOnDraggable();
    release();

    resolver.device(InputDevice.KEYBOARD_MOUSE);
    frame();

    verify(stage).touchCancelled(anyInt(), anyInt(), anyInt(), anyInt());
    verify(stage, never()).touchUp(anyInt(), anyInt(), anyInt(), anyInt());
  }
}
