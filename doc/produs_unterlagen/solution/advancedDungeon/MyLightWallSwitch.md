# Lösung für MyLightWallSwitch

# Lichtwand aktivieren

Diese Funktion schaltet eine Lichtwand ein/aus, indem der zugehörige Emitter aktiviert/deaktivert wird.

```java
@Override
public void activate(Entity emitter) {
  LightWallFactory.activate(emitter);
}
@Override
public void deactivate(Entity emitter) {
    LightWallFactory.deactivate(emitter);
}

```
