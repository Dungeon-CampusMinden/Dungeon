package produsAdvanced.abstraction.portals;

import contrib.components.CollideComponent;
import contrib.components.ProjectileComponent;
import contrib.components.SkillComponent;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.projectileSkill.ProjectileSkill;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.*;
import core.utils.components.path.SimpleIPath;
import java.util.*;
import produsAdvanced.abstraction.portals.components.PortalComponent;
import produsAdvanced.abstraction.portals.components.PortalExtendComponent;

/**
 * A factory class for creating and managing portals in the game.
 *
 * <p>Portals are created in pairs (blue and green) and allow entities to teleport from one to the
 * other. The factory handles portal creation, positioning, collision behavior, projectile
 * redirection, and cleanup.
 *
 * <p>Only one blue and one green portal can exist at a time. If a new portal is created at the same
 * position as the other, the old one is cleared automatically.
 */
public class PortalFactory {

  /** Name of the blue portal. */
  private static String BLUE_PORTAL_NAME = "BLUE_PORTAL";

  /** Name of the green portal. */
  private static String GREEN_PORTAL_NAME = "GREEN_PORTAL";

  private static SimpleIPath BLUE_PORTAL_TEXTURE = new SimpleIPath("portal/blue_portal");
  private static SimpleIPath GREEN_PORTAL_TEXTURE = new SimpleIPath("portal/green_portal");

  /**
   * Creates a portal of the given color at the specified position.
   *
   * <p>If a portal of the same color already exists, it will be replaced. If the new portal
   * overlaps with the other portal, the other portal will be removed.
   *
   * @param point the position where the portal should be placed
   * @param direction The output direction of the portal.
   * @param color the portal color, see {@link PortalColor}
   */
  public static void createPortal(Point point, Direction direction, PortalColor color) {
    if (color == PortalColor.BLUE) {
      createBluePortal(point, direction);
    } else {
      createGreenPortal(point, direction);
    }
  }

  private static void createBluePortal(Point point, Direction direction) {
    clearBluePortal();
    getGreenPortal()
        .ifPresent(
            greenPortal -> {
              if (greenPortal.fetch(PositionComponent.class).get().position().equals(point)) {
                PortalFactory.clearGreenPortal();
              }
            });
    getBluePortal()
        .ifPresentOrElse(
            bluePortal -> {
              moveExistingPortal(bluePortal, direction, point, PortalColor.BLUE);
            },
            () -> {
              ;
              Entity portal = new Entity(BLUE_PORTAL_NAME);
              portal.add(new DrawComponent(BLUE_PORTAL_TEXTURE));

              PositionComponent pc = new PositionComponent(point);
              pc.viewDirection(direction);
              portal.add(pc);

              CollideComponent cc =
                  setCollideComponent(direction, PortalFactory::onBlueCollideEnter);
              cc.isSolid(false);
              portal.add(cc);

              portal.add(new PortalComponent());

              Game.add(portal);
              ignorePortalInProjectiles(portal);
            });
  }

  private static void createGreenPortal(Point point, Direction direction) {
    clearGreenPortal();
    getBluePortal()
        .ifPresent(
            bluePortal -> {
              if (bluePortal.fetch(PositionComponent.class).get().position().equals(point)) {
                PortalFactory.clearBluePortal();
              }
            });
    getGreenPortal()
        .ifPresentOrElse(
            greenPortal -> {
              moveExistingPortal(greenPortal, direction, point, PortalColor.GREEN);
            },
            () -> {
              Entity portal = new Entity(GREEN_PORTAL_NAME);
              portal.add(new DrawComponent(GREEN_PORTAL_TEXTURE));

              PositionComponent pc = new PositionComponent(point);
              pc.viewDirection(direction);
              portal.add(new PortalComponent());

              CollideComponent cc =
                  setCollideComponent(direction, PortalFactory::onGreenCollideEnter);
              cc.isSolid(false);

              portal.add(pc);
              portal.add(cc);

              Game.add(portal);
              ignorePortalInProjectiles(portal);
            });
  }

  /**
   * Moves a portal to a new position and updates the direction and collision component.
   *
   * @param portal The portal that gets moved and updated.
   * @param direction The output direction of the portal.
   * @param point The position of the new portal.
   * @param color The color of the portal.
   */
  public static void moveExistingPortal(
      Entity portal, Direction direction, Point point, PortalColor color) {
    portal.fetch(PositionComponent.class).get().position(point);
    portal.fetch(PositionComponent.class).get().viewDirection(direction);

    CollideComponent cc =
        setCollideComponent(
            direction,
            color == PortalColor.BLUE
                ? PortalFactory::onBlueCollideEnter
                : PortalFactory::onGreenCollideEnter);
    cc.isSolid(false);
    portal.remove(CollideComponent.class);
    portal.add(cc);
  }

