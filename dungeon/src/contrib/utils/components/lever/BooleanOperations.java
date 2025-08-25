package contrib.utils.components.lever;

import contrib.components.LeverComponent;

/**
 * Utility methods for evaluating boolean logic operations on {@link LeverComponent} instances.
 *
 * <p>This class provides common logic gates for two levers, such as AND, OR, XOR, NAND, NOR, and
 * XNOR, as well as a NOT operation for a single lever.
 */
public class BooleanOperations {

  /**
   * Returns {@code true} if both levers are on.
   *
   * @param a First Levercomponent
   * @param b Second Levercomponent
   * @return if both levers are on.
   */
  public static boolean and(LeverComponent a, LeverComponent b) {
    return a.isOn() && b.isOn();
  }

  /**
   * Returns {@code true} if at least one lever is on.
   *
   * @param a First Levercomponent
   * @param b Second Levercomponent
   * @return if at least one lever is on.
   */
  public static boolean or(LeverComponent a, LeverComponent b) {
    return a.isOn() || b.isOn();
  }

  /**
   * Returns {@code true} if exactly one lever is on.
   *
   * @param a First Levercomponent
   * @param b Second Levercomponent
   * @return if exactly one lever is on.
   */
  public static boolean xor(LeverComponent a, LeverComponent b) {
    return a.isOn() ^ b.isOn();
  }

  /**
   * Returns {@code true} if not both levers are on. Equivalent to {@code !and(a, b)}.
   *
   * @param a First Levercomponent
   * @param b Second Levercomponent
   * @return if not both levers are on
   */
  public static boolean nand(LeverComponent a, LeverComponent b) {
    return !(a.isOn() && b.isOn());
  }

  /**
   * Returns {@code true} if neither lever is on. Equivalent to {@code !or(a, b)}. *
   *
   * @param a First Levercomponent
   * @param b Second Levercomponent
   * @return if neither lever is on
   */
  public static boolean nor(LeverComponent a, LeverComponent b) {
    return !(a.isOn() || b.isOn());
  }

  /**
   * Returns {@code true} if both levers are in the same state (both on or both off). *
   *
   * @param a First Levercomponent
   * @param b Second Levercomponent
   * @return if both levers are in the same state
   */
  public static boolean xnor(LeverComponent a, LeverComponent b) {
    return a.isOn() == b.isOn();
  }

  /**
   * Returns {@code true} if the lever is off. *
   *
   * @param lever Levercomponent to check
   * @return if the lever is off
   */
  public static boolean not(LeverComponent lever) {
    return !lever.isOn();
  }
}
