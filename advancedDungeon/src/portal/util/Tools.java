package portal.util;

import core.Entity;
import core.components.PositionComponent;
import portal.portals.abstraction.PortalUtils;

/**
 * Kleine Hilfsfunktionen für das Arbeiten mit Portalen und Spielfiguren.
 *
 * <p>Diese Klasse sammelt nützliche Werkzeuge, die an verschiedenen Stellen im Spiel gebraucht
 * werden können.
 *
 * <p>Die Methoden hier helfen dabei, Informationen aus Objekten auszulesen, ohne den Code unnötig
 * kompliziert zu machen.
 */
public class Tools {

  /**
   * Holt die Positions-Information einer Spielfigur oder eines Objekts.
   *
   * <p>Viele Objekte im Spiel haben eine Position. Diese Methode versucht, die Positionsdaten aus
   * dem übergebenen Objekt zu lesen.
   *
   * <p>Wenn das Objekt keine Position besitzt, wird {@code null} zurückgegeben.
   *
   * @param entity Das Objekt, dessen Position abgefragt werden soll
   * @return Die Position des Objekts oder {@code null}, wenn keine vorhanden ist
   */
  public static PositionComponent getPositionComponent(Entity entity) {
    return entity.fetch(PositionComponent.class).orElse(null);
  }

  /**
   * Liefert das Portal mit dem angegebenen Namen zurück.
   *
   * <p>Im Spiel gibt es verschiedene Portale (z.B. ein blaues und ein grünes Portal). Mit dieser
   * Methode kann eines davon abgefragt werden.
   *
   * <p>Wenn das gewünschte Portal nicht existiert, wird {@code null} zurückgegeben.
   *
   * @param portalName Der Name des Portals
   * @return Das passende Portal-Objekt oder {@code null}, wenn es nicht existiert
   */
  public static Entity getPortal(String portalName) {
    if (portalName.equals(PortalUtils.BLUE_PORTAL_NAME))
      return PortalUtils.getBluePortal().orElse(null);
    else return PortalUtils.getGreenPortal().orElse(null);
  }
}
