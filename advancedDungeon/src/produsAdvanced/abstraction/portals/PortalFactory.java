package produsAdvanced.abstraction.portals;

import contrib.components.CollideComponent;
import contrib.components.ProjectileComponent;
import core.Component;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
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

    portal.add(cc);
    portal.add(new DrawComponent(new SimpleIPath("portals/green_portal")));
    Game.add(portal);
    greenPortal = portal;
  }

  public static void onGreenCollideEnter(Entity portal, Entity other, Direction dir) {
    if (bluePortal != null && !isEntityPortal(other)) {
//      System.out.println("Used Green Portal - Teleported " + other.name() + " to " + bluePortal.fetch(PositionComponent.class).get().position().translate(greenPortalDirection));
      PositionComponent pc = other.fetch(PositionComponent.class).get();
      pc.position(bluePortal.fetch(PositionComponent.class).get().position().translate(bluePortalDirection.opposite()));
      handleProjectiles(other);
    }
  }

  public static void onBlueCollideEnter(Entity portal, Entity other, Direction dir) {
    if (greenPortal != null && !isEntityPortal(other)) {
//      System.out.println("Used Blue Portal - Teleported " + other.name() + " to " + greenPortal.fetch(PositionComponent.class).get().position().translate(bluePortalDirection));
      PositionComponent pc = other.fetch(PositionComponent.class).get();
      pc.position(greenPortal.fetch(PositionComponent.class).get().position().translate(greenPortalDirection.opposite()));
      handleProjectiles(other);
    }
  }

  public static void handleProjectiles(Entity projectile) {
    System.out.println("Projectile ID: " + projectile.id());
    if (!projectile.isPresent(ProjectileComponent.class)) {
      return;
    }
    Entity entity = new Entity();
    for (Component component : projectile.componentStream().toList()) {
      entity.add(component);
    }
    Game.remove(projectile);
    Game.add(entity);
    System.out.println("Projectile ID: " + entity.id());
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
