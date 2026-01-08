```java
  private float mass = 20f;
  private boolean isPickupable = true;
  private String texture = "portal/kubus/kubus.png";

  public Entity spawn(Point spawn) {
    return Sphere.portalSphere(spawn, mass, isPickupable, texture);
  }

```
