package contrib.components;

import core.Component;

/**
 * Component that tracks the total mass currently on a pressure plate.
 *
 * <p>The pressure plate is considered triggered if the total mass on it meets or exceeds a
 * configurable mass threshold.
 *
 * <p>Mass can be increased or decreased as entities step on or off the plate.
 */
public class PressurePlateComponent implements Component {

  /** Default mass threshold to trigger the pressure plate. */
  public static final float DEFAULT_MASS_TRIGGER = 0.1f;

  /** Total mass currently on the pressure plate. */
  private float currentMass = 0f;

  /** Mass threshold that triggers the pressure plate. */
  private final float massTrigger;

  /**
   * Creates a pressure plate component with a specified mass trigger threshold.
   *
   * @param massTrigger the mass threshold to trigger the plate
   */
  public PressurePlateComponent(float massTrigger) {
    this.massTrigger = massTrigger;
  }

  /** Creates a pressure plate component with the default mass trigger threshold. */
  public PressurePlateComponent() {
    this(DEFAULT_MASS_TRIGGER);
  }

  /**
   * Adds the specified mass to the pressure plate.
   *
   * @param mass the mass to add when an entity steps onto the plate
   */
  public void increase(float mass) {
    currentMass += mass;
  }

  /**
   * Removes the specified mass from the pressure plate, never dropping below zero.
   *
   * @param mass the mass to remove when an entity steps off the plate
   */
  public void decrease(float mass) {
    currentMass -= mass;
    if (currentMass < 0f) currentMass = 0f;
  }

  /**
   * Returns the total mass currently on the pressure plate.
   *
   * @return the current total mass
   */
  public float currentMass() {
    return currentMass;
  }

  /**
   * Returns the mass threshold that triggers the pressure plate.
   *
   * @return the mass trigger threshold
   */
  public float massTrigger() {
    return massTrigger;
  }

  /**
   * Checks whether the pressure plate is currently triggered.
   *
   * @return true if the current mass is greater than or equal to the trigger threshold, false
   *     otherwise
   */
  public boolean isTriggered() {
    return currentMass >= massTrigger;
  }
}
