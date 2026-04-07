package portal.riddles;

import core.Entity;
import portal.lightBridge.BridgeSwitch;
import portal.lightBridge.LightBridgeFactory;

/**
 * Dein eigener Schalter für eine Lichtbrücke.
 *
 * <p>Diese Klasse bestimmt, was passieren soll, wenn ein Schalter eine Lichtbrücke ein- oder
 * ausschaltet.
 *
 * <p>Die Lichtbrücke selbst wird vom Spiel gebaut – du entscheidest hier nur, <b>wann sie aktiv
 * ist</b> und <b>wann nicht</b>.
 */
public class MyBridgeSwitch extends BridgeSwitch {

  /**
   * Wird aufgerufen, wenn der Schalter aktiviert wird.
   *
   * <p>Hier legst du fest, was passieren soll, wenn die Lichtbrücke eingeschaltet wird.
   *
   * @param emitter Die Lichtbrücke (oder ihr Erzeuger), die aktiviert werden soll
   */
  public void activate(Entity emitter) {
    LightBridgeFactory.activate(emitter);
  }

  public void deactivate(Entity emitter) {
    LightBridgeFactory.deactivate(emitter);
  }
}
