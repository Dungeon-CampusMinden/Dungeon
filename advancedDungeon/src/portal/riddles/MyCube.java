package portal.riddles;

import core.Entity;
import core.utils.Point;
import portal.physicsobject.Cube;
import portal.physicsobject.PortalCube;

public class MyCube extends PortalCube {

  private float mass = 5f;
  private boolean isPickupable = true;
  private String texture = "portal/portal_cube/portal_cube.png";

  public Entity spawn(Point spawn) {
    return Cube.portalCube(spawn, mass, isPickupable, texture);
  }
}
