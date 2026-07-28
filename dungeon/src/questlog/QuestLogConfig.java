package questlog;

import com.badlogic.gdx.Input;
import core.configuration.ConfigKey;
import core.configuration.ConfigMap;
import core.configuration.values.ConfigIntValue;

/**
 * Keyboard configuration for quest log controls.
 *
 * <p>Games that use the quest log should include this config class in their starter configuration
 * so the binding can be loaded from the configured keyboard file. The default key opens the
 * proof-of-concept quest log UI.
 */
@ConfigMap(path = {"keyboard"})
public final class QuestLogConfig {

  /** Keybinding used to open the quest log. Defaults to {@code B}. */
  public static final ConfigKey<Integer> OPEN_QUESTLOG =
      new ConfigKey<>(new String[] {"questlog", "open"}, new ConfigIntValue(Input.Keys.B));

  private QuestLogConfig() {}
}
