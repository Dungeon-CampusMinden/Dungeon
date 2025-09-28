package produsAdvanced.abstraction.portals;

import contrib.components.CollideComponent;
import contrib.components.ProjectileComponent;
import contrib.components.SkillComponent;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.projectileSkill.ProjectileSkill;
import core.Component;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.*;
import core.utils.components.path.SimpleIPath;
import produsAdvanced.abstraction.portals.components.PortalComponent;

import java.util.*;
import java.util.stream.Collectors;

public class PortalFactory {

  private static Entity bluePortal = null;
  private static Entity greenPortal = null;

  private static Direction bluePortalDirection;
  private static Direction greenPortalDirection;

  public static void createPortal(Point point, PortalColor color) {
    Entity portal = preparePortal(point, color);

    // Components
    portal.add(new DrawComponent(new SimpleIPath(getPortalPath(color))));

    PositionComponent pc = new PositionComponent(point);
    portal.add(new PortalComponent());

    Direction dir = setPortalDirection(point, color);
    pc.viewDirection(dir);

    CollideComponent cc = setCollideComponent(dir, getCollideHandler(color));
    cc.isSolid(false);

    portal.add(pc);
    portal.add(cc);

    // Save reference
    assignPortalReference(color, portal);

    Game.add(portal);
    ignorePortalInProjectiles(portal);
  }

  private static Entity preparePortal(Point point, PortalColor color) {
    switch (color) {
      case BLUE -> {
        clearBluePortal();
        removeIfOverlap(greenPortal, point, PortalFactory::clearGreenPortal);
        return new Entity("blue_portal");
      }
      case GREEN -> {
        clearGreenPortal();
        removeIfOverlap(bluePortal, point, PortalFactory::clearBluePortal);
        return new Entity("green_portal");
      }
      default -> throw new IllegalArgumentException("Unknown portal color: " + color);
    }
  }

  private static void removeIfOverlap(Entity portal, Point point, Runnable clearAction) {
    if (portal != null && portal.fetch(PositionComponent.class).get().position().equals(point)) {
      clearAction.run();
    }
  }

  private static String getPortalPath(PortalColor color) {
    return switch (color) {
      case BLUE -> "portals/blue_portal";
      case GREEN -> "portals/green_portal";
    };
  }

  private static TriConsumer<Entity, Entity, Direction> getCollideHandler(PortalColor color) {
    return (color == PortalColor.BLUE) ? PortalFactory::onBlueCollideEnter
      : PortalFactory::onGreenCollideEnter;
  }

  private static void assignPortalReference(PortalColor color, Entity portal) {
    if (color == PortalColor.BLUE) {
      bluePortal = portal;
    } else {
      greenPortal = portal;
    }
  }

  private static void ignorePortalInProjectiles(Entity portal) {
    Game.allEntities()
      .filter(entity -> entity.isPresent(SkillComponent.class))
      .forEach(entity -> {
        SkillComponent skillComponent = entity.fetch(SkillComponent.class).get();
        for (Skill skill : skillComponent.getSkills()) {
          if (skill instanceof ProjectileSkill projectileSkill) {
            projectileSkill.ignoreEntity(portal);
          }
        }
      });
  }

  private static Direction setPortalDirection(Point point, PortalColor color ) {
    Set<Tile> neighbours = Game.neighbours(Game.tileAt(point).get()).stream().filter(tile -> tile.levelElement() == LevelElement.FLOOR).collect(Collectors.toSet());
    ArrayList<Tuple<Point, Double>> list = new ArrayList<>();
    for (Tile tile : neighbours) {
      double distance = point.distance(tile.position().toCenteredPoint());
      list.add(new Tuple<>(tile.position(),distance));
    }

    list.sort(Comparator.comparingDouble(Tuple::b));


    Point best = list.getFirst().a();
    Point pointDirection = new Point(point.x()-best.x(), point.y()-best.y());
    Direction dir = toDirection(pointDirection);

    if (color == PortalColor.GREEN) {
      greenPortalDirection = dir;
    } else {
      bluePortalDirection = dir;
    }

    return dir;
  }

  private static Direction toDirection(Point p) {
    if (p.equals(new Point(0, 1)))   return Direction.UP;
    if (p.equals(new Point(0, -1)))  return Direction.DOWN;
    if (p.equals(new Point(1, 0)))   return Direction.RIGHT;
    return Direction.LEFT; // default / fallback
  }

