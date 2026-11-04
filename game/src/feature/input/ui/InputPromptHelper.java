package feature.input.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.XmlReader;
import engine.input.Binding;
import engine.input.Binding.Pad;
import engine.input.Binding.PadAxisId;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Finds the input prompts (keyboard keys, mouse buttons, controller buttons) on the spritesheets
 * located under {@code assets/hud/input/}, whose atlases are read on first use.
 *
 * <p>Keys and mouse buttons live on the keyboard & mouse spritesheet, pad inputs on the controller
 * spritesheets.
 */
public final class InputPromptHelper {

  /** Path of the keyboard & mouse spritesheet inside the assets directory. */
  private static final String KEYBOARD_MOUSE_TEXTURE_PATH = "hud/input/keyboard_mouse.png";

  /** Path of the Xbox spritesheet inside the assets directory. */
  private static final String XBOX_TEXTURE_PATH = "hud/input/xbox.png";

  /** Path of the PlayStation spritesheet inside the assets directory. */
  private static final String PLAYSTATION_TEXTURE_PATH = "hud/input/playstation.png";

  /** Width and height (in pixels) of every region in the spritesheets. */
  private static final int REGION_SIZE = 128;

  /**
   * Maps a libGDX {@link Input.Keys} key code to its base region name (without {@code _outline}).
   */
  private static final Map<Integer, String> KEYBOARD_KEY_NAMES = new HashMap<>();

  /**
   * Maps a libGDX {@link Input.Buttons} mouse button code to its base region name (without {@code
   * _outline}).
   */
  private static final Map<Integer, String> MOUSE_BUTTON_NAMES = new HashMap<>();

  /** Maps a pad binding to its base region name on the Xbox spritesheet. */
  private static final Map<Binding, String> XBOX_PAD_NAMES = new HashMap<>();

  /** Maps a pad binding to its base region name on the PlayStation spritesheet. */
  private static final Map<Binding, String> PLAYSTATION_PAD_NAMES = new HashMap<>();

  /** Maps region names to their {@code [x, y]} positions, per spritesheet. */
  private static final Map<InputMethod, Map<String, int[]>> REGIONS =
      new EnumMap<>(InputMethod.class);

  static {
    initKeyboardMappings();
    initMouseMappings();
    initPadMappings();
  }

  private InputPromptHelper() {}

  /**
   * Describes the location of a single input prompt within a spritesheet.
   *
   * @param texturePath the path of the spritesheet texture inside the assets directory.
   * @param x the x coordinate of the region's top-left corner in pixels.
   * @param y the y coordinate of the region's top-left corner in pixels.
   * @param width the region's width in pixels.
   * @param height the region's height in pixels.
   */
  public record InputPromptRegion(String texturePath, int x, int y, int width, int height) {}

  /**
   * Looks up the spritesheet location for a keyboard key prompt.
   *
   * @param keyCode a libGDX {@link Input.Keys} key code.
   * @return the {@link InputPromptRegion} describing the spritesheet location, or {@code null} if
   *     no mapping exists.
   */
  public static InputPromptRegion lookupKey(int keyCode) {
    return lookupRegion(InputMethod.KEYBOARD, KEYBOARD_KEY_NAMES.get(keyCode));
  }

  /**
   * Looks up the spritesheet location for a mouse button prompt.
   *
   * @param button a libGDX {@link Input.Buttons} button code.
   * @return the {@link InputPromptRegion} describing the spritesheet location, or {@code null} if
   *     no mapping exists.
   */
  public static InputPromptRegion lookupMouseButton(int button) {
    return lookupRegion(InputMethod.KEYBOARD, MOUSE_BUTTON_NAMES.get(button));
  }

  /**
   * Looks up the spritesheet location for a pad button or axis prompt.
   *
   * @param inputMethod the controller look, {@link InputMethod#XBOX} or {@link
   *     InputMethod#PLAYSTATION}.
   * @param binding the pad binding.
   * @return the {@link InputPromptRegion} describing the spritesheet location, or {@code null} if
   *     no mapping exists.
   */
  public static InputPromptRegion lookupPad(InputMethod inputMethod, Binding binding) {
    return lookupRegion(inputMethod, padRegionName(inputMethod, binding));
  }

  /**
   * Names the region of a pad binding.
   *
   * @param inputMethod the controller look
   * @param binding the pad binding
   * @return the base region name, or {@code null} if no mapping exists
   */
  static String padRegionName(InputMethod inputMethod, Binding binding) {
    return (inputMethod == InputMethod.PLAYSTATION ? PLAYSTATION_PAD_NAMES : XBOX_PAD_NAMES)
        .get(binding);
  }

