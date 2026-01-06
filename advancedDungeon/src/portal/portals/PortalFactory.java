package portal.portals;

import contrib.components.CollideComponent;
import contrib.components.ProjectileComponent;
import contrib.components.SkillComponent;
import contrib.systems.EventScheduler;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.projectileSkill.ProjectileSkill;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.*;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import java.util.*;
import portal.portals.abstraction.PortalUtils;
import portal.portals.components.PortalComponent;
import portal.portals.components.PortalExtendComponent;
import portal.portals.components.PortalIgnoreComponent;

/**
 * A factory class for creating and managing portals in the game.
 *
 * <p>Portals are created in pairs (blue and green) and allow entities to teleport from one to the
 * other. The factory handles everything portal related like the teleportation and extending of
 * entities.
 *
 * <p>Only one blue and one green portal can exist at a time. If a new portal is created at the same
 * position as the other, the old one is cleared automatically.
 *
 * <p>Needs the {@link PortalExtendSystem PortalExtendSystem} to be used with entities that extend.
 */
public class PortalFactory {

  private static final SimpleIPath BLUE_PORTAL_TEXTURE = new SimpleIPath("portal/blue_portal");
  private static final SimpleIPath GREEN_PORTAL_TEXTURE = new SimpleIPath("portal/green_portal");

  private static final int PORTAL_DELAY = 600;

  /**
   * Creates a portal of the given color at the specified position.
   *
   * <p>If a portal of the same color already exists, it will be replaced. If the new portal
   * overlaps with the other portal, the other portal will be removed.
   *
   * @param point the position where the portal will be placed.
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

  /**
   * Creates a blue portal at the given point and direction.
   *
   * @param point the position where the portal will be placed.
   * @param direction the output direction of the portal.
   */
  private static void createBluePortal(Point point, Direction direction) {
    PortalUtils.getGreenPortal()
        .ifPresent(
            greenPortal -> {
              if (greenPortal.fetch(PositionComponent.class).get().position().equals(point)) {
                Entity other =
                    greenPortal.fetch(PortalComponent.class).get().getExtendedEntityThrough();
                if (other != null) {
                  clearExtendedEntity(greenPortal, other);
                }
                clearGreenPortal();
              }
            });
    PortalUtils.getBluePortal()
        .ifPresentOrElse(
            bluePortal -> {
              Point oldPosition = bluePortal.fetch(PositionComponent.class).get().position();
              if (oldPosition.equals(point)) {
                return;
              }
              Entity other =
                  bluePortal.fetch(PortalComponent.class).get().getExtendedEntityThrough();
              if (other != null) {
                clearExtendedEntity(bluePortal, other);
              }
              moveExistingPortal(bluePortal, direction, point, PortalColor.BLUE);
              updateVisual(PortalColor.BLUE, direction);
            },
            () -> {
              Entity portal = new Entity(PortalUtils.BLUE_PORTAL_NAME);
              Map<String, Animation> animationMap =
                  Animation.loadAnimationSpritesheet(BLUE_PORTAL_TEXTURE);

              State fallback = new State("NONE", animationMap.get("fallback"));
              State top = new State("UP", animationMap.get("bottom"));
              State bottom = new State("DOWN", animationMap.get("fallback"));
              State left = new State("LEFT", animationMap.get("left"));
              State right = new State("RIGHT", animationMap.get("right"));
              StateMachine sm = new StateMachine(Arrays.asList(fallback, top, bottom, left, right));
              sm.setState(direction.name(), null);
              portal.add(new DrawComponent(sm));
              updateVisual(PortalColor.BLUE, direction);

              PositionComponent pc = new PositionComponent(point);
              pc.viewDirection(direction);
              portal.add(pc);

              CollideComponent cc =
                  setCollideComponent(direction, PortalFactory::onBlueCollideEnter);
              cc.isSolid(false);
              cc.onHold(PortalFactory::onHoldBlue);
              portal.add(cc);

              portal.add(new PortalComponent());

              Game.add(portal);
              ignorePortalInProjectiles(portal);
            });
  }

