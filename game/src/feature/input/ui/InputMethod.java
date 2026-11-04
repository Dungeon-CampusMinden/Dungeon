package feature.input.ui;

import java.util.List;
import java.util.Locale;

/**
 * Enumerates the input methods that can be displayed via {@link InputPromptHelper}.
 *
 * <p>{@link #TOUCH} is reserved for a future implementation.
 */
public enum InputMethod {
  /** Keyboard and mouse input. */
  KEYBOARD,
  /** PlayStation controller input. */
  PLAYSTATION,
  /** Xbox controller input, also the look of every other controller. */
  XBOX,
  /** Touch / on-screen input (not yet supported). */
  TOUCH;

  private static final List<String> PLAYSTATION_NAMES =
      List.of("playstation", "dualsense", "dualshock", "sony", "ps3", "ps4", "ps5");

  /**
   * Picks the prompt graphics for a controller.
   *
   * @param controllerName the name the controller reports
   * @return {@link #PLAYSTATION} for Sony controllers, otherwise {@link #XBOX}
   */
  public static InputMethod ofController(String controllerName) {
    String name = controllerName.toLowerCase(Locale.ROOT);
    return PLAYSTATION_NAMES.stream().anyMatch(name::contains) ? PLAYSTATION : XBOX;
  }
}
