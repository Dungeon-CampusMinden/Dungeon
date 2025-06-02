package client;

import com.badlogic.gdx.Input;

/** Keyboard-configuration for blockly-package. */
@core.configuration.ConfigMap(path = {"keyboard"})
public class KeyboardConfig {
  /** Toggle the visibility of the blockly hud. */
  public static final core.configuration.ConfigKey<Integer> TOGGLE_BLOCKLY_HUD =
      new core.configuration.ConfigKey<>(
          new String[] {"blockly", "toggle_blockly_hud"},
          new core.configuration.values.ConfigIntValue(com.badlogic.gdx.Input.Keys.B));
}
