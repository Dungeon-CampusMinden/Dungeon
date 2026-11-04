package engine.input;

import engine.utils.Vector2;

/** Actions and contexts of the game itself. */
public final class CoreActions {
  /** Analog movement. Digital movement comes from the {@code MOVE_*} actions. */
  public static final ActionId MOVE = new ActionId("core", "move");

  /** Move up. */
  public static final ActionId MOVE_UP = new ActionId("core", "move_up");

  /** Move down. */
  public static final ActionId MOVE_DOWN = new ActionId("core", "move_down");

  /** Move left. */
  public static final ActionId MOVE_LEFT = new ActionId("core", "move_left");

  /** Move right. */
  public static final ActionId MOVE_RIGHT = new ActionId("core", "move_right");

  /** Uses the main skill while held. */
  public static final ActionId SKILL_MAIN = new ActionId("core", "skill_main");

  /** Uses the second skill while held. */
  public static final ActionId SKILL_SECOND = new ActionId("core", "skill_second");

  /** Selects the next main skill. */
  public static final ActionId SKILL_MAIN_NEXT = new ActionId("core", "skill_main_next");

  /** Selects the previous main skill. */
  public static final ActionId SKILL_MAIN_PREV = new ActionId("core", "skill_main_prev");

  /** Selects the next second skill. */
  public static final ActionId SKILL_SECOND_NEXT = new ActionId("core", "skill_second_next");

  /** Selects the previous second skill. */
  public static final ActionId SKILL_SECOND_PREV = new ActionId("core", "skill_second_prev");

  /** Interacts with the world at pointer 0. */
  public static final ActionId INTERACT = new ActionId("core", "interact");

  /** Opens the inventory; closes it as well inside the inventory context. */
  public static final ActionId INVENTORY = new ActionId("core", "inventory");

  /** Opens the pause menu. */
  public static final ActionId PAUSE_MENU = new ActionId("core", "pause_menu");

  /** Opens the quest log; closes it as well inside the quest log context. */
  public static final ActionId QUESTLOG = new ActionId("core", "questlog");

  /** Shows the room's controls. */
  public static final ActionId SHOW_CONTROLS = new ActionId("core", "show_controls");

  /** Toggles fullscreen. */
  public static final ActionId FULLSCREEN = new ActionId("core", "fullscreen");

  /** Everything a hero can do. */
  public static final ContextSpec GAMEPLAY =
      ContextSpec.of(
          "gameplay",
          ContextLayer.BASE,
          false,
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
      ContextSpec.of("system", ContextLayer.SYSTEM, false, FULLSCREEN);

  /** Mirror of a server-side control lock. */
  public static final ContextSpec SERVER_LOCK =
      ContextSpec.of("server_lock", ContextLayer.SERVER_LOCK, true);

  /** A focused text field; characters, Enter and Escape stay with the widget. */
  public static final ContextSpec TEXT = ContextSpec.of("text", ContextLayer.TEXT, true);

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
    return InputResolver.clampLength(digital.add(GameInput.vector(MOVE)));
  }

  static void define(BindingTable table) {
    table.define(MOVE, "pad:LSTICK");
    table.define(MOVE_UP, "key:W", "pad:DPAD_UP");
    table.define(MOVE_DOWN, "key:S", "pad:DPAD_DOWN");
    table.define(MOVE_LEFT, "key:A", "pad:DPAD_LEFT");
    table.define(MOVE_RIGHT, "key:D", "pad:DPAD_RIGHT");
    table.define(SKILL_MAIN, "key:Q", "pad:RT");
    table.define(SKILL_SECOND, "key:R", "pad:LT");
    table.define(SKILL_MAIN_NEXT, "key:RIGHT", "pad:RB");
    table.define(SKILL_MAIN_PREV, "key:LEFT", "pad:LB");
    table.define(SKILL_SECOND_NEXT, "key:UP");
    table.define(SKILL_SECOND_PREV, "key:DOWN");
    table.define(INTERACT, "mouse:LEFT", "key:E", "pad:A");
    table.define(INVENTORY, "key:TAB", "key:I", "pad:Y");
    table.define(PAUSE_MENU, "key:ESCAPE", "pad:START");
    table.define(QUESTLOG, "key:B", "pad:BACK");
    table.define(SHOW_CONTROLS, "key:F1");
    table.define(FULLSCREEN, "key:F11");
    table.context(GAMEPLAY);
    table.context(SYSTEM);
    table.context(SERVER_LOCK);
    table.context(TEXT);
  }
}
