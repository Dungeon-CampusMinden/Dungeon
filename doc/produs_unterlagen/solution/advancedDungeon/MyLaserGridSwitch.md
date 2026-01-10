# Lösung für MyLaserGridSwitch

# Lasergitter aktivieren

Diese Funktion aktiviert/deaktiviert alle übergebenen Lasergitter, indem deren Lasergitter-Komponenten eingeschaltet werden.

```java
@Override
public void activate(Entity[] grid) {
  for (Entity laser : grid) Tools.getLaserGridComponent(laser).activate();
}

@Override
public void deactivate(Entity[] grid) {
  for (Entity laser : grid) Tools.getLaserGridComponent(laser).deactivate();
}
```
