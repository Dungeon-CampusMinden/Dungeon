package portal.riddles;

import core.Entity;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import portal.portals.abstraction.Calculations;

/**
 * Eine konkrete Implementierung von {@link Calculations}, die grundlegende Berechnungen für das
 * Spiel enthält.
 *
 * <p>Diese Klasse ist als <b>zentrale Stelle für eigene Berechnungslogik</b> gedacht. Hier können
 * zum Beispiel Positionen, Abstände oder Zielpunkte berechnet werden.
 *
 * <p>Im Laufe des Spiels oder der Aufgaben können hier weitere Berechnungsmethoden hinzukommen.
 */
public class MyCalculations extends Calculations {

  /**
   * Berechnet die Position, an der ein Objekt nach einer Portal-Reise wieder erscheint.
   *
   * <p>Diese Methode wird von der Spiel-Engine aufgerufen, wenn ein {@link Entity}-Objekt ein
   * Portal benutzt.
   *
   * <p>Deine Aufgabe ist es, eine neue {@link Point}-Position zu berechnen und zurückzugeben.
   *
   * <p>Du kannst zum Beispiel:
   *
   * <ul>
   *   <li>die aktuelle Position des Portals als Ausgangspunkt nehmen
   *   <li>einen festen Versatz hinzufügen (z.B. +5 nach rechts)
   *   <li>eine komplett neue Zielposition bestimmen
   * </ul>
   *
   * @param portal Das Portal, das benutzt wurde
   * @return Die berechnete Zielposition als {@link Point}
   */
  public Point calculatePortalExit(Entity portal) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Berechnet, wie weit sich eine Lichtwand ausbreiten darf.
   *
   * <p>Diese Methode wird verwendet, um herauszufinden, <b>wo eine Lichtwand enden soll</b>.
   *
   * <p>Die Lichtwand startet an einem Punkt und breitet sich Schritt für Schritt in eine bestimmte
   * Richtung aus. Sie stoppt, sobald sie auf ein Feld trifft, das sie nicht durchdringen darf (z.B.
   * eine Wand).
   *
   * <p>Deine Aufgabe ist es, den letzten Punkt zu berechnen, den die Lichtwand noch erreichen kann.
   *
   * <p>Tipp:
   *
   * <ul>
   *   <li>Bewege dich vom Startpunkt immer weiter in die Richtung
   *   <li>Prüfe bei jedem Schritt, welches Feld dort liegt
   *   <li>Stoppe, sobald ein verbotenes Feld erreicht wird
   * </ul>
   *
   * @param from Der Startpunkt der Lichtwand
   * @param beamDirection Die Richtung, in die sich die Lichtwand ausbreitet
   * @param stoppingTiles Felder, an denen die Lichtwand stoppen muss
   * @return Der letzte Punkt, den die Lichtwand noch erreichen darf
   */
  public Point calculateLightWallAndBridgeEnd(
      Point from, Direction beamDirection, LevelElement[] stoppingTiles) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Bestimmt die Kraft, mit der der Traktorstrahl Objekte bewegt.
   *
   * <p>Diese Methode wird aufgerufen, wenn sich ein Objekt im Traktorstrahl befindet.
   *
   * <p>Du legst hier fest, <b>in welche Richtung</b> und <b>wie stark</b> das Objekt geschoben oder
   * gezogen wird.
   *
   * <p>Die Richtung ergibt sich aus dem übergebenen {@link Direction}-Wert, die Stärke bestimmst du
   * über die Länge des zurückgegebenen Vektors.
   *
   * <p>Beispiele:
   *
   * <ul>
   *   <li>Eine kleine Kraft → Objekt bewegt sich langsam
   *   <li>Eine große Kraft → Objekt bewegt sich schnell
   * </ul>
   *
   * @param direction Die Richtung, in die der Traktorstrahl zeigt
   * @return Ein {@link Vector2}, der die Kraft des Traktorstrahls beschreibt
   */
  public Vector2 beamForce(Direction direction) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Bestimmt die entgegengesetzte Kraft des Traktorstrahls.
   *
   * <p>Diese Methode wird verwendet, wenn ein Objekt <b>in die andere Richtung</b> bewegt werden
   * soll, zum Beispiel beim Zurückstoßen oder Herausziehen aus dem Strahl.
   *
   * <p>Meist ist diese Kraft genau das Gegenteil von {@link #beamForce(Direction)}. Du kannst sie
   * aber auch schwächer oder stärker machen, um besondere Effekte zu erzeugen.
   *
   * @param direction Die ursprüngliche Richtung des Traktorstrahls
   * @return Ein {@link Vector2}, der die entgegengesetzte Kraft beschreibt
   */
  public Vector2 reversedBeamForce(Direction direction) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
