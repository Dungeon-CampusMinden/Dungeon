package contrib.hud.newhud;

import core.Game;
import core.System;
import java.util.ArrayList;
import java.util.List;

public class HeadUpDisplaySystem extends System {

  private final List<HUDElement> elements = new ArrayList<>();
  private int height;
  private int width;

  public HeadUpDisplaySystem() {
    super(System.AuthoritativeSide.CLIENT); // nur Client-seitig
    height = Game.windowHeight();
    width = Game.windowWidth();
  }

  /** HUDElement registrieren */
  public void register(HUDElement element) {
    elements.add(element);
    element.init(); // Initialisieren (Layout, Startwerte etc.)
  }

  @Override
  public void execute() {
    Boolean updateLayout = false;
    if (height != Game.windowHeight() || width != Game.windowWidth()) {
      updateLayout = true;
      height = Game.windowHeight();
      width = Game.windowWidth();
    }

    // update() aller Elemente jedes Frame
    for (HUDElement e : elements) {
      e.update();
      if (updateLayout) {
        e.layout();
        java.lang.System.out.println("yeet");
      }
    }
  }
}
