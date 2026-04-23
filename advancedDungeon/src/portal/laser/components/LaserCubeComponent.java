package portal.laser.components;

import core.Component;
import portal.laser.LaserCubeStatus;

/** Holder component for the LaserCube. */
public class LaserCubeComponent implements Component {

  private boolean active = false;
  private LaserCubeStatus currentStatus = LaserCubeStatus.NONE;

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

  public LaserCubeStatus getCurrentStatus() {
    return currentStatus;
  }

  public void setCurrentStatus(LaserCubeStatus currentStatus) {
    this.currentStatus = currentStatus;
  }
}
