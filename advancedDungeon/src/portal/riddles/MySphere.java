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

  private float mass = 20f;
  private boolean isPickupable = true;
  private String texture = "portal/kubus/kubus.png";

  public Entity spawn(Point spawn) {
    return Sphere.portalSphere(spawn, mass, isPickupable, texture);
  }
}
