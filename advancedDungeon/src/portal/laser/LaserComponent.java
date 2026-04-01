package portal.laser;

import core.Component;

/**
 * Similar to the {@link portal.util.ToggleableComponent ToggleableComponent} it stores a boolean to
 * determine if the laser is active. Also used to determine a group of laser related entities.
 */
public class LaserComponent implements Component {

  private boolean active;
  private boolean isBeingDeactivated;

  /**
   * Creates a LaserComponent with a given state.
   *
   * @param active true if active, otherwise false.
   */
  public LaserComponent(boolean active) {
    this.active = active;
  }

  /**
   * Returns the state of the laser.
   *
   * @return true if active, otherwise false.
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Sets the new state of the LaserComponent.
   *
   * @param active new state of the component.
   */
  public void setActive(boolean active) {
    this.active = active;
  }

  /**
   * Returns if the laser is already being deactivated.
   *
   * @return true if the laser is being deactivated already, otherwise false.
   */
  public boolean isBeingDeactivated() {
    return isBeingDeactivated;
  }

  /**
   * Sets the state if the laser counts as being deactivated or not.
   *
   * @param beingDeactivated true if laser is being deactivated after this, otherwise false.
   */
  public void setBeingDeactivated(boolean beingDeactivated) {
    isBeingDeactivated = beingDeactivated;
  }
}
