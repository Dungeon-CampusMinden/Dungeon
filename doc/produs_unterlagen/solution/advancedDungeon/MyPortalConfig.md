# Lösung für MyPortalConfig

# Portal-Konfiguration

Diese Konfiguration legt die Eigenschaften des Portals fest. Durch Anpassen der Werte können Abklingzeit, Geschwindigkeit, Reichweite und Zielposition des Portals verändert werden.

```java
@Override
public long cooldown() {
  return 500;
}

@Override
public float speed() {
  return 10;
}

@Override
public float range() {
  return Integer.MAX_VALUE;
}

@Override
public Supplier<Point> target() {
  return () -> hero.getMousePosition();
}
```
