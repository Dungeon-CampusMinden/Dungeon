package portal.laser;

import contrib.components.AttachmentComponent;
import contrib.components.CollideComponent;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import contrib.systems.PositionSync;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import portal.portals.components.PortalExtendComponent;

/** Class to create the LaserCube Entity. */
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
    pc.rotation(directionToRotation(direction));
    pc.viewDirection(direction);
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
                        || you.fetch(LaserCubeComponent.class).get().isBeingMoved()) {
                      return;
                    }
                    you.fetch(LaserCubeComponent.class).get().setActive(true);
                    Point newPos =
                        new Point(
                            pc.position().x() + pc.viewDirection().x(),
                            pc.position().y() + pc.viewDirection().y());
                    LaserFactory.extendLaser(
                        pc.viewDirection(),
                        newPos.translate(pc.viewDirection().opposite()),
                        lc.getSegments(),
                        other.fetch(PortalExtendComponent.class).get(),
                        lc);
                  });
        };

    CollideComponent cc =
        new CollideComponent(
            Vector2.of(-0.05f / 2, -0.05f / 2),
            Vector2.of(1.05f, 1.05f),
            collideEnter,
            CollideComponent.DEFAULT_COLLIDER);

    cc.collideLeave(
        (self, other, dir) -> {
          other
              .fetch(LaserComponent.class)
              .ifPresent(
                  lc -> {
                    self.fetch(LaserCubeComponent.class).get().setActive(false);
                  });
          if (!cc.isSolid() && !attached[0]) {
            cc.isSolid(true);
          }
        });
    laserCube.add(cc);

    laserCube.add(
        new InteractionComponent(
            () ->
                new Interaction(
                    (interacted, interactor) -> {
                      PositionComponent interactorPositioncomponent =
                          interactor.fetch(PositionComponent.class).orElseThrow();
                      PositionComponent interactedPositioncomponent =
                          interacted.fetch(PositionComponent.class).orElseThrow();

                      if (!attached[0]) {
                        AttachmentComponent attachmentComponent =
                            new AttachmentComponent(
                                Vector2.ZERO,
                                interactedPositioncomponent,
                                interactorPositioncomponent);
                        attachmentComponent.setTextureRotating(false);
                        attachmentComponent.setRotatingWithOrigin(true);
                        laserCube.add(attachmentComponent);
                        cc.isSolid(false);
                        attached[0] = true;
                        laserCube.fetch(LaserCubeComponent.class).get().setBeingMoved(true);
                      } else {
                        laserCube.remove(AttachmentComponent.class);
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
                        Point snappedPosition =
                            new Point(
                                Math.round(interactedPositioncomponent.position().x()),
                                Math.round(interactedPositioncomponent.position().y()));
                        interactedPositioncomponent.position(snappedPosition);
                        interactedPositioncomponent.viewDirection(
                            interactorPositioncomponent.viewDirection());
                        interactedPositioncomponent.rotation(
                            directionToRotation(interactorPositioncomponent.viewDirection()));
                        attached[0] = false;
                        laserCube.fetch(LaserCubeComponent.class).get().setBeingMoved(false);
                        PositionSync.syncPosition(interacted);
                      }
                    },
                    2f)));

    return laserCube;
  }

  /**
   * Transforms a direction into its corresponding number.
   *
   * @param direction direction to be transformed.
   * @return the corresponding number for the direction.
   */
  private static float directionToRotation(Direction direction) {
    float rotation;
    switch (direction) {
      case DOWN -> rotation = 180f;
      case LEFT -> rotation = 90f;
      case RIGHT -> rotation = -90f;
      default -> rotation = 0f;
    }
    return rotation;
  }
}
