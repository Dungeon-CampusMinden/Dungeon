package contrib.hud.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.SimpleIPath;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper for retrieving {@link TextureRegion}s that visually represent input prompts (keyboard
 * keys, mouse buttons, controller buttons, ...).
 *
 * <p>The class wraps the spritesheets located under {@code assets/hud/input/}. The underlying
 * {@link Texture} for each spritesheet is loaded lazily through {@link TextureMap#instance()} on
 * the first request and is kept alive for the lifetime of the application; this helper does not own
 * the texture and therefore does not need to dispose of it.
 *
 * <p>Currently only the {@link InputMethod#KEYBOARD} (keyboard & mouse) spritesheet is wired up.
 * Calls for other input methods will throw {@link UnsupportedOperationException} until support for
 * them is added.
 *
 * <p>Mouse button look-ups use the dedicated {@link #getMouseButton(int)} family of overloads to
 * avoid collisions with the keyboard key code space. Mouse buttons are always rendered using the
 * keyboard & mouse spritesheet regardless of the currently active input method.
 */
public final class InputPromptHelper {

  /** Path of the keyboard & mouse spritesheet inside the assets directory. */
  private static final String KEYBOARD_MOUSE_TEXTURE_PATH = "hud/input/keyboard_mouse.png";

  /** Width and height (in pixels) of every region in the keyboard / mouse spritesheet. */
  private static final int REGION_SIZE = 128;

  /** Currently active input method. Defaults to {@link InputMethod#KEYBOARD}. */
  private static InputMethod currentInputMethod = InputMethod.KEYBOARD;

  /** Maps the atlas region name to its {@code [x, y]} top-left position in the spritesheet. */
  private static final Map<String, int[]> KEYBOARD_MOUSE_REGIONS = new HashMap<>();

  /**
   * Maps a libGDX {@link Input.Keys} key code to its base region name (without {@code _outline}).
   */
  private static final Map<Integer, String> KEYBOARD_KEY_NAMES = new HashMap<>();

  /**
   * Maps a libGDX {@link Input.Buttons} mouse button code to its base region name (without {@code
   * _outline}).
   */
  private static final Map<Integer, String> MOUSE_BUTTON_NAMES = new HashMap<>();

  /** Cache of already created {@link TextureRegion}s keyed by region name. */
  private static final Map<String, TextureRegion> REGION_CACHE = new HashMap<>();

  static {
    initKeyboardMouseRegions();
    initKeyboardMappings();
    initMouseMappings();
  }

  private InputPromptHelper() {}

  /**
   * Returns the currently active input method.
   *
   * @return the active {@link InputMethod}.
   */
  public static InputMethod getInputMethod() {
    return currentInputMethod;
  }

  /**
   * Sets the currently active input method.
   *
   * @param inputMethod the {@link InputMethod} to activate.
   */
  public static void setInputMethod(InputMethod inputMethod) {
    currentInputMethod = inputMethod;
  }

  /**
   * Returns the texture region for the given keyboard key code under the given input method, using
   * the solid (non-outline) variant.
   *
   * @param inputMethod the input method to look up the prompt for.
   * @param keyCode a libGDX {@link Input.Keys} key code.
   * @return the corresponding {@link TextureRegion}, or {@code null} if no mapping exists.
   * @throws UnsupportedOperationException if the input method is not yet supported.
   */
  public static TextureRegion get(InputMethod inputMethod, int keyCode) {
    return get(inputMethod, keyCode, false);
  }

  /**
   * Returns the texture region for the given keyboard key code under the given input method.
   *
   * @param inputMethod the input method to look up the prompt for.
   * @param keyCode a libGDX {@link Input.Keys} key code.
   * @param outline if {@code true}, return the outlined variant if available.
   * @return the corresponding {@link TextureRegion}, or {@code null} if no mapping exists.
   * @throws UnsupportedOperationException if the input method is not yet supported.
   */
  public static TextureRegion get(InputMethod inputMethod, int keyCode, boolean outline) {
    String baseName = keyNameMapFor(inputMethod).get(keyCode);
    return regionFor(inputMethod, baseName, outline);
  }

  /**
   * Returns the texture region for the given keyboard key code under the {@linkplain
   * #getInputMethod() current input method}, using the solid (non-outline) variant.
   *
   * @param keyCode a libGDX {@link Input.Keys} key code.
   * @return the corresponding {@link TextureRegion}, or {@code null} if no mapping exists.
   */
  public static TextureRegion get(int keyCode) {
    return get(currentInputMethod, keyCode, false);
  }

  /**
   * Returns the texture region for the given keyboard key code under the {@linkplain
   * #getInputMethod() current input method}.
   *
   * @param keyCode a libGDX {@link Input.Keys} key code.
   * @param outline if {@code true}, return the outlined variant if available.
   * @return the corresponding {@link TextureRegion}, or {@code null} if no mapping exists.
   */
  public static TextureRegion get(int keyCode, boolean outline) {
    return get(currentInputMethod, keyCode, outline);
  }

  /**
   * Returns the texture region for the given mouse button, using the solid (non-outline) variant.
   *
   * <p>Mouse prompts are always rendered using the keyboard & mouse spritesheet, independently of
   * the currently active {@link InputMethod}.
   *
   * @param button a libGDX {@link Input.Buttons} button code.
   * @return the corresponding {@link TextureRegion}, or {@code null} if no mapping exists.
   */
  public static TextureRegion getMouseButton(int button) {
    return getMouseButton(button, false);
  }

  /**
   * Returns the texture region for the given mouse button.
   *
   * <p>Mouse prompts are always rendered using the keyboard & mouse spritesheet, independently of
   * the currently active {@link InputMethod}.
   *
   * @param button a libGDX {@link Input.Buttons} button code.
   * @param outline if {@code true}, return the outlined variant if available.
   * @return the corresponding {@link TextureRegion}, or {@code null} if no mapping exists.
   */
  public static TextureRegion getMouseButton(int button, boolean outline) {
    return regionFor(InputMethod.KEYBOARD, MOUSE_BUTTON_NAMES.get(button), outline);
  }

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
   * Looks up the spritesheet location for a keyboard key prompt without creating a {@link
   * TextureRegion}. This is useful for callers that load and crop the texture themselves (for
   * example markup parsers that desugar a {@code [key]} tag into a regular image reference).
   *
   * @param inputMethod the input method to look up the prompt for.
   * @param keyCode a libGDX {@link Input.Keys} key code.
   * @param outline if {@code true}, return the outlined variant if available.
   * @return the {@link InputPromptRegion} describing the spritesheet location, or {@code null} if
   *     no mapping exists.
   * @throws UnsupportedOperationException if the input method is not yet supported.
   */
  public static InputPromptRegion lookupKey(InputMethod inputMethod, int keyCode, boolean outline) {
    String baseName = keyNameMapFor(inputMethod).get(keyCode);
    return lookupRegion(inputMethod, baseName, outline);
  }

  /**
   * Looks up the spritesheet location for a mouse button prompt without creating a {@link
   * TextureRegion}. Mouse buttons always live on the keyboard & mouse spritesheet regardless of the
   * active input method.
   *
   * @param button a libGDX {@link Input.Buttons} button code.
   * @param outline if {@code true}, return the outlined variant if available.
   * @return the {@link InputPromptRegion} describing the spritesheet location, or {@code null} if
   *     no mapping exists.
   */
  public static InputPromptRegion lookupMouseButton(int button, boolean outline) {
    return lookupRegion(InputMethod.KEYBOARD, MOUSE_BUTTON_NAMES.get(button), outline);
  }

  /**
   * Resolves a base region name to its spritesheet location, honoring the {@code _outline} fallback
   * rules used by {@link #regionFor(InputMethod, String, boolean)}.
   *
   * @param inputMethod the input method whose spritesheet to look up.
   * @param baseName the base region name (without {@code _outline}), or {@code null}.
   * @param outline if {@code true}, prefer the outlined variant when present.
   * @return the matching {@link InputPromptRegion}, or {@code null} if no mapping exists.
   */
  private static InputPromptRegion lookupRegion(
      InputMethod inputMethod, String baseName, boolean outline) {
    if (baseName == null) return null;
    Map<String, int[]> regions = regionMapFor(inputMethod);

    String name = outline ? baseName + "_outline" : baseName;
    if (!regions.containsKey(name)) {
      name = baseName;
      if (!regions.containsKey(name)) return null;
    }
    int[] xy = regions.get(name);
    return new InputPromptRegion(
        texturePathFor(inputMethod), xy[0], xy[1], REGION_SIZE, REGION_SIZE);
  }

  /**
   * Returns the region map for the given input method.
   *
   * @param inputMethod the input method to resolve.
   * @return the {@code regionName -> [x, y]} map for the input method's spritesheet.
   * @throws UnsupportedOperationException if the input method has no spritesheet wired up yet.
   */
  private static Map<String, int[]> regionMapFor(InputMethod inputMethod) {
    return switch (inputMethod) {
      case KEYBOARD -> KEYBOARD_MOUSE_REGIONS;
      case PLAYSTATION, XBOX, TOUCH ->
          throw new UnsupportedOperationException(
              "Input method " + inputMethod + " is not yet supported.");
    };
  }

  /**
   * Returns the spritesheet texture path for the given input method.
   *
   * @param inputMethod the input method to resolve.
   * @return the asset path of the spritesheet texture for the input method.
   * @throws UnsupportedOperationException if the input method has no spritesheet wired up yet.
   */
  private static String texturePathFor(InputMethod inputMethod) {
    return switch (inputMethod) {
      case KEYBOARD -> KEYBOARD_MOUSE_TEXTURE_PATH;
      case PLAYSTATION, XBOX, TOUCH ->
          throw new UnsupportedOperationException(
              "Input method " + inputMethod + " is not yet supported.");
    };
  }

  /**
   * Returns the libGDX-key-code -> base-region-name map for the given input method.
   *
   * @param inputMethod the input method to resolve.
   * @return the {@code keyCode -> regionName} map for the input method.
   * @throws UnsupportedOperationException if the input method has no spritesheet wired up yet.
   */
  private static Map<Integer, String> keyNameMapFor(InputMethod inputMethod) {
    return switch (inputMethod) {
      case KEYBOARD -> KEYBOARD_KEY_NAMES;
      case PLAYSTATION, XBOX, TOUCH ->
          throw new UnsupportedOperationException(
              "Input method " + inputMethod + " is not yet supported.");
    };
  }

  /**
   * Resolves and caches a {@link TextureRegion} for the given base region name within the
   * spritesheet of the given input method. If {@code outline} is {@code true} but no {@code
   * _outline} variant exists, the base region is returned instead.
   *
   * @param inputMethod the input method whose spritesheet to use.
   * @param baseName the base region name (without {@code _outline}), or {@code null}.
   * @param outline if {@code true}, prefer the outlined variant when present.
   * @return the cached {@link TextureRegion}, or {@code null} if no mapping exists.
   */
  private static TextureRegion regionFor(
      InputMethod inputMethod, String baseName, boolean outline) {
    if (baseName == null) return null;
    Map<String, int[]> regions = regionMapFor(inputMethod);

    String name = outline ? baseName + "_outline" : baseName;
    if (!regions.containsKey(name)) {
      name = baseName;
      if (!regions.containsKey(name)) return null;
    }

    String cacheKey = inputMethod.name() + ":" + name;
    TextureRegion cached = REGION_CACHE.get(cacheKey);
    if (cached != null) return cached;

    int[] xy = regions.get(name);
    Texture texture = TextureMap.instance().textureAt(new SimpleIPath(texturePathFor(inputMethod)));
    TextureRegion region = new TextureRegion(texture, xy[0], xy[1], REGION_SIZE, REGION_SIZE);
    REGION_CACHE.put(cacheKey, region);
    return region;
  }

  private static void putRegion(String name, int x, int y) {
    KEYBOARD_MOUSE_REGIONS.put(name, new int[] {x, y});
  }

  /**
   * Hard-coded mappings derived from kenney's texture atlas xml (coordinates doubled to match the
   * 2x-scaled spritesheet, where every cell is 128x128 instead of 64x64).
   */
  private static void initKeyboardMouseRegions() {
    putRegion("keyboard", 0, 0);
    putRegion("keyboard_0", 128, 0);
    putRegion("keyboard_0_outline", 256, 0);
    putRegion("keyboard_1", 384, 0);
    putRegion("keyboard_1_outline", 512, 0);
    putRegion("keyboard_2", 640, 0);
    putRegion("keyboard_2_outline", 768, 0);
    putRegion("keyboard_3", 896, 0);
    putRegion("keyboard_3_outline", 1024, 0);
    putRegion("keyboard_4", 1152, 0);
    putRegion("keyboard_4_outline", 1280, 0);
    putRegion("keyboard_5", 1408, 0);
    putRegion("keyboard_5_outline", 1536, 0);
    putRegion("keyboard_6", 1664, 0);
    putRegion("keyboard_6_outline", 1792, 0);
    putRegion("keyboard_7", 1920, 0);
    putRegion("keyboard_7_outline", 0, 128);
    putRegion("keyboard_8", 128, 128);
    putRegion("keyboard_8_outline", 256, 128);
    putRegion("keyboard_9", 384, 128);
    putRegion("keyboard_9_outline", 512, 128);
    putRegion("keyboard_a", 640, 128);
    putRegion("keyboard_a_outline", 768, 128);
    putRegion("keyboard_alt", 896, 128);
    putRegion("keyboard_alt_outline", 1024, 128);
    putRegion("keyboard_any", 1152, 128);
    putRegion("keyboard_any_outline", 1280, 128);
    putRegion("keyboard_apostrophe", 1408, 128);
    putRegion("keyboard_apostrophe_outline", 1536, 128);
    putRegion("keyboard_arrow_down", 1664, 128);
    putRegion("keyboard_arrow_down_outline", 1792, 128);
    putRegion("keyboard_arrow_left", 1920, 128);
    putRegion("keyboard_arrow_left_outline", 0, 256);
    putRegion("keyboard_arrow_right", 128, 256);
    putRegion("keyboard_arrow_right_outline", 256, 256);
    putRegion("keyboard_arrow_up", 384, 256);
    putRegion("keyboard_arrow_up_outline", 512, 256);
    putRegion("keyboard_arrows", 640, 256);
    putRegion("keyboard_arrows_all", 768, 256);
    putRegion("keyboard_arrows_down", 896, 256);
    putRegion("keyboard_arrows_down_outline", 1024, 256);
    putRegion("keyboard_arrows_horizontal", 1152, 256);
    putRegion("keyboard_arrows_horizontal_outline", 1280, 256);
    putRegion("keyboard_arrows_left", 1408, 256);
    putRegion("keyboard_arrows_left_outline", 1536, 256);
    putRegion("keyboard_arrows_none", 1664, 256);
    putRegion("keyboard_arrows_right", 1792, 256);
    putRegion("keyboard_arrows_right_outline", 1920, 256);
    putRegion("keyboard_arrows_up", 0, 384);
    putRegion("keyboard_arrows_up_outline", 128, 384);
    putRegion("keyboard_arrows_vertical", 256, 384);
    putRegion("keyboard_arrows_vertical_outline", 384, 384);
    putRegion("keyboard_asterisk", 512, 384);
    putRegion("keyboard_asterisk_outline", 640, 384);
    putRegion("keyboard_b", 768, 384);
    putRegion("keyboard_b_outline", 896, 384);
    putRegion("keyboard_backspace", 1024, 384);
    putRegion("keyboard_backspace_icon", 1152, 384);
    putRegion("keyboard_backspace_icon_alternative", 1280, 384);
    putRegion("keyboard_backspace_icon_alternative_outline", 1408, 384);
    putRegion("keyboard_backspace_icon_outline", 1536, 384);
    putRegion("keyboard_backspace_outline", 1664, 384);
    putRegion("keyboard_bracket_close", 1792, 384);
    putRegion("keyboard_bracket_close_outline", 1920, 384);
    putRegion("keyboard_bracket_greater", 0, 512);
    putRegion("keyboard_bracket_greater_outline", 128, 512);
    putRegion("keyboard_bracket_less", 256, 512);
    putRegion("keyboard_bracket_less_outline", 384, 512);
    putRegion("keyboard_bracket_open", 512, 512);
    putRegion("keyboard_bracket_open_outline", 640, 512);
    putRegion("keyboard_c", 768, 512);
    putRegion("keyboard_c_outline", 896, 512);
    putRegion("keyboard_capslock", 1024, 512);
    putRegion("keyboard_capslock_icon", 1152, 512);
    putRegion("keyboard_capslock_icon_outline", 1280, 512);
    putRegion("keyboard_capslock_outline", 1408, 512);
    putRegion("keyboard_caret", 1536, 512);
    putRegion("keyboard_caret_outline", 1664, 512);
    putRegion("keyboard_colon", 1792, 512);
    putRegion("keyboard_colon_outline", 1920, 512);
    putRegion("keyboard_comma", 0, 640);
    putRegion("keyboard_comma_outline", 128, 640);
    putRegion("keyboard_command", 256, 640);
    putRegion("keyboard_command_outline", 384, 640);
    putRegion("keyboard_ctrl", 512, 640);
    putRegion("keyboard_ctrl_outline", 640, 640);
    putRegion("keyboard_d", 768, 640);
    putRegion("keyboard_d_outline", 896, 640);
    putRegion("keyboard_delete", 1024, 640);
    putRegion("keyboard_delete_outline", 1152, 640);
    putRegion("keyboard_e", 1280, 640);
    putRegion("keyboard_e_outline", 1408, 640);
    putRegion("keyboard_end", 1536, 640);
    putRegion("keyboard_end_outline", 1664, 640);
    putRegion("keyboard_enter", 1792, 640);
    putRegion("keyboard_enter_outline", 1920, 640);
    putRegion("keyboard_equals", 0, 768);
    putRegion("keyboard_equals_outline", 128, 768);
    putRegion("keyboard_escape", 256, 768);
    putRegion("keyboard_escape_outline", 384, 768);
    putRegion("keyboard_exclamation", 512, 768);
    putRegion("keyboard_exclamation_outline", 640, 768);
    putRegion("keyboard_f", 768, 768);
    putRegion("keyboard_f_outline", 896, 768);
    putRegion("keyboard_f1", 1024, 768);
    putRegion("keyboard_f1_outline", 1152, 768);
    putRegion("keyboard_f10", 1280, 768);
    putRegion("keyboard_f10_outline", 1408, 768);
    putRegion("keyboard_f11", 1536, 768);
    putRegion("keyboard_f11_outline", 1664, 768);
    putRegion("keyboard_f12", 1792, 768);
    putRegion("keyboard_f12_outline", 1920, 768);
    putRegion("keyboard_f2", 0, 896);
    putRegion("keyboard_f2_outline", 128, 896);
    putRegion("keyboard_f3", 256, 896);
    putRegion("keyboard_f3_outline", 384, 896);
    putRegion("keyboard_f4", 512, 896);
    putRegion("keyboard_f4_outline", 640, 896);
    putRegion("keyboard_f5", 768, 896);
    putRegion("keyboard_f5_outline", 896, 896);
    putRegion("keyboard_f6", 1024, 896);
    putRegion("keyboard_f6_outline", 1152, 896);
    putRegion("keyboard_f7", 1280, 896);
    putRegion("keyboard_f7_outline", 1408, 896);
    putRegion("keyboard_f8", 1536, 896);
    putRegion("keyboard_f8_outline", 1664, 896);
    putRegion("keyboard_f9", 1792, 896);
    putRegion("keyboard_f9_outline", 1920, 896);
    putRegion("keyboard_function", 0, 1024);
    putRegion("keyboard_function_outline", 128, 1024);
    putRegion("keyboard_g", 256, 1024);
    putRegion("keyboard_g_outline", 384, 1024);
    putRegion("keyboard_h", 512, 1024);
    putRegion("keyboard_h_outline", 640, 1024);
    putRegion("keyboard_home", 768, 1024);
    putRegion("keyboard_home_outline", 896, 1024);
    putRegion("keyboard_i", 1024, 1024);
    putRegion("keyboard_i_outline", 1152, 1024);
    putRegion("keyboard_insert", 1280, 1024);
    putRegion("keyboard_insert_outline", 1408, 1024);
    putRegion("keyboard_j", 1536, 1024);
    putRegion("keyboard_j_outline", 1664, 1024);
    putRegion("keyboard_k", 1792, 1024);
    putRegion("keyboard_k_outline", 1920, 1024);
    putRegion("keyboard_l", 0, 1152);
    putRegion("keyboard_l_outline", 128, 1152);
    putRegion("keyboard_m", 256, 1152);
    putRegion("keyboard_m_outline", 384, 1152);
    putRegion("keyboard_minus", 512, 1152);
    putRegion("keyboard_minus_outline", 640, 1152);
    putRegion("keyboard_n", 768, 1152);
    putRegion("keyboard_n_outline", 896, 1152);
    putRegion("keyboard_numlock", 1024, 1152);
    putRegion("keyboard_numlock_outline", 1152, 1152);
    putRegion("keyboard_numpad_enter", 1280, 1152);
    putRegion("keyboard_numpad_enter_outline", 1408, 1152);
    putRegion("keyboard_numpad_plus", 1536, 1152);
    putRegion("keyboard_numpad_plus_outline", 1664, 1152);
    putRegion("keyboard_o", 1792, 1152);
    putRegion("keyboard_o_outline", 1920, 1152);
    putRegion("keyboard_option", 0, 1280);
    putRegion("keyboard_option_outline", 128, 1280);
    putRegion("keyboard_outline", 256, 1280);
    putRegion("keyboard_p", 384, 1280);
    putRegion("keyboard_p_outline", 512, 1280);
    putRegion("keyboard_page_down", 640, 1280);
    putRegion("keyboard_page_down_outline", 768, 1280);
    putRegion("keyboard_page_up", 896, 1280);
    putRegion("keyboard_page_up_outline", 1024, 1280);
    putRegion("keyboard_period", 1152, 1280);
    putRegion("keyboard_period_outline", 1280, 1280);
    putRegion("keyboard_plus", 1408, 1280);
    putRegion("keyboard_plus_outline", 1536, 1280);
    putRegion("keyboard_printscreen", 1664, 1280);
    putRegion("keyboard_printscreen_outline", 1792, 1280);
    putRegion("keyboard_q", 1920, 1280);
    putRegion("keyboard_q_outline", 0, 1408);
    putRegion("keyboard_question", 128, 1408);
    putRegion("keyboard_question_outline", 256, 1408);
    putRegion("keyboard_quote", 384, 1408);
    putRegion("keyboard_quote_outline", 512, 1408);
    putRegion("keyboard_r", 640, 1408);
    putRegion("keyboard_r_outline", 768, 1408);
    putRegion("keyboard_return", 896, 1408);
    putRegion("keyboard_return_outline", 1024, 1408);
    putRegion("keyboard_s", 1152, 1408);
    putRegion("keyboard_s_outline", 1280, 1408);
    putRegion("keyboard_semicolon", 1408, 1408);
    putRegion("keyboard_semicolon_outline", 1536, 1408);
    putRegion("keyboard_shift", 1664, 1408);
    putRegion("keyboard_shift_icon", 1792, 1408);
    putRegion("keyboard_shift_icon_outline", 1920, 1408);
    putRegion("keyboard_shift_outline", 0, 1536);
    putRegion("keyboard_slash_back", 128, 1536);
    putRegion("keyboard_slash_back_outline", 256, 1536);
    putRegion("keyboard_slash_forward", 384, 1536);
    putRegion("keyboard_slash_forward_outline", 512, 1536);
    putRegion("keyboard_space", 640, 1536);
    putRegion("keyboard_space_icon", 768, 1536);
    putRegion("keyboard_space_icon_outline", 896, 1536);
    putRegion("keyboard_space_outline", 1024, 1536);
    putRegion("keyboard_t", 1152, 1536);
    putRegion("keyboard_t_outline", 1280, 1536);
    putRegion("keyboard_tab", 1408, 1536);
    putRegion("keyboard_tab_icon", 1536, 1536);
    putRegion("keyboard_tab_icon_alternative", 1664, 1536);
    putRegion("keyboard_tab_icon_alternative_outline", 1792, 1536);
    putRegion("keyboard_tab_icon_outline", 1920, 1536);
    putRegion("keyboard_tab_outline", 0, 1664);
    putRegion("keyboard_tilde", 128, 1664);
    putRegion("keyboard_tilde_outline", 256, 1664);
    putRegion("keyboard_u", 384, 1664);
    putRegion("keyboard_u_outline", 512, 1664);
    putRegion("keyboard_v", 640, 1664);
    putRegion("keyboard_v_outline", 768, 1664);
    putRegion("keyboard_w", 896, 1664);
    putRegion("keyboard_w_outline", 1024, 1664);
    putRegion("keyboard_win", 1152, 1664);
    putRegion("keyboard_win_outline", 1280, 1664);
    putRegion("keyboard_x", 1408, 1664);
    putRegion("keyboard_x_outline", 1536, 1664);
    putRegion("keyboard_y", 1664, 1664);
    putRegion("keyboard_y_outline", 1792, 1664);
    putRegion("keyboard_z", 1920, 1664);
    putRegion("keyboard_z_outline", 0, 1792);
    putRegion("mouse", 128, 1792);
    putRegion("mouse_horizontal", 256, 1792);
    putRegion("mouse_left", 384, 1792);
    putRegion("mouse_left_outline", 512, 1792);
    putRegion("mouse_move", 640, 1792);
    putRegion("mouse_outline", 768, 1792);
    putRegion("mouse_right", 896, 1792);
    putRegion("mouse_right_outline", 1024, 1792);
    putRegion("mouse_scroll", 1152, 1792);
    putRegion("mouse_scroll_down", 1280, 1792);
    putRegion("mouse_scroll_down_outline", 1408, 1792);
    putRegion("mouse_scroll_outline", 1536, 1792);
    putRegion("mouse_scroll_up", 1664, 1792);
    putRegion("mouse_scroll_up_outline", 1792, 1792);
    putRegion("mouse_scroll_vertical", 1920, 1792);
    putRegion("mouse_scroll_vertical_outline", 0, 1920);
    putRegion("mouse_small", 128, 1920);
    putRegion("mouse_vertical", 256, 1920);
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
