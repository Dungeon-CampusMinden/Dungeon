package produsAdvanced.riddles;

import java.awt.*;
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
    // TODO Verhalten für bestimmte Tasten ergänzt.
  }
}
