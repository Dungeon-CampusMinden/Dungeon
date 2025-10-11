package produsAdvanced.abstraction.portals;

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
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.*;
import core.utils.components.path.SimpleIPath;
import java.util.*;
import java.util.stream.Collectors;
import produsAdvanced.abstraction.portals.components.PortalComponent;
import produsAdvanced.abstraction.portals.components.PortalExtendComponent;
import produsAdvanced.abstraction.portals.components.TractorBeamComponent;

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

  /**
   * Creates a portal of the given color at the specified position.
   *
   * <p>If a portal of the same color already exists, it will be replaced. If the new portal
   * overlaps with the other portal, the other portal will be removed.
   *
   * @param point the position where the portal should be placed
   * @param color the portal color, see {@link PortalColor}
   */
  public static void createPortal(Point point, PortalColor color) {
    if (color == PortalColor.GREEN) {
      if (getGreenPortal().isPresent()) {
        System.out.println("HIER: if (getGreenPortal().isPresent()) {");
        removeIfOverlap(getBluePortal(), point, PortalFactory::clearBluePortal);
        getGreenPortal().ifPresent(greenPortal -> {
          greenPortal.fetch(PositionComponent.class).get().position(point);
          Direction dir = setPortalDirection(point, color);
          greenPortal.fetch(PositionComponent.class).get().viewDirection(dir);

          CollideComponent cc = setCollideComponent(dir, getCollideHandler(color));
          cc.isSolid(false);
          greenPortal.remove(CollideComponent.class);
          greenPortal.add(cc);
        });
        return;
      }
    }
    if (color == PortalColor.BLUE) {
      if (getBluePortal().isPresent()) {
        System.out.println("HIER: if (getBluePortal().isPresent()) {");
        removeIfOverlap(getGreenPortal(), point, PortalFactory::clearGreenPortal);
        getBluePortal().ifPresent(bluePortal -> {
          bluePortal.fetch(PositionComponent.class).get().position(point);
          Direction dir = setPortalDirection(point, color);
          bluePortal.fetch(PositionComponent.class).get().viewDirection(dir);

          CollideComponent cc = setCollideComponent(dir, getCollideHandler(color));
          cc.isSolid(false);
          bluePortal.remove(CollideComponent.class);
          bluePortal.add(cc);
        });
        return;
      }
    }

    Entity portal = preparePortal(point, color);

    portal.add(new DrawComponent(new SimpleIPath(getPortalPath(color))));

    PositionComponent pc = new PositionComponent(point);
    portal.add(new PortalComponent());

    Direction dir = setPortalDirection(point, color);
    pc.viewDirection(dir);

    CollideComponent cc = setCollideComponent(dir, getCollideHandler(color));
    cc.isSolid(false);

    portal.add(pc);
    portal.add(cc);

    Game.add(portal);
    ignorePortalInProjectiles(portal);
  }

  /**
   * Prepares a new portal entity of the given color at the specified position.
   *
   * <p>If a portal of the same color already exists, it will be removed. If the new portal would
   * overlap with the other portal, that portal will also be cleared before creation.
   *
   * @param point the position where the portal will be placed
   * @param color the color of the portal to create (blue or green)
   * @return a new portal entity with the given color
   * @throws IllegalArgumentException if the color is not recognized
   */
  private static Entity preparePortal(Point point, PortalColor color) {
    switch (color) {
      case BLUE -> {
        clearBluePortal();
        removeIfOverlap(getGreenPortal(), point, PortalFactory::clearGreenPortal);
        return new Entity(BLUE_PORTAL_NAME);
      }
      case GREEN -> {
        clearGreenPortal();
        removeIfOverlap(getBluePortal(), point, PortalFactory::clearBluePortal);
        return new Entity(GREEN_PORTAL_NAME);
      }
      default -> throw new IllegalArgumentException("Unknown portal color: " + color);
    }
  }

  /**
   * Removes the given portal if it exists at the specified position.
   *
   * @param portal the portal entity to check
   * @param point the position to check against
   * @param clearAction the action to execute if overlap is detected
   */
  private static void removeIfOverlap(Optional<Entity> portal, Point point, Runnable clearAction) {
    if (portal.isPresent()
        && portal.get().fetch(PositionComponent.class).get().position().equals(point)) {
      clearAction.run();
    }
  }

  /**
   * Returns the corresponding sprite path.
   *
   * @param color the portal color
   * @return the path to the portal sprite
   */
  private static String getPortalPath(PortalColor color) {
    return switch (color) {
      case BLUE -> "portal/blue_portal";
      case GREEN -> "portal/green_portal";
    };
  }

  /**
   * Returns the corresponding collide handler.
   *
   * @param color the portal color
   * @return a handler that defines what happens when an entity collides with the portal
   */
  private static TriConsumer<Entity, Entity, Direction> getCollideHandler(PortalColor color) {
    return (color == PortalColor.BLUE)
        ? PortalFactory::onBlueCollideEnter
        : PortalFactory::onGreenCollideEnter;
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
   * Determines the direction a portal should face, based on nearby floor tiles.
   *
   * <p>The closest floor tile to the portal is used to decide its orientation. The calculated
   * direction is also stored for teleportation logic.
   *
   * @param point the position of the portal
   * @param color the portal color
   * @return the direction the portal should face
   */
  private static Direction setPortalDirection(Point point, PortalColor color) {
    Set<Tile> neighbours =
        Game.neighbours(Game.tileAt(point).get()).stream()
            .filter(tile -> tile.levelElement() == LevelElement.FLOOR)
            .collect(Collectors.toSet());
    ArrayList<Tuple<Point, Double>> list = new ArrayList<>();
    for (Tile tile : neighbours) {
      double distance = point.distance(tile.position().toCenteredPoint());
      list.add(new Tuple<>(tile.position(), distance));
    }

    /* Sorts the list so the nearestTile is at the first slot */
    list.sort(Comparator.comparingDouble(Tuple::b));

    Point nearestTile = list.getFirst().a();
    Point pointDirection = new Point(point.x() - nearestTile.x(), point.y() - nearestTile.y());
    Direction direction = toDirection(pointDirection).opposite();

    return direction;
  }

  /**
   * Converts a point into a direction constant.
   *
   * @param p the point offset
   * @return the corresponding direction
   */
  private static Direction toDirection(Point p) {
    if (p.equals(new Point(0, 1))) return Direction.UP;
    if (p.equals(new Point(0, -1))) return Direction.DOWN;
    if (p.equals(new Point(1, 0))) return Direction.RIGHT;
    return Direction.LEFT; // default / fallback
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
      PortalExtendComponent pec = other.fetch(PortalExtendComponent.class).get();
      System.out.println("TEST onGreenCollideEnter");
      if (pec.checkBlue()) {
        return;
      }
      pec.throughGreen = true;
      java.lang.System.out.println("throughGreen is true now");
      return;
    }

    if (getBluePortal().isPresent() && !isEntityPortal(other)) {
      PositionComponent projectilePositionComponent =
              other.fetch(PositionComponent.class).get();
      Direction greenPortalDirection =
              portal.fetch(PositionComponent.class).get().viewDirection();
      Direction bluePortalDirection =
              getBluePortal().get().fetch(PositionComponent.class).get().viewDirection();
      projectilePositionComponent.position(
              getBluePortal()
                      .get()
                      .fetch(PositionComponent.class)
                      .get()
                      .position()
                      .translate(bluePortalDirection.scale(1.2)));
      handleProjectiles(other, greenPortalDirection, bluePortalDirection);
    };
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
      PortalExtendComponent pec = other.fetch(PortalExtendComponent.class).get();
      System.out.println("TEST onBlueCollideEnter");
      if (pec.checkGreen()) {
        return;
      }
      pec.throughBlue = true;
      java.lang.System.out.println("throughBlue is true now");
      return;
    }

    if (getGreenPortal().isPresent() && !isEntityPortal(other)) {
      PositionComponent projectilePositionComponent =
              other.fetch(PositionComponent.class).get();
      Direction bluePortalDirection =
              portal.fetch(PositionComponent.class).get().viewDirection();
      Direction greenPortalDirection =
              getGreenPortal().get().fetch(PositionComponent.class).get().viewDirection();
      projectilePositionComponent.position(
              getGreenPortal()
                      .get()
                      .fetch(PositionComponent.class)
                      .get()
                      .position()
                      .translate(greenPortalDirection.scale(1.2)));
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
   * Converts an angle from degrees to radians.
   *
   * @param degrees the angle in degrees
   * @return the angle converted to radians
   */
  private static double toRadians(double degrees) {
    return degrees * Math.PI / 180.0;
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
    double offsetY = 0.6;
    double hitboxX = 1.4;
    double hitboxY = 0.6;
    switch (dir) {
      case DOWN -> {
        return new CollideComponent(
            Vector2.of(offsetX, offsetX),
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
            Vector2.of(offsetX, offsetX),
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

  public static void onCollideLeave(Entity portal, Entity other, Direction direction) {
    System.out.println("onCollideLeave CALLED BY " + portal.name());
    other.fetch(PortalExtendComponent.class).ifPresent(pec -> {
      if (pec.isExtended()) {
        System.out.println("TRIMMED " + other.name() + " with " + portal.name());
        pec.onTrim.accept(other);
        getGreenPortal().ifPresent(greenPortal -> {
          if (greenPortal == portal) {
            pec.throughGreen = false;
            pec.isExtended = false;
          }
        });
        getBluePortal().ifPresent(bluePortal -> {
          if (bluePortal == portal) {
            pec.throughBlue = false;
            pec.isExtended = false;
          }
        });

      }
    });
  }

  /** Removes both the blue and green portals from the game, if present. */
  public static void clearAllPortals() {
    clearBluePortal();
    clearGreenPortal();
  }

  /** Removes the blue portal from the game, if present. */
  public static void clearBluePortal() {
    getBluePortal().ifPresent(Game::remove);
  }

  /** Removes the green portal from the game, if present. */
  public static void clearGreenPortal() {
    getGreenPortal().ifPresent(Game::remove);
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

  private static void extendCollision() {

  }
}
