package core.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * System that tracks input states reliably across all platforms.
 *
 * <p>This system wraps libGDX's InputProcessor to track key and button press states, providing a
 * workaround for platform-specific issues (particularly on macOS) where {@link
 * com.badlogic.gdx.Input#isKeyJustPressed(int)} doesn't work reliably.
 *
 * <p>Usage: Call {@link #init()} after setting up the input processor (e.g., after {@link
 * com.badlogic.gdx.Gdx.Input#setInputProcessor(InputProcessor)}), then call {@link #update()} once
 * per frame (typically at the end of your render/update loop). Use the static methods {@link
 * #isKeyJustPressed(int)}, {@link #isKeyPressed(int)}, {@link #isKeyJustReleased(int)}, {@link
 * #isButtonJustPressed(int)}, {@link #isButtonPressed(int)}, {@link #isButtonJustReleased(int)},
 * {@link #isKeyDoubleTapped(int)}, {@link #isButtonDoubleTapped(int)}, {@link #isKeyHeld(int,
 * long)}, and {@link #isButtonHeld(int, long)} instead of the corresponding {@link
 * com.badlogic.gdx.Gdx.Input} methods.
 *
 * <p>Time-based methods use milliseconds from {@link TimeUtils#millis()}. Double-tap checks return
 * true only on the second tap's press frame, and hold checks return true once the duration has
 * elapsed while the key/button remains pressed.
 */
public final class InputManager {

  private static final long DEFAULT_DOUBLE_TAP_INTERVAL_MS = 300L;

  private static final Set<Integer> justPressedKeys = new HashSet<>();
  private static final Set<Integer> pressedKeys = new HashSet<>();
  private static final Set<Integer> justReleasedKeys = new HashSet<>();
  private static final Set<Integer> justPressedButtons = new HashSet<>();
  private static final Set<Integer> pressedButtons = new HashSet<>();
  private static final Set<Integer> justReleasedButtons = new HashSet<>();
  private static final Map<Integer, Long> lastKeyTapTimesMs = new HashMap<>();
  private static final Map<Integer, Long> previousKeyTapTimesMs = new HashMap<>();
  private static final Map<Integer, Long> lastButtonTapTimesMs = new HashMap<>();
  private static final Map<Integer, Long> previousButtonTapTimesMs = new HashMap<>();
  private static final Map<Integer, Long> keyDownTimesMs = new HashMap<>();
  private static final Map<Integer, Long> buttonDownTimesMs = new HashMap<>();

  private InputManager() {} // static utility class

  /**
   * Initializes the input state tracking by wrapping the current input processor.
   *
   * <p>This method should be called after setting up the input processor (e.g., after setting the
   * Stage as input processor). This replaces the current processor with a wrapper that forwards to
   * the previous one.
   */
  public static void init() {
    InputProcessor oldProcessor = Gdx.input.getInputProcessor();
    Gdx.input.setInputProcessor(
        new InputProcessor() {
          @Override
          public boolean keyDown(int keycode) {
            registerPress(
                keycode,
                justPressedKeys,
                pressedKeys,
                justReleasedKeys,
                lastKeyTapTimesMs,
                previousKeyTapTimesMs,
                keyDownTimesMs,
                TimeUtils.millis());
            return oldProcessor != null && oldProcessor.keyDown(keycode);
          }

          @Override
          public boolean keyUp(int keycode) {
            registerRelease(keycode, pressedKeys, justReleasedKeys, keyDownTimesMs);
            return oldProcessor != null && oldProcessor.keyUp(keycode);
          }

          @Override
          public boolean keyTyped(char character) {
            return oldProcessor != null && oldProcessor.keyTyped(character);
          }

          @Override
          public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            registerPress(
                button,
                justPressedButtons,
                pressedButtons,
                justReleasedButtons,
                lastButtonTapTimesMs,
                previousButtonTapTimesMs,
                buttonDownTimesMs,
                TimeUtils.millis());
            return oldProcessor != null
                && oldProcessor.touchDown(screenX, screenY, pointer, button);
          }

          @Override
          public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            registerRelease(button, pressedButtons, justReleasedButtons, buttonDownTimesMs);
            return oldProcessor != null && oldProcessor.touchUp(screenX, screenY, pointer, button);
          }

          @Override
          public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
            registerRelease(button, pressedButtons, justReleasedButtons, buttonDownTimesMs);
            return oldProcessor != null
                && oldProcessor.touchCancelled(screenX, screenY, pointer, button);
          }

          @Override
          public boolean touchDragged(int screenX, int screenY, int pointer) {
            return oldProcessor != null && oldProcessor.touchDragged(screenX, screenY, pointer);
          }

          @Override
          public boolean mouseMoved(int screenX, int screenY) {
            return oldProcessor != null && oldProcessor.mouseMoved(screenX, screenY);
          }

          @Override
          public boolean scrolled(float amountX, float amountY) {
            return oldProcessor != null && oldProcessor.scrolled(amountX, amountY);
          }
        });
  }

  /**
   * Checks if a key was just pressed in the current frame.
   *
   * @param keycode The key to check.
   * @return true if the key was just pressed, false otherwise.
   */
  public static boolean isKeyJustPressed(int keycode) {
    return justPressedKeys.contains(keycode);
  }

  /**
   * Checks if a key is currently pressed.
   *
   * @param keycode The key to check.
   * @return true if the key is pressed (including the frame it was just pressed), false otherwise.
   */
  public static boolean isKeyPressed(int keycode) {
    return pressedKeys.contains(keycode) || justPressedKeys.contains(keycode);
  }

  /**
   * Checks if a key was just released in the current frame.
   *
   * @param keycode The key to check.
   * @return true if the key was just released, false otherwise.
   */
  public static boolean isKeyJustReleased(int keycode) {
    return justReleasedKeys.contains(keycode);
  }

  /**
   * Checks if a key was double-tapped in the current frame.
   *
   * @param keycode The key to check.
   * @return true only on the second tap's press frame if the previous tap was within the default
   *     interval, false otherwise.
   */
  public static boolean isKeyDoubleTapped(int keycode) {
    return isKeyDoubleTapped(keycode, DEFAULT_DOUBLE_TAP_INTERVAL_MS);
  }

  /**
   * Checks if a key was double-tapped in the current frame.
   *
   * @param keycode The key to check.
   * @param maxIntervalMs Maximum time in milliseconds between taps.
   * @return true only on the second tap's press frame if the previous tap was within the given
   *     interval, false otherwise.
   */
  public static boolean isKeyDoubleTapped(int keycode, long maxIntervalMs) {
    return isDoubleTapped(
        keycode, justPressedKeys, lastKeyTapTimesMs, previousKeyTapTimesMs, maxIntervalMs);
  }

  /**
   * Checks if a mouse button was just pressed in the current frame.
   *
   * @param button The button to check.
   * @return true if the button was just pressed, false otherwise.
   */
  public static boolean isButtonJustPressed(int button) {
    return justPressedButtons.contains(button);
  }

  /**
   * Checks if a mouse button is currently pressed.
   *
   * @param button The button to check.
   * @return true if the button is pressed (including the frame it was just pressed), false
   *     otherwise.
   */
  public static boolean isButtonPressed(int button) {
    return pressedButtons.contains(button) || justPressedButtons.contains(button);
  }

  /**
   * Checks if a mouse button was just released in the current frame.
   *
   * @param button The button to check.
   * @return true if the button was just released, false otherwise.
   */
  public static boolean isButtonJustReleased(int button) {
    return justReleasedButtons.contains(button);
  }

  /**
   * Checks if a mouse button was double-tapped in the current frame.
   *
   * @param button The button to check.
   * @return true only on the second tap's press frame if the previous tap was within the default
   *     interval, false otherwise.
   */
  public static boolean isButtonDoubleTapped(int button) {
    return isButtonDoubleTapped(button, DEFAULT_DOUBLE_TAP_INTERVAL_MS);
  }

  /**
   * Checks if a mouse button was double-tapped in the current frame.
   *
   * @param button The button to check.
   * @param maxIntervalMs Maximum time in milliseconds between taps.
   * @return true only on the second tap's press frame if the previous tap was within the given
   *     interval, false otherwise.
   */
  public static boolean isButtonDoubleTapped(int button, long maxIntervalMs) {
    return isDoubleTapped(
        button, justPressedButtons, lastButtonTapTimesMs, previousButtonTapTimesMs, maxIntervalMs);
  }

  /**
   * Checks if a key has been held for at least the given duration.
   *
   * @param keycode The key to check.
   * @param holdDurationMs Duration in milliseconds the key must be held. If {@code <= 0}, behaves
   *     like {@link #isKeyPressed(int)}.
   * @return true if the key has been held long enough, false otherwise.
   */
  public static boolean isKeyHeld(int keycode, long holdDurationMs) {
    return isHeld(keycode, holdDurationMs, keyDownTimesMs, InputManager::isKeyPressed);
  }

  /**
   * Checks if a mouse button has been held for at least the given duration.
   *
   * @param button The button to check.
   * @param holdDurationMs Duration in milliseconds the button must be held. If {@code <= 0},
   *     behaves like {@link #isButtonPressed(int)}.
   * @return true if the button has been held long enough, false otherwise.
   */
  public static boolean isButtonHeld(int button, long holdDurationMs) {
    return isHeld(button, holdDurationMs, buttonDownTimesMs, InputManager::isButtonPressed);
  }

  /**
   * Clears all tracked input states.
   *
   * <p>Useful when changing input processors or when focus is lost. This also clears tap history
   * and hold start times.
   */
  public static void reset() {
    justPressedKeys.clear();
    pressedKeys.clear();
    justReleasedKeys.clear();
    justPressedButtons.clear();
    pressedButtons.clear();
    justReleasedButtons.clear();
    lastKeyTapTimesMs.clear();
    previousKeyTapTimesMs.clear();
    lastButtonTapTimesMs.clear();
    previousButtonTapTimesMs.clear();
    keyDownTimesMs.clear();
    buttonDownTimesMs.clear();
  }

  /**
   * Updates the input states.
   *
   * <p>This method should be called once per frame.
   */
  public static void update() {
    // Move justPressed keys/buttons to pressed state and clear justPressed
    updateFrame(justPressedKeys, pressedKeys, justReleasedKeys);
    updateFrame(justPressedButtons, pressedButtons, justReleasedButtons);
  }

  private static void registerPress(
      int code,
      Set<Integer> justPressed,
      Set<Integer> pressed,
      Set<Integer> justReleased,
      Map<Integer, Long> lastTapTimesMs,
      Map<Integer, Long> previousTapTimesMs,
      Map<Integer, Long> downTimesMs,
      long nowMs) {
    boolean isNewPress =
        !pressed.contains(code) && (!justPressed.contains(code) || justReleased.contains(code));
    if (isNewPress) {
      justReleased.remove(code);
      Long lastTap = lastTapTimesMs.get(code);
      if (lastTap != null) {
        previousTapTimesMs.put(code, lastTap);
      } else {
        previousTapTimesMs.remove(code);
      }
      lastTapTimesMs.put(code, nowMs);
      downTimesMs.put(code, nowMs);
    }
    justPressed.add(code);
  }

  private static void registerRelease(
      int code, Set<Integer> pressed, Set<Integer> justReleased, Map<Integer, Long> downTimesMs) {
    pressed.remove(code);
    justReleased.add(code);
    downTimesMs.remove(code);
  }

  private static boolean isDoubleTapped(
      int code,
      Set<Integer> justPressed,
      Map<Integer, Long> lastTapTimesMs,
      Map<Integer, Long> previousTapTimesMs,
      long maxIntervalMs) {
    if (!justPressed.contains(code) || maxIntervalMs < 0) {
      return false;
    }
    Long lastTap = lastTapTimesMs.get(code);
    Long previousTap = previousTapTimesMs.get(code);
    return lastTap != null && previousTap != null && lastTap - previousTap <= maxIntervalMs;
  }

  private static boolean isHeld(
      int code,
      long holdDurationMs,
      Map<Integer, Long> downTimesMs,
      java.util.function.IntPredicate isPressed) {
    if (holdDurationMs <= 0) {
      return isPressed.test(code);
    }
    Long downTime = downTimesMs.get(code);
    if (downTime == null || !isPressed.test(code)) {
      return false;
    }
    return TimeUtils.timeSinceMillis(downTime) >= holdDurationMs;
  }

  private static void updateFrame(
      Set<Integer> justPressed, Set<Integer> pressed, Set<Integer> justReleased) {
    if (!justReleased.isEmpty()) {
      justPressed.removeAll(justReleased);
    }
    pressed.addAll(justPressed);
    justPressed.clear();
    justReleased.clear();
  }
}
