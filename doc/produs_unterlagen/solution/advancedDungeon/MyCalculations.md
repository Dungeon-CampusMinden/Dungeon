# Lösung für MyPortalUtils

## Portal-Austritt berechnen

Diese Funktion bestimmt die Austrittsposition eines Portals. Abhängig davon, welches Portal betreten wird, wird das jeweils andere Portal verwendet und die Position einen Schritt in Blickrichtung verschoben.
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

##Endpunkt von Lichtwand oder Brücke berechnen

Diese Funktion berechnet den letzten freien Punkt in einer gegebenen Richtung, bevor ein blockierendes Level-Element erreicht wird. Sie wird für Lichtwände und Lichtbrücken verwendet.
```java
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
```
##Traktorstrahl-Kraft berechnen

```java
private static final float FORCE_MAGNITUDE = 20f;
public Vector2 beamForce(Direction direction) {
    return Vector2.of(direction.x() * FORCE_MAGNITUDE, direction.y() * FORCE_MAGNITUDE);
}
public Vector2 reversedBeamForce(Direction direction) {
    return this.beamForce(direction).scale(-1);
}
```
