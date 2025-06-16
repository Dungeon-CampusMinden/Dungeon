package produsAdvanced.riddles;

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
   * <p>Maustasten werden als "LMB" (Linke Maustaste), "RMB" (Rechte Maustaste) und MMB (Mittlere
   * Maustaste) angegeben
   *
   * @param key Der gedrückte Knopf als Zeichenkette (z.B. "W", "A", "D").
   */
  protected void processKey(String key) {
    throw new UnsupportedOperationException("Diese Methode muss noch implementiert werden.");
  }
}
