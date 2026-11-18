package engine.input;

import com.badlogic.gdx.Input;
import engine.input.Binding.Pad;
import engine.input.Binding.PadAxisId;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Learns one binding from the player's next input, for one device: keyboard and mouse, or the pad.
 *
 * <p>Control, Shift and Alt held while another key goes down become its prefixes, so the result is
 * a combo such as {@code Shift+A}. A modifier that comes up again without another key is the
 * binding itself. The pad has no combos; moving its sticks stays movement and cursor, pressing one
 * in is a button.
 *
 * <p>While capturing, no input reaches the UI or an action, except the presses the owner lets pass
 * to its own buttons. Escape and the pad's START cancel, so neither can be captured. Input of the
 * other device is ignored; only the pad's B also cancels a capture for keyboard and mouse, so that
 * a player with the pad in hand leaves it the usual way. The system key (Windows, Command) is never
 * a binding of its own.
 *
 * <p>The resolver shows it every press before routing: keys as {@link Binding.Key} without
 * prefixes, mouse buttons, a notch of the wheel, and pad buttons, triggers and stick directions.
 */
public final class BindingCapture {
  private final InputResolver resolver;
  private final boolean pad;
  private final Predicate<Binding> passes;
  private final Consumer<Optional<Binding>> result;
  // A modifier that went down and may still become a binding of its own.
  private Binding.Key pendingModifier;

  /**
   * Creates a capture. It takes effect through {@link InputResolver#capture(BindingCapture)} and
   * stays until its owner ends it there; the result may arrive several times if the owner keeps
   * capturing after a binding it cannot use.
   *
   * @param resolver the resolver that feeds this capture
   * @param pad true to learn a pad binding, false for keyboard and mouse
   * @param passes tells for a press whether it should reach the UI instead, e.g. a click on a
   *     button of the capturing dialog
   * @param result receives the captured binding, or empty if the player cancelled
   */
  public BindingCapture(
      InputResolver resolver,
      boolean pad,
      Predicate<Binding> passes,
      Consumer<Optional<Binding>> result) {
    this.resolver = resolver;
    this.pad = pad;
    this.passes = passes;
    this.result = result;
  }

  // Sees a press before it is routed; true takes it from the UI and from every action.
  boolean down(Binding input) {
    if (passes.test(input)) return false;
    if (input.equals(new Binding.Key(Input.Keys.ESCAPE))
        || input.equals(new Binding.PadButton(Pad.START))
        || (!pad && input.equals(new Binding.PadButton(Pad.B)))) {
      result.accept(Optional.empty());
    } else if (input.isPad() == pad) {
      capture(input);
    }
    return true;
  }

  private void capture(Binding input) {
    pendingModifier = null;
    switch (input) {
      case Binding.Key key -> {
        // The system key binds nothing; on macOS, Command still counts as Control for the next key.
        boolean bindable = key.keycode() != Input.Keys.SYM && key.keycode() != Input.Keys.UNKNOWN;
        if (bindable && InputResolver.modifierOf(key.keycode()).isPresent()) {
          pendingModifier = key;
        } else if (bindable) {
          Set<Integer> held = new HashSet<>(Binding.MODIFIERS);
          held.removeIf(prefix -> !resolver.modifierHeld(prefix));
          result.accept(Optional.of(new Binding.Key(key.keycode(), held)));
        }
      }
      case Binding.PadAxis axis -> {
        if (axis.axis() == PadAxisId.LT || axis.axis() == PadAxisId.RT) {
          result.accept(Optional.of(axis));
        }
      }
      case Binding.MouseButton button -> result.accept(Optional.of(button));
      case Binding.MouseWheelStep step -> result.accept(Optional.of(step));
      case Binding.PadButton button -> result.accept(Optional.of(button));
      case Binding.MouseWheel wheel -> {}
    }
  }

  // The resolver cancelled all held input, e.g. because the window lost its focus.
  void cancelled() {
    pendingModifier = null;
  }

  // Sees every key release, whether or not its press was taken.
  void up(Binding input) {
    if (!input.equals(pendingModifier)) return;
    pendingModifier = null;
    result.accept(Optional.of(input));
  }
}
