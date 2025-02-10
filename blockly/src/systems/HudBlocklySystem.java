package systems;

import client.Client;
import core.System;

/**
 * This system will call the updateActors method of all entities with the BlocklyUIComponent when
 * the screen size changed. This system will always save the last width and height of the screen and
 * will compare it to the current screen size.
 */
public class HudBlocklySystem extends System {
  /** True if the screen size is currently adjusting. */
  private boolean isSizeAdjusting = false;

  @Override
  public void execute() {
    if (isSizeAdjusting) {
      Client.recreateHud();
      isSizeAdjusting = false;
    }
  }

  @Override
  public void windowResize(int width, int height) {
    Client.recreateHud();
    isSizeAdjusting = true;
  }
}
