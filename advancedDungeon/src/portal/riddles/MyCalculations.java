package portal.riddles;

import core.Entity;
import core.utils.Point;
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
    throw new UnsupportedOperationException("Da stimmt etwas nicht mit meinen Berechnungen.");
  }
}
