package portal.riddles;

import core.Entity;
import core.Game;
import portal.energyPellet.abstraction.EnergyPelletCatcherBehavior;
import portal.riddles.utils.Tools;

/**
 * Dein eigenes Verhalten für einen Energy-Pellet-Catcher.
 *
 * <p>Diese Klasse wird verwendet, um festzulegen, <b>was passieren soll</b>, wenn ein Energy Pellet
 * einen Catcher trifft.
 *
 * <p>Der Catcher ist so etwas wie ein Schalter, der durch ein Pellet ausgelöst werden kann. Hier
 * entscheidest du, wie dieser Schalter reagieren soll.
 */
public class MyEnergyPelletCatcherBehavior extends EnergyPelletCatcherBehavior {

  /**
   * Wird aufgerufen, wenn ein Energy Pellet den Catcher trifft.
   *
   * <p>Hier kannst du bestimmen, was beim Treffer passieren soll, zum Beispiel:
   *
   * <ul>
   *   <li>den Catcher an- oder ausschalten
   *   <li>das Pellet verschwinden lassen
   *   <li>eine eigene Spielregel umsetzen
   * </ul>
   *
   * <p>Der {@code catcher} ist der Schalter selbst, der {@code pellet} ist das getroffene Energy
   * Pellet.
   *
   * @param catcher Der Catcher, der getroffen wurde
   * @param pellet Das Energy Pellet, das den Catcher trifft
   */
  @Override
  public void catchPellet(Entity catcher, Entity pellet) {
    Tools.getToggleComponent(catcher).toggle();
    Game.remove(pellet);
  }
}
