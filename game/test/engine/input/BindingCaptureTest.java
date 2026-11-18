package engine.input;

import static engine.input.Binding.key;
import static engine.input.Binding.pad;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import engine.input.Binding.Pad;
import engine.input.Binding.PadAxisId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests how {@link BindingCapture} turns the player's input into one binding. */
class BindingCaptureTest {
  private static final ActionId JUMP = new ActionId("test", "jump", key(Keys.A), pad(Pad.A));

  private final List<Optional<Binding>> results = new ArrayList<>();
  private InputResolver resolver;

  @BeforeEach
  void setUp() {
    resolver = new InputResolver(new BindingTable());
    resolver.activate(new InputContext(ContextSpec.of("game", ContextLayer.BASE, JUMP)));
  }

  private void capture(boolean pad) {
    resolver.capture(new BindingCapture(resolver, pad, input -> false, results::add));
  }

  @Test
  void heldModifierBecomesThePrefixOfTheNextKey() {
    capture(false);

    resolver.keyDown(Keys.SHIFT_RIGHT);
    resolver.keyDown(Keys.A);
    resolver.keyUp(Keys.A);
    resolver.keyUp(Keys.SHIFT_RIGHT);

    assertEquals(List.of(Optional.of(key(Keys.SHIFT_LEFT, Keys.A))), results);
  }

  @Test
  void heldModifierAlsoPrefixesAMouseButton() {
    capture(false);

    resolver.keyDown(Keys.SHIFT_LEFT);
    resolver.touchDown(0, 0, 0, Buttons.LEFT);
    resolver.keyUp(Keys.SHIFT_LEFT);

    assertEquals(
        List.of(Optional.of(new Binding.MouseButton(Buttons.LEFT, Set.of(Keys.SHIFT_LEFT)))),
        results);
  }

  @Test
  void modifierReleasedWithoutAnotherKeyIsTheBindingItself() {
    capture(false);

    resolver.keyDown(Keys.SHIFT_RIGHT);
    assertEquals(List.of(), results);
    resolver.keyUp(Keys.SHIFT_RIGHT);

    assertEquals(List.of(Optional.of(key(Keys.SHIFT_RIGHT))), results);
  }

  @Test
  void capturedInputStartsNoActionAndEscapeOrPadBCancel() {
    capture(false);

    resolver.keyDown(Keys.SYM);
    resolver.keyUp(Keys.SYM);
    resolver.keyDown(Keys.A);
    resolver.padDown(pad(Pad.A));
    resolver.keyDown(Keys.ESCAPE);
    resolver.padDown(pad(Pad.B));
    resolver.advance(0);

    assertEquals(List.of(Optional.of(key(Keys.A)), Optional.empty(), Optional.empty()), results);
    assertFalse(resolver.pressed(JUMP));
  }

  @Test
  void padCaptureTakesButtonsAndTriggersButNoStick() {
    capture(true);

    resolver.padAxis(pad(PadAxisId.LSTICK), 1, 0);
    resolver.padDown(pad(PadAxisId.LSTICK_RIGHT));
    resolver.padDown(pad(PadAxisId.RT));
    resolver.padDown(pad(Pad.START));

    assertEquals(List.of(Optional.of(pad(PadAxisId.RT)), Optional.empty()), results);
  }
}
