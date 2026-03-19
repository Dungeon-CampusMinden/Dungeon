package contrib.hud.newhud;

import core.Game;
import core.System;
import java.util.ArrayList;
import java.util.List;

/**
 * The HeadUpDisplaySystem manages and updates all registered {@link HUDElement} instances on the
 * client side.
 *
 * <p>It is responsible for calling the update method each frame and reacts to window size changes
 * by triggering a layout update for all registered elements.
 */
public class HeadUpDisplaySystem extends System {

  private final List<HUDElement> elements = new ArrayList<>();
  private int height;
  private int width;

  /** Creates a new HeadUpDisplaySystem that runs exclusively on the client side. */
  public HeadUpDisplaySystem() {
    super(System.AuthoritativeSide.CLIENT); // nur Client-seitig
    height = Game.windowHeight();
    width = Game.windowWidth();
  }

  /**
   * Registers a HUD element with this system and initializes it.
   *
   * @param element The HUD element to register.
   */
  public void register(HUDElement element) {
    elements.add(element);
    element.init();
  }

  @Override
  public void execute() {
    boolean updateLayout = false;
    if (height != Game.windowHeight() || width != Game.windowWidth()) {
      updateLayout = true;
      height = Game.windowHeight();
      width = Game.windowWidth();
    }

    // update() aller Elemente jedes Frame
    for (HUDElement e : elements) {
      e.update();
      if (updateLayout) {
        e.layoutElement();
      }
    }
  }
}