  /**
   * Sets the portal to be ignored by all {@link ProjectileSkill} so it doesn't trigger the
   * projectiles onCollide.
   *
   * @param portal
   */
  private static void ignorePortalInProjectiles(Entity portal) {
    Game.allEntities()
        .filter(entity -> entity.isPresent(SkillComponent.class))
        .forEach(
            entity -> {
              SkillComponent skillComponent = entity.fetch(SkillComponent.class).get();
              for (Skill skill : skillComponent.getSkills()) {
                if (skill instanceof ProjectileSkill projectileSkill) {
                  projectileSkill.ignoreEntity(portal);
                }
              }
            });
  }

  /**
   * Teleports an entity that collides with the green portal to the corresponding blue portal.
   *
   * @param portal the green portal entity
   * @param other the entity entering the portal
   * @param dir the direction of collision
   */
  public static void onGreenCollideEnter(Entity portal, Entity other, Direction dir) {
    if (other.fetch(PortalExtendComponent.class).isPresent()) {
      System.out.println("ENTERED A PEC ON GREEN");
      PortalExtendComponent pec = other.fetch(PortalExtendComponent.class).get();
      if (pec.isThroughBlue()) {
        return;
      }
      pec.setThroughGreen(true);
      portal.fetch(PortalComponent.class).get().setExtendedEntityThrough(other);
      getBluePortal()
          .ifPresent(
              bluePortal -> {
                bluePortal.fetch(PortalComponent.class).get().setExtendedEntityThrough(other);
              });
      return;
    }

    if (getBluePortal().isPresent() && !isEntityPortal(other)) {
      PositionComponent projectilePositionComponent = other.fetch(PositionComponent.class).get();
      Direction greenPortalDirection = portal.fetch(PositionComponent.class).get().viewDirection();
      Direction bluePortalDirection =
          getBluePortal().get().fetch(PositionComponent.class).get().viewDirection();
      projectilePositionComponent.position(
          getBluePortal()
              .get()
              .fetch(PositionComponent.class)
              .get()
              .position()
              .translate(bluePortalDirection.scale(1.3)));
      handleProjectiles(other, greenPortalDirection, bluePortalDirection);
    }
    ;
  }

