package portal.riddles;

import core.Entity;
import core.utils.Point;
import portal.physicsobject.Cube;
import portal.physicsobject.PortalCube;

/**
 * Dein eigener Würfel.
 *
 * <p>Diese Klasse bestimmt, <b>wie ein Würfel aussieht</b> und <b>wie er sich im Spiel verhält</b>.
 *
 * <p>Hier kannst du Eigenschaften festlegen, zum Beispiel:
 *
 * <ul>
 *   <li>wie schwer der Würfel ist
 *   <li>ob man ihn aufheben kann
 *   <li>welches Aussehen (Textur) er hat
 * </ul>
 *
 * <p>Immer wenn im Spiel ein Würfel erzeugt werden soll, wird diese Klasse verwendet.
 */
public class MyCube extends PortalCube {

  /** Die Masse des Würfels (beeinflusst, wie schwer er zu bewegen ist). */
  private float mass = 0.1f;

  /** Gibt an, ob der Würfel aufgehoben werden kann. */
  private boolean isPickupable = false;

  /** Der Pfad zur Grafik des Würfels. */
  private String texture = "portal/portal_cube/portal_cube.png";

  /**
   * Erzeugt einen neuen Würfel an der angegebenen Position.
   *
   * <p>Hier wird der Würfel mit den oben festgelegten Eigenschaften erstellt und im Spiel
   * platziert.
   *
   * @param spawn Die Position, an der der Würfel erscheinen soll
   * @return Der erzeugte Würfel als {@link Entity}
   */
  @Override
  public Entity spawn(Point spawn) {
    return Cube.portalCube(new Point(0, 0), mass, isPickupable, texture);
  }
}
