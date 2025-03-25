package level;

import core.Entity;
import core.Game;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.level.utils.Coordinate;

public class Utils {

  public static void cameraFocusOn(Coordinate coordinate) {
    Game.hero().get().remove(CameraComponent.class);
    Entity focusPoint = new Entity();
    focusPoint.add(new PositionComponent(coordinate.toPoint()));
    focusPoint.add(new CameraComponent());
    Game.add(focusPoint);
  }
}
