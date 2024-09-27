package systems;

import com.badlogic.gdx.Gdx;
import components.BlocklyUIComponent;
import contrib.components.UIComponent;
import core.Entity;
import core.System;
import core.components.PositionComponent;
import core.utils.Point;

import java.util.Optional;

/**
 * This system will call the updateActors method of all entities with the BlocklyUIComponent when the screen size
 * changed. This system will always save the last width and height of the screen and will compare it to the current
 * screen size.
 */
public class HudBlocklySystem extends System {
  private int lastWidth;
  private int lastHeight;
  private boolean firstTick = true;
  @Override
  public void execute() {
    if (firstTick) {
      lastWidth =  Gdx.graphics.getWidth();
      lastHeight = Gdx.graphics.getHeight();
      firstTick = false;
      return;
    }
    int currentWidth = Gdx.graphics.getWidth();
    int currentHeight = Gdx.graphics.getHeight();
    if (currentWidth == lastWidth && currentHeight == lastHeight) {
      return;
    }
    filteredEntityStream(BlocklyUIComponent.class).forEach(this::handleChangedScreenSize);
    lastWidth = currentWidth;
    lastHeight = currentHeight;
  }

  public void handleChangedScreenSize(Entity entity) {
    entity.fetch(BlocklyUIComponent.class).ifPresent(BlocklyUIComponent::updateActors);
  }

}
