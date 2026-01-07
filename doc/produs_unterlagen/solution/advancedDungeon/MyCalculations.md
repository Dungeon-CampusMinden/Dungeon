

```java
  public Point calculatePortalExit(Entity portal) {
    Entity otherPortal;
    if (portal.name().equals(PortalUtils.BLUE_PORTAL_NAME))
      otherPortal = Tools.getPortal(PortalUtils.GREEN_PORTAL_NAME);
    else otherPortal = Tools.getPortal(PortalUtils.BLUE_PORTAL_NAME);
    PositionComponent pc = Tools.getPositionComponent(otherPortal);
    Direction direction = pc.viewDirection();
    return pc.position().translate(direction);
  }

```
