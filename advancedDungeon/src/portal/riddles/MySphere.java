package portal.riddles;

import core.Entity;
import core.utils.Point;
import portal.physicsobject.PortalSphere;
import portal.physicsobject.Sphere;

/**
 * Deine eigene Kugel.
 *
 * <p>Diese Klasse bestimmt, <b>wie sich eine Kugel im Spiel verhält</b> und <b>wie sie
 * aussieht</b>.
 *
 * <p>Hier kannst du Eigenschaften festlegen, zum Beispiel:
 *
 * <ul>
 *   <li>wie schwer die Kugel ist
 *   <li>ob man sie aufheben kann
 *   <li>welche Grafik (Textur) sie verwendet
 * </ul>
 *
 * <p>Immer wenn im Spiel eine Kugel erzeugt werden soll, wird diese Klasse verwendet.
 */
public class MySphere extends PortalSphere {

  /** Die Masse der Kugel (beeinflusst, wie leicht sie rollt). */
  private float mass = 0.1f;

  /** Gibt an, ob die Kugel aufgehoben werden kann. */
  private boolean isPickupable = false;

  /** Der Pfad zur Grafik der Kugel. */
  private String texture = "portal/kubus/kubus.png";

  /**
   * Erzeugt eine neue Kugel an der angegebenen Position.
   *
   * <p>Hier wird die Kugel mit den oben festgelegten Eigenschaften erstellt und im Spiel platziert.
   *
   * @param spawn Die Position, an der die Kugel erscheinen soll
   * @return Die erzeugte Kugel als {@link Entity}
   */
  @Override
  public Entity spawn(Point spawn) {
    return Sphere.portalSphere(new Point(0, 0), mass, isPickupable, texture);
  }
}