  /**
   * Creates a green portal at the given point and direction.
   *
   * @param point the position where the portal will be placed.
   * @param direction the output direction of the portal.
   */
  private static void createGreenPortal(Point point, Direction direction) {
    PortalUtils.getBluePortal()
        .ifPresent(
            bluePortal -> {
              if (bluePortal.fetch(PositionComponent.class).get().position().equals(point)) {
                Entity other =
                    bluePortal.fetch(PortalComponent.class).get().getExtendedEntityThrough();
                if (other != null) {
                  clearExtendedEntity(bluePortal, other);
                }
                clearBluePortal();
              }
            });
    PortalUtils.getGreenPortal()
        .ifPresentOrElse(
            greenPortal -> {
              Point oldPosition = greenPortal.fetch(PositionComponent.class).get().position();
              if (oldPosition.equals(point)) {
                return;
              }

              Entity other =
                  greenPortal.fetch(PortalComponent.class).get().getExtendedEntityThrough();
              if (other != null) {
                clearExtendedEntity(greenPortal, other);
              }
              moveExistingPortal(greenPortal, direction, point, PortalColor.GREEN);
              updateVisual(PortalColor.GREEN, direction);
            },
            () -> {
              Entity portal = new Entity(PortalUtils.GREEN_PORTAL_NAME);
              Map<String, Animation> animationMap =
                  Animation.loadAnimationSpritesheet(GREEN_PORTAL_TEXTURE);

              State fallback = new State("NONE", animationMap.get("fallback"));
              State top = new State("UP", animationMap.get("bottom"));
              State bottom = new State("DOWN", animationMap.get("fallback"));
              State left = new State("LEFT", animationMap.get("left"));
              State right = new State("RIGHT", animationMap.get("right"));
              StateMachine sm = new StateMachine(Arrays.asList(fallback, top, bottom, left, right));
              sm.setState(direction.name(), null);
              portal.add(new DrawComponent(sm));
              updateVisual(PortalColor.GREEN, direction);

              PositionComponent pc = new PositionComponent(point);
              pc.viewDirection(direction);
              portal.add(new PortalComponent());

              CollideComponent cc =
                  setCollideComponent(direction, PortalFactory::onGreenCollideEnter);
              cc.isSolid(false);
              cc.onHold(PortalFactory::onHoldGreen);

              portal.add(pc);
              portal.add(cc);

              Game.add(portal);
              ignorePortalInProjectiles(portal);
            });
  }

  /**
   * Updates the visual of a portal according to its direction.
   *
   * @param color the color of the portal.
   * @param direction the new direction of the portal.
   */
  private static void updateVisual(PortalColor color, Direction direction) {
    if (color == PortalColor.GREEN) {
      PortalUtils.getGreenPortal()
          .flatMap(portal -> portal.fetch(DrawComponent.class))
          .ifPresent(dc -> dc.stateMachine().setState(direction.name(), null));
    } else {
      PortalUtils.getBluePortal()
          .flatMap(portal -> portal.fetch(DrawComponent.class))
          .ifPresent(dc -> dc.stateMachine().setState(direction.name(), null));
    }
  }

  /**
   * Handles the teleportation of a player while the collision between the green portal and the
   * player is happening. Only works when the player looks into the direction of the portal.
   *
   * @param portal the portal entity.
   * @param other the player entity.
   * @param direction the direction the collision is currently having.
   */
  private static void onHoldGreen(Entity portal, Entity other, Direction direction) {
    if (other.fetch(PortalIgnoreComponent.class).isPresent()) {
      return;
    }

    PositionComponent portalPositionComponent = portal.fetch(PositionComponent.class).get();
    PositionComponent otherPositionComponent = other.fetch(PositionComponent.class).get();

    if (PortalUtils.getBluePortal().isPresent() && !isEntityPortal(other)) {
      if (Game.player().isPresent()
          && Game.player().get().name().equals(other.name())
          && otherPositionComponent.viewDirection()
              != portalPositionComponent.viewDirection().opposite()) {
        return;
      }
      otherPositionComponent.position(PortalUtils.calculatePortalExit(portal));
      other.add(new PortalIgnoreComponent());
      EventScheduler.scheduleAction(() -> other.remove(PortalIgnoreComponent.class), PORTAL_DELAY);
      other
          .fetch(VelocityComponent.class)
          .ifPresent(
              vc -> {
                vc.clearForces();
                vc.currentVelocity(Vector2.ONE);
              });

      if (Game.player().get().name().equals(other.name())) {
        handleRotation(other, PortalColor.GREEN);
      }
    }
  }

