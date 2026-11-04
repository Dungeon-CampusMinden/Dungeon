package engine.input;

import com.badlogic.gdx.Input;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A physical input that can drive an action, stored as a readable string such as {@code key:E},
 * {@code key:Ctrl+C}, {@code key:F3+A}, {@code mouse:RIGHT}, {@code mouse:WHEEL} or {@code pad:A}.
 *
 * <p>Keys are stored as positions (libGDX keycodes on a US layout); {@link KeyNames} resolves their
 * label on the active layout.
 */
public sealed interface Binding {

  /**
   * The input that produces the edge for this binding, without any combo prefixes.
   *
   * @return the triggering input
   */
  default Binding trigger() {
    return this;
  }

  /**
   * The readable form of this binding.
   *
   * @return the string stored in the binding file
   */
  String format();

  /**
   * Whether a pad produces this input.
   *
   * @return true for pad buttons and axes
   */
  default boolean isPad() {
    return false;
  }

  /**
   * A keyboard key, optionally combined with prefix keys that must already be held.
   *
   * <p>{@link Input.Keys#CONTROL_LEFT}, {@link Input.Keys#SHIFT_LEFT} and {@link
   * Input.Keys#ALT_LEFT} as prefixes stand for the modifier on either side.
   *
   * @param keycode libGDX keycode of the triggering key
   * @param prefixes keycodes of the required prefix keys
   */
  record Key(int keycode, Set<Integer> prefixes) implements Binding {
    /**
     * Creates a key binding.
     *
     * @param keycode libGDX keycode of the triggering key
     * @param prefixes keycodes of the required prefix keys
     */
    public Key {
      prefixes = Set.copyOf(prefixes);
    }

    /**
     * Creates a key binding without prefixes.
     *
     * @param keycode libGDX keycode
     */
    public Key(int keycode) {
      this(keycode, Set.of());
    }

    @Override
    public Binding trigger() {
      return prefixes.isEmpty() ? this : new Key(keycode);
    }

    @Override
    public String format() {
      StringBuilder text = new StringBuilder("key:");
      prefixes.stream().sorted().forEach(code -> text.append(prefixName(code)).append('+'));
      return text.append(KeyCodes.name(keycode)).toString();
    }
  }

  /**
   * A mouse button.
   *
   * @param button libGDX button code
   */
  record MouseButton(int button) implements Binding {
    @Override
    public String format() {
      return "mouse:" + mouseName(button);
    }
  }

  /** The vertical mouse wheel, read as a one-dimensional axis. */
  record MouseWheel() implements Binding {
    @Override
    public String format() {
      return "mouse:WHEEL";
    }
  }

  /**
   * A pad button.
   *
   * @param button the button
   */
  record PadButton(Pad button) implements Binding {
    @Override
    public String format() {
      return "pad:" + button.name();
    }

    @Override
    public boolean isPad() {
      return true;
    }
  }

  /**
   * A pad axis.
   *
   * <p>{@link PadAxisId#LSTICK} and {@link PadAxisId#RSTICK} are two-dimensional; triggers and
   * stick directions act like buttons with hysteresis.
   *
   * @param axis the axis
   */
  record PadAxis(PadAxisId axis) implements Binding {
    @Override
    public String format() {
      return "pad:" + axis.name();
    }

    @Override
    public boolean isPad() {
      return true;
    }
  }

  /** Pad buttons in SDL layout. */
  enum Pad {
    /** Bottom face button. */
    A(0),
    /** Right face button. */
    B(1),
    /** Left face button. */
    X(2),
    /** Top face button. */
    Y(3),
    /** View or select button. */
    BACK(4),
    /** Menu or start button. */
    START(6),
    /** Left shoulder. */
    LB(9),
    /** Right shoulder. */
    RB(10),
    /** D-pad up. */
    DPAD_UP(11),
    /** D-pad down. */
    DPAD_DOWN(12),
    /** D-pad left. */
    DPAD_LEFT(13),
    /** D-pad right. */
    DPAD_RIGHT(14);

    private final int index;

    Pad(int index) {
      this.index = index;
    }

    /**
     * Button index reported by the controller backend.
     *
     * @return the SDL button index
     */
    public int index() {
      return index;
    }
  }

  /** Pad axes and the digital directions derived from them. */
  enum PadAxisId {
    /** Left stick as vector. */
    LSTICK,
    /** Right stick as vector. */
    RSTICK,
    /** Left trigger. */
    LT,
    /** Right trigger. */
    RT,
    /** Left stick pushed up. */
    LSTICK_UP,
    /** Left stick pushed down. */
    LSTICK_DOWN,
    /** Left stick pushed left. */
    LSTICK_LEFT,
    /** Left stick pushed right. */
    LSTICK_RIGHT
  }

