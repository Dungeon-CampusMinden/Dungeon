package feature.components;

import static engine.input.Binding.CTRL;
import static engine.input.Binding.key;

import com.badlogic.gdx.Input.Keys;
import engine.input.ActionId;
import engine.input.Binding;
import engine.input.ContextLayer;
import engine.input.ContextSpec;

/** Actions and the context of the debugger. */
public final class DebugActions {
  /** Toggles the debug HUD; F3 fires on release because it is also a combo prefix. */
  public static final ActionId TOGGLE_HUD = debug("toggle_hud", key(Keys.F3));

  /** Toggles the system list. */
  public static final ActionId TOGGLE_SYSTEM_LIST =
      debug("toggle_system_list", key(Keys.F3, Keys.A));

  /** Toggles the network telemetry. */
  public static final ActionId TOGGLE_NETWORK_TELEMETRY =
      debug("toggle_network_telemetry", key(Keys.F3, Keys.N));

  /** Toggles the scene2d debug lines. */
  public static final ActionId TOGGLE_SCENE_HUD = debug("toggle_scene_hud", key(Keys.F5));

  /** Zooms in. */
  public static final ActionId ZOOM_IN = debug("zoom_in", key(Keys.K));

  /** Zooms out. */
  public static final ActionId ZOOM_OUT = debug("zoom_out", key(Keys.L));

  /** Teleports the hero to the cursor. */
  public static final ActionId TELEPORT = debug("teleport", key(Keys.O));

  /** Spawns a monster at the cursor. */
  public static final ActionId SPAWN_MONSTER = debug("spawn_monster", key(Keys.F3, Keys.X));

  /** Opens all doors. */
  public static final ActionId OPEN_DOORS = debug("open_doors", key(Keys.F3, Keys.C));

  /** Copies the network telemetry. */
  public static final ActionId COPY_TELEMETRY = debug("copy_telemetry", key(CTRL, Keys.C));

  /** Shows all components of hovered entities while held. */
  public static final ActionId SHOW_COMPONENTS =
      debug("show_components", key(Keys.F3, Keys.SHIFT_LEFT), key(Keys.F3, Keys.SHIFT_RIGHT));

  /** Raises the multi-purpose debug value. */
  public static final ActionId VALUE_UP = debug("value_up", key(Keys.F3, Keys.UP));

  /** Lowers the multi-purpose debug value. */
  public static final ActionId VALUE_DOWN = debug("value_down", key(Keys.F3, Keys.DOWN));

  /** Runs the registered debug actions. */
  public static final ActionId ACTION = debug("action", key(Keys.NUMPAD_1));

  /** Pauses the game. */
  public static final ActionId PAUSE = debug("pause", key(Keys.F3, Keys.P));

  /** Advances one frame while paused. */
  public static final ActionId ADVANCE_FRAME = debug("advance_frame", key(Keys.M));

  /** Lies above editor and dialogs and below text fields, so typing triggers nothing. */
  public static final ContextSpec DEBUG =
      ContextSpec.of(
          "debug",
          ContextLayer.DEBUG,
          TOGGLE_HUD,
          TOGGLE_SYSTEM_LIST,
          TOGGLE_NETWORK_TELEMETRY,
          TOGGLE_SCENE_HUD,
          ZOOM_IN,
          ZOOM_OUT,
          TELEPORT,
          SPAWN_MONSTER,
          OPEN_DOORS,
          COPY_TELEMETRY,
          SHOW_COMPONENTS,
          VALUE_UP,
          VALUE_DOWN,
          ACTION,
          PAUSE,
          ADVANCE_FRAME);

  private DebugActions() {}

  private static ActionId debug(String name, Binding... defaults) {
    return new ActionId("debug", name, defaults);
  }
}
