package portal.riddles;

import core.Entity;
import portal.tractorBeam.TractorBeamLever;

/**
 * Dein eigener Hebel für einen Traktorstrahl.
 *
 * <p>Ein Traktorstrahl kann Objekte schieben oder ziehen.
 *
 * <p>Diese Klasse bestimmt, <b>was passieren soll</b>, wenn der Hebel betätigt wird.
 *
 * <p>Zum Beispiel kannst du:
 *
 * <ul>
 *   <li>die Richtung des Strahls umkehren
 *   <li>sein Verhalten verändern
 * </ul>
 */
public class MyTractorBeamLever extends TractorBeamLever {

  /**
   * Wird aufgerufen, wenn der Hebel benutzt wird.
   *
   * <p>Hier legst du fest, wie der Traktorstrahl reagieren soll.
   *
   * @param tractorBeam Der Traktorstrahl, der verändert werden soll
   */
  @Override
  public void reverse(Entity tractorBeam) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
