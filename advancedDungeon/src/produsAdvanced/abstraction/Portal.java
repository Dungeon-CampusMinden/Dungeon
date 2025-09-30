package produsAdvanced.abstraction;

import contrib.components.CollideComponent;
import contrib.entities.EntityFactory;
<<<<<<< HEAD
<<<<<<< HEAD
import contrib.entities.MiscFactory;
=======
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
import contrib.entities.MiscFactory;
>>>>>>> d483f6ff (added direction to portals)
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
<<<<<<< HEAD
<<<<<<< HEAD
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.*;
import core.utils.components.path.SimpleIPath;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
=======
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import core.utils.components.path.SimpleIPath;

import java.io.IOException;
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.*;
import core.utils.components.path.SimpleIPath;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
>>>>>>> d483f6ff (added direction to portals)

public class Portal {

  private static Entity bluePortal;
  private static Entity greenPortal;
<<<<<<< HEAD
<<<<<<< HEAD
  private static Entity stone;

  private static Point bluePortalDirection;
  private static Point greenPortalDirection;

  public static void createBluePortal(Point point, Vector2 currentVelocity, Point projectilePosition) {
    clearBluePortal();
=======
=======
  private static Entity stone;
>>>>>>> d483f6ff (added direction to portals)

  private static Point bluePortalDirection;
  private static Point greenPortalDirection;

<<<<<<< HEAD
  public static void createBluePortal(Point point) {
<<<<<<< HEAD
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
=======
  public static void createBluePortal(Point point, Vector2 currentVelocity, Point projectilePosition) {
>>>>>>> d483f6ff (added direction to portals)
    clearBluePortal();
>>>>>>> d4f46e36 (implemented singleton pattern behaviour for portals)
    Entity portal;
    portal = new Entity("blue_portal");
    portal.add(new PositionComponent(point));
    CollideComponent cc = new CollideComponent(CollideComponent.DEFAULT_OFFSET, Vector2.of(1.05,1.05), Portal::onBlueCollideEnter, CollideComponent.DEFAULT_COLLIDER);
    portal.add(cc);

<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> d483f6ff (added direction to portals)
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
<<<<<<< HEAD
    portal.add(new DrawComponent(new SimpleIPath("portals/blue_portal")));
=======
=======
>>>>>>> d483f6ff (added direction to portals)
    try {
      portal.add(new DrawComponent(new SimpleIPath("portals/blue")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
>>>>>>> 36adc3c1 (added green and blue portal variants)
    Game.add(portal);
    bluePortal = portal;
  }

<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> d483f6ff (added direction to portals)
  public static void createGreenPortal(Point point, Vector2 currentVelocity, Point projectilePosition) {
    clearGreenPortal();
=======
  public static void createGreenPortal(Point point) {
<<<<<<< HEAD
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
    clearGreenPortal();
>>>>>>> d4f46e36 (implemented singleton pattern behaviour for portals)
    Entity portal;
    portal = new Entity("green_portal");
    portal.add(new PositionComponent(point));
    CollideComponent cc = new CollideComponent(CollideComponent.DEFAULT_OFFSET, Vector2.of(1.05,1.05), Portal::onGreenCollideEnter, CollideComponent.DEFAULT_COLLIDER);
    portal.add(cc);

<<<<<<< HEAD
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
=======
    try {
      portal.add(new DrawComponent(new SimpleIPath("portals/green")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.add(portal);
    greenPortal = portal;
  }

  public static void onGreenCollideEnter(Entity portal, Entity other, Direction dir) {
<<<<<<< HEAD
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
    Entity hero = Game.hero().get();
    if (other.equals(hero) && bluePortal != null) {
      PositionComponent pc = hero.fetch(PositionComponent.class).get();
      pc.position(bluePortal.fetch(PositionComponent.class).get().position());
    }
>>>>>>> d483f6ff (added direction to portals)
    System.out.println("Green Portal entered");
  }

  public static void onBlueCollideEnter(Entity portal, Entity other, Direction dir) {
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> d483f6ff (added direction to portals)
    Entity hero = Game.hero().get();
    if (other.equals(hero) && greenPortal != null) {
      PositionComponent pc = hero.fetch(PositionComponent.class).get();
      pc.position(greenPortal.fetch(PositionComponent.class).get().position());
    }

<<<<<<< HEAD
=======
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
>>>>>>> d483f6ff (added direction to portals)
    System.out.println("Blue Portal entered");
  }


  public static void clearAllPortals() {
    clearBluePortal();
    clearGreenPortal();
  }

  public static void clearBluePortal() {
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> d4f46e36 (implemented singleton pattern behaviour for portals)
    if (bluePortal != null) {
      Game.remove(bluePortal);
      bluePortal = null;
    }
<<<<<<< HEAD
  }

  public static void clearGreenPortal() {
    if (greenPortal != null) {
      Game.remove(greenPortal);
      greenPortal = null;
    }
=======
    bluePortal = null;
  }

  public static void clearGreenPortal() {
    greenPortal = null;
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
  }

  public static void clearGreenPortal() {
    if (greenPortal != null) {
      Game.remove(greenPortal);
      greenPortal = null;
    }
>>>>>>> d4f46e36 (implemented singleton pattern behaviour for portals)
  }

}
