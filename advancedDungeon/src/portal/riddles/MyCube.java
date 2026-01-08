package portal.riddles;

import core.Entity;
import core.utils.Point;
import portal.physicsobject.Cube;

public class MyCube extends Cube {

  private float mass = 0f;
  private boolean isPickupable = false;
  private String texture = "portal/portal_cube/portal_cube.png";
  private Entity cube;
  public MyCube(Point spawn) {
   cube= Cube.portalCube(spawn, mass, isPickupable, texture);
  }

  public Entity cube(){
    return this.cube;
  }
}
