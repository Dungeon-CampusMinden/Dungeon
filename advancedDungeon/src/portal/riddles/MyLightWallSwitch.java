package portal.riddles;

import core.Entity;
import portal.lightWall.LightWallSwitch;

/**
 * Dein eigener Schalter für eine Lichtwand.
 *
 * <p>Eine Lichtwand kann Wege blockieren oder freigeben.
 *
 * <p>Diese Klasse bestimmt, <b>wann die Lichtwand aktiv ist</b> und <b>wann sie ausgeschaltet
 * wird</b>.
 *
 * <p>Die Lichtwand selbst wird vom Spiel gebaut – du steuerst hier nur ihr Verhalten.
 */
public class MyLightWallSwitch extends LightWallSwitch {

  /**
   * Wird aufgerufen, wenn die Lichtwand eingeschaltet werden soll.
   *
   * @param emitter Die Lichtwand (oder ihr Erzeuger)
   */
  @Override
  public void activate(Entity emitter) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Wird aufgerufen, wenn die Lichtwand ausgeschaltet werden soll.
   *
   * @param emitter Die Lichtwand (oder ihr Erzeuger)
   */
  @Override
  public void deactivate(Entity emitter) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
