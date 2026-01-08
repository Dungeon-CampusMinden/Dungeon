package portal.riddles;

import core.Entity;
import core.utils.Point;
import portal.physicsobject.PortalSphere;
import portal.physicsobject.Sphere;

public class MySphere extends PortalSphere {

  private float mass = 5f;
  private boolean isPickupable = true;
  private String texture = "portal/pellet_launcher/pellet_launcher.png";

  public Entity spawn(Point spawn) {
    return Sphere.portalSphere(spawn, mass, isPickupable, texture);
  }
}
