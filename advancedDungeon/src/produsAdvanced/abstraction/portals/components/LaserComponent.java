package produsAdvanced.abstraction.portals.components;

import core.Component;
import core.Entity;
import core.utils.Direction;
import core.utils.Point;
import entities.LaserFactory;
import java.util.ArrayList;
import java.util.List;

public class LaserComponent implements Component {

  private final Direction direction;
  private List<Entity> segments = new ArrayList<>();
  private boolean active = false;

  public LaserComponent(Direction direction) {
    this.direction = direction;
  }

  public Direction getDirection() {
    return direction;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean value) {
    this.active = value;
  }

  public List<Entity> getSegments() {
    return segments;
  }

  public void extend(Direction direction, Point from, PortalExtendComponent pec) {
    LaserFactory.extendLaser(direction, from, this.segments, pec, this);
  }

  public void trim() {
    LaserFactory.trimAfterFirstEmitter(this.segments);
  }
}
