package engine.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.DefaultLwjgl3Input;
import engine.Game;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.lwjgl.glfw.GLFW;

/**
 * Names bindings for the player.
 *
 * <p>Bindings store key positions. Printable keys show what the active keyboard layout prints at
 * that position; everything else uses localized names from {@code language_default}.
 */
public final class KeyNames {
  private static final Map<Integer, Integer> GLFW_KEYS = new HashMap<>();

  private KeyNames() {}

  /**
   * Names a binding.
   *
   * @param binding the binding
   * @return its label for the player, for example {@code Strg+C} or {@code Z}
   */
  public static String label(Binding binding) {
    return switch (binding) {
      case Binding.Key key -> keyLabel(key);
      case Binding.MouseButton mouse ->
          localized(binding.format().replace(':', '.'), binding.format());
      case Binding.MouseWheel wheel -> localized("mouse.WHEEL", binding.format());
      case Binding.PadButton pad -> localized("pad." + pad.button(), pad.button().name());
      case Binding.PadAxis axis -> localized("pad." + axis.axis(), axis.axis().name());
    };
  }

  private static String keyLabel(Binding.Key key) {
    String prefixes =
        key.prefixes().stream().sorted().map(KeyNames::singleKey).collect(Collectors.joining("+"));
    String name = singleKey(key.keycode());
    return prefixes.isEmpty() ? name : prefixes + "+" + name;
  }

  private static String singleKey(int keycode) {
    String printed = printedName(keycode);
    if (printed != null) return printed;
    String format = new Binding.Key(keycode).format();
    return localized("key." + format.substring("key:".length()), Input.Keys.toString(keycode));
  }

  private static String localized(String key, String fallback) {
    String path = "input." + key;
    String text = Game.localization().text(path);
    return text.contains(path) ? fallback : text;
  }

  /**
   * Finds the key whose glyph shows the label of a key position on the active layout. On a German
   * layout, the position of Z shows Y.
   *
   * @param keycode libGDX keycode of the position
   * @return the keycode to draw
   */
  public static int glyphKeycode(int keycode) {
    String printed = printedName(keycode);
    if (printed == null) return keycode;
    int glyph = Input.Keys.valueOf(printed);
    return glyph == -1 ? keycode : glyph;
  }

  // Asks the window system what a key position prints on the active layout. Returns the upper-case
  // name, or null without a window or for keys that print nothing.
  private static String printedName(int keycode) {
    if (!(Gdx.input instanceof DefaultLwjgl3Input input)) return null;
    if (GLFW_KEYS.isEmpty()) {
      for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++) {
        GLFW_KEYS.putIfAbsent(input.getGdxKeyCode(key), key);
      }
    }
    Integer glfwKey = GLFW_KEYS.get(keycode);
    if (glfwKey == null) return null;
    String name = GLFW.glfwGetKeyName(glfwKey, 0);
    return name == null || name.isBlank() ? null : name.toUpperCase(Locale.ROOT);
  }
}
