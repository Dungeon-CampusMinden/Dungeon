package produsAdvanced.riddles;

import produsAdvanced.abstraction.Berry;
import produsAdvanced.abstraction.Fireball;
import produsAdvanced.abstraction.FireballSkill;

/**
 * Eine konkrete Implementierung von {@link Fireball}, die bestimmt, wie ein Feuerball sich im Spiel
 * verhält.
 */
public class MyFireball extends Fireball {

  /** Referenz auf die Feuerball-Fähigkeit, die die Eigenschaften des Feuerballs bestimmt. */
  private FireballSkill fireballSkill;

  /**
   * Erstellt eine neue Instanz eines benutzerdefinierten Feuerballs.
   *
   * @param fireballSkill Die {@link FireballSkill} Instanz, die die Eigenschaften des Feuerballs
   *     definiert.
   */
  public MyFireball(FireballSkill fireballSkill) {
    super(fireballSkill);
    this.fireballSkill = fireballSkill;
  }

  /**
   * Wird aufgerufen, wenn der Feuerball eine Beere trifft.
   *
   * <p>In dieser Methode kannst du die Logik implementieren, die bestimmt, was mit der Beere
   * passieren soll, wenn sie vom Feuerball getroffen wird. Zum Beispiel könntest du:
   *
   * <ul>
   *   <li>Die Beere verschwinden lassen
   *   <li>Ausgeben, ob die Beere giftig ist
   *   <li>Das Aussehen der Beere ändern
   * </ul>
   *
   * @param berry Die Beere, die vom Feuerball getroffen wurde.
   */
  @Override
  public void onBerryHit(Berry berry) {
    throw new UnsupportedOperationException("Diese Methode muss noch implementiert werden.");
  }
}
