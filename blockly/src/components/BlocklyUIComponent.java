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

  public BlocklyUIComponent(BlocklyHUD hud) {
    this.hud = hud;
  }

  public void updateActors() {
    hud.updateActors();
  }
}
