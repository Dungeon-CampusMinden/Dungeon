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

  public static void createBluePortal(Point point, Vector2 currentVelocity) {
    Entity portal;
    clearBluePortal();
    if (greenPortal != null && greenPortal.fetch(PositionComponent.class).get().position().equals(point)) {
      clearGreenPortal();
    }
    portal = new Entity("blue_portal");
    PositionComponent pc = new PositionComponent(point);
    portal.add(new PortalComponent());

    // checking all Neighbours, for each compare the currentVelocity with the impact direction, put them into a list
    // get the best score of the list(?), if its a wall go to next best one, the resulting neighbour is the  direction where
    // the hero gets teleported into so he doesnt get stuck in the wall

    Set<Tile> neighbours = Game.neighbours(Game.tileAt(point).get()).stream().filter(tile -> tile.levelElement() == LevelElement.FLOOR).collect(Collectors.toSet());
    ArrayList<Tuple<Point, Double>> list = new ArrayList<>();
    for (Tile tile : neighbours) {
      double distance = point.distance(tile.position().toCenteredPoint());
      list.add(new Tuple<>(tile.position(),distance));
    }

    list.sort(Comparator.comparingDouble(Tuple::b));

    CollideComponent cc;
    // list first is best one
    Point best = list.getFirst().a();
    Point pointDirection = new Point(point.x()-best.x(), point.y()-best.y());
    if (pointDirection.equals(new Point(0,1))) {
      bluePortalDirection = Direction.UP;
    } else if (pointDirection.equals(new Point(0,-1))) {
      bluePortalDirection = Direction.DOWN;
    } else if (pointDirection.equals(new Point(1,0))) {
      bluePortalDirection = Direction.RIGHT;
    } else {
      bluePortalDirection = Direction.LEFT;
    }
    pc.viewDirection(bluePortalDirection);
    portal.add(pc);
    cc = setCollideComponent(bluePortalDirection, PortalFactory::onBlueCollideEnter);
    cc.isSolid(false);

    portal.add(cc);

    portal.add(new DrawComponent(new SimpleIPath("portals/blue_portal")));
    Game.add(portal);
    bluePortal = portal;
    Game.allEntities().filter(entity -> entity.isPresent(SkillComponent.class))
      .forEach(entity -> {
        SkillComponent skillComponent = entity.fetch(SkillComponent.class).get();
        for (Skill skill : skillComponent.getSkills()) {
          if (skill instanceof ProjectileSkill) {
            ProjectileSkill projectileSkill = (ProjectileSkill) skill;
            projectileSkill.ignoreEntity(portal);
          }
        }
      });
  }

  public static void createGreenPortal(Point point, Vector2 currentVelocity) {
    Entity portal;
    clearGreenPortal();
    if (bluePortal != null &&  bluePortal.fetch(PositionComponent.class).get().position().equals(point)) {
      clearBluePortal();
    }
    portal = new Entity("green_portal");
    PositionComponent pc = new PositionComponent(point);
    portal.add(new PortalComponent());
    Set<Tile> neighbours = Game.neighbours(Game.tileAt(point).get()).stream().filter(tile -> tile.levelElement() == LevelElement.FLOOR).collect(Collectors.toSet());
    ArrayList<Tuple<Point, Double>> list = new ArrayList<>();
    for (Tile tile : neighbours) {
      list.add(new Tuple<>(tile.position(),point.distance(tile.position().toCenteredPoint())));
    }

    list.sort(Comparator.comparingDouble(Tuple::b));

    CollideComponent cc;
    // list first is best one
    Point best = list.getFirst().a();
    Point pointDirection = new Point(point.x()-best.x(), point.y()-best.y());
    if (pointDirection.equals(new Point(0,1))) {
      greenPortalDirection = Direction.UP;
    } else if (pointDirection.equals(new Point(0,-1))) {
      greenPortalDirection = Direction.DOWN;
    } else if (pointDirection.equals(new Point(1,0))) {
      greenPortalDirection = Direction.RIGHT;
    } else {
      greenPortalDirection = Direction.LEFT;
    }
    pc.viewDirection(greenPortalDirection);
    cc = setCollideComponent(greenPortalDirection, PortalFactory::onGreenCollideEnter);
    cc.isSolid(false);
    portal.add(pc);
    portal.add(cc);
    portal.add(new DrawComponent(new SimpleIPath("portals/green_portal")));
    Game.add(portal);
    greenPortal = portal;
    Game.allEntities().filter(entity -> entity.isPresent(SkillComponent.class))
      .forEach(entity -> {
        SkillComponent skillComponent = entity.fetch(SkillComponent.class).get();
        for (Skill skill : skillComponent.getSkills()) {
          if (skill instanceof ProjectileSkill) {
            ProjectileSkill projectileSkill = (ProjectileSkill) skill;
            projectileSkill.ignoreEntity(portal);
          }
        }
      });
  }

  public static void onGreenCollideEnter(Entity portal, Entity other, Direction dir) {
    if (bluePortal != null && !isEntityPortal(other)) {
      PositionComponent pc = other.fetch(PositionComponent.class).get();
      pc.position(bluePortal.fetch(PositionComponent.class).get().position().translate(bluePortalDirection.opposite()));
      handleProjectiles(other, greenPortalDirection.opposite(), bluePortalDirection.opposite());
    }
  }

  public static void onBlueCollideEnter(Entity portal, Entity other, Direction dir) {
    if (greenPortal != null && !isEntityPortal(other)) {
      PositionComponent pc = other.fetch(PositionComponent.class).get();
      pc.position(greenPortal.fetch(PositionComponent.class).get().position().translate(greenPortalDirection.opposite()));
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
    double offset06 = 0.5;
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
