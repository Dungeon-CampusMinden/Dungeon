package systems;

import components.BlocklyUIComponent;
import core.Entity;
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
    filteredEntityStream(BlocklyUIComponent.class).forEach(this::handleChangedScreenSize);
    lastWidth = currentWidth;
    lastHeight = currentHeight;
  }

  /**
   * Handler for changed screen size. Will call the updateActors function of the BlocklyUIComponent
   * of the given entity.
   *
   * @param entity Entity that has a BlocklyUIComponent and needs to update the size of its actors
   *     due to screen size change.
   */
  public void handleChangedScreenSize(Entity entity) {
    entity.fetch(BlocklyUIComponent.class).ifPresent(BlocklyUIComponent::updateActors);
  }
}
