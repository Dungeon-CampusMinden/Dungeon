package portal.laser;

import core.Component;

/**
 * Similar to the {@link portal.util.ToggleableComponent ToggleableComponent} it stores a boolean to
 * determine if the laser is active. Also used to determine a group of laser related entities.
 */
public class LaserComponent implements Component {

  private boolean active;

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
}
