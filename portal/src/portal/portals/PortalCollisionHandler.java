package portal.portals;

import contrib.components.CollideComponent;
import contrib.components.ProjectileComponent;
import contrib.systems.EventScheduler;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import java.util.Optional;
import portal.portals.components.PortalComponent;
import portal.portals.components.PortalExtendComponent;
import portal.portals.components.PortalIgnoreComponent;
import portal.riddles.utils.PortalUtils;

/** Collision Handler for portals. */
public class PortalCollisionHandler {

  private static final int PORTAL_DELAY = 600;

  /**
   * Handles the removal of extended entities through a portal.
   *
   * @param portal The portal which the extended entity goes through.
   * @param other The entity that is extended.
   * @param direction Direction where it extends to.
   */
  public static void onCollideLeave(Entity portal, Entity other, Direction direction) {
    PortalExtendHandler.clearExtendedEntity(portal, other);
  }

  /**
   * Creates a collision component for the portal, with bounds adjusted based on its facing
   * direction.
   *
   * @param dir the direction the portal faces
   * @param onCollideEnter the handler for collisions entering the portal
   * @return a configured {@link CollideComponent}
   */
  public static CollideComponent setCollideComponent(
      Direction dir, TriConsumer<Entity, Entity, Direction> onCollideEnter) {
    double offsetX = 0;
    double offsetY = 0.7;
    double hitboxX = 1;
    double hitboxY = 0.7;

    Vector2 offset;
    Vector2 hitbox;
    switch (dir) {
      case DOWN -> {
        offset = Vector2.of(offsetX, -hitboxY / 2);
        hitbox = Vector2.of(hitboxX, hitboxY);
      }
      case UP -> {
        offset = Vector2.of(offsetX, offsetY);
        hitbox = Vector2.of(hitboxX, hitboxY);
      }
      case LEFT -> {
        offset = Vector2.of(-offsetY / 2, offsetX);
        hitbox = Vector2.of(hitboxY, hitboxX);
      }
      case RIGHT -> {
        offset = Vector2.of(offsetY, offsetX);
        hitbox = Vector2.of(hitboxY, hitboxX);
      }
      default -> {
        // Error State, should never happen
        return new CollideComponent();
      }
    }
    return new CollideComponent(
        offset, hitbox, onCollideEnter, PortalCollisionHandler::onCollideLeave);
  }

  /**
   * Returns a consumer that teleports an entity that collides with the blue portal to the
   * corresponding green portal. If the entity has a {@link PortalExtendComponent} its going to
   * extend it if both portals are alive.
   *
   * @param portalColor the color of the portal
   * @return the Triconsumer for the oncollide handler
   */
  public static TriConsumer<Entity, Entity, Direction> createOnCollideHandler(
      PortalColor portalColor) {
    return (portal, other, direction) -> {
      Optional<Entity> otherPortal = getOtherPortal(portalColor);

      PositionComponent otherPc = other.fetch(PositionComponent.class).get();
      PositionComponent portalPc = portal.fetch(PositionComponent.class).get();

      if (other.fetch(PortalExtendComponent.class).isPresent()) {
        handleExtendCollide(portal, other, otherPortal, portalColor);
        return;
      }

      if (other.fetch(PortalIgnoreComponent.class).isPresent()) return;
      if (otherPortal.isEmpty()) return;
      if (isEntityPortal(other)) return;
      if (isPlayerFacingWrongDirection(other, portalPc, otherPc)) return;

      teleportEntity(other, portal, otherPc);

      if (isPlayer(other)) handleRotation(other, portalColor);

      Direction portalDirection = portal.fetch(PositionComponent.class).get().viewDirection();
      Direction otherPortalDirection =
          otherPortal.get().fetch(PositionComponent.class).get().viewDirection();

      handleProjectiles(other, portalDirection, otherPortalDirection);
    };
  }

  private static void handleExtendCollide(
      Entity portal, Entity other, Optional<Entity> otherPortal, PortalColor color) {
    PortalExtendComponent pec = other.fetch(PortalExtendComponent.class).get();

    if (pec.isThroughBlue() && color == PortalColor.GREEN) return;
    if (pec.isThroughGreen() && color == PortalColor.BLUE) return;

    if (color == PortalColor.GREEN) pec.setThroughGreen(true);
    else pec.setThroughBlue(true);

    portal.fetch(PortalComponent.class).ifPresent(pc -> pc.setExtendedEntityThrough(other));
    otherPortal
        .flatMap(p -> p.fetch(PortalComponent.class))
        .ifPresent(pc -> pc.setExtendedEntityThrough(other));
  }

  /**
   * Checks if the given entity is a portal.
   *
   * @param entity the entity to check
   * @return true if the entity is a portal, false otherwise
   */
  private static boolean isEntityPortal(Entity entity) {
    return entity.isPresent(PortalComponent.class);
  }

