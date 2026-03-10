package portal.portals;

import contrib.components.CollideComponent;
import contrib.components.SkillComponent;
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
import portal.portals.components.PortalComponent;
import portal.riddles.utils.PortalUtils;

// TODO: die ganze klasse muss refactored werden

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

  /**
   * Creates a portal of the given color at the specified position.
   *
   * <p>If a portal of the same color already exists, it will be replaced. If the new portal
   * overlaps with the other portal, the other portal will be removed.
   *
   * @param point the position where the portal will be placed.
   * @param direction The output direction of the portal.
   * @param color the portal color, see {@link PortalColor}f
   */
  public static void createPortal(Point point, Direction direction, PortalColor color) {
    Optional<Entity> portalToCreate =
        color == PortalColor.BLUE ? PortalUtils.getBluePortal() : PortalUtils.getGreenPortal();
    Optional<Entity> otherPortal =
        color == PortalColor.BLUE ? PortalUtils.getGreenPortal() : PortalUtils.getBluePortal();
    String portalName =
        color == PortalColor.BLUE ? PortalUtils.BLUE_PORTAL_NAME : PortalUtils.GREEN_PORTAL_NAME;
    SimpleIPath texturePath =
        color == PortalColor.BLUE ? BLUE_PORTAL_TEXTURE : GREEN_PORTAL_TEXTURE;

    checkIfOtherPortalIsPresent(otherPortal, point);
    portalToCreate.ifPresentOrElse(portal -> updateExistingPortal(portal, point, direction, color),
        () ->
          spawnNewPortal(portalName, texturePath, point, direction, color)
        );
  }

  private static void spawnNewPortal(String portalName, SimpleIPath texturePath, Point point, Direction direction, PortalColor color ) {
    Entity portal = new Entity(portalName);
    // To allow collision with the stationary Portal elements like lightwalls.
    portal.add(new VelocityComponent(0.0000000001f));
    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(texturePath);

    State fallback = new State("NONE", animationMap.get("fallback"));
    State top = new State("UP", animationMap.get("bottom"));
    State bottom = new State("DOWN", animationMap.get("fallback"));
    State left = new State("LEFT", animationMap.get("left"));
    State right = new State("RIGHT", animationMap.get("right"));
    StateMachine sm = new StateMachine(Arrays.asList(fallback, top, bottom, left, right));
    sm.setState(direction.name(), null);
    portal.add(new DrawComponent(sm));
    updateVisual(color, direction);

    PositionComponent pc = new PositionComponent(point);
    pc.viewDirection(direction);
    portal.add(pc);

    CollideComponent cc = PortalCollisionHandler.setCollideComponent(
      direction, PortalCollisionHandler.createOnCollideHandler(color));
    cc.onHold(PortalCollisionHandler.createOnHoldHandler(color));

    cc.isSolid(false);
    portal.add(cc);
    portal.add(new PortalComponent());

    Game.add(portal);
    ignorePortalInProjectiles(portal);
  }

  private static void updateExistingPortal(Entity portal, Point point, Direction direction, PortalColor color) {
    Point oldPosition = portal.fetch(PositionComponent.class).get().position();
    // wenn das Portal an die gleiche stelle geschossen wird, passiert nichts
    if (oldPosition.equals(point)) {
      return;
    }

    // cleared the Extended Entity property
    Entity other = portal.fetch(PortalComponent.class).get().getExtendedEntityThrough();
    if (other != null) {
      PortalExtendHandler.clearExtendedEntity(portal, other);
    }

    // moves the portal
    moveExistingPortal(portal, direction, point, color);
    updateVisual(color, direction);
  }


  private static void checkIfOtherPortalIsPresent(Optional<Entity> otherPortal, Point point) {
    otherPortal.ifPresent(
      portal -> {
        if (portal.fetch(PositionComponent.class).get().position().equals(point)) {
          Entity other = portal.fetch(PortalComponent.class).get().getExtendedEntityThrough();
          if (other != null) {
            PortalExtendHandler.clearExtendedEntity(portal, other);
          }
          otherPortal.ifPresent(PortalFactory::clearPortal);
        }
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
        PortalCollisionHandler.setCollideComponent(
            direction,
            color == PortalColor.BLUE
                ? PortalCollisionHandler.createOnCollideHandler(PortalColor.BLUE)
                : PortalCollisionHandler.createOnCollideHandler(PortalColor.GREEN));
    cc.isSolid(false);
    cc.onHold(
        color == PortalColor.BLUE
            ? PortalCollisionHandler.createOnHoldHandler(PortalColor.BLUE)
            : PortalCollisionHandler.createOnHoldHandler(PortalColor.GREEN));
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
   * Removes both the blue and green portals from the game, if present. Also clears the extended
   * entity if its exists.
   */
  public static void clearAllPortals() {
    PortalUtils.getBluePortal().ifPresent(PortalFactory::clearPortal);
    PortalUtils.getGreenPortal().ifPresent(PortalFactory::clearPortal);
  }

  private static void clearPortal(Entity portal) {
    Entity other = portal.fetch(PortalComponent.class).get().getExtendedEntityThrough();
    if (other != null) {
      PortalExtendHandler.clearExtendedEntity(portal, other);
    }
    Game.remove(portal);
  }
}
