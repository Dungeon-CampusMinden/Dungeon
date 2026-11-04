package feature.leveleditor;

import engine.input.ActionId;
import engine.input.BindingTable;
import engine.input.ContextLayer;
import engine.input.ContextSpec;
import engine.input.UiActions;

/** Actions and contexts of the level editor. */
public final class EditorActions {
  /** Toggles the editor, or its option in the main menu. */
  public static final ActionId TOGGLE = new ActionId("editor", "toggle");

  /** Moves the camera up. */
  public static final ActionId CAMERA_UP = new ActionId("editor", "camera_up");

  /** Moves the camera down. */
  public static final ActionId CAMERA_DOWN = new ActionId("editor", "camera_down");

  /** Moves the camera left. */
  public static final ActionId CAMERA_LEFT = new ActionId("editor", "camera_left");

  /** Moves the camera right. */
  public static final ActionId CAMERA_RIGHT = new ActionId("editor", "camera_right");

  /** Moves the camera faster while held. */
  public static final ActionId CAMERA_FAST = new ActionId("editor", "camera_fast");

  /** Zooms the camera; a one-dimensional axis. */
  public static final ActionId ZOOM = new ActionId("editor", "zoom");

  /** Toggles the debug shader. */
  public static final ActionId DEBUG_SHADER = new ActionId("editor", "debug_shader");

  /** Primary mode value up. */
  public static final ActionId PRIMARY_NEXT = new ActionId("editor", "primary_next");

  /** Primary mode value down. */
  public static final ActionId PRIMARY_PREV = new ActionId("editor", "primary_prev");

  /** Secondary mode value up. */
  public static final ActionId SECONDARY_NEXT = new ActionId("editor", "secondary_next");

  /** Secondary mode value down. */
  public static final ActionId SECONDARY_PREV = new ActionId("editor", "secondary_prev");

  /** Third mode action. */
  public static final ActionId TERTIARY = new ActionId("editor", "tertiary");

  /** Picks what lies under the cursor. */
  public static final ActionId PICK = new ActionId("editor", "pick");

  /** Places at the cursor. */
  public static final ActionId PLACE = new ActionId("editor", "place");

  /** Alternative action at the cursor. */
  public static final ActionId ALT = new ActionId("editor", "alt");

  /** Editor modes 1 to 6, in order. */
  public static final ActionId[] MODES = {
    new ActionId("editor", "mode_1"),
    new ActionId("editor", "mode_2"),
    new ActionId("editor", "mode_3"),
    new ActionId("editor", "mode_4"),
    new ActionId("editor", "mode_5"),
    new ActionId("editor", "mode_6")
  };

  /** The main menu, which offers the editor. */
  public static final ContextSpec MENU = ContextSpec.of("menu", ContextLayer.BASE, false, TOGGLE);

  /** A running game with the editor available. */
  public static final ContextSpec EDITOR_TOGGLE =
      ContextSpec.of("editor_toggle", ContextLayer.EDITOR_TOGGLE, false, TOGGLE);

  /** The active editor. It blocks gameplay, so WASD moves its camera and not the hero. */
  public static final ContextSpec EDITOR =
      ContextSpec.of(
          "editor",
          ContextLayer.EDITOR,
          true,
          TOGGLE,
          CAMERA_UP,
          CAMERA_DOWN,
          CAMERA_LEFT,
          CAMERA_RIGHT,
          CAMERA_FAST,
          ZOOM,
          DEBUG_SHADER,
          PRIMARY_NEXT,
          PRIMARY_PREV,
          SECONDARY_NEXT,
          SECONDARY_PREV,
          TERTIARY,
          PICK,
          PLACE,
          ALT,
          MODES[0],
          MODES[1],
          MODES[2],
          MODES[3],
          MODES[4],
          MODES[5]);

  /** A popup of the editor. */
  public static final ContextSpec EDITOR_POPUP =
      ContextSpec.of("editor_popup", ContextLayer.EDITOR_POPUP, true, UiActions.CLOSE);

  private EditorActions() {}

  /**
   * Defines the editor actions.
   *
   * @param table the binding table of this process
   */
  public static void define(BindingTable table) {
    table.define(TOGGLE, "key:F4");
    table.define(CAMERA_UP, "key:W");
    table.define(CAMERA_DOWN, "key:S");
    table.define(CAMERA_LEFT, "key:A");
    table.define(CAMERA_RIGHT, "key:D");
    table.define(CAMERA_FAST, "key:SHIFT_LEFT", "key:SHIFT_RIGHT");
    table.define(ZOOM, "mouse:WHEEL");
    table.define(DEBUG_SHADER, "key:SPACE");
    table.define(PRIMARY_NEXT, "key:E");
    table.define(PRIMARY_PREV, "key:Q");
    table.define(SECONDARY_NEXT, "key:C");
    table.define(SECONDARY_PREV, "key:Z");
    table.define(TERTIARY, "key:X");
    table.define(PICK, "key:V", "mouse:MIDDLE");
    table.define(PLACE, "mouse:LEFT");
    table.define(ALT, "mouse:RIGHT");
    for (int i = 0; i < MODES.length; i++) table.define(MODES[i], "key:NUM_" + (i + 1));
    table.context(MENU);
    table.context(EDITOR_TOGGLE);
    table.context(EDITOR);
    table.context(EDITOR_POPUP);
  }
}
