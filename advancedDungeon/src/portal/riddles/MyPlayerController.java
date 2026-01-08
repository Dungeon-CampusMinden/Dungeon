package portal.riddles;

import core.Game;
import core.utils.Direction;
import core.utils.Vector2;
import portal.controlls.Hero;
import portal.controlls.PlayerController;
import portal.tractorBeam.TractorBeamFactory;

/**
 * Eine konkrete Implementierung von {@link PlayerController}, die Eingaben verarbeitet und damit
 * den übergebenen {@link Hero} steuert.
 *
 * <p>Diese Klasse dient als´Vorlage für benutzerdefinierte Steuerungslogiken, die zur Laufzeit
 * geladen oder dynamisch kompiliert werden können.
 */
public class MyPlayerController extends PlayerController {

  /** Referenz auf den Helden, der gesteuert werden soll. */
  private final Hero hero;

  /**
   * Erstellt eine neue Instanz des Steuerungscontrollers.
   *
   * @param hero Die Spielfigur, die durch diese Steuerung beeinflusst wird.
   */
  public MyPlayerController(Hero hero) {
    super(hero);
    this.hero = hero;
  }

  /**
   * Verarbeitet einen Tastendruck.
   *
   * <p>Diese Methode wird von der Engine aufgerufen, wenn ein registrierter Tastendruck erfolgt.
   * Die konkrete Umsetzung kann Bewegungen, Aktionen oder andere Spiellogik auslösen.
   *
   * <p>Maustasten werden als "LMB" (Linke Maustaste), "RMB" (Rechte Maustaste) und MMB (Mittlere
   * Maustaste) angegeben
   *
   * @param key Der gedrückte Knopf als Zeichenkette (z.B. "W", "A", "D").
   */
  protected void processKey(String key) {
    if (key.equals("W")) move(0, 5);
    if (key.equals("S")) move(0, -5);
    if (key.equals("A")) move(-5, 0);
    if (key.equals("D")) move(5, 0);
    if (key.equals("Q")) hero.shootSkill();
    if (key.equals("F")) hero.nextSkill();
    if (key.equals("E")) hero.interact(hero.getMousePosition());
    if (key.equals("T")) spawn();
  }

  private void spawn() {
    Game.add(TractorBeamFactory.createTractorBeam(hero.getMousePosition(), Direction.RIGHT));
  }

  private void move(int x, int y) {
    hero.setSpeed(Vector2.of(x, y));
    // or for diagonal movement
    hero.setXSpeed(x);
    hero.setYSpeed(y);
  }
}
