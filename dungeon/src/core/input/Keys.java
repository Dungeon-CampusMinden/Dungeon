package core.input;

/**
 * Constants for keyboard input key codes.
 *
 * <p>Keys provides a centralized registry of key code constants for common keyboard inputs,
 * abstracting away platform-specific key code values. It maps human-readable names to their
 * corresponding key code integers.
 *
 * <p>Supported key categories:
 * <ul>
 *   <li>Numbers: NUM_0 to NUM_9
 *   <li>Arrows: UP, DOWN, LEFT, RIGHT
 *   <li>Letters: A to Z
 *   <li>Punctuation: COMMA, PERIOD
 *   <li>Modifiers: SHIFT_LEFT, SHIFT_RIGHT
 *   <li>Common controls: TAB, SPACE, ENTER, BACKSPACE, ESCAPE
 *   <li>Function keys: F1 to F12
 *   <li>Special: ANY_KEY (matches any key), UNKNOWN
 * </ul>
 *
 * <p>This class is not instantiable; all members are static constants.
 */
public final class Keys {

  public static final int ANY_KEY = -1;
  public static final int UNKNOWN = 0;

  // Numbers
  public static final int NUM_0 = 7;
  public static final int NUM_1 = 8;
  public static final int NUM_2 = 9;
  public static final int NUM_3 = 10;
  public static final int NUM_4 = 11;
  public static final int NUM_5 = 12;
  public static final int NUM_6 = 13;
  public static final int NUM_7 = 14;
  public static final int NUM_8 = 15;
  public static final int NUM_9 = 16;

  // Arrows
  public static final int UP = 19;
  public static final int DOWN = 20;
  public static final int LEFT = 21;
  public static final int RIGHT = 22;

  // Letters
  public static final int A = 29;
  public static final int B = 30;
  public static final int C = 31;
  public static final int D = 32;
  public static final int E = 33;
  public static final int F = 34;
  public static final int G = 35;
  public static final int H = 36;
  public static final int I = 37;
  public static final int J = 38;
  public static final int K = 39;
  public static final int L = 40;
  public static final int M = 41;
  public static final int N = 42;
  public static final int O = 43;
  public static final int P = 44;
  public static final int Q = 45;
  public static final int R = 46;
  public static final int S = 47;
  public static final int T = 48;
  public static final int U = 49;
  public static final int V = 50;
  public static final int W = 51;
  public static final int X = 52;
  public static final int Y = 53;
  public static final int Z = 54;

  // Punctuation
  public static final int COMMA = 55;
  public static final int PERIOD = 56;

  // Modifiers
  public static final int SHIFT_LEFT = 59;
  public static final int SHIFT_RIGHT = 60;

  // Common controls
  public static final int TAB = 61;
  public static final int SPACE = 62;
  public static final int ENTER = 66;
  public static final int BACKSPACE = 67;
  public static final int ESCAPE = 111;
  public static final int DELETE = 112;

  // Function keys
  public static final int F1 = 131;
  public static final int F2 = 132;
  public static final int F3 = 133;
  public static final int F4 = 134;
  public static final int F5 = 135;
  public static final int F6 = 136;
  public static final int F7 = 137;
  public static final int F8 = 138;
  public static final int F9 = 139;
  public static final int F10 = 140;
  public static final int F11 = 141;
  public static final int F12 = 142;

  private Keys() {}

  /**
   * Converts a key code to its human-readable string representation.
   *
   * <p>This method maps key code integers to their string names (e.g., UP, DOWN, A, ENTER).
   * For known keys, it returns the key name. For unknown key codes, it returns a format string
   * containing the raw key code (e.g., "KEY(123)").
   *
   * <p>The special key code ANY_KEY returns "ANY_KEY".
   *
   * @param keycode the key code to convert
   * @return the human-readable string representation of the key code
   */
  public static String toString(int keycode) {
    if (keycode == ANY_KEY) return "ANY_KEY";
    return switch (keycode) {
      case UNKNOWN -> "UNKNOWN";

      case NUM_0 -> "0";
      case NUM_1 -> "1";
      case NUM_2 -> "2";
      case NUM_3 -> "3";
      case NUM_4 -> "4";
      case NUM_5 -> "5";
      case NUM_6 -> "6";
      case NUM_7 -> "7";
      case NUM_8 -> "8";
      case NUM_9 -> "9";

      case UP -> "UP";
      case DOWN -> "DOWN";
      case LEFT -> "LEFT";
      case RIGHT -> "RIGHT";

      case A -> "A";
      case B -> "B";
      case C -> "C";
      case D -> "D";
      case E -> "E";
      case F -> "F";
      case G -> "G";
      case H -> "H";
      case I -> "I";
      case J -> "J";
      case K -> "K";
      case L -> "L";
      case M -> "M";
      case N -> "N";
      case O -> "O";
      case P -> "P";
      case Q -> "Q";
      case R -> "R";
      case S -> "S";
      case T -> "T";
      case U -> "U";
      case V -> "V";
      case W -> "W";
      case X -> "X";
      case Y -> "Y";
      case Z -> "Z";

      case COMMA -> ",";
      case PERIOD -> ".";

      case SHIFT_LEFT -> "SHIFT_LEFT";
      case SHIFT_RIGHT -> "SHIFT_RIGHT";

      case TAB -> "TAB";
      case SPACE -> "SPACE";
      case ENTER -> "ENTER";
      case BACKSPACE -> "BACKSPACE";
      case ESCAPE -> "ESCAPE";
      case DELETE -> "DELETE";

      case F1 -> "F1";
      case F2 -> "F2";
      case F3 -> "F3";
      case F4 -> "F4";
      case F5 -> "F5";
      case F6 -> "F6";
      case F7 -> "F7";
      case F8 -> "F8";
      case F9 -> "F9";
      case F10 -> "F10";
      case F11 -> "F11";
      case F12 -> "F12";

      default -> "KEY(" + keycode + ")";
    };
  }
}