  /**
   * Resolves a base region name to its spritesheet location, preferring its {@code _outline}
   * variant. The sheets have none for a few inputs such as the sticks.
   *
   * @param inputMethod the input method whose spritesheet to look up.
   * @param baseName the base region name (without {@code _outline}), or {@code null}.
   * @return the matching {@link InputPromptRegion}, or {@code null} if no mapping exists.
   */
  private static InputPromptRegion lookupRegion(InputMethod inputMethod, String baseName) {
    if (baseName == null) return null;
    Map<String, int[]> regions = REGIONS.computeIfAbsent(inputMethod, InputPromptHelper::readAtlas);

    String name = baseName + "_outline";
    if (!regions.containsKey(name)) {
      name = baseName;
      if (!regions.containsKey(name)) return null;
    }
    int[] xy = regions.get(name);
    return new InputPromptRegion(
        texturePathFor(inputMethod), xy[0], xy[1], REGION_SIZE, REGION_SIZE);
  }

  private static Map<String, int[]> readAtlas(InputMethod inputMethod) {
    String atlas = texturePathFor(inputMethod).replace(".png", ".xml");
    Map<String, int[]> regions = new HashMap<>();
    for (XmlReader.Element region :
        new XmlReader().parse(Gdx.files.internal(atlas)).getChildrenByName("SubTexture")) {
      regions.put(
          region.getAttribute("name"),
          new int[] {region.getIntAttribute("x"), region.getIntAttribute("y")});
    }
    return regions;
  }

  private static String texturePathFor(InputMethod inputMethod) {
    return switch (inputMethod) {
      case KEYBOARD -> KEYBOARD_MOUSE_TEXTURE_PATH;
      case XBOX -> XBOX_TEXTURE_PATH;
      case PLAYSTATION -> PLAYSTATION_TEXTURE_PATH;
    };
  }

  private static void putPad(Binding binding, String xbox, String playstation) {
    XBOX_PAD_NAMES.put(binding, xbox);
    PLAYSTATION_PAD_NAMES.put(binding, playstation);
  }

  private static void initPadMappings() {
    putPad(Binding.pad(Pad.A), "xbox_button_a", "playstation_button_cross");
    putPad(Binding.pad(Pad.B), "xbox_button_b", "playstation_button_circle");
    putPad(Binding.pad(Pad.X), "xbox_button_x", "playstation_button_square");
    putPad(Binding.pad(Pad.Y), "xbox_button_y", "playstation_button_triangle");
    putPad(Binding.pad(Pad.BACK), "xbox_button_view", "playstation4_button_share");
    putPad(Binding.pad(Pad.START), "xbox_button_menu", "playstation4_button_options");
    putPad(Binding.pad(Pad.LB), "xbox_lb", "playstation_trigger_l1");
    putPad(Binding.pad(Pad.RB), "xbox_rb", "playstation_trigger_r1");
    putPad(Binding.pad(Pad.DPAD_UP), "xbox_dpad_up", "playstation_dpad_up");
    putPad(Binding.pad(Pad.DPAD_DOWN), "xbox_dpad_down", "playstation_dpad_down");
    putPad(Binding.pad(Pad.DPAD_LEFT), "xbox_dpad_left", "playstation_dpad_left");
    putPad(Binding.pad(Pad.DPAD_RIGHT), "xbox_dpad_right", "playstation_dpad_right");
    putPad(Binding.pad(PadAxisId.LT), "xbox_lt", "playstation_trigger_l2");
    putPad(Binding.pad(PadAxisId.RT), "xbox_rt", "playstation_trigger_r2");
    putPad(Binding.pad(PadAxisId.LSTICK), "xbox_stick_l", "playstation_stick_l");
    putPad(Binding.pad(PadAxisId.LSTICK_UP), "xbox_stick_l_up", "playstation_stick_l_up");
    putPad(Binding.pad(PadAxisId.LSTICK_DOWN), "xbox_stick_l_down", "playstation_stick_l_down");
    putPad(Binding.pad(PadAxisId.LSTICK_LEFT), "xbox_stick_l_left", "playstation_stick_l_left");
    putPad(Binding.pad(PadAxisId.LSTICK_RIGHT), "xbox_stick_l_right", "playstation_stick_l_right");
  }

