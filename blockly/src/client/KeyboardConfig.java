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

  /** Switch to DFS algorithm. */
  public static final core.configuration.ConfigKey<Integer> SELECT_DFS =
      new core.configuration.ConfigKey<>(
          new String[] {"pathfinding", "select_dfs"},
          new core.configuration.values.ConfigIntValue(Input.Keys.J));

  /** Switch to BFS algorithm. */
  public static final core.configuration.ConfigKey<Integer> SELECT_BFS =
      new core.configuration.ConfigKey<>(
          new String[] {"pathfinding", "select_bfs"},
          new core.configuration.values.ConfigIntValue(Input.Keys.K));

  /** Switch to the algorithm that the students will implement. */
  public static final core.configuration.ConfigKey<Integer> SELECT_SUS_ALGO =
      new core.configuration.ConfigKey<>(
          new String[] {"pathfinding", "select_sus_algo"},
          new core.configuration.values.ConfigIntValue(Input.Keys.L));
}
