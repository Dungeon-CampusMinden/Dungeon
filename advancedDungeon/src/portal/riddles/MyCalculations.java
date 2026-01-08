package portal.riddles;

import core.Entity;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import java.util.Arrays;
import portal.portals.abstraction.Calculations;
import portal.riddles.utils.PortalUtils;
import portal.riddles.utils.Tools;

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

  private static final float FORCE_MAGNITUDE = 20f;

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
    Entity otherPortal;

    if (portal.name().equals(PortalUtils.BLUE_PORTAL_NAME))
      otherPortal = Tools.getPortal(PortalUtils.GREEN_PORTAL_NAME);
    else otherPortal = Tools.getPortal(PortalUtils.BLUE_PORTAL_NAME);

    PositionComponent pc = Tools.getPositionComponent(otherPortal);
    Direction direction = pc.viewDirection();
    return pc.position().translate(direction);
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
    Point lastPoint = from;
    Point currentPoint = from;

    while (true) {
      Tile currentTile = Tools.tileAt(currentPoint);
      if (currentTile == null) break;
      System.out.println("TEST");
      if (Arrays.asList(stoppingTiles).contains(currentTile.levelElement())) break;
      lastPoint = currentPoint;
      currentPoint = currentPoint.translate(beamDirection);
    }
    System.out.println("LP" + lastPoint);
    return lastPoint;
  }

  @Override
  public Vector2 beamForce(Direction direction) {
    return Vector2.of(direction.x() * FORCE_MAGNITUDE, direction.y() * FORCE_MAGNITUDE);
  }

  @Override
  public Vector2 reversedBeamForce(Direction direction) {
    return this.beamForce(direction).scale(-1);
  }
}