  /**
   * Handles the teleportation of a player while the collision between the blue portal and the
   * player is happening. Only works when the player looks into the direction of the portal.
   *
   * @param portal the portal entity.
   * @param other the player entity.
   * @param direction the direction the collision is currently having.
   */
  private static void onHoldBlue(Entity portal, Entity other, Direction direction) {
    if (other.fetch(PortalIgnoreComponent.class).isPresent()) {
      return;
    }
    PositionComponent portalPositionComponent = portal.fetch(PositionComponent.class).get();
    PositionComponent otherPositionComponent = other.fetch(PositionComponent.class).get();

    if (PortalUtils.getGreenPortal().isPresent() && !isEntityPortal(other)) {
      if (Game.player().isPresent()
          && Game.player().get().name().equals(other.name())
          && otherPositionComponent.viewDirection()
              != portalPositionComponent.viewDirection().opposite()) {
        return;
      }
      Direction greenPortalDirection =
          PortalUtils.getGreenPortal().get().fetch(PositionComponent.class).get().viewDirection();
      otherPositionComponent.position(PortalUtils.calculatePortalExit(portal));

      other.add(new PortalIgnoreComponent());
      EventScheduler.scheduleAction(() -> other.remove(PortalIgnoreComponent.class), PORTAL_DELAY);

      other
          .fetch(VelocityComponent.class)
          .ifPresent(
              vc -> {
                vc.clearForces();
                vc.currentVelocity(Vector2.ONE);
              });
      if (Game.player().get().name().equals(other.name())) {
        handleRotation(other, PortalColor.BLUE);
      }
    }
  }

  /**
   * Moves a portal to a new position, updates the direction and collision component.
   *
   * @param portal The portal that gets moved and updated.
   * @param direction The output direction of the portal.
   * @param point The position of the new portal.
   * @param color The color of the portal.
   */
  private static void moveExistingPortal(
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
    cc.onHold(color == PortalColor.BLUE ? PortalFactory::onHoldBlue : PortalFactory::onHoldGreen);
    portal.remove(CollideComponent.class);
    portal.add(cc);
  }

  /**
   * Sets the portal to be ignored by all {@link ProjectileSkill} so it doesn't trigger the
   * projectiles onCollide.
   *
   * @param portal the portal entity that is going to be ignored by the skills.
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
   * Teleports an entity that collides with the green portal to the corresponding blue portal. If
   * the entity has a {@link PortalExtendComponent} its going to extend it if both portals are
   * alive.
   *
   * @param portal the green portal entity
   * @param other the entity entering the portal
   * @param dir the direction of collision
   */
  public static void onGreenCollideEnter(Entity portal, Entity other, Direction dir) {

    PositionComponent otherPositionComponent = other.fetch(PositionComponent.class).get();
    PositionComponent portalPositionComponent = portal.fetch(PositionComponent.class).get();

    if (other.fetch(PortalExtendComponent.class).isPresent()) {
      PortalExtendComponent pec = other.fetch(PortalExtendComponent.class).get();
      if (pec.isThroughBlue()) {
        return;
      }
      pec.setThroughGreen(true);
      portal.fetch(PortalComponent.class).get().setExtendedEntityThrough(other);
      PortalUtils.getBluePortal()
          .ifPresent(
              bluePortal ->
                  bluePortal.fetch(PortalComponent.class).get().setExtendedEntityThrough(other));
      return;
    }

    if (other.fetch(PortalIgnoreComponent.class).isPresent()) {
      return;
    }

    if (PortalUtils.getBluePortal().isPresent() && !isEntityPortal(other)) {
      if (Game.player().isPresent()
          && Game.player().get().name().equals(other.name())
          && otherPositionComponent.viewDirection()
              != portalPositionComponent.viewDirection().opposite()) {
        return;
      }

      Direction greenPortalDirection = portal.fetch(PositionComponent.class).get().viewDirection();
      Direction bluePortalDirection =
          PortalUtils.getBluePortal().get().fetch(PositionComponent.class).get().viewDirection();
      otherPositionComponent.position(PortalUtils.calculatePortalExit(portal));

      other.add(new PortalIgnoreComponent());
      EventScheduler.scheduleAction(() -> other.remove(PortalIgnoreComponent.class), PORTAL_DELAY);

      if (Game.player().get().name().equals(other.name())) {
        handleRotation(other, PortalColor.GREEN);
      }

      handleProjectiles(other, greenPortalDirection, bluePortalDirection);
    }
  }

