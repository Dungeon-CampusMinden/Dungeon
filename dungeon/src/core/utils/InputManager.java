package core.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Manages keyboard and mouse input states for a single frame.
 *
 * <p>This class tracks the state of keys and mouse buttons, allowing queries about whether a key
 * or button is pressed, was just pressed/released, or was double-tapped. It also tracks how long a
 * key or button has been held and provides a buffer for typed character input.
 *
 * <p>The class uses a frame-based input model where state is updated once per frame via {@link
 * #update()}. To receive input events, call the {@code notify*} methods.
 *
 * <p>This is a static utility class and cannot be instantiated.
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

  /** Buffer for typed characters (text input), independent of key press state tracking. */
  private static final StringBuilder typedCharacters = new StringBuilder();

  private InputManager() {} // static utility class

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
   * Returns all currently buffered typed characters and clears the buffer.
   *
   * <p>This is intended for text-input UIs such as FREE_INPUT dialogs.
   *
   * @return typed characters since the last consume call
   */
  public static String consumeTypedCharacters() {
    if (typedCharacters.isEmpty()) {
      return "";
    }

    String result = typedCharacters.toString();
    typedCharacters.setLength(0);
    return result;
  }

  /**
   * Clears all tracked input states.
   *
   * <p>Useful when changing input processors or when focus is lost. This also clears tap history,
   * hold start times and typed text input.
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
    typedCharacters.setLength(0);
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
    return core.utils.Time.sinceMs(downTime) >= holdDurationMs;
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

  /**
   * Notifies the InputManager that a key has been pressed.
   *
   * <p>This method should be called by an input handler when a key down event is received. It
   * updates the internal state to mark the key as just pressed.
   *
   * @param keycode The code of the key that was pressed.
   */
  public static void notifyKeyDown(int keycode) {
    if (pressedKeys.contains(keycode) || justPressedKeys.contains(keycode)) return;

    registerPress(
      keycode,
      justPressedKeys,
      pressedKeys,
      justReleasedKeys,
      lastKeyTapTimesMs,
      previousKeyTapTimesMs,
      keyDownTimesMs,
      core.utils.Time.nowMs());
  }

  /**
   * Notifies the InputManager that a key has been released.
   *
   * <p>This method should be called by an input handler when a key up event is received. It
   * updates the internal state to mark the key as just released.
   *
   * @param keycode The code of the key that was released.
   */
  public static void notifyKeyUp(int keycode) {
    if (!pressedKeys.contains(keycode) && !justPressedKeys.contains(keycode)) return;
    registerRelease(keycode, pressedKeys, justReleasedKeys, keyDownTimesMs);
  }

  /**
   * Registers a typed character for text-input UIs.
   *
   * @param character the typed character
   */
  public static void notifyKeyTyped(char character) {
    if (character == java.awt.event.KeyEvent.CHAR_UNDEFINED) {
      return;
    }
    typedCharacters.append(character);
  }

  /**
   * Notifies the InputManager that a mouse button has been pressed.
   *
   * <p>This method should be called by an input handler when a mouse button down event is
   * received. It updates the internal state to mark the button as just pressed.
   *
   * @param button The code of the mouse button that was pressed.
   */
  public static void notifyButtonDown(int button) {
    if (pressedButtons.contains(button) || justPressedButtons.contains(button)) return;

    registerPress(
      button,
      justPressedButtons,
      pressedButtons,
      justReleasedButtons,
      lastButtonTapTimesMs,
      previousButtonTapTimesMs,
      buttonDownTimesMs,
      core.utils.Time.nowMs());
  }

  /**
   * Notifies the InputManager that a mouse button has been released.
   *
   * <p>This method should be called by an input handler when a mouse button up event is received.
   * It updates the internal state to mark the button as just released.
   *
   * @param button The code of the mouse button that was released.
   */
  public static void notifyButtonUp(int button) {
    if (!pressedButtons.contains(button) && !justPressedButtons.contains(button)) return;
    registerRelease(button, pressedButtons, justReleasedButtons, buttonDownTimesMs);
  }
}
