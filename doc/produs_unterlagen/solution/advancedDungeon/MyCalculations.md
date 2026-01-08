

```java
private static final float FORCE_MAGNITUDE = 20f;


public Point calculatePortalExit(Entity portal) {
    Entity otherPortal;

    if (portal.name().equals(PortalUtils.BLUE_PORTAL_NAME))
        otherPortal = Tools.getPortal(PortalUtils.GREEN_PORTAL_NAME);
    else otherPortal = Tools.getPortal(PortalUtils.BLUE_PORTAL_NAME);

    PositionComponent pc = Tools.getPositionComponent(otherPortal);
    Direction direction = pc.viewDirection();
    return pc.position().translate(direction);
}

public Point calculateLightWallAndBridgeEnd(
    Point from, Direction beamDirection, LevelElement[] stoppingTiles) {
    Point lastPoint = from;
    Point currentPoint = from;

    while (true) {
        Tile currentTile = Tools.tileAt(currentPoint);
        if (currentTile == null) break;
        if (Arrays.asList(stoppingTiles).contains(currentTile.levelElement())) break;
        lastPoint = currentPoint;
        currentPoint = currentPoint.translate(beamDirection);
    }
    return lastPoint;
}

public Vector2 beamForce(Direction direction) {
    return Vector2.of(direction.x() * FORCE_MAGNITUDE, direction.y() * FORCE_MAGNITUDE);
}

public Vector2 reversedBeamForce(Direction direction) {
    return this.beamForce(direction).scale(-1);
}
```
