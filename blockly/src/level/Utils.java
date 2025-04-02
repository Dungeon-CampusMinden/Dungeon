package level;

import core.Entity;
import core.Game;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.level.utils.Coordinate;

/** WTF */
public class Utils {
  /** WTF */
  public static void cameraFocusOn(Coordinate coordinate) {
    Game.entityStream()
        .filter(e -> e.isPresent(CameraComponent.class))
        .forEach(entity -> entity.remove(CameraComponent.class));

    Entity focusPoint = new Entity();
    focusPoint.add(new PositionComponent(coordinate.toPoint()));
    focusPoint.add(new CameraComponent());
    Game.add(focusPoint);
  }

  /** WTF */
  public static void cameraFocusHero() {
    Game.entityStream()
        .filter(e -> e.isPresent(CameraComponent.class))
        .forEach(entity -> entity.remove(CameraComponent.class));

    Game.hero().get().add(new CameraComponent());
  }
}
