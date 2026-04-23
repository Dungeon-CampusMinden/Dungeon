package portal.laser.components;

import core.Component;
import core.utils.Direction;

/** Holder component for laser entities that are extended via the portals. */
public class LaserPartComponent implements Component {

  private Direction laserDirection;

  public LaserPartComponent(Direction laserDirection) {
    this.laserDirection = laserDirection;
  }

  public Direction getLaserDirection() {
    return laserDirection;
  }
}
