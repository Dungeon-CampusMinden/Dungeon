package core.configuration;

import core.configuration.values.ConfigIntValue;
import core.input.Keys;

/**
 * A configuration class that maps keyboard-related settings.
 *
 * <p>This class defines configurable key bindings for various actions such as movement, toggling
 * fullscreen, pausing the game, and advancing frames.
 *
 * <p>It uses {@link ConfigKey} to associate each action with a specific key and allows
 * configuration through a JSON-like structure specified by paths.
 */
@ConfigMap(path = {"keyboard"})
public class KeyboardConfig {

  /**
   * Configuration key for toggling fullscreen mode.
   *
   * <p>Default binding: F11
   */
  public static final ConfigKey<Integer> TOGGLE_FULLSCREEN =
      new ConfigKey<>(new String[] {"graphics", "fullscreen"}, new ConfigIntValue(Keys.F11));

  /**
   * Configuration key for moving the character upward.
   *
   * <p>Default binding: W
   */
  public static final ConfigKey<Integer> MOVEMENT_UP =
      new ConfigKey<>(new String[] {"movement", "up"}, new ConfigIntValue(Keys.W));

  /**
   * Configuration key for moving the character downward.
   *
   * <p>Default binding: S
   */
  public static final ConfigKey<Integer> MOVEMENT_DOWN =
      new ConfigKey<>(new String[] {"movement", "down"}, new ConfigIntValue(Keys.S));

  /**
   * Configuration key for moving the character to the left.
   *
   * <p>Default binding: A
   */
  public static final ConfigKey<Integer> MOVEMENT_LEFT =
      new ConfigKey<>(new String[] {"movement", "left"}, new ConfigIntValue(Keys.A));

  /**
   * Configuration key for moving the character to the right.
   *
   * <p>Default binding: D
   */
  public static final ConfigKey<Integer> MOVEMENT_RIGHT =
      new ConfigKey<>(new String[] {"movement", "right"}, new ConfigIntValue(Keys.D));

  /**
   * Configuration key for pausing the game.
   *
   * <p>Default binding: P
   */
  public static final ConfigKey<Integer> PAUSE =
      new ConfigKey<>(new String[] {"pause", "pause_game"}, new ConfigIntValue(Keys.P));

  /**
   * Configuration key for advancing to the next frame during pause.
   *
   * <p>Default binding: M
   */
  public static final ConfigKey<Integer> ADVANCE_FRAME =
      new ConfigKey<>(new String[] {"pause", "advance_frame"}, new ConfigIntValue(Keys.M));
}
