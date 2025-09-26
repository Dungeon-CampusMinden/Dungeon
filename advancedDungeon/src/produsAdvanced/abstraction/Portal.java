package produsAdvanced.abstraction;

import contrib.components.CollideComponent;
import contrib.entities.EntityFactory;
import contrib.entities.MiscFactory;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.*;
import core.utils.components.path.SimpleIPath;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Portal {

  private static Entity bluePortal;
  private static Entity greenPortal;
  private static Entity stone;

  private static Point bluePortalDirection;
  private static Point greenPortalDirection;

  public static void createBluePortal(Point point, Vector2 currentVelocity, Point projectilePosition) {
    clearBluePortal();
    Entity portal;
    portal = new Entity("blue_portal");
    portal.add(new PositionComponent(point));
    CollideComponent cc = new CollideComponent(CollideComponent.DEFAULT_OFFSET, Vector2.of(1.05,1.05), Portal::onBlueCollideEnter, CollideComponent.DEFAULT_COLLIDER);
    portal.add(cc);

    // checking all Neighbours, for each compare the currentVelocity with the impact direction, put them into a liste
    // get the best score of the list(?), if its a wall go to next best one, the resulting neighbour the the direction where
    // the hero gets teleported into so he doesnt get stuck in the wall

    Set<Tile> neighbours = Game.neighbours(Game.tileAt(point).get()).stream().filter(tile -> tile.levelElement() == LevelElement.FLOOR).collect(Collectors.toSet());
    ArrayList<Tuple<Point, Double>> list = new ArrayList<>();
    for (Tile tile : neighbours) {
      double distance = projectilePosition.distance(tile.position().toCenteredPoint());
      System.out.println("---");
      System.out.println("Coords: " + tile.position().toCenteredPoint().toString() + "Distanz : " + distance);
      list.add(new Tuple<>(tile.position(),distance));
    }
    System.out.println("Projectile Position: " + projectilePosition.toString());

    list.sort(Comparator.comparingDouble(Tuple::b));

    // list first is best one
    Point best = list.getFirst().a();
    System.out.println(new Point(point.x()-best.x(), point.y()-best.y()));
    portal.add(new DrawComponent(new SimpleIPath("portals/blue_portal")));
    Game.add(portal);
    bluePortal = portal;
  }

  public static void createGreenPortal(Point point, Vector2 currentVelocity, Point projectilePosition) {
    clearGreenPortal();
    Entity portal;
    portal = new Entity("green_portal");
    portal.add(new PositionComponent(point));
    CollideComponent cc = new CollideComponent(CollideComponent.DEFAULT_OFFSET, Vector2.of(1.05,1.05), Portal::onGreenCollideEnter, CollideComponent.DEFAULT_COLLIDER);
    portal.add(cc);

    portal.add(new DrawComponent(new SimpleIPath("portals/green_portal")));
    Game.add(portal);
    greenPortal = portal;
  }

  public static void onGreenCollideEnter(Entity portal, Entity other, Direction dir) {
    Entity hero = Game.hero().get();
    if (other.equals(hero) && bluePortal != null) {
      PositionComponent pc = hero.fetch(PositionComponent.class).get();
      pc.position(bluePortal.fetch(PositionComponent.class).get().position());
    }
    System.out.println("Green Portal entered");
  }

  public static void onBlueCollideEnter(Entity portal, Entity other, Direction dir) {
    Entity hero = Game.hero().get();
    if (other.equals(hero) && greenPortal != null) {
      PositionComponent pc = hero.fetch(PositionComponent.class).get();
      pc.position(greenPortal.fetch(PositionComponent.class).get().position());
    }

    System.out.println("Blue Portal entered");
  }


  public static void clearAllPortals() {
    clearBluePortal();
    clearGreenPortal();
  }

  public static void clearBluePortal() {
    if (bluePortal != null) {
      Game.remove(bluePortal);
      bluePortal = null;
    }
  }

  public static void clearGreenPortal() {
    if (greenPortal != null) {
      Game.remove(greenPortal);
      greenPortal = null;
    }
  }

}
