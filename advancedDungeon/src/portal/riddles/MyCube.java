package portal.riddles;

import core.Entity;
import core.utils.Point;
import core.utils.Vector2;
import portal.physicsobject.Cube;
import portal.physicsobject.PortalCube;

public class MyCube extends PortalCube {

  private float mass = 20f;
  private boolean isPickupable = true;
  private String texture = "portal/portal_cube/portal_cube.png";

  public Entity spawn(Point spawn) {
    return Cube.portalCube(spawn.translate(Vector2.of(0, 0)), mass, isPickupable, texture);
  }
}
