package produsAdvanced.abstraction.portals;

import contrib.components.CollideComponent;
import contrib.components.ProjectileComponent;
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

import java.util.*;
import java.util.stream.Collectors;

public class PortalFactory {

  private static Entity bluePortal;
  private static Entity greenPortal;

  private static Direction bluePortalDirection;
  private static Direction greenPortalDirection;

  public static void createBluePortal(Point point, Vector2 currentVelocity) {
    Entity portal;
    clearBluePortal();
    if (greenPortal != null && greenPortal.fetch(PositionComponent.class).get().position().equals(point)) {
      clearGreenPortal();
    }
    portal = new Entity("blue_portal");
    portal.add(new PositionComponent(point));
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
      cc = new CollideComponent(Vector2.of(-0.1,-0.1), Vector2.of(1.2,0.5), PortalFactory::onBlueCollideEnter, CollideComponent.DEFAULT_COLLIDER);
      bluePortalDirection = Direction.UP;
    } else if (pointDirection.equals(new Point(0,-1))) {
      bluePortalDirection = Direction.DOWN;
      cc = new CollideComponent(Vector2.of(-0.1,0.6), Vector2.of(1.2,0.5), PortalFactory::onBlueCollideEnter, CollideComponent.DEFAULT_COLLIDER);
    } else if (pointDirection.equals(new Point(1,0))) {
      bluePortalDirection = Direction.RIGHT;
      cc = new CollideComponent(Vector2.of(-0.1,-0.1), Vector2.of(0.5,1.2), PortalFactory::onBlueCollideEnter, CollideComponent.DEFAULT_COLLIDER);
    } else {
      bluePortalDirection = Direction.LEFT;
      cc = new CollideComponent(Vector2.of(0.6,-0.1), Vector2.of(0.5,1.2), PortalFactory::onBlueCollideEnter, CollideComponent.DEFAULT_COLLIDER);
    }
    cc.isSolid(false);

    portal.add(cc);

    portal.add(new DrawComponent(new SimpleIPath("portals/blue_portal")));
    Game.add(portal);
    bluePortal = portal;
  }

  public static void createGreenPortal(Point point, Vector2 currentVelocity) {
    Entity portal;
    clearGreenPortal();
    if (bluePortal != null &&  bluePortal.fetch(PositionComponent.class).get().position().equals(point)) {
      clearBluePortal();
    }
    portal = new Entity("green_portal");
    portal.add(new PositionComponent(point));
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
      cc = new CollideComponent(Vector2.of(-0.1,-0.1), Vector2.of(1.2,0.5), PortalFactory::onGreenCollideEnter, CollideComponent.DEFAULT_COLLIDER);
      greenPortalDirection = Direction.UP;
    } else if (pointDirection.equals(new Point(0,-1))) {
      greenPortalDirection = Direction.DOWN;
      cc = new CollideComponent(Vector2.of(-0.1,0.6), Vector2.of(1.2,0.5), PortalFactory::onGreenCollideEnter, CollideComponent.DEFAULT_COLLIDER);
    } else if (pointDirection.equals(new Point(1,0))) {
      greenPortalDirection = Direction.RIGHT;
      cc = new CollideComponent(Vector2.of(-0.1,-0.1), Vector2.of(0.5,1.2), PortalFactory::onGreenCollideEnter, CollideComponent.DEFAULT_COLLIDER);
    } else {
      greenPortalDirection = Direction.LEFT;
      cc = new CollideComponent(Vector2.of(0.6,-0.1), Vector2.of(0.5,1.2), PortalFactory::onGreenCollideEnter, CollideComponent.DEFAULT_COLLIDER);
    }
    cc.isSolid(false);
    portal.add(cc);
    portal.add(new DrawComponent(new SimpleIPath("portals/green_portal")));
    Game.add(portal);
    greenPortal = portal;
  }

  public static void onGreenCollideEnter(Entity portal, Entity other, Direction dir) {
    if (bluePortal != null && !isEntityPortal(other)) {
      PositionComponent pc = other.fetch(PositionComponent.class).get();
      pc.position(bluePortal.fetch(PositionComponent.class).get().position().translate(bluePortalDirection.opposite().scale(1.01)));
      handleProjectiles(other, greenPortalDirection.opposite(), bluePortalDirection.opposite());
    }
  }

  public static void onBlueCollideEnter(Entity portal, Entity other, Direction dir) {
    if (greenPortal != null && !isEntityPortal(other)) {
      PositionComponent pc = other.fetch(PositionComponent.class).get();
      pc.position(greenPortal.fetch(PositionComponent.class).get().position().translate(greenPortalDirection.opposite().scale(1.01)));
      handleProjectiles(other, bluePortalDirection.opposite(), greenPortalDirection.opposite());
    }
  }

  public static void handleProjectiles(Entity projectile, Direction entry, Direction exit) {
    if (!projectile.isPresent(ProjectileComponent.class)) {
      return;
    }
    Entity entity = new Entity();
    for (Component component : projectile.componentStream().toList()) {
      entity.add(component);
    }
    Game.remove(projectile);

    VelocityComponent vc = projectile.fetch(VelocityComponent.class).get();
    vc.currentVelocity(rotateVelocityThroughPortals(vc.currentVelocity(), entry, exit));
    PositionComponent pc = projectile.fetch(PositionComponent.class).get();
    pc.rotation((float) exit.angleDeg());
    pc.position(pc.position().translate(exit));
    Game.add(entity);
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
}