  /**
   * returns an on hold handler.
   *
   * @param color the color of the portal
   * @return the Triconsumer for the on hold handler
   */
  public static TriConsumer<Entity, Entity, Direction> createOnHoldHandler(PortalColor color) {
    return (portal, other, direction) -> {
      if (other.fetch(PortalIgnoreComponent.class).isPresent()) {
        return;
      }

      if (isEntityPortal(other)) {
        return;
      }
      if (getOtherPortal(color).isEmpty()) return;

      PositionComponent portalPositionComponent = portal.fetch(PositionComponent.class).get();
      PositionComponent otherPositionComponent = other.fetch(PositionComponent.class).get();

      if (isPlayerFacingWrongDirection(other, portalPositionComponent, otherPositionComponent))
        return;

      teleportEntity(other, portal, otherPositionComponent);

      other
          .fetch(VelocityComponent.class)
          .ifPresent(
              vc -> {
                vc.clearForces();
                vc.currentVelocity(Vector2.ZERO);
              });

      if (isPlayer(other)) handleRotation(other, color);
    };
  }

  private static void teleportEntity(Entity other, Entity portal, PositionComponent otherPc) {
    otherPc.position(PortalUtils.calculatePortalExit(portal));
    other.add(new PortalIgnoreComponent());
    EventScheduler.scheduleAction(() -> other.remove(PortalIgnoreComponent.class), PORTAL_DELAY);
  }

  private static boolean isPlayerFacingWrongDirection(
      Entity other, PositionComponent portalPc, PositionComponent otherPc) {
    return isPlayer(other) && otherPc.viewDirection() != portalPc.viewDirection().opposite();
  }

  private static boolean isPlayer(Entity other) {
    return Game.player().isPresent() && Game.player().get().name().equals(other.name());
  }

  private static Optional<Entity> getOtherPortal(PortalColor color) {
    return color == PortalColor.BLUE ? PortalUtils.getGreenPortal() : PortalUtils.getBluePortal();
  }

  /**
   * Handles the rotation of an entity when it goes through the portal so it looks like it exited
   * the portal.
   *
   * @param other the entity that is getting rotated.
   * @param color the output portal.
   */
  private static void handleRotation(Entity other, PortalColor color) {

    PositionComponent otherPositionComponent = other.fetch(PositionComponent.class).get();
    Direction blueDirection =
        PortalUtils.getBluePortal().get().fetch(PositionComponent.class).get().viewDirection();
    Direction greenDirection =
        PortalUtils.getGreenPortal().get().fetch(PositionComponent.class).get().viewDirection();

    other
        .fetch(VelocityComponent.class)
        .ifPresent(
            vc -> {
              vc.clearForces();
              vc.currentVelocity(Vector2.ZERO);
            });

    if (color == PortalColor.BLUE) {
      otherPositionComponent.viewDirection(greenDirection);
    } else {
      otherPositionComponent.viewDirection(blueDirection);
    }
  }

  /**
   * Adjusts a projectile's velocity and goal when it passes through a portal, preserving direction
   * relative to portal orientation.
   *
   * @param projectile the projectile entity
   * @param entry the direction the projectile entered the portal
   * @param exit the direction it exits from the linked portal
   */
  private static void handleProjectiles(Entity projectile, Direction entry, Direction exit) {
    if (!projectile.isPresent(ProjectileComponent.class)) {
      return;
    }
    VelocityComponent velocityComponent = projectile.fetch(VelocityComponent.class).get();
    PositionComponent positionComponent = projectile.fetch(PositionComponent.class).get();
    ProjectileComponent projectileComponent = projectile.fetch(ProjectileComponent.class).get();
    projectile.remove(ProjectileComponent.class);

    Vector2 velocity =
        rotateVelocityThroughPortals(velocityComponent.currentVelocity(), entry, exit);
    Vector2 goal =
        projectileComponent
            .goalLocation()
            .vectorTo(positionComponent.position())
            .rotateDeg(velocity.angleDeg());
    projectile.add(
        new ProjectileComponent(
            positionComponent.position(),
            new Point(goal.x(), goal.y()),
            projectileComponent.forceToApply(),
            projectileComponent.onEndReached()));

    velocityComponent.currentVelocity(velocity);
    positionComponent.rotation((float) velocity.angleDeg());
    positionComponent.position(positionComponent.position().translate(exit.scale(0.2)));
  }

  /**
   * Rotates a velocity vector according to the relative orientation of the entry and exit portals.
   *
   * @param velocity the original velocity vector
   * @param portalA the entry portal direction
   * @param portalB the exit portal direction
   * @return the rotated velocity vector after teleportation
   */
  private static Vector2 rotateVelocityThroughPortals(
      Vector2 velocity, Direction portalA, Direction portalB) {
    Vector2 nA = Vector2.of(portalA.x(), portalA.y()).normalize();
    Vector2 nB = Vector2.of(portalB.x(), portalB.y()).normalize();

    Vector2 tA = Vector2.of(-nA.y(), nA.x());
    Vector2 tB = Vector2.of(-nB.y(), nB.x());

    double compNormal = velocity.dot(nA);
    double compTangent = velocity.dot(tA);

    return nB.scale(-compNormal).add(tB.scale(compTangent));
  }
}
