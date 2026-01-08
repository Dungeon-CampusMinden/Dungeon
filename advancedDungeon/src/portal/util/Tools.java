package portal.util;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.utils.Point;

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
   * Holt den Schalter-Zustand eines Objekts.
   *
   * <p>Einige Objekte im Spiel können an- oder ausgeschaltet werden, zum Beispiel Pellet-Catcher.
   * Diese Methode versucht, die entsprechende Schalter-Komponente aus dem übergebenen Objekt zu
   * lesen.
   *
   * <p>Wenn das Objekt keinen Schalter-Zustand besitzt, wird {@code null} zurückgegeben.
   *
   * @param entity Das Objekt, dessen Schalter-Zustand abgefragt werden soll
   * @return Die {@link ToggleableComponent} des Objekts oder {@code null}, wenn keine vorhanden ist
   */
  public static ToggleableComponent getToggleComponent(Entity entity) {
    return entity.fetch(ToggleableComponent.class).orElse(null);
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

  /**
   * Gibt das Spielfeld-Feld an einer bestimmten Position zurück.
   *
   * <p>Das Spiel besteht aus vielen einzelnen Feldern (Tiles). Mit dieser Methode kannst du
   * herausfinden, <b>welches Feld</b> sich an einer bestimmten Position befindet.
   *
   * <p>Das ist besonders nützlich, wenn du prüfen möchtest, ob sich dort zum Beispiel eine Wand,
   * ein Portal oder ein anderes Hindernis befindet.
   *
   * <p>Wenn sich an der Position kein Feld befindet, wird {@code null} zurückgegeben.
   *
   * @param point Die Position, an der nach einem Feld gesucht wird
   * @return Das Feld an dieser Position oder {@code null}, wenn es keines gibt
   */
  public static Tile tileAt(Point point) {
    return Game.tileAt(point).orElse(null);
  }
}
