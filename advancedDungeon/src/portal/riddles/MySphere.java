package portal.riddles;

import core.Entity;
import core.utils.Point;
import portal.physicsobject.PortalSphere;
import portal.physicsobject.Sphere;

public class MySphere extends PortalSphere {

  private float mass = 5f;
  private boolean isPickupable = false;
  private String texture = "portal/kubus/kubus.png";

  public Entity spawn(Point spawn) {
    return Sphere.portalSphere(spawn, mass, isPickupable, texture);
  }
}
