package entities;

import core.Entity;

/**
 * Inherit from this class to add a new HUD to the screen. Implement the updateActors function to
 * control the behaviour when the screen size changed. Implement the createEntity function to create
 * a new entity with a BlocklyUIComponent holding the hud object with a class inheriting from this
 * abstract class.
 */
public abstract class BlocklyHUD {
  /**
   * Create a new entity with a BlocklyUIComponent holding the hud object with a class inheriting
   * from this abstract class.
   *
   * @return Returns a new entity with a BlocklyUIComponent holding the hud object
   */
  public abstract Entity createEntity();
}
