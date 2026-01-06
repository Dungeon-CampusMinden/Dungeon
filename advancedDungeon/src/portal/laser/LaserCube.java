package portal.laser;

import contrib.components.CollideComponent;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import portal.portals.components.PortalExtendComponent;

public class LaserCube {
  private static final SimpleIPath LASER_CUBE = new SimpleIPath("portal/laser/laser_cube.png");

  /**
   * Creates a laser cube entity at the given position.
   *
   * @param position The initial position of the laser cube.
   * @param direction The direction the laser cube is facing.
   * @return A new laser cube entity.
   */
  public static Entity laserCube(Point position, Direction direction) {
    Entity laserCube = new Entity("laserCube");

    final boolean[] attached = {false};

    PositionComponent pc = new PositionComponent(position);
    float rotation;
    switch (direction) {
      case DOWN -> rotation = 180f;
      case LEFT -> rotation = 90f;
      case RIGHT -> rotation = -90f;
      default -> rotation = 0f;
    }
    pc.rotation(rotation);
    laserCube.add(pc);
    laserCube.add(new DrawComponent(new Animation(LASER_CUBE)));
    laserCube.add(new LaserCubeComponent());
    TriConsumer<Entity, Entity, Direction> collideEnter =
        (you, other, collisionDir) -> {
          other
              .fetch(LaserComponent.class)
              .ifPresent(
                  lc -> {
                    if (you.fetch(LaserCubeComponent.class).get().isActive()
                        || collisionDir == direction) {
                      return;
                    }
                    you.fetch(LaserCubeComponent.class).get().setActive(true);
                    Point newPos =
                        new Point(position.x() + direction.x(), position.y() + direction.y());
                    LaserFactory.extendLaser(
                        direction,
                        newPos.translate(direction.opposite()),
                        lc.getSegments(),
                        other.fetch(PortalExtendComponent.class).get(),
                        lc);
                  });
        };

    TriConsumer<Entity, Entity, Direction> collideLeave =
        (you, other, collisionDir) -> {
          other
              .fetch(LaserComponent.class)
              .ifPresent(
                  lc -> {
                    you.fetch(LaserCubeComponent.class).get().setActive(false);
                  });
        };

    laserCube.add(
        new CollideComponent(
            Vector2.of(-0.05f / 2, -0.05f / 2),
            Vector2.of(1.05f, 1.05f),
            collideEnter,
            collideLeave));

    laserCube.add(
        new InteractionComponent(
            () ->
                new Interaction(
                    (interacted, interactor) -> {
                      if (!attached[0]) {
                        interacted
                            .fetch(CollideComponent.class)
                            .ifPresent(
                                collide -> {
                                  collide.isSolid(false);
                                });
                        interactor
                            .fetch(VelocityComponent.class)
                            .ifPresent(
                                vc -> {
                                  interacted.add(vc);
                                  attached[0] = true;
                                });

                      } else {
                        interacted.remove(VelocityComponent.class);
                        interacted
                            .fetch(PositionComponent.class)
                            .ifPresent(
                                pc1 -> {
                                  pc1.position(
                                      pc1.position()
                                          .translate(0.5f, 0.5f)
                                          .toCoordinate()
                                          .toPoint());
                                  interactor
                                      .fetch(PositionComponent.class)
                                      .ifPresent(
                                          pc2 -> {
                                            float pc2rotation;
                                            switch (pc2.viewDirection()) {
                                              case DOWN -> pc2rotation = 180f;
                                              case LEFT -> pc2rotation = 90f;
                                              case RIGHT -> pc2rotation = -90f;
                                              default -> pc2rotation = 0f;
                                            }
                                            pc1.rotation(pc2rotation);
                                            pc1.viewDirection(pc2.viewDirection());
                                          });
                                });
                        interacted
                            .fetch(CollideComponent.class)
                            .ifPresent(
                                collide -> {
                                  collide.isSolid(true);
                                });
                        attached[0] = false;
                      }
                    })));

    return laserCube;
  }
}
