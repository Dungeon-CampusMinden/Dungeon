package components;

import core.Component;
import entities.BlocklyHUD;

/**
 * Entities with this component will be updated by the HudBlocklySystem when the screen size
 * changed. Entities must implement the updateActors method to control the behaviour when the screen
 * size is changing.
 */
public final class BlocklyUIComponent implements Component {
  private final BlocklyHUD hud;

  /**
   * Constructor of the BlocklyUIComponent. Store the given HUD object. The HudBlocklySystem will
   * call the updateActors function of the HUD object when the screen size changed.
   *
   * @param hud The HUD object of this component
   */
  public BlocklyUIComponent(BlocklyHUD hud) {
    this.hud = hud;
  }

  /** Call teh updateActors function of the HUD object. */
  public void updateActors() {
    hud.updateActors();
  }
}

