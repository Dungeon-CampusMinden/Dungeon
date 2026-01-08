package portal.laser;

import core.Component;
import core.Entity;
import core.utils.Direction;
import core.utils.Point;
import java.util.ArrayList;
import java.util.List;
import portal.portals.components.PortalExtendComponent;

/** Helper component for the laser. */
public class LaserComponent implements Component {

  private final Direction direction;
  private List<Entity> segments = new ArrayList<>();
  private boolean active = false;
  private boolean throughCube = false;

  /**
   * Creates a LaserComponent with its original direction.
   *
   * @param direction original direction of the laser
   */
  public LaserComponent(Direction direction) {
    this.direction = direction;
  }

  /**
   * Returns the direction of the original laser.
   *
   * @return direction of the laser.
   */
  public Direction getDirection() {
    return direction;
  }

  /**
   * Returns the laser's activation state.
   *
   * @return true if laser is active, otherwise false.
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Sets the laser's activation state.
   *
   * @param value the new active state of the laser.
   */
  public void setActive(boolean value) {
    this.active = value;
  }

  /**
   * Returns all the entities of the laser.
   *
   * @return list of all entities for this laser.
   */
  public List<Entity> getSegments() {
    return segments;
  }

  /**
   * Extends the laser.
   *
   * @param direction direction the laser is getting extending into.
   * @param from starting point of the laser.
   * @param pec helper component for extending and retracting the laser.
   */
  public void extend(Direction direction, Point from, PortalExtendComponent pec) {
    LaserFactory.extendLaser(direction, from, this.segments, pec, this);
  }

  /** Retracts the laser. */
  public void trim() {
    LaserFactory.trimAfterFirstEmitter(this.segments);
  }

  /**
   * Returns whether the laser is going through the cube or not.
   *
   * @return true if laser is going through the cube, otherwise false.
   */
  public boolean isThroughCube() {
    return throughCube;
  }

  /**
   * Sets if the laser is going through the cube.
   *
   * @param throughCube new throughCube state.
   */
  public void setThroughCube(boolean throughCube) {
    this.throughCube = throughCube;
  }
}
