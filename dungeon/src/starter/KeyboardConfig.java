package starter;

import com.badlogic.gdx.Input;
import core.configuration.ConfigKey;
import core.configuration.ConfigMap;
import core.configuration.values.ConfigIntValue;

/** Keyboard-configuration for the dungeon-package. */
@ConfigMap(path = {"keyboard"})
public class KeyboardConfig {
  /** Questlog. */
  public static final ConfigKey<Integer> QUESTLOG =
      new ConfigKey<>(new String[] {"menu", "questlog"}, new ConfigIntValue(Input.Keys.M));

  /** Game infos. */
  public static final ConfigKey<Integer> INFOS =
      new ConfigKey<>(new String[] {"info", "game_infos"}, new ConfigIntValue(Input.Keys.L));
}
