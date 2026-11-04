package engine.input;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import engine.Game;
import engine.input.Binding.Pad;
import engine.input.Binding.PadAxis;
import engine.input.Binding.PadAxisId;
import engine.input.Binding.PadButton;
import engine.utils.Vector2;
import java.util.EnumSet;
import java.util.Set;

/**
 * Reads the first connected controller once per frame and feeds the resolver.
 *
 * <p>Buttons, triggers and stick directions become presses; the left stick stays analog. The right
 * stick moves pointer 0. Unplugging cancels everything the pad holds.
 */
final class PadInput {
  private static final int AXIS_LEFT_X = 0;
  private static final int AXIS_LEFT_Y = 1;
  private static final int AXIS_RIGHT_X = 2;
  private static final int AXIS_RIGHT_Y = 3;
  private static final int AXIS_LT = 4;
  private static final int AXIS_RT = 5;
  private static final float STICK_DEADZONE = 0.2f;
  private static final float PRESS_THRESHOLD = 0.5f;
  private static final float RELEASE_THRESHOLD = 0.3f;
  private static final float CURSOR_SPEED = 800f;

  private static final Set<Pad> heldButtons = EnumSet.noneOf(Pad.class);
  private static final Set<PadAxisId> heldAxes = EnumSet.noneOf(PadAxisId.class);
  private static Controller connected;

  private PadInput() {}

  static void reset() {
    heldButtons.clear();
    heldAxes.clear();
    connected = null;
  }

  static void poll(InputResolver resolver, float delta) {
    Controller controller =
        Controllers.getControllers().size == 0 ? null : Controllers.getControllers().first();
    if (controller != connected) {
      resolver.cancelPad();
      heldButtons.clear();
      heldAxes.clear();
      connected = controller;
    }
    if (controller == null) return;

    for (Pad pad : Pad.values()) readButton(resolver, controller, pad);
    readThreshold(resolver, PadAxisId.LT, Math.abs(controller.getAxis(AXIS_LT)));
    readThreshold(resolver, PadAxisId.RT, Math.abs(controller.getAxis(AXIS_RT)));

    Vector2 left =
        radialDeadzone(controller.getAxis(AXIS_LEFT_X), -controller.getAxis(AXIS_LEFT_Y));
    if (!left.isZero()) resolver.device(InputDevice.PAD);
    resolver.padAxis(new PadAxis(PadAxisId.LSTICK), left.x(), left.y());
    readThreshold(resolver, PadAxisId.LSTICK_UP, left.y());
    readThreshold(resolver, PadAxisId.LSTICK_DOWN, -left.y());
    readThreshold(resolver, PadAxisId.LSTICK_LEFT, -left.x());
    readThreshold(resolver, PadAxisId.LSTICK_RIGHT, left.x());

    moveCursor(
        resolver,
        radialDeadzone(controller.getAxis(AXIS_RIGHT_X), -controller.getAxis(AXIS_RIGHT_Y)),
        delta);
  }

  private static void readButton(InputResolver resolver, Controller controller, Pad pad) {
    boolean down = controller.getButton(pad.index());
    if (down == heldButtons.contains(pad)) return;
    if (down) {
      heldButtons.add(pad);
      resolver.device(InputDevice.PAD);
      resolver.padDown(new PadButton(pad));
    } else {
      heldButtons.remove(pad);
      resolver.padUp(new PadButton(pad));
    }
  }

  /** Digital reading of an analog value, with hysteresis against flicker at the threshold. */
  private static void readThreshold(InputResolver resolver, PadAxisId axis, float value) {
    boolean held = heldAxes.contains(axis);
    if (!held && value > PRESS_THRESHOLD) {
      heldAxes.add(axis);
      resolver.device(InputDevice.PAD);
      resolver.padDown(new PadAxis(axis));
    } else if (held && value < RELEASE_THRESHOLD) {
      heldAxes.remove(axis);
      resolver.padUp(new PadAxis(axis));
    }
  }

  /**
   * Applies a radial dead zone and stretches the rest back to the full range.
   *
   * @param x raw horizontal value
   * @param y raw vertical value
   * @return zero inside the dead zone, otherwise a vector of length at most 1
   */
  static Vector2 radialDeadzone(float x, float y) {
    Vector2 raw = Vector2.of(x, y);
    float length = (float) raw.length();
    if (length < STICK_DEADZONE) return Vector2.ZERO;
    float scaled = Math.min(1f, (length - STICK_DEADZONE) / (1f - STICK_DEADZONE));
    return raw.normalize().scale(scaled);
  }

  private static void moveCursor(InputResolver resolver, Vector2 stick, float delta) {
    if (stick.isZero()) return;
    resolver.device(InputDevice.PAD);
    Vector2 pointer = resolver.pointer(0);
    float x = (float) pointer.x() + (float) stick.x() * CURSOR_SPEED * delta;
    float y = (float) pointer.y() - (float) stick.y() * CURSOR_SPEED * delta;
    resolver.pointer(
        0,
        Math.max(0f, Math.min(x, Game.windowWidth() - 1f)),
        Math.max(0f, Math.min(y, Game.windowHeight() - 1f)));
  }
}
