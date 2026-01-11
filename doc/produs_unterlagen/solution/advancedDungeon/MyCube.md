# Lösung für MyCube

# Cube erzeugen

Diese Funktion erzeugt einen Portal-Würfel an einer gegebenen Position. Die Eigenschaften des Würfels wie Masse, Aufhebbarkeit und Textur können über die Konstanten der Klasse angepasst werden.

```java
private float mass = 20f;
private boolean isPickupable = true;
private String texture = "portal/portal_cube/portal_cube.png";

public Entity spawn(Point spawn) {
  return Cube.portalCube(spawn, mass, isPickupable, texture);
}
