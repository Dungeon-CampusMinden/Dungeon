package feature.leveleditor;

import static engine.input.Binding.key;
import static engine.input.Binding.mouse;
import static engine.input.Binding.wheel;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import engine.input.ActionId;
import engine.input.Binding;
import engine.input.ContextLayer;
import engine.input.ContextSpec;
import engine.input.UiActions;

/** Actions and contexts of the level editor. */
public final class EditorActions {
  /** Toggles the editor, or its option in the main menu. */
  public static final ActionId TOGGLE = editor("toggle", key(Keys.F4));

  /** Moves the camera up. */
  public static final ActionId CAMERA_UP = editor("camera_up", key(Keys.W));

  /** Moves the camera down. */
  public static final ActionId CAMERA_DOWN = editor("camera_down", key(Keys.S));

  /** Moves the camera left. */
  public static final ActionId CAMERA_LEFT = editor("camera_left", key(Keys.A));

  /** Moves the camera right. */
  public static final ActionId CAMERA_RIGHT = editor("camera_right", key(Keys.D));

  /** Moves the camera faster while held. */
  public static final ActionId CAMERA_FAST =
      editor("camera_fast", key(Keys.SHIFT_LEFT), key(Keys.SHIFT_RIGHT));

  /** Zooms the camera; a one-dimensional axis. */
  public static final ActionId ZOOM = editor("zoom", wheel());

  /** Toggles the debug shader. */
  public static final ActionId DEBUG_SHADER = editor("debug_shader", key(Keys.SPACE));

  /** Primary mode value up. */
  public static final ActionId PRIMARY_NEXT = editor("primary_next", key(Keys.E));

  /** Primary mode value down. */
  public static final ActionId PRIMARY_PREV = editor("primary_prev", key(Keys.Q));

  /** Secondary mode value up. */
  public static final ActionId SECONDARY_NEXT = editor("secondary_next", key(Keys.C));

  /** Secondary mode value down. */
  public static final ActionId SECONDARY_PREV = editor("secondary_prev", key(Keys.Z));

  /** Third mode action. */
  public static final ActionId TERTIARY = editor("tertiary", key(Keys.X));

  /** Picks what lies under the cursor. */
  public static final ActionId PICK = editor("pick", key(Keys.V), mouse(Buttons.MIDDLE));

  /** Places at the cursor. */
  public static final ActionId PLACE = editor("place", mouse(Buttons.LEFT));

  /** Alternative action at the cursor. */
  public static final ActionId ALT = editor("alt", mouse(Buttons.RIGHT));

  /** Editor modes 1 to 6, in order. */
  public static final ActionId[] MODES = {
    editor("mode_1", key(Keys.NUM_1)),
    editor("mode_2", key(Keys.NUM_2)),
    editor("mode_3", key(Keys.NUM_3)),
    editor("mode_4", key(Keys.NUM_4)),
    editor("mode_5", key(Keys.NUM_5)),
    editor("mode_6", key(Keys.NUM_6))
  };

  /** The main menu, which offers the editor. */
  public static final ContextSpec MENU = ContextSpec.of("menu", ContextLayer.BASE, TOGGLE);

  /** A running game with the editor available. */
  public static final ContextSpec EDITOR_TOGGLE =
      ContextSpec.of("editor_toggle", ContextLayer.EDITOR_TOGGLE, TOGGLE);

  /** The active editor. It blocks gameplay, so WASD moves its camera and not the hero. */
  public static final ContextSpec EDITOR =
      ContextSpec.of(
              "editor",
              ContextLayer.EDITOR,
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
              MODES[5])
          .blocking();

  /** A popup of the editor. */
  public static final ContextSpec EDITOR_POPUP =
      ContextSpec.of("editor_popup", ContextLayer.EDITOR_POPUP, UiActions.CLOSE).blocking();

  private EditorActions() {}

  private static ActionId editor(String name, Binding... defaults) {
    return new ActionId("editor", name, defaults);
  }
}
