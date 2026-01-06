package portal.energyPellet;

import contrib.components.CollideComponent;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.path.SimpleIPath;
import portal.components.ToggleableComponent;

public class EnergyPelletCatcher {
  private static final SimpleIPath PELLET_CATCHER = new SimpleIPath("portal/pellet_catcher");

  /**
   * Creates a new entity that can catch energy pellets.
   *
   * @param position       the position of the pellet catcher.
   * @param catchDirection the direction the pellet catcher is facing.
   * @return a new energyPelletCatcher entity.
   */
  public static Entity energyPelletCatcher(Point position, Direction catchDirection) {
    Entity catcher = new Entity("energyPelletCatcher");
    catcher.add(new PositionComponent(position));
    DrawComponent dc = EnergyPelletLauncher.chooseTexture(catchDirection, PELLET_CATCHER);
    catcher.add(dc);
    catcher.add(new ToggleableComponent(false));

    TriConsumer<Entity, Entity, Direction> action =
      (self, other, direction) -> {
        if (other.name().matches("energyPelletLauncher_\\d+_skill_projectile")) {
          self.fetch(ToggleableComponent.class).ifPresent(ToggleableComponent::toggle);
          Game.remove(other);
        }
      };

    CollideComponent colComp = new CollideComponent(action, CollideComponent.DEFAULT_COLLIDER);
    catcher.add(colComp);

    return catcher;
  }

}
