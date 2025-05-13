package produsAdvanced.riddles;

import java.awt.*;
import produsAdvanced.abstraction.Berry;
import produsAdvanced.abstraction.Hero;
import produsAdvanced.abstraction.PlayerController;

/**
 * Eine konkrete Implementierung von {@link PlayerController}, die Eingaben verarbeitet und damit
 * den übergebenen {@link Hero} steuert.
 *
 * <p>Diese Klasse dient als´Vorlage für benutzerdefinierte Steuerungslogiken, die zur Laufzeit
 * geladen oder dynamisch kompiliert werden können.
 */
public class MyPlayerController extends PlayerController {

  /** Referenz auf den Helden, der gesteuert werden soll. */
  private Hero hero;

  /**
   * Erstellt eine neue Instanz des Steuerungs-Controllers.
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
   * @param key Der gedrückte Knopf als Zeichenkette (z.B. "W", "A", "D").
   */
  @Override
  protected void processKey(String key) {
    System.out.println(key);
    if (key.equals("W")) move(0, 10);
    if (key.equals("S")) move(0, -10);
    if (key.equals("A")) move(-10, 0);
    if (key.equals("D")) move(10, 0);
    if (key.equals("E")) hero.interact();
    if (key.equals("P")) pickUpberry();
    if (key.equals("I")) hero.openInventory();
    // TODO Verhalten für bestimmte Tasten ergänzt.
  }

  private void pickUpberry() {
    Berry b = hero.getBerryAt(hero.getMousePosition());
    if (b != null && b.isToxic()) {}
  }

  private void move(int x, int y) {
    hero.setXSpeed(x);
    hero.setYSpeed(y);
  }
}
