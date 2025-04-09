package level;

import core.Entity;
import core.Game;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.utils.MissingHeroException;
import core.utils.components.MissingComponentException;
import server.Server;
import utils.Direction;

public class LevelManagementUtils {

  public static void cameraFocusOn(Coordinate coordinate) {
    Game.entityStream()
        .filter(e -> e.isPresent(CameraComponent.class))
        .forEach(entity -> entity.remove(CameraComponent.class));

    Entity focusPoint = new Entity();
    focusPoint.add(new PositionComponent(coordinate.toPoint()));
    focusPoint.add(new CameraComponent());
    Game.add(focusPoint);
  }

  public static void cameraFocusHero() {
    Game.entityStream()
        .filter(e -> e.isPresent(CameraComponent.class))
        .forEach(entity -> entity.remove(CameraComponent.class));
    Game.hero().orElseThrow(() -> new MissingHeroException()).add(new CameraComponent());
  }

  public static void centerHero() {
    Entity hero = Game.hero().orElseThrow(() -> new MissingHeroException());
    PositionComponent pc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    pc.position(pc.position().toCoordinate().toCenteredPoint());
  }

  public static void heroViewDiretion(PositionComponent.Direction viewDirection) {
    Entity hero = Game.hero().orElseThrow(() -> new MissingHeroException());
    Server.turnEntity(hero, Direction.fromPositionCompDirection(viewDirection));
  }
}
