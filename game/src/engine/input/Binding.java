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
 * A physical input that can drive an action. Code creates bindings with the factories below; the
 * binding file stores them as readable strings such as {@code key:E}, {@code key:Ctrl+C}, {@code
 * key:F3+A}, {@code mouse:RIGHT}, {@code mouse:Shift+LEFT}, {@code mouse:WHEEL}, {@code
 * mouse:WHEEL_UP} or {@code pad:A}.
 *
 * <p>Keys are stored as positions (libGDX keycodes on a US layout); {@link KeyNames} resolves their
 * label on the active layout.
 */
public sealed interface Binding {

  /**
   * The modifiers a player can combine with a key, in the order combos are written. Each stands for
   * the key on either side.
   */
  List<Integer> MODIFIERS =
      List.of(Input.Keys.CONTROL_LEFT, Input.Keys.SHIFT_LEFT, Input.Keys.ALT_LEFT);

  /**
   * Creates a key binding. All keys but the last are prefixes that must already be held; the last
   * one triggers.
   *
   * @param first the only key, or the first prefix
   * @param rest further prefixes and the triggering key
   * @return the binding
   */
  static Key key(int first, int... rest) {
    if (rest.length == 0) return new Key(first);
    Set<Integer> prefixes = new LinkedHashSet<>();
    prefixes.add(first);
    for (int i = 0; i < rest.length - 1; i++) prefixes.add(rest[i]);
    return new Key(rest[rest.length - 1], prefixes);
  }

  /**
   * Creates a mouse button binding.
   *
   * @param button libGDX button code
   * @return the binding
   */
  static MouseButton mouse(int button) {
    return new MouseButton(button);
  }

  /**
   * Creates the binding of the vertical mouse wheel.
   *
   * @return the binding
   */
  static MouseWheel wheel() {
    return new MouseWheel();
  }

  /**
   * Creates the binding of one notch of the mouse wheel in one direction.
   *
   * @param up true for a notch away from the player, false for one towards them
   * @return the binding
   */
  static MouseWheelStep wheelStep(boolean up) {
    return new MouseWheelStep(up);
  }

  /**
   * Creates a pad button binding.
   *
   * @param button the button
   * @return the binding
   */
  static PadButton pad(Pad button) {
    return new PadButton(button);
  }

  /**
   * Creates a pad axis binding.
   *
   * @param axis the axis
   * @return the binding
   */
  static PadAxis pad(PadAxisId axis) {
    return new PadAxis(axis);
  }

  /**
   * The input that produces the edge for this binding, without any combo prefixes.
   *
   * @return the triggering input
   */
  default Binding trigger() {
    return this;
  }

  /**
   * The keys that must already be held when the triggering input goes down. Keys, mouse buttons and
   * notches of the wheel can have some; {@link Input.Keys#CONTROL_LEFT}, {@link
   * Input.Keys#SHIFT_LEFT} and {@link Input.Keys#ALT_LEFT} stand for the modifier on either side.
   *
   * @return keycodes of the required prefix keys, empty for a binding that is no combo
   */
  default Set<Integer> prefixes() {
    return Set.of();
  }

