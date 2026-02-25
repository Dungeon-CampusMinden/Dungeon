package core.configuration;

import core.input.Keys;
import core.configuration.values.ConfigIntValue;

/** Keyboardconfiguration for the core-package. */
@ConfigMap(path = {"keyboard"})
public class KeyboardConfig {
  /** WTF? . */
  public static final ConfigKey<Integer> TOGGLE_FULLSCREEN =
      new ConfigKey<>(new String[] {"graphics", "fullscreen"}, new ConfigIntValue(Keys.F11));

  /** WTF? . */
  public static final ConfigKey<Integer> MOVEMENT_UP =
      new ConfigKey<>(new String[] {"movement", "up"}, new ConfigIntValue(Keys.W));

  /** WTF? . */
  public static final ConfigKey<Integer> MOVEMENT_DOWN =
      new ConfigKey<>(new String[] {"movement", "down"}, new ConfigIntValue(Keys.S));

  /** WTF? . */
  public static final ConfigKey<Integer> MOVEMENT_LEFT =
      new ConfigKey<>(new String[] {"movement", "left"}, new ConfigIntValue(Keys.A));

  /** WTF? . */
  public static final ConfigKey<Integer> MOVEMENT_RIGHT =
      new ConfigKey<>(new String[] {"movement", "right"}, new ConfigIntValue(Keys.D));

  /** WTF? . */
  public static final ConfigKey<Integer> PAUSE =
      new ConfigKey<>(new String[] {"pause", "pause_game"}, new ConfigIntValue(Keys.P));

  /** WTF? . */
  public static final ConfigKey<Integer> ADVANCE_FRAME =
      new ConfigKey<>(new String[] {"pause", "advance_frame"}, new ConfigIntValue(Keys.M));
}
