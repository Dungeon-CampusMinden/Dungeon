package core.configuration;

import com.badlogic.gdx.Input;
import core.configuration.values.ConfigIntValue;

/** Keyboardconfiguration for the core-package. */
@ConfigMap(path = {"keyboard"})
public class KeyboardConfig {
  /** WTF? . */
  public static final ConfigKey<Integer> TOGGLE_FULLSCREEN =
      new ConfigKey<>(new String[] {"graphics", "fullscreen"}, new ConfigIntValue(Input.Keys.F11));

  /** WTF? . */
  public static final ConfigKey<Integer> MOVEMENT_UP =
      new ConfigKey<>(new String[] {"movement", "up"}, new ConfigIntValue(Input.Keys.W));

  /** WTF? . */
  public static final ConfigKey<Integer> MOVEMENT_DOWN =
      new ConfigKey<>(new String[] {"movement", "down"}, new ConfigIntValue(Input.Keys.S));

  /** WTF? . */
  public static final ConfigKey<Integer> MOVEMENT_LEFT =
      new ConfigKey<>(new String[] {"movement", "left"}, new ConfigIntValue(Input.Keys.A));

  /** WTF? . */
  public static final ConfigKey<Integer> MOVEMENT_RIGHT =
      new ConfigKey<>(new String[] {"movement", "right"}, new ConfigIntValue(Input.Keys.D));

  public static final ConfigKey<Integer> MOVEMENT_UP_SECOND =
      new ConfigKey<>(new String[] {"movement", "up_second"}, new ConfigIntValue(Input.Keys.UP));

  public static final ConfigKey<Integer> MOVEMENT_DOWN_SECOND =
      new ConfigKey<>(
          new String[] {"movement", "down_second"}, new ConfigIntValue(Input.Keys.DOWN));

  public static final ConfigKey<Integer> MOVEMENT_LEFT_SECOND =
      new ConfigKey<>(
          new String[] {"movement", "left_second"}, new ConfigIntValue(Input.Keys.LEFT));

  public static final ConfigKey<Integer> MOVEMENT_RIGHT_SECOND =
      new ConfigKey<>(
          new String[] {"movement", "right_second"}, new ConfigIntValue(Input.Keys.RIGHT));

  public static final ConfigKey<Integer> MOUSE_MOVE =
      new ConfigKey<>(
          new String[] {"movement", "mouse_move"}, new ConfigIntValue(Input.Buttons.RIGHT));

  /** WTF? . */
  public static final ConfigKey<Integer> PAUSE =
      new ConfigKey<>(new String[] {"pause", "pause_game"}, new ConfigIntValue(Input.Keys.P));
}
