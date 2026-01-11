# Lösung für MyTractorBeamLever

# Schubrichtung des Traktorstrahls ändern

Diese Funktion kehrt die Schubrichtung des Traktorstrahls um. Sie wird verwendet, um die Wirkungsrichtung des Traktorstrahls dynamisch zu verändern.

```java
@Override
public void reverse(Entity tractorBeam) {
  TractorBeamFactory.reverse(tractorBeam);
}
```