  /**
   * The prefixes in the order a combo is written and shown: Control, Shift, Alt, then other keys.
   *
   * @return the ordered prefixes
   */
  default List<Integer> orderedPrefixes() {
    List<Integer> ordered = new ArrayList<>(MODIFIERS);
    ordered.retainAll(prefixes());
    prefixes().stream().filter(code -> !MODIFIERS.contains(code)).sorted().forEach(ordered::add);
    return ordered;
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
      return "key:" + prefixText(this) + KeyCodes.name(keycode);
    }
  }

  /**
   * A mouse button, optionally combined with prefix keys that must already be held.
   *
   * @param button libGDX button code
   * @param prefixes keycodes of the required prefix keys
   */
  record MouseButton(int button, Set<Integer> prefixes) implements Binding {
    /**
     * Creates a mouse button binding.
     *
     * @param button libGDX button code
     * @param prefixes keycodes of the required prefix keys
     */
    public MouseButton {
      prefixes = Set.copyOf(prefixes);
    }

    /**
     * Creates a mouse button binding without prefixes.
     *
     * @param button libGDX button code
     */
    public MouseButton(int button) {
      this(button, Set.of());
    }

    @Override
    public Binding trigger() {
      return prefixes.isEmpty() ? this : new MouseButton(button);
    }

    @Override
    public String format() {
      return "mouse:" + prefixText(this) + mouseName(button);
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
   * One notch of the mouse wheel in one direction; it acts like a short press. Prefix keys that
   * must already be held make it a combo.
   *
   * @param up true for a notch away from the player, false for one towards them
   * @param prefixes keycodes of the required prefix keys
   */
  record MouseWheelStep(boolean up, Set<Integer> prefixes) implements Binding {
    /**
     * Creates the binding of a notch.
     *
     * @param up true for a notch away from the player, false for one towards them
     * @param prefixes keycodes of the required prefix keys
     */
    public MouseWheelStep {
      prefixes = Set.copyOf(prefixes);
    }

    /**
     * Creates the binding of a notch without prefixes.
     *
     * @param up true for a notch away from the player, false for one towards them
     */
    public MouseWheelStep(boolean up) {
      this(up, Set.of());
    }

    @Override
    public Binding trigger() {
      return prefixes.isEmpty() ? this : new MouseWheelStep(up);
    }

    @Override
    public String format() {
      return "mouse:" + prefixText(this) + (up ? "WHEEL_UP" : "WHEEL_DOWN");
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
   * <p>{@link PadAxisId#LSTICK} is two-dimensional; triggers and stick directions act like buttons
   * with hysteresis.
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
    /** Left stick pressed in. */
    LSTICK_CLICK(7),
    /** Right stick pressed in. */
    RSTICK_CLICK(8),
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

  // Splits "Ctrl+Shift+A" into its prefixes and returns the last part, the triggering input.
  private static String parsePrefixes(String text, String value, Set<Integer> prefixes) {
    String[] parts = value.split("\\+", -1);
    for (int i = 0; i < parts.length - 1; i++) prefixes.add(prefixCode(text, parts[i]));
    return parts[parts.length - 1];
  }

  private static Binding parseKey(String text, String value) {
    Set<Integer> prefixes = new LinkedHashSet<>();
    return new Key(keyCode(text, parsePrefixes(text, value, prefixes)), prefixes);
  }

  private static Binding parseMouse(String text, String value) {
    Set<Integer> prefixes = new LinkedHashSet<>();
    String input = parsePrefixes(text, value, prefixes);
    if (input.equals("WHEEL") && prefixes.isEmpty()) return new MouseWheel();
    return switch (input) {
      case "LEFT" -> new MouseButton(Input.Buttons.LEFT, prefixes);
      case "RIGHT" -> new MouseButton(Input.Buttons.RIGHT, prefixes);
      case "MIDDLE" -> new MouseButton(Input.Buttons.MIDDLE, prefixes);
      case "BACK" -> new MouseButton(Input.Buttons.BACK, prefixes);
      case "FORWARD" -> new MouseButton(Input.Buttons.FORWARD, prefixes);
      case "WHEEL_UP" -> new MouseWheelStep(true, prefixes);
      case "WHEEL_DOWN" -> new MouseWheelStep(false, prefixes);
      default -> throw new IllegalArgumentException("Unknown mouse input: " + text);
    };
  }

  private static String prefixText(Binding binding) {
    StringBuilder text = new StringBuilder();
    binding.orderedPrefixes().forEach(code -> text.append(prefixName(code)).append('+'));
    return text.toString();
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
          // Some codes have a second name, such as DPAD_UP for UP or META_ALT_LEFT_ON for NUM_9.
          String name = field.getName();
          if (!name.startsWith("DPAD_") && !name.startsWith("META_")) NAMES.putIfAbsent(code, name);
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
