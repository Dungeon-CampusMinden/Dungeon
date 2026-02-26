package portal.laser;

import core.Component;

/** Holder component for the LaserCube. */
public class LaserCubeComponent implements Component {

  private boolean active = false;
  private boolean isBeingMoved = false;

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

  /**
   * Returns if the cube is getting moved by a player.
   *
   * @return true if the cube is getting moved, otherwise false.
   */
  public boolean isBeingMoved() {
    return isBeingMoved;
  }

  /**
   * Sets if the cube is getting moved by a player.
   *
   * @param beingMoved true if the cube is getting moved, otherwise false.
   */
  public void setBeingMoved(boolean beingMoved) {
    isBeingMoved = beingMoved;
  }
}