  /**
   * Parses the readable form of a binding.
   *
   * @param text the binding string
   * @return the parsed binding
   * @throws IllegalArgumentException if the string names no known input
   */
  static Binding parse(String text) {
    int separator = text.indexOf(':');
    if (separator < 0) throw new IllegalArgumentException("Binding without device: " + text);
    String device = text.substring(0, separator);
    String value = text.substring(separator + 1);
    return switch (device) {
      case "key" -> parseKey(text, value);
      case "mouse" -> parseMouse(text, value);
      case "pad" -> parsePad(text, value);
      default -> throw new IllegalArgumentException("Unknown binding device: " + text);
    };
  }

  /**
   * Parses several bindings.
   *
   * @param texts the binding strings
   * @return the parsed bindings, in order
   * @throws IllegalArgumentException if a string names no known input
   */
  static List<Binding> parseAll(String... texts) {
    return Arrays.stream(texts).map(Binding::parse).toList();
  }

  private static Binding parseKey(String text, String value) {
    String[] parts = value.split("\\+", -1);
    Set<Integer> prefixes = new LinkedHashSet<>();
    for (int i = 0; i < parts.length - 1; i++) prefixes.add(prefixCode(text, parts[i]));
    return new Key(keyCode(text, parts[parts.length - 1]), prefixes);
  }

  private static Binding parseMouse(String text, String value) {
    return switch (value) {
      case "LEFT" -> new MouseButton(Input.Buttons.LEFT);
      case "RIGHT" -> new MouseButton(Input.Buttons.RIGHT);
      case "MIDDLE" -> new MouseButton(Input.Buttons.MIDDLE);
      case "BACK" -> new MouseButton(Input.Buttons.BACK);
      case "FORWARD" -> new MouseButton(Input.Buttons.FORWARD);
      case "WHEEL" -> new MouseWheel();
      default -> throw new IllegalArgumentException("Unknown mouse input: " + text);
    };
  }

  private static Binding parsePad(String text, String value) {
    for (Pad pad : Pad.values()) if (pad.name().equals(value)) return new PadButton(pad);
    for (PadAxisId axis : PadAxisId.values())
      if (axis.name().equals(value)) return new PadAxis(axis);
    throw new IllegalArgumentException("Unknown pad input: " + text);
  }

  private static int prefixCode(String text, String name) {
    return switch (name.toLowerCase(Locale.ROOT)) {
      case "ctrl" -> Input.Keys.CONTROL_LEFT;
      case "shift" -> Input.Keys.SHIFT_LEFT;
      case "alt" -> Input.Keys.ALT_LEFT;
      default -> keyCode(text, name);
    };
  }

  private static String prefixName(int code) {
    return switch (code) {
      case Input.Keys.CONTROL_LEFT -> "Ctrl";
      case Input.Keys.SHIFT_LEFT -> "Shift";
      case Input.Keys.ALT_LEFT -> "Alt";
      default -> KeyCodes.name(code);
    };
  }

  private static String mouseName(int button) {
    return switch (button) {
      case Input.Buttons.LEFT -> "LEFT";
      case Input.Buttons.RIGHT -> "RIGHT";
      case Input.Buttons.MIDDLE -> "MIDDLE";
      case Input.Buttons.BACK -> "BACK";
      case Input.Buttons.FORWARD -> "FORWARD";
      default -> throw new IllegalArgumentException("Unknown mouse button: " + button);
    };
  }

  private static int keyCode(String text, String name) {
    Integer code = KeyCodes.CODES.get(name);
    if (code == null) throw new IllegalArgumentException("Unknown key in binding: " + text);
    return code;
  }

  /** Maps the constant names of {@link Input.Keys} to their keycodes and back. */
  final class KeyCodes {
    private static final Map<String, Integer> CODES = new TreeMap<>();
    private static final Map<Integer, String> NAMES = new HashMap<>();

    static {
      List<Field> fields = new ArrayList<>(Arrays.asList(Input.Keys.class.getFields()));
      fields.sort((a, b) -> a.getName().compareTo(b.getName()));
      for (Field field : fields) {
        if (field.getType() != int.class || !Modifier.isStatic(field.getModifiers())) continue;
        try {
          int code = field.getInt(null);
          CODES.put(field.getName(), code);
          NAMES.putIfAbsent(code, field.getName());
        } catch (IllegalAccessException e) {
          throw new IllegalStateException(e);
        }
      }
    }

    private KeyCodes() {}

    private static String name(int code) {
      String name = NAMES.get(code);
      if (name == null) throw new IllegalArgumentException("Unknown keycode: " + code);
      return name;
    }
  }
}