  /**
   * Teleports an entity that collides with the blue portal to the corresponding green portal. If
   * the entity has a {@link PortalExtendComponent} its going to extend it if both portals are
   * alive.
   *
   * @param portal the blue portal entity
   * @param other the entity entering the portal
   * @param dir the direction of collision
   */
  public static void onBlueCollideEnter(Entity portal, Entity other, Direction dir) {

    PositionComponent otherPositionComponent = other.fetch(PositionComponent.class).get();
    PositionComponent portalPositionComponent = portal.fetch(PositionComponent.class).get();

    if (other.fetch(PortalExtendComponent.class).isPresent()) {
      PortalExtendComponent pec = other.fetch(PortalExtendComponent.class).get();
      if (pec.isThroughGreen()) {
        return;
      }
      pec.setThroughBlue(true);
      portal.fetch(PortalComponent.class).get().setExtendedEntityThrough(other);
      PortalUtils.getGreenPortal()
          .ifPresent(
              greenPortal ->
                  greenPortal.fetch(PortalComponent.class).get().setExtendedEntityThrough(other));
      return;
    }

    if (other.fetch(PortalIgnoreComponent.class).isPresent()) {
      return;
    }

    if (PortalUtils.getGreenPortal().isPresent() && !isEntityPortal(other)) {
      if (Game.player().isPresent()
          && Game.player().get().name().equals(other.name())
          && otherPositionComponent.viewDirection()
              != portalPositionComponent.viewDirection().opposite()) {
        return;
      }

      Direction bluePortalDirection = portal.fetch(PositionComponent.class).get().viewDirection();
      Direction greenPortalDirection =
          PortalUtils.getGreenPortal().get().fetch(PositionComponent.class).get().viewDirection();

      // move through portal
      otherPositionComponent.position(PortalUtils.calculatePortalExit(portal));

      other.add(new PortalIgnoreComponent());
      EventScheduler.scheduleAction(() -> other.remove(PortalIgnoreComponent.class), PORTAL_DELAY);

      if (Game.player().get().name().equals(other.name())) {
        handleRotation(other, PortalColor.BLUE);
      }

      handleProjectiles(other, bluePortalDirection, greenPortalDirection);
    }
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
    return new CollideComponent(offset, hitbox, onCollideEnter, PortalFactory::onCollideLeave);
  }

  /**
   * Handles the removal of extended entities through a portal.
   *
   * @param portal The portal which the extended entity goes through.
   * @param other The entity that is extended.
   * @param direction Direction where it extends to.
   */
  private static void onCollideLeave(Entity portal, Entity other, Direction direction) {
    clearExtendedEntity(portal, other);
  }

  /**
   * Trims an extended entity by calling its {@link PortalExtendComponent} onTrim Consumer.
   *
   * @param portal The portal the entity is entering at first.
   * @param other The entity that is being extended.
   */
  private static void clearExtendedEntity(Entity portal, Entity other) {
    other
        .fetch(PortalExtendComponent.class)
        .ifPresent(
            pec -> {
              if (pec.isExtended()) {
                pec.onTrim.accept(other);
                pec.setExtended(false);
              }
              PortalUtils.getGreenPortal()
                  .ifPresent(
                      greenPortal -> {
                        if (portal == greenPortal) {
                          pec.setThroughGreen(false);
                          greenPortal
                              .fetch(PortalComponent.class)
                              .ifPresent(pc -> pc.setExtendedEntityThrough(null));
                        }
                      });
              PortalUtils.getBluePortal()
                  .ifPresent(
                      bluePortal -> {
                        if (portal == bluePortal) {
                          pec.setThroughBlue(false);
                          bluePortal
                              .fetch(PortalComponent.class)
                              .ifPresent(pc -> pc.setExtendedEntityThrough(null));
                        }
                      });
            });
  }

  /**
   * Removes both the blue and green portals from the game, if present. Also clears the extended
   * entity if its exists.
   */
  public static void clearAllPortals() {
    clearBluePortal();
    clearGreenPortal();
  }

  /**
   * Removes the blue portal from the game, if present. Also clears the extended entity if its
   * exists.
   */
  public static void clearBluePortal() {
    PortalUtils.getBluePortal()
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
    PortalUtils.getGreenPortal()
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
}
