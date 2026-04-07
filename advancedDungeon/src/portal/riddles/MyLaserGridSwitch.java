package portal.riddles;

import core.Entity;
import portal.laserGrid.LaserGridSwitch;
import portal.riddles.utils.Tools;

/**
 * Dein eigener Schalter für ein Laser-Gitter.
 *
 * <p>Ein Laser-Gitter besteht aus mehreren Laser-Strahlen, die an- oder ausgeschaltet werden
 * können.
 *
 * <p>Diese Klasse bestimmt, <b>was passieren soll</b>, wenn das Laser-Gitter eingeschaltet oder
 * ausgeschaltet wird.
 *
 * <p>Die Laser selbst sind bereits im Level vorhanden. Du entscheidest hier nur, <b>wann sie aktiv
 * sind</b> und <b>wann nicht</b>.
 */
public class MyLaserGridSwitch extends LaserGridSwitch {

  /**
   * Wird aufgerufen, wenn das Laser-Gitter aktiviert werden soll.
   *
   * <p>Hier legst du fest, was mit den Lasern passieren soll, wenn der Schalter eingeschaltet wird.
   *
   * @param grid Die Laser-Elemente des Gitters
   */
  @Override
  public void activate(Entity[] grid) {
    for (Entity laser : grid) Tools.getLaserGridComponent(laser).activate();
  }

  @Override
  public void deactivate(Entity[] grid) {
    for (Entity laser : grid) Tools.getLaserGridComponent(laser).deactivate();
  }
}