  /** Maps libGDX {@link Input.Keys} key codes to their base region names. */
  private static void initKeyboardMappings() {
    // Digits
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUM_0, "keyboard_0");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUM_1, "keyboard_1");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUM_2, "keyboard_2");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUM_3, "keyboard_3");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUM_4, "keyboard_4");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUM_5, "keyboard_5");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUM_6, "keyboard_6");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUM_7, "keyboard_7");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUM_8, "keyboard_8");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUM_9, "keyboard_9");

    // Numpad digits reuse the digit graphics
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUMPAD_0, "keyboard_0");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUMPAD_1, "keyboard_1");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUMPAD_2, "keyboard_2");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUMPAD_3, "keyboard_3");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUMPAD_4, "keyboard_4");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUMPAD_5, "keyboard_5");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUMPAD_6, "keyboard_6");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUMPAD_7, "keyboard_7");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUMPAD_8, "keyboard_8");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUMPAD_9, "keyboard_9");

    // Letters
    KEYBOARD_KEY_NAMES.put(Input.Keys.A, "keyboard_a");
    KEYBOARD_KEY_NAMES.put(Input.Keys.B, "keyboard_b");
    KEYBOARD_KEY_NAMES.put(Input.Keys.C, "keyboard_c");
    KEYBOARD_KEY_NAMES.put(Input.Keys.D, "keyboard_d");
    KEYBOARD_KEY_NAMES.put(Input.Keys.E, "keyboard_e");
    KEYBOARD_KEY_NAMES.put(Input.Keys.F, "keyboard_f");
    KEYBOARD_KEY_NAMES.put(Input.Keys.G, "keyboard_g");
    KEYBOARD_KEY_NAMES.put(Input.Keys.H, "keyboard_h");
    KEYBOARD_KEY_NAMES.put(Input.Keys.I, "keyboard_i");
    KEYBOARD_KEY_NAMES.put(Input.Keys.J, "keyboard_j");
    KEYBOARD_KEY_NAMES.put(Input.Keys.K, "keyboard_k");
    KEYBOARD_KEY_NAMES.put(Input.Keys.L, "keyboard_l");
    KEYBOARD_KEY_NAMES.put(Input.Keys.M, "keyboard_m");
    KEYBOARD_KEY_NAMES.put(Input.Keys.N, "keyboard_n");
    KEYBOARD_KEY_NAMES.put(Input.Keys.O, "keyboard_o");
    KEYBOARD_KEY_NAMES.put(Input.Keys.P, "keyboard_p");
    KEYBOARD_KEY_NAMES.put(Input.Keys.Q, "keyboard_q");
    KEYBOARD_KEY_NAMES.put(Input.Keys.R, "keyboard_r");
    KEYBOARD_KEY_NAMES.put(Input.Keys.S, "keyboard_s");
    KEYBOARD_KEY_NAMES.put(Input.Keys.T, "keyboard_t");
    KEYBOARD_KEY_NAMES.put(Input.Keys.U, "keyboard_u");
    KEYBOARD_KEY_NAMES.put(Input.Keys.V, "keyboard_v");
    KEYBOARD_KEY_NAMES.put(Input.Keys.W, "keyboard_w");
    KEYBOARD_KEY_NAMES.put(Input.Keys.X, "keyboard_x");
    KEYBOARD_KEY_NAMES.put(Input.Keys.Y, "keyboard_y");
    KEYBOARD_KEY_NAMES.put(Input.Keys.Z, "keyboard_z");

    // Function keys
    KEYBOARD_KEY_NAMES.put(Input.Keys.F1, "keyboard_f1");
    KEYBOARD_KEY_NAMES.put(Input.Keys.F2, "keyboard_f2");
    KEYBOARD_KEY_NAMES.put(Input.Keys.F3, "keyboard_f3");
    KEYBOARD_KEY_NAMES.put(Input.Keys.F4, "keyboard_f4");
    KEYBOARD_KEY_NAMES.put(Input.Keys.F5, "keyboard_f5");
    KEYBOARD_KEY_NAMES.put(Input.Keys.F6, "keyboard_f6");
    KEYBOARD_KEY_NAMES.put(Input.Keys.F7, "keyboard_f7");
    KEYBOARD_KEY_NAMES.put(Input.Keys.F8, "keyboard_f8");
    KEYBOARD_KEY_NAMES.put(Input.Keys.F9, "keyboard_f9");
    KEYBOARD_KEY_NAMES.put(Input.Keys.F10, "keyboard_f10");
    KEYBOARD_KEY_NAMES.put(Input.Keys.F11, "keyboard_f11");
    KEYBOARD_KEY_NAMES.put(Input.Keys.F12, "keyboard_f12");

    // Arrow keys
    KEYBOARD_KEY_NAMES.put(Input.Keys.UP, "keyboard_arrow_up");
    KEYBOARD_KEY_NAMES.put(Input.Keys.DOWN, "keyboard_arrow_down");
    KEYBOARD_KEY_NAMES.put(Input.Keys.LEFT, "keyboard_arrow_left");
    KEYBOARD_KEY_NAMES.put(Input.Keys.RIGHT, "keyboard_arrow_right");

    // Modifiers
    KEYBOARD_KEY_NAMES.put(Input.Keys.SHIFT_LEFT, "keyboard_shift");
    KEYBOARD_KEY_NAMES.put(Input.Keys.SHIFT_RIGHT, "keyboard_shift");
    KEYBOARD_KEY_NAMES.put(Input.Keys.CONTROL_LEFT, "keyboard_ctrl");
    KEYBOARD_KEY_NAMES.put(Input.Keys.CONTROL_RIGHT, "keyboard_ctrl");
    KEYBOARD_KEY_NAMES.put(Input.Keys.ALT_LEFT, "keyboard_alt");
    KEYBOARD_KEY_NAMES.put(Input.Keys.ALT_RIGHT, "keyboard_alt");

    // Whitespace / control
    KEYBOARD_KEY_NAMES.put(Input.Keys.SPACE, "keyboard_space");
    KEYBOARD_KEY_NAMES.put(Input.Keys.TAB, "keyboard_tab");
    KEYBOARD_KEY_NAMES.put(Input.Keys.ENTER, "keyboard_enter");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUMPAD_ENTER, "keyboard_numpad_enter");
    KEYBOARD_KEY_NAMES.put(Input.Keys.ESCAPE, "keyboard_escape");
    KEYBOARD_KEY_NAMES.put(Input.Keys.BACKSPACE, "keyboard_backspace");
    KEYBOARD_KEY_NAMES.put(Input.Keys.FORWARD_DEL, "keyboard_delete");
    KEYBOARD_KEY_NAMES.put(Input.Keys.INSERT, "keyboard_insert");
    KEYBOARD_KEY_NAMES.put(Input.Keys.HOME, "keyboard_home");
    KEYBOARD_KEY_NAMES.put(Input.Keys.END, "keyboard_end");
    KEYBOARD_KEY_NAMES.put(Input.Keys.PAGE_UP, "keyboard_page_up");
    KEYBOARD_KEY_NAMES.put(Input.Keys.PAGE_DOWN, "keyboard_page_down");
    KEYBOARD_KEY_NAMES.put(Input.Keys.CAPS_LOCK, "keyboard_capslock");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUM_LOCK, "keyboard_numlock");
    KEYBOARD_KEY_NAMES.put(Input.Keys.PRINT_SCREEN, "keyboard_printscreen");

    // Punctuation / symbols
    KEYBOARD_KEY_NAMES.put(Input.Keys.APOSTROPHE, "keyboard_apostrophe");
    KEYBOARD_KEY_NAMES.put(Input.Keys.COMMA, "keyboard_comma");
    KEYBOARD_KEY_NAMES.put(Input.Keys.PERIOD, "keyboard_period");
    KEYBOARD_KEY_NAMES.put(Input.Keys.MINUS, "keyboard_minus");
    KEYBOARD_KEY_NAMES.put(Input.Keys.PLUS, "keyboard_plus");
    KEYBOARD_KEY_NAMES.put(Input.Keys.EQUALS, "keyboard_equals");
    KEYBOARD_KEY_NAMES.put(Input.Keys.SEMICOLON, "keyboard_semicolon");
    KEYBOARD_KEY_NAMES.put(Input.Keys.SLASH, "keyboard_slash_forward");
    KEYBOARD_KEY_NAMES.put(Input.Keys.BACKSLASH, "keyboard_slash_back");
    KEYBOARD_KEY_NAMES.put(Input.Keys.LEFT_BRACKET, "keyboard_bracket_open");
    KEYBOARD_KEY_NAMES.put(Input.Keys.RIGHT_BRACKET, "keyboard_bracket_close");
    KEYBOARD_KEY_NAMES.put(Input.Keys.GRAVE, "keyboard_tilde");
    KEYBOARD_KEY_NAMES.put(Input.Keys.STAR, "keyboard_asterisk");
    KEYBOARD_KEY_NAMES.put(Input.Keys.NUMPAD_ADD, "keyboard_numpad_plus");
  }

  /** Maps libGDX {@link Input.Buttons} mouse button codes to their base region names. */
  private static void initMouseMappings() {
    MOUSE_BUTTON_NAMES.put(Input.Buttons.LEFT, "mouse_left");
    MOUSE_BUTTON_NAMES.put(Input.Buttons.RIGHT, "mouse_right");
    MOUSE_BUTTON_NAMES.put(Input.Buttons.MIDDLE, "mouse_scroll");
  }
}
