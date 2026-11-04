package engine.input;

import static engine.input.Binding.key;
import static engine.input.Binding.mouse;
import static engine.input.Binding.pad;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import engine.input.Binding.Pad;
import engine.input.Binding.PadAxisId;
import engine.utils.Vector2;
import java.util.List;

/** Actions and contexts of the game itself. */
public final class CoreActions {
  /** Analog movement on the stick. Keys move through the {@code MOVE_*} actions. */
  public static final ActionId MOVE = core("move", pad(PadAxisId.LSTICK));

  /** Move up. */
  public static final ActionId MOVE_UP = core("move_up", key(Keys.W));

  /** Move down. */
  public static final ActionId MOVE_DOWN = core("move_down", key(Keys.S));

  /** Move left. */
  public static final ActionId MOVE_LEFT = core("move_left", key(Keys.A));

  /** Move right. */
  public static final ActionId MOVE_RIGHT = core("move_right", key(Keys.D));

  /** The key movement actions, in the order prompts show them. */
  public static final List<ActionId> MOVE_KEYS = List.of(MOVE_UP, MOVE_LEFT, MOVE_DOWN, MOVE_RIGHT);

  /** Uses the main skill while held. */
  public static final ActionId SKILL_MAIN = core("skill_main", key(Keys.Q), pad(PadAxisId.RT));

  /** Uses the second skill while held. */
  public static final ActionId SKILL_SECOND = core("skill_second", key(Keys.R), pad(PadAxisId.LT));

  /** Selects the next main skill. */
  public static final ActionId SKILL_MAIN_NEXT =
      core("skill_main_next", key(Keys.RIGHT), pad(Pad.RB));

  /** Selects the previous main skill. */
  public static final ActionId SKILL_MAIN_PREV =
      core("skill_main_prev", key(Keys.LEFT), pad(Pad.LB));

  /** Selects the next second skill. */
  public static final ActionId SKILL_SECOND_NEXT = core("skill_second_next", key(Keys.UP));

  /** Selects the previous second skill. */
  public static final ActionId SKILL_SECOND_PREV = core("skill_second_prev", key(Keys.DOWN));

  /** Interacts with the world at pointer 0. */
  public static final ActionId INTERACT =
      core("interact", mouse(Buttons.LEFT), key(Keys.E), pad(Pad.A));

  /** Opens the inventory; closes it as well inside the inventory context. */
  public static final ActionId INVENTORY =
      core("inventory", key(Keys.TAB), key(Keys.I), pad(Pad.Y));

  /** Opens the pause menu. */
  public static final ActionId PAUSE_MENU = core("pause_menu", key(Keys.ESCAPE), pad(Pad.START));

  /** Opens the quest log; closes it as well inside the quest log context. */
  public static final ActionId QUESTLOG = core("questlog", key(Keys.B), pad(Pad.BACK));

  /** Shows the room's controls. */
  public static final ActionId SHOW_CONTROLS = core("show_controls", key(Keys.F1));

  /** Toggles fullscreen. */
  public static final ActionId FULLSCREEN = core("fullscreen", key(Keys.F11));

  /** Everything a hero can do. */
  public static final ContextSpec GAMEPLAY =
      ContextSpec.of(
          "gameplay",
          ContextLayer.BASE,
          MOVE,
          MOVE_UP,
          MOVE_DOWN,
          MOVE_LEFT,
          MOVE_RIGHT,
          SKILL_MAIN,
          SKILL_SECOND,
          SKILL_MAIN_NEXT,
          SKILL_MAIN_PREV,
          SKILL_SECOND_NEXT,
          SKILL_SECOND_PREV,
          INTERACT,
          INVENTORY,
          PAUSE_MENU,
          QUESTLOG,
          SHOW_CONTROLS);

  /** Actions that work everywhere. */
  public static final ContextSpec SYSTEM =
      ContextSpec.of("system", ContextLayer.SYSTEM, FULLSCREEN);

  /** Mirror of a server-side control lock. */
  public static final ContextSpec SERVER_LOCK =
      ContextSpec.of("server_lock", ContextLayer.SERVER_LOCK).blocking();

  /** A focused text field; characters, Enter and Escape stay with the widget. */
  public static final ContextSpec TEXT = ContextSpec.of("text", ContextLayer.TEXT).blocking();

  private CoreActions() {}

  /**
   * Reads the movement the player asks for.
   *
   * @return the movement vector of length at most 1; keys give a normalized vector, the stick its
   *     analog value
   */
  public static Vector2 move() {
    int x = (GameInput.pressed(MOVE_RIGHT) ? 1 : 0) - (GameInput.pressed(MOVE_LEFT) ? 1 : 0);
    int y = (GameInput.pressed(MOVE_UP) ? 1 : 0) - (GameInput.pressed(MOVE_DOWN) ? 1 : 0);
    Vector2 digital = Vector2.of(x, y).normalize();
    return digital.add(GameInput.vector(MOVE)).clampLength(1);
  }

  private static ActionId core(String name, Binding... defaults) {
    return new ActionId("core", name, defaults);
  }
}
