package portal.laser;

import core.Component;
import core.Entity;

/** Holder component for the LaserCube. */
public class LaserCubeComponent implements Component {

  private boolean active = false;
  private boolean isBeingMoved = false;
  private LaserCubeStatus currentStatus = LaserCubeStatus.NONE;
  private Entity onEnterCube;
  private Entity onEnterLaser;
  private Entity onLeaveCube;
  private Entity onLeaveLaser;


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

  public LaserCubeStatus getCurrentStatus() {
    return currentStatus;
  }

  public void setCurrentStatus(LaserCubeStatus currentStatus) {
    this.currentStatus = currentStatus;
  }

  public Entity getOnEnterCube() {
    return onEnterCube;
  }

  public void setOnEnterCube(Entity onEnterCube) {
    this.onEnterCube = onEnterCube;
  }

  public Entity getOnEnterLaser() {
    return onEnterLaser;
  }

  public void setOnEnterLaser(Entity onEnterLaser) {
    this.onEnterLaser = onEnterLaser;
  }

  public Entity getOnLeaveCube() {
    return onLeaveCube;
  }

  public void setOnLeaveCube(Entity onLeaveCube) {
    this.onLeaveCube = onLeaveCube;
  }

  public Entity getOnLeaveLaser() {
    return onLeaveLaser;
  }

  public void setOnLeaveLaser(Entity onLeaveLaser) {
    this.onLeaveLaser = onLeaveLaser;
  }
}