  /**
   * Teleports an entity that collides with the blue portal to the corresponding green portal.
   *
   * @param portal the blue portal entity
   * @param other the entity entering the portal
   * @param dir the direction of collision
   */
  public static void onBlueCollideEnter(Entity portal, Entity other, Direction dir) {
    if (other.fetch(PortalExtendComponent.class).isPresent()) {
      System.out.println("ENTERED A PEC ON BLUE");
      PortalExtendComponent pec = other.fetch(PortalExtendComponent.class).get();
      if (pec.isThroughGreen()) {
        return;
      }
      pec.setThroughBlue(true);
      portal.fetch(PortalComponent.class).get().setExtendedEntityThrough(other);
      getGreenPortal()
          .ifPresent(
              greenPortal -> {
                greenPortal.fetch(PortalComponent.class).get().setExtendedEntityThrough(other);
              });
      return;
    }

    if (getGreenPortal().isPresent() && !isEntityPortal(other)) {
      PositionComponent projectilePositionComponent = other.fetch(PositionComponent.class).get();
      Direction bluePortalDirection = portal.fetch(PositionComponent.class).get().viewDirection();
      Direction greenPortalDirection =
          getGreenPortal().get().fetch(PositionComponent.class).get().viewDirection();
      projectilePositionComponent.position(
          getGreenPortal()
              .get()
              .fetch(PositionComponent.class)
              .get()
              .position()
              .translate(greenPortalDirection.scale(1.3)));
      handleProjectiles(other, bluePortalDirection, greenPortalDirection);
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
  public static void handleProjectiles(Entity projectile, Direction entry, Direction exit) {
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
  public static Vector2 rotateVelocityThroughPortals(
      Vector2 velocity, Direction portalA, Direction portalB) {
    // Build forward/normal vectors from Directions and normalize (safety)
    Vector2 nA = Vector2.of(portalA.x(), portalA.y()).normalize(); // entry normal
    Vector2 nB = Vector2.of(portalB.x(), portalB.y()).normalize(); // exit normal

    // Tangent vectors: rotate normal 90° CCW (math convention).
    // If your game uses Y-down screen coords or a different convention, see notes below.
    Vector2 tA = Vector2.of(-nA.y(), nA.x()); // entry tangent
    Vector2 tB = Vector2.of(-nB.y(), nB.x()); // exit tangent

    // Decompose incoming velocity into entry basis components
    double compNormal = velocity.dot(nA); // projection on normal
    double compTangent = velocity.dot(tA); // projection on tangent

    // Reconstruct in exit basis.
    // We negate the normal component so "into portal" becomes "out of portal".
    Vector2 out = nB.scale(-compNormal).add(tB.scale(compTangent));

    return out;
  }

  /**
   * Creates a collision component for the portal, with bounds adjusted based on its facing
   * direction.
   *
   * @param dir the direction the portal faces
   * @param onCollideEnter the handler for collisions entering the portal
   * @return a configured {@link CollideComponent}
   */
  private static CollideComponent setCollideComponent(
      Direction dir, TriConsumer<Entity, Entity, Direction> onCollideEnter) {
    double offsetX = -0.2;
    double offsetY = 0.7;
    double hitboxX = 1.4;
    double hitboxY = 0.7;
    switch (dir) {
      case DOWN -> {
        return new CollideComponent(
            Vector2.of(offsetX, -hitboxY / 2),
            Vector2.of(hitboxX, hitboxY),
            onCollideEnter,
            PortalFactory::onCollideLeave);
      }
      case UP -> {
        return new CollideComponent(
            Vector2.of(offsetX, offsetY),
            Vector2.of(hitboxX, hitboxY),
            onCollideEnter,
            PortalFactory::onCollideLeave);
      }
      case LEFT -> {
        return new CollideComponent(
            Vector2.of(-offsetY / 2, offsetX),
            Vector2.of(hitboxY, hitboxX),
            onCollideEnter,
            PortalFactory::onCollideLeave);
      }
      case RIGHT -> {
        return new CollideComponent(
            Vector2.of(offsetY, offsetX),
            Vector2.of(hitboxY, hitboxX),
            onCollideEnter,
            PortalFactory::onCollideLeave);
      }
      default -> {
        return new CollideComponent();
      }
    }
  }

  /**
   * Handles the removal of extended entities through a portal.
   *
   * @param portal The portal which the extended entity goes through.
   * @param other The entity that is extended.
   * @param direction Direction where it extends to.
   */
  public static void onCollideLeave(Entity portal, Entity other, Direction direction) {
    System.out.println("ONCOLLIDE LEAVE CALLED BY " +portal.name());
    clearExtendedEntity(portal, other);
  }

  /**
   * Trims an extended entity by calling its {@link PortalExtendComponent} onTrim Consumer.
   *
   * @param portal The portal the entity is entering at first.
   * @param other The entity that is being extended.
   */
  public static void clearExtendedEntity(Entity portal, Entity other) {
    System.out.println(portal.name() + " trimmed");
    other
        .fetch(PortalExtendComponent.class)
        .ifPresent(
            pec -> {
              if (pec.isExtended()) {
                pec.onTrim.accept(other);
                pec.setExtended(false);
              }
              getGreenPortal()
                  .ifPresent(
                      greenPortal -> {

                          pec.setThroughGreen(false);
                          greenPortal.fetch(PortalComponent.class).ifPresent(pc ->{
                            pc.setExtendedEntityThrough(null);
                          });

                      });
              getBluePortal()
                  .ifPresent(
                      bluePortal -> {

                          pec.setThroughBlue(false);
                          bluePortal.fetch(PortalComponent.class).ifPresent(pc ->{
                            pc.setExtendedEntityThrough(null);
                          });

                      });
            });
  }

  /** Removes both the blue and green portals from the game, if present. */
  public static void clearAllPortals() {
    clearBluePortal();
    clearGreenPortal();
  }

  /** Removes the blue portal from the game, if present. */
  public static void clearBluePortal() {
    getBluePortal()
        .ifPresent(
            portal -> {
              Entity other = portal.fetch(PortalComponent.class).get().getExtendedEntityThrough();
              if (other != null) {
                clearExtendedEntity(portal, other);
              }
              Game.remove(portal);
            });
  }

  /** Removes the green portal from the game, if present. */
  public static void clearGreenPortal() {
    getGreenPortal()
        .ifPresent(
            portal -> {
              Entity other = portal.fetch(PortalComponent.class).get().getExtendedEntityThrough();
              if (other != null) {
                clearExtendedEntity(portal, other);
              }
              Game.remove(portal);
            });
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
   * Returns the blue portal, if it exists.
   *
   * @return an {@link Optional} containing the blue portal entity, or empty if none exists
   */
  public static Optional<Entity> getBluePortal() {
    return Game.allEntities().filter(entity -> entity.name().equals(BLUE_PORTAL_NAME)).findFirst();
  }

  /**
   * Returns the green portal, if it exists.
   *
   * @return an {@link Optional} containing the green portal entity, or empty if none exists
   */
  public static Optional<Entity> getGreenPortal() {
    return Game.allEntities().filter(entity -> entity.name().equals(GREEN_PORTAL_NAME)).findFirst();
  }
}
