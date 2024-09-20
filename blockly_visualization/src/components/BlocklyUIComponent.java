package components;

import core.Component;
import entities.BlocklyHUD;

public final class BlocklyUIComponent implements Component {
  private BlocklyHUD hud;
  public BlocklyUIComponent(BlocklyHUD hud) {
    this.hud = hud;
  }

  public void updateActors() {
    hud.updateActors();
  }
}
