package contrib.configuration;

import com.badlogic.gdx.Input;
import core.configuration.ConfigKey;
import core.configuration.ConfigMap;
import core.configuration.values.ConfigIntValue;

/** WTF? . */
@ConfigMap(path = {"keyboard"})
public class KeyboardConfig {
  /** WTF? . */
  public static final ConfigKey<Integer> INVENTORY_OPEN =
      new ConfigKey<>(new String[] {"inventory", "open"}, new ConfigIntValue(Input.Keys.I));

  /** WTF? . */
  public static final ConfigKey<Integer> CLOSE_UI =
      new ConfigKey<>(new String[] {"ui", "close"}, new ConfigIntValue(Input.Keys.ESCAPE));

  /** WTF? . */
  public static final ConfigKey<Integer> INTERACT_WORLD =
      new ConfigKey<>(new String[] {"interact", "world"}, new ConfigIntValue(Input.Keys.E));

  /** WTF? . */
  public static final ConfigKey<Integer> MOUSE_INTERACT_WORLD =
      new ConfigKey<>(
          new String[] {"interact", "mouse_world"}, new ConfigIntValue(Input.Buttons.LEFT));

  /** WTF? . */
  public static final ConfigKey<Integer> USE_ITEM =
      new ConfigKey<>(new String[] {"item", "use"}, new ConfigIntValue(Input.Keys.E));

  public static final ConfigKey<Integer> TRANSFER_ITEM =
      new ConfigKey<>(
          new String[] {"inventory", "transfer"}, new ConfigIntValue(Input.Buttons.RIGHT));

  /** WTF? . */
  public static final ConfigKey<Integer> FIRST_SKILL =
      new ConfigKey<>(new String[] {"skill", "fireball"}, new ConfigIntValue(Input.Keys.Q));

  public static final ConfigKey<Integer> MOUSE_FIRST_SKILL =
      new ConfigKey<>(
          new String[] {"skill", "mouse_fireball"}, new ConfigIntValue(Input.Buttons.LEFT));

  /** WTF? . */
  public static final ConfigKey<Integer> DEBUG_ZOOM_IN =
      new ConfigKey<>(new String[] {"debug", "zoom_in"}, new ConfigIntValue(Input.Keys.K));

  /** WTF? . */
  public static final ConfigKey<Integer> DEBUG_ZOOM_OUT =
      new ConfigKey<>(new String[] {"debug", "zoom_out"}, new ConfigIntValue(Input.Keys.L));

  /** WTF? . */
  public static final ConfigKey<Integer> DEBUG_TOGGLE_LEVELSIZE =
      new ConfigKey<>(new String[] {"debug", "toggle_levelsize"}, new ConfigIntValue(Input.Keys.Z));

  /** WTF? . */
  public static final ConfigKey<Integer> DEBUG_SPAWN_MONSTER =
      new ConfigKey<>(new String[] {"debug", "spawn_monster"}, new ConfigIntValue(Input.Keys.X));

  /** WTF? . */
  public static final ConfigKey<Integer> DEBUG_TELEPORT_TO_START =
      new ConfigKey<>(new String[] {"debug", "teleport_Start"}, new ConfigIntValue(Input.Keys.J));

  /** WTF? . */
  public static final ConfigKey<Integer> DEBUG_TELEPORT_TO_END =
      new ConfigKey<>(new String[] {"debug", "teleport_end"}, new ConfigIntValue(Input.Keys.H));

  /** WTF? . */
  public static final ConfigKey<Integer> DEBUG_TELEPORT_ON_END =
      new ConfigKey<>(new String[] {"debug", "teleport_onEnd"}, new ConfigIntValue(Input.Keys.G));

  /** WTF? . */
  public static final ConfigKey<Integer> DEBUG_TELEPORT_TO_CURSOR =
      new ConfigKey<>(new String[] {"debug", "teleport_cursor"}, new ConfigIntValue(Input.Keys.O));
}
