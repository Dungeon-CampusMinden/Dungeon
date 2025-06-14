package produsAdvanced.abstraction;

/**
 * Eine abstrakte Klasse, die einen Feuerball im Spiel darstellt.
 *
 * <p>Ein Feuerball ist eine Angriffsform, die der Spieler im Spiel einsetzen kann. Diese Klasse
 * gibt nur die Grundstruktur vor. Um einen eigenen Feuerball zu erstellen, musst du eine neue
 * Klasse schreiben, die von Fireball erbt und die abstrakte Methode implementiert.
 */
public abstract class Fireball {

  /**
   * Erstellt einen neuen Feuerball mit den angegebenen Eigenschaften.
   *
   * @param fireballSkill Die Feuerball-Fähigkeit, die die Eigenschaften des Feuerballs bestimmt
   *     (wie Geschwindigkeit, Reichweite und Schaden).
   */
  public Fireball(FireballSkill fireballSkill) {}

  /**
   * Diese Methode wird automatisch aufgerufen, wenn der Feuerball eine Beere trifft.
   *
   * <p>Du musst diese Methode in deiner eigenen Feuerball-Klasse implementieren, um festzulegen,
   * was passieren soll, wenn der Feuerball eine Beere trifft. Zum Beispiel könntest du die Beere
   * verschwinden lassen, wenn sie giftig ist.
   *
   * @param berry Die Beere, die vom Feuerball getroffen wurde.
   */
  protected abstract void onBerryHit(Berry berry);
}
