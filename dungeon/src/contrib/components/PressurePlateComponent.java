package contrib.components;

import core.Component;

/**
 * A component that allows a pressure plate to track how many entities are currently standing on it.
 *
 * <p>The number of entities currently on the pressure plate can be queried with {@link
 * #standingCount()}, and presence can be checked with {@link #atLeastOne()}.
 */
public class PressurePlateComponent implements Component {

  /** The number of entities currently standing on the pressure plate. */
  private int standingCount = 0;

  /**
   * Increases the standing count by one. Should be called when an entity steps on the pressure
   * plate.
   */
  public void increase() {
    standingCount++;
  }

  /**
   * Decreases the standing count by one, but never below zero. Should be called when an entity
   * steps off the pressure plate.
   */
  public void decrease() {
    if (standingCount > 0) standingCount--;
  }

  /**
   * Returns the current number of entities standing on the pressure plate.
   *
   * @return the standing count
   */
  public int standingCount() {
    return standingCount;
  }

  /**
   * Returns whether at least one entity is standing on the pressure plate.
   *
   * @return {@code true} if one or more entities are on the plate, {@code false} otherwise
   */
  public boolean atLeastOne() {
    return standingCount > 0;
  }
}
