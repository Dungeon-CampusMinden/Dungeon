package core.input;

/**
 * Constants for keyboard input key codes.
 *
 * <p>Keys provides a centralized registry of key code constants for common keyboard inputs,
 * abstracting away platform-specific key code values. It maps human-readable names to their
 * corresponding key code integers.
 *
 * <p>Supported key categories:
 *
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

  /** Matches any key input. */
  public static final int ANY_KEY = -1;

  /** Represents an unknown or unrecognized key. */
  public static final int UNKNOWN = 0;

  // Numbers
  /** Key code for the 0 (zero) key. */
  public static final int NUM_0 = 7;

  /** Key code for key 1. */
  public static final int NUM_1 = 8;

  /** Key code for key 2. */
  public static final int NUM_2 = 9;

  /** Key code for key 3. */
  public static final int NUM_3 = 10;

  /** Key code for key 4. */
  public static final int NUM_4 = 11;

  /** Key code for key 5. */
  public static final int NUM_5 = 12;

  /** Key code for key 6. */
  public static final int NUM_6 = 13;

  /** Key code for key 7. */
  public static final int NUM_7 = 14;

  /** Key code for key 8. */
  public static final int NUM_8 = 15;

  /** Key code for key 9. */
  public static final int NUM_9 = 16;

  // Arrows
  /** Key code for the Up arrow key. */
  public static final int UP = 19;

  /** Key code for the Down arrow key. */
  public static final int DOWN = 20;

  /** Key code for the Left arrow key. */
  public static final int LEFT = 21;

  /** Key code for the Right arrow key. */
  public static final int RIGHT = 22;

  // Letters
  /** Key code for key A. */
  public static final int A = 29;

  /** Key code for key B. */
  public static final int B = 30;

  /** Key code for key C. */
  public static final int C = 31;

  /** Key code for key D. */
  public static final int D = 32;

  /** Key code for key E. */
  public static final int E = 33;

  /** Key code for key F. */
  public static final int F = 34;

  /** Key code for key G. */
  public static final int G = 35;

  /** Key code for key H. */
  public static final int H = 36;

  /** Key code for key I. */
  public static final int I = 37;

  /** Key code for key J. */
  public static final int J = 38;

  /** Key code for key K. */
  public static final int K = 39;

  /** Key code for key L. */
  public static final int L = 40;

  /** Key code for key M. */
  public static final int M = 41;

  /** Key code for key N. */
  public static final int N = 42;

  /** Key code for key O. */
  public static final int O = 43;

  /** Key code for key P. */
  public static final int P = 44;

  /** Key code for key Q. */
  public static final int Q = 45;

  /** Key code for key R. */
  public static final int R = 46;

  /** Key code for key S. */
  public static final int S = 47;

  /** Key code for key T. */
  public static final int T = 48;

  /** Key code for key U. */
  public static final int U = 49;

  /** Key code for key V. */
  public static final int V = 50;

  /** Key code for key W. */
  public static final int W = 51;

  /** Key code for key X. */
  public static final int X = 52;

  /** Key code for key Y. */
  public static final int Y = 53;

  /** Key code for key Z. */
  public static final int Z = 54;

  // Punctuation
  /** Key code for the Comma key. */
  public static final int COMMA = 55;

  /** Key code for the Period key. */
  public static final int PERIOD = 56;

  // Modifiers
  /** Key code for the left Shift modifier key. */
  public static final int SHIFT_LEFT = 59;

  /** Key code for the right Shift modifier key. */
  public static final int SHIFT_RIGHT = 60;

  // Common controls
  /** Key code for the Tab key. */
  public static final int TAB = 61;

  /** Key code for the Space bar key. */
  public static final int SPACE = 62;

  /** Key code for the Enter/Return key. */
  public static final int ENTER = 66;

  /** Key code for the Backspace key. */
  public static final int BACKSPACE = 67;

  /** Key code for the Escape key. */
  public static final int ESCAPE = 111;

  /** Key code for the Delete key. */
  public static final int DELETE = 112;

  // Function keys
  /** Key code for the F1 function key. */
  public static final int F1 = 131;

  /** Key code for the F2 function key. */
  public static final int F2 = 132;

  /** Key code for the F3 function key. */
  public static final int F3 = 133;

  /** Key code for the F4 function key. */
  public static final int F4 = 134;

  /** Key code for the F5 function key. */
  public static final int F5 = 135;

  /** Key code for the F6 function key. */
  public static final int F6 = 136;

  /** Key code for the F7 function key. */
  public static final int F7 = 137;

  /** Key code for the F8 function key. */
  public static final int F8 = 138;

  /** Key code for the F9 function key. */
  public static final int F9 = 139;

  /** Key code for the F10 function key. */
  public static final int F10 = 140;

  /** Key code for the F11 function key. */
  public static final int F11 = 141;

  /** Key code for the F12 function key. */
  public static final int F12 = 142;

  private Keys() {}

  /**
   * Converts a key code to its human-readable string representation.
   *
   * <p>This method maps key code integers to their string names (e.g., UP, DOWN, A, ENTER). For
   * known keys, it returns the key name. For unknown key codes, it returns a format string
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
