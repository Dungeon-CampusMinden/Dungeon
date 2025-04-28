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

  /** Step through the pathfinding algorithm. */
  public static final core.configuration.ConfigKey<Integer> STEP_PATHFINDING =
      new core.configuration.ConfigKey<>(
          new String[] {"pathfinding", "step_pathfinding"},
          new core.configuration.values.ConfigIntValue(com.badlogic.gdx.Input.Keys.B));

  /** Start moving the hero along the path while inside the pathfinding level. */
  public static final core.configuration.ConfigKey<Integer> START_MOVING_PATHFINDING =
      new core.configuration.ConfigKey<>(
          new String[] {"pathfinding", "start_moving_pathfinding"},
          new core.configuration.values.ConfigIntValue(Input.Keys.SPACE));
}
