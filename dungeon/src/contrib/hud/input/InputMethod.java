package contrib.hud.input;

/**
 * Enumerates the input methods that can be displayed via {@link InputPromptHelper}.
 *
 * <p>Currently only {@link #KEYBOARD} is fully supported. The remaining values are reserved for
 * future implementations.
 */
public enum InputMethod {
  /** Keyboard and mouse input. */
  KEYBOARD,
  /** PlayStation controller input (not yet supported). */
  PLAYSTATION,
  /** Xbox controller input (not yet supported). */
  XBOX,
  /** Touch / on-screen input (not yet supported). */
  TOUCH
}
