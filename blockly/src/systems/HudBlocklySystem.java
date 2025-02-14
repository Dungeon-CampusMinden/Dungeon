package systems;

import client.Client;
import core.Game;
import core.System;

/**
 * This system will call the updateActors method of all entities with the BlocklyUIComponent when
 * the screen size changed. This system will always save the last width and height of the screen and
 * will compare it to the current screen size.
 */
public class HudBlocklySystem extends System {
  private int lastWidth;
  private int lastHeight;
  private boolean firstTick = true;

  @Override
  public void execute() {
    if (firstTick) {
      lastWidth = Game.windowWidth();
      lastHeight = Game.windowHeight();
      firstTick = false;
      return;
    }
    int currentWidth = Game.windowWidth();
    int currentHeight = Game.windowHeight();
    if (currentWidth == lastWidth && currentHeight == lastHeight) {
      return;
    }
    if (currentWidth == 0 || currentHeight == 0) {
      return;
    }

    Client.resetBlocklyHUD();

    lastWidth = currentWidth;
    lastHeight = currentHeight;
  }
}
