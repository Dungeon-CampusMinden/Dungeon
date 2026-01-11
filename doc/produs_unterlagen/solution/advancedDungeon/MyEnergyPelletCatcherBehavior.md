# Lösung für MyEnergyPelletCatcherBehavior

# Energiegeschoss abfangen

Diese Funktion beschreibt das Verhalten eines Energiegeschoss-Fängers. Beim Abfangen wird der Zustand des Fängers umgeschaltet und das Energiegeschoss aus dem Spiel entfernt.

```java
public void catchPellet(Entity catcher, Entity pellet) {
  Tools.getToggleComponent(catcher).toggle();
  Game.remove(pellet);
}
