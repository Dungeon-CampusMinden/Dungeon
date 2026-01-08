```java
  private float mass = 20f;
  private boolean isPickupable = true;
  private String texture = "portal/portal_cube/portal_cube.png";

  public Entity spawn(Point spawn) {
    return Cube.portalCube(spawn, mass, isPickupable, texture);
  }

```
