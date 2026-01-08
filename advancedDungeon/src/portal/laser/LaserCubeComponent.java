package portal.laser;

import core.Component;

/** Holder component for the LaserCube. */
public class LaserCubeComponent implements Component {

  private boolean active = false;

  /**
   * Returns the state of the laserCube.
   *
   * @return state of the laserCube.
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Sets the state of the laserCube.
   *
   * @param active true if active, otherwise false.
   */
  public void setActive(boolean active) {
    this.active = active;
  }
}
