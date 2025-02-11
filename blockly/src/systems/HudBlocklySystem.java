package systems;

import client.Client;
import core.System;

/**
 * This system will call the updateActors method of all entities with the BlocklyUIComponent when
 * the window size changed. This system will always save the last width and height of the window and
 * will compare it to the current window size.
 */
public class HudBlocklySystem extends System {
  /** True when the window size was changed once. */
  private boolean isSizeAdjusting = false;

  @Override
  public void execute() {
    // The windowResize method will be called once when the window size was changed.
    // So we need to call the recreateHud method a second time once after the
    // window size is set.
    if (isSizeAdjusting) {
      Client.recreateHud();
      isSizeAdjusting = false;
    }
  }

  @Override
  public void windowResize(int width, int height) {
    Client.recreateHud();
    // Indicates that the window size was changed for the first time.
    isSizeAdjusting = true;
  }
}
