package feature.components;

import engine.input.ActionId;
import engine.input.BindingTable;
import engine.input.ContextLayer;
import engine.input.ContextSpec;

/** Actions and the context of the debugger. */
public final class DebugActions {
  /** Toggles the debug HUD; F3 fires on release because it is also a combo prefix. */
  public static final ActionId TOGGLE_HUD = new ActionId("debug", "toggle_hud");

  /** Toggles the system list. */
  public static final ActionId TOGGLE_SYSTEM_LIST = new ActionId("debug", "toggle_system_list");

  /** Toggles the network telemetry. */
  public static final ActionId TOGGLE_NETWORK_TELEMETRY =
      new ActionId("debug", "toggle_network_telemetry");

  /** Toggles the scene2d debug lines. */
  public static final ActionId TOGGLE_SCENE_HUD = new ActionId("debug", "toggle_scene_hud");

  /** Zooms in. */
  public static final ActionId ZOOM_IN = new ActionId("debug", "zoom_in");

  /** Zooms out. */
  public static final ActionId ZOOM_OUT = new ActionId("debug", "zoom_out");

  /** Teleports the hero to the cursor. */
  public static final ActionId TELEPORT = new ActionId("debug", "teleport");

  /** Spawns a monster at the cursor. */
  public static final ActionId SPAWN_MONSTER = new ActionId("debug", "spawn_monster");

  /** Opens all doors. */
  public static final ActionId OPEN_DOORS = new ActionId("debug", "open_doors");

  /** Copies the network telemetry. */
  public static final ActionId COPY_TELEMETRY = new ActionId("debug", "copy_telemetry");

  /** Shows all components of hovered entities while held. */
  public static final ActionId SHOW_COMPONENTS = new ActionId("debug", "show_components");

  /** Raises the multi-purpose debug value. */
  public static final ActionId VALUE_UP = new ActionId("debug", "value_up");

  /** Lowers the multi-purpose debug value. */
  public static final ActionId VALUE_DOWN = new ActionId("debug", "value_down");

  /** Runs the registered debug actions. */
  public static final ActionId ACTION = new ActionId("debug", "action");

  /** Pauses the game. */
  public static final ActionId PAUSE = new ActionId("debug", "pause");

  /** Advances one frame while paused. */
  public static final ActionId ADVANCE_FRAME = new ActionId("debug", "advance_frame");

  /** Lies above editor and dialogs and below text fields, so typing triggers nothing. */
  public static final ContextSpec DEBUG =
      ContextSpec.of(
          "debug",
          ContextLayer.DEBUG,
          false,
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

  /**
   * Defines the debug actions.
   *
   * @param table the binding table of this process
   */
  public static void define(BindingTable table) {
    table.define(TOGGLE_HUD, "key:F3");
    table.define(TOGGLE_SYSTEM_LIST, "key:F3+A");
    table.define(TOGGLE_NETWORK_TELEMETRY, "key:F3+N");
    table.define(TOGGLE_SCENE_HUD, "key:F5");
    table.define(ZOOM_IN, "key:K");
    table.define(ZOOM_OUT, "key:L");
    table.define(TELEPORT, "key:O");
    table.define(SPAWN_MONSTER, "key:F3+X");
    table.define(OPEN_DOORS, "key:F3+C");
    table.define(COPY_TELEMETRY, "key:Ctrl+C");
    table.define(SHOW_COMPONENTS, "key:F3+SHIFT_LEFT", "key:F3+SHIFT_RIGHT");
    table.define(VALUE_UP, "key:F3+UP");
    table.define(VALUE_DOWN, "key:F3+DOWN");
    table.define(ACTION, "key:NUMPAD_1");
    table.define(PAUSE, "key:F3+P");
    table.define(ADVANCE_FRAME, "key:M");
    table.context(DEBUG);
  }
}
