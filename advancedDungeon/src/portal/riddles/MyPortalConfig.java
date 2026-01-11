package portal.riddles;

import core.utils.Point;
import java.util.function.Supplier;
import portal.controlls.Hero;
import portal.portals.abstraction.PortalConfig;

/**
 * Eine konkrete Implementierung von {@link PortalConfig}, mit der der Portal-Skill des Helden
 * konfiguriert wird.
 *
 * <p>Diese Klasse bestimmt, <b>wie sich der Portal-Schuss verhält</b>, zum Beispiel wie oft er
 * benutzt werden kann, wie schnell er ist, wie weit er reicht und auf welchen Punkt er geschossen
 * wird.
 *
 * <p>Die Werte in dieser Klasse können verändert werden, um unterschiedliche Portal-Verhalten
 * auszuprobieren.
 */
public class MyPortalConfig extends PortalConfig {

  /** Der Held, zu dem diese Portal-Konfiguration gehört. */
  private Hero hero;

  /**
   * Erstellt eine neue Portal-Konfiguration für einen Helden.
   *
   * @param hero Der Held, dessen Portal-Schuss konfiguriert wird
   */
  public MyPortalConfig(Hero hero) {
    super(hero);
    this.hero = hero;
  }

  /**
   * Bestimmt die Abklingzeit (Cooldown) des Portal-Schusses.
   *
   * <p>Der zurückgegebene Wert gibt an, wie viele Millisekunden gewartet werden müssen, bis der
   * Portal-Schuss erneut ausgeführt werden kann.
   *
   * <p>Große Werte bedeuten lange Wartezeiten, kleine Werte bedeuten kurze Wartezeiten.
   *
   * @return Die Abklingzeit in Millisekunden
   */
  @Override
  public long cooldown() {
    return 300;
  }

  /**
   * Bestimmt die Geschwindigkeit des Portal-Schusses.
   *
   * <p>Dieser Wert beeinflusst, wie schnell sich der Portal-Schuss in Richtung seines Zielpunkts
   * bewegt.
   *
   * @return Die Geschwindigkeit des Portal-Schusses
   */
  @Override
  public float speed() {
    return 1;
  }

  /**
   * Bestimmt die Reichweite des Portal-Schusses.
   *
   * <p>Die Reichweite gibt an, wie weit entfernt der Zielpunkt maximal sein darf.
   *
   * @return Die Reichweite des Portal-Schusses
   */
  @Override
  public float range() {
    return 1;
  }

  /**
   * Legt den Zielpunkt des Portal-Schusses fest.
   *
   * <p>Diese Methode liefert eine Funktion, die bestimmt, <b>auf welchen Punkt der Portal-Schuss
   * zielt</b>.
   *
   * <p>Der zurückgegebene {@link Point} beschreibt die Position, die der Portal-Schuss anfliegen
   * soll. Auf Basis dieses Punktes berechnet die Engine anschließend die Flugbahn.
   *
   * @return Eine Funktion, die den Zielpunkt des Portal-Schusses liefert
   */
  @Override
  public Supplier<Point> target() {
    return () -> new Point(0, 0);
  }
}