  public static void onGreenCollideEnter(Entity portal, Entity other, Direction dir) {
    if (bluePortal != null && !isEntityPortal(other)) {
      PositionComponent pc = other.fetch(PositionComponent.class).get();
      pc.position(bluePortal.fetch(PositionComponent.class).get().position().translate(bluePortalDirection.opposite().scale(1.2)));
      handleProjectiles(other, greenPortalDirection.opposite(), bluePortalDirection.opposite());
    }
  }

  public static void onBlueCollideEnter(Entity portal, Entity other, Direction dir) {
    if (greenPortal != null && !isEntityPortal(other)) {
      PositionComponent pc = other.fetch(PositionComponent.class).get();
      pc.position(greenPortal.fetch(PositionComponent.class).get().position().translate(greenPortalDirection.opposite().scale(1.2)));
      handleProjectiles(other, bluePortalDirection.opposite(), greenPortalDirection.opposite());
    }
  }

  public static void handleProjectiles(Entity projectile, Direction entry, Direction exit) {
    if (!projectile.isPresent(ProjectileComponent.class)) {
      return;
    }
    VelocityComponent vc = projectile.fetch(VelocityComponent.class).get();
    PositionComponent pc = projectile.fetch(PositionComponent.class).get();
    ProjectileComponent prc = projectile.fetch(ProjectileComponent.class).get();
    projectile.remove(ProjectileComponent.class);

    Vector2 velocity = rotateVelocityThroughPortals(vc.currentVelocity(), entry, exit);
    Vector2 goal = prc.goalLocation().vectorTo(pc.position()).rotateDeg(velocity.angleDeg());
    projectile.add(new ProjectileComponent(pc.position(), new Point(goal.x(),goal.y()), prc.forceToApply(), prc.onEndReached()));
    vc.currentVelocity(velocity);
    pc.rotation((float) velocity.angleDeg());
    pc.position(pc.position().translate(exit));
  }

  public static Vector2 rotateVelocityThroughPortals(Vector2 velocity, Direction portalA, Direction portalB) {
    // angles of portal orientations
    double angleA = Math.atan2(portalA.y(), portalA.x());
    double angleB = Math.atan2(portalB.y(), portalB.x());

    // relative rotation, flip included (+π)
    double delta = angleB - angleA + Math.PI;

    double cos = Math.cos(delta);
    double sin = Math.sin(delta);

    double newX = velocity.x() * cos - velocity.y() * sin;
    double newY = velocity.x() * sin + velocity.y() * cos;

    return Vector2.of(newX, newY);
  }


  private static CollideComponent setCollideComponent(Direction dir, TriConsumer<Entity, Entity, Direction> onCollideEnter) {
    double offsetMinus01 = -0.2;
    double offset06 = 0.6;
    double offset12 = 1.4;
    double offset05 = 0.6;
    switch (dir){
      case UP -> {
        return new CollideComponent(Vector2.of(offsetMinus01,offsetMinus01), Vector2.of(offset12,offset05), onCollideEnter, CollideComponent.DEFAULT_COLLIDER);
      }
      case DOWN -> {
        return new CollideComponent(Vector2.of(offsetMinus01,offset06), Vector2.of(offset12,offset05), onCollideEnter, CollideComponent.DEFAULT_COLLIDER);
      }
      case RIGHT -> {
        return new CollideComponent(Vector2.of(offsetMinus01,offsetMinus01), Vector2.of(offset05,offset12), onCollideEnter, CollideComponent.DEFAULT_COLLIDER);
      }
      case LEFT -> {
        return new CollideComponent(Vector2.of(offset06,offsetMinus01), Vector2.of(offset05,offset12), onCollideEnter, CollideComponent.DEFAULT_COLLIDER);
      }
      default -> {
        return new CollideComponent();
      }
    }
  }

  public static void clearAllPortals() {
    clearBluePortal();
    clearGreenPortal();
  }

  public static void clearBluePortal() {
    if (bluePortal != null) {
      System.out.println("Blue Portal removed");
      Game.remove(bluePortal);
      bluePortal = null;
    }
  }

  public static void clearGreenPortal() {
    if (greenPortal != null) {
      System.out.println("Green Portal removed");
      Game.remove(greenPortal);
      greenPortal = null;
    }
  }

  private static boolean isEntityPortal(Entity entity) {
    return entity.isPresent(PortalComponent.class);
  }

  public static Optional<Entity> getBluePortal() {
    return Optional.ofNullable(bluePortal);
  }

  public static Optional<Entity> getGreenPortal() {
    return Optional.ofNullable(greenPortal);
  }

}
