package portal.riddles;

import portal.physicsobject.CubeSpawner;

/**
 * Deine eigene Würfel-Spawner-Klasse.
 *
 * <p>Diese Klasse wird aufgerufen, wenn ein Schalter im Level betätigt wird.
 *
 * <p>Hier bestimmst du, <b>was passieren soll</b>, wenn der Schalter ausgelöst wird – zum Beispiel:
 *
 * <ul>
 *   <li>einen Würfel erscheinen lassen
 *   <li>mehrere Würfel erzeugen
 *   <li>Kugeln spawnen
 * </ul>
 */
public class MyCubeSpawner extends CubeSpawner {

  /**
   * Wird aufgerufen, wenn der Schalter aktiviert wird.
   *
   * <p>Schreibe hier deinen Code hinein, um Objekte im Spiel erscheinen zu lassen.
   */
  @Override
  public void spawn() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
