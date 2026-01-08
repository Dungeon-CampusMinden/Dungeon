package portal.tractorBeam;

import core.Entity;

/**
 * Base abstraction for controlling a tractor beam.
 *
 * <p>This class defines how a tractor beam reacts to a lever interaction.
 *
 * <p>Concrete implementations decide how the tractor beam changes its behavior, such as reversing
 * direction or force.
 */
public abstract class TractorBeamLever {

  /**
   * Reverses the direction of the tractor beam.
   *
   * @param tractorBeam the tractor beam entity to modify
   */
  public abstract void reverse(Entity tractorBeam);
}
