package portal.physicsobject;

import contrib.components.AttachmentComponent;
import contrib.components.CollideComponent;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;

/** A Cube can be picked up and used to trigger {@link PressurePlates}. */
public class Cube {
  private static final SimpleIPath PORTAL_CUBE =
      new SimpleIPath("portal/portal_cube/portal_cube.png");
  private static final float cube_mass = 3f;
  private static final float cube_maxSpeed = 10f;

  /**
   * Creates a portal cube entity at the given position.
   *
   * @param position The initial position of the portal cube.
   * @param mass The mass of the cube
   * @return A new portal cube entity.
   */
  public static Entity portalCube(Point position, float mass) {
    Entity portalCube = new Entity("attachablePortalCube");
    portalCube.add(new PortalCubeComponent());
    portalCube.add(new PositionComponent(position));
    portalCube.add(new VelocityComponent(cube_maxSpeed, mass, entity -> {}, false));
    portalCube.add(new DrawComponent(new Animation(PORTAL_CUBE)));

    final boolean[] attached = {false};
    CollideComponent cc = new CollideComponent();
    cc.collideLeave(
        (self, other, dir) -> {
          if (!cc.isSolid() && !attached[0]) {
            cc.isSolid(true);
          }
        });
    portalCube.add(cc);

    portalCube.add(new InteractionComponent(() -> pickupInteraction(attached, portalCube, cc)));

    return portalCube;
  }

  /**
   * Creates a portal cube entity at the given position.
   *
   * @param position The initial position of the portal cube.
   * @return A new portal cube entity.
   */
  public static Entity portalCube(Point position) {
    return portalCube(position, cube_mass);
  }

  private static Interaction pickupInteraction(
      boolean[] attached, Entity portalCube, CollideComponent cc) {
    return new Interaction(
        (cube, hero) -> {
          PositionComponent interactorPositioncomponent =
              hero.fetch(PositionComponent.class).orElseThrow();
          PositionComponent interactedPositioncomponent =
              cube.fetch(PositionComponent.class).orElseThrow();
          if (!attached[0]) {
            AttachmentComponent attachmentComponent =
                new AttachmentComponent(
                    Vector2.ZERO, interactedPositioncomponent, interactorPositioncomponent);
            portalCube.add(attachmentComponent);
            cc.isSolid(false);
            attached[0] = true;
          } else {
            portalCube.remove(AttachmentComponent.class);
            Game.tileAt(interactedPositioncomponent.coordinate())
                .ifPresent(
                    tile -> {
                      if (tile.levelElement() == LevelElement.WALL
                          || tile.levelElement() == LevelElement.GITTER
                          || tile.levelElement() == LevelElement.GLASSWALL
                          || tile.levelElement() == LevelElement.PORTAL) {
                        interactedPositioncomponent.position(
                            interactorPositioncomponent.position());
                      }
                    });
            attached[0] = false;
          }
        },
        2f);
  }
}
