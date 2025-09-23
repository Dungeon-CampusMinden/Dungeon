package entities;

import contrib.components.*;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;

/**
 * A utility class for building different miscellaneous entities in the game world of the advanced
 * dungeon.
 */
public class AdvancedFactory {

  private static final SimpleIPath PORTAL_CUBE = new SimpleIPath("portal/portal_cube.png");
  private static final float cube_mass = 3f;
  private static final float cube_maxSpeed = 10f;

  /**
   * Creates a portal cube entity at the given position.
   *
   * @param position The initial position of the portal cube.
   * @return A new portal cube entity.
   */
  public static Entity attachablePortalCube(Point position) {
    Entity portalCube = new Entity("attachablePortalCube");

    portalCube.add(new PositionComponent(position));
    portalCube.add(new VelocityComponent(cube_maxSpeed, cube_mass, entity -> {}, false));
    portalCube.add(new DrawComponent(new Animation(PORTAL_CUBE)));
    portalCube.add(new CollideComponent());

    final boolean[] attached = {false};

    portalCube.add(
        new InteractionComponent(
            2.0f,
            true,
            (interacted, interactor) -> {
              if (!attached[0]) {

                interactor
                    .fetch(VelocityComponent.class)
                    .ifPresent(
                        vc -> {
                          interacted.remove(VelocityComponent.class);
                          interacted.add(vc);
                          attached[0] = true;
                        });
              } else {
                interacted.remove(VelocityComponent.class);
                interacted.add(
                    new VelocityComponent(cube_maxSpeed, cube_mass, entity -> {}, false));
                attached[0] = false;
              }
            }));

    return portalCube;
  }
}
