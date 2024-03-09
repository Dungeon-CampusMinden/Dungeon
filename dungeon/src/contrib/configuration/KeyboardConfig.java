package contrib.configuration;

import com.badlogic.gdx.Input;
import core.configuration.ConfigKey;
import core.configuration.ConfigMap;
import core.configuration.values.ConfigIntValue;

/** Keyboard-configuration for the core-package. */
@ConfigMap(path = {"keyboard"})
public class KeyboardConfig {
  /** Keys for the core-package for open inventory. */
  public static final ConfigKey<Integer> INVENTORY_OPEN =
      new ConfigKey<>(new String[] {"inventory", "open"}, new ConfigIntValue(Input.Keys.I));

  /** Keys for the core-package for close inventory. */
  public static final ConfigKey<Integer> CLOSE_UI =
      new ConfigKey<>(new String[] {"ui", "close"}, new ConfigIntValue(Input.Keys.ESCAPE));

  /** Keys for the core-package for world interaction. */
  public static final ConfigKey<Integer> INTERACT_WORLD =
      new ConfigKey<>(new String[] {"interact", "world"}, new ConfigIntValue(Input.Keys.E));

  /** Keys for the core-package for mouse world interaction. */
  public static final ConfigKey<Integer> MOUSE_INTERACT_WORLD =
      new ConfigKey<>(new String[] {"interact", "mouse"}, new ConfigIntValue(Input.Buttons.LEFT));

  /** Keys for the core-package for use item. */
  public static final ConfigKey<Integer> USE_ITEM =
      new ConfigKey<>(new String[] {"item", "use"}, new ConfigIntValue(Input.Keys.E));

  /** Keys for the core-package for first skill. */
  public static final ConfigKey<Integer> FIRST_SKILL =
      new ConfigKey<>(new String[] {"skill", "first"}, new ConfigIntValue(Input.Keys.Q));

  /** Keys for the core-package for debug zoom in. */
  public static final ConfigKey<Integer> DEBUG_ZOOM_IN =
      new ConfigKey<>(new String[] {"debug", "zoom_in"}, new ConfigIntValue(Input.Keys.K));

  /** Keys for the core-package for debug zoom out. */
  public static final ConfigKey<Integer> DEBUG_ZOOM_OUT =
      new ConfigKey<>(new String[] {"debug", "zoom_out"}, new ConfigIntValue(Input.Keys.L));

  /** Keys for the core-package for debug toggle level size. */
  public static final ConfigKey<Integer> DEBUG_TOGGLE_LEVELSIZE =
      new ConfigKey<>(new String[] {"debug", "toggle_levelsize"}, new ConfigIntValue(Input.Keys.Z));

  /** Keys for the core-package for debug spawn monster. */
  public static final ConfigKey<Integer> DEBUG_SPAWN_MONSTER =
      new ConfigKey<>(new String[] {"debug", "spawn_monster"}, new ConfigIntValue(Input.Keys.X));

  /** Keys for the core-package for debug teleport to start. */
  public static final ConfigKey<Integer> DEBUG_TELEPORT_TO_START =
      new ConfigKey<>(new String[] {"debug", "teleport_Start"}, new ConfigIntValue(Input.Keys.J));

  /** Keys for the core-package for debug teleport to end. */
  public static final ConfigKey<Integer> DEBUG_TELEPORT_TO_END =
      new ConfigKey<>(new String[] {"debug", "teleport_end"}, new ConfigIntValue(Input.Keys.H));

  /** Keys for the core-package for debug teleport to on start. */
  public static final ConfigKey<Integer> DEBUG_TELEPORT_ON_END =
      new ConfigKey<>(new String[] {"debug", "teleport_onEnd"}, new ConfigIntValue(Input.Keys.G));

  /** Keys for the core-package for debug teleport to cursor. */
  public static final ConfigKey<Integer> DEBUG_TELEPORT_TO_CURSOR =
      new ConfigKey<>(new String[] {"debug", "teleport_cursor"}, new ConfigIntValue(Input.Keys.O));
}
