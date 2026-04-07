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

  private float mass = 20f;
  private boolean isPickupable = true;
  private String texture = "portal/portal_cube/portal_cube.png";

  public Entity spawn(Point spawn) {
    return Cube.portalCube(spawn, mass, isPickupable, texture);
  }
}
