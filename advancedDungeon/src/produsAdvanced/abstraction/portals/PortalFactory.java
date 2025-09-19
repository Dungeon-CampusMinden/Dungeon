package produsAdvanced.abstraction.portals;

import contrib.components.CollideComponent;
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD:advancedDungeon/src/produsAdvanced/abstraction/Portal.java
=======
<<<<<<<< HEAD:advancedDungeon/src/produsAdvanced/abstraction/Portal.java
>>>>>>> ac8cf0c7 (restructed portal related files)
import contrib.entities.EntityFactory;
<<<<<<< HEAD
<<<<<<< HEAD
import contrib.entities.MiscFactory;
=======
<<<<<<< HEAD
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
import contrib.entities.MiscFactory;
>>>>>>> d483f6ff (added direction to portals)
=======
>>>>>>> 20f3a7f9 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/PortalFactory.java
=======
import contrib.components.ProjectileComponent;
import core.Component;
>>>>>>> a9350c91 (added projectile handling)
=======
>>>>>>> ef71cb29 (added green and blue portal variants)
=======
import contrib.entities.MiscFactory;
>>>>>>> 878b072b (added direction to portals)
========
>>>>>>>> ac8cf0c7 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/PortalFactory.java
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
import contrib.components.ProjectileComponent;
import core.Component;
>>>>>>> 25d26bca (added projectile handling)
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
import core.components.VelocityComponent;
>>>>>>> a9350c91 (added projectile handling)
=======
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
import core.components.VelocityComponent;
>>>>>>> 25d26bca (added projectile handling)
=======
>>>>>>> 3c53d7e1 (moved PÃortalComponent out of portalSkills)
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.*;
import core.utils.components.path.SimpleIPath;
<<<<<<< HEAD
import produsAdvanced.abstraction.portals.portalSkills.BluePortalSkill;
import produsAdvanced.abstraction.portals.portalSkills.GreenPortalSkill;
<<<<<<< HEAD
<<<<<<< HEAD
import produsAdvanced.abstraction.portals.portalSkills.PortalComponent;
=======
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
import produsAdvanced.abstraction.portals.portalSkills.PortalComponent;
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
=======
>>>>>>> 3c53d7e1 (moved PÃortalComponent out of portalSkills)

import java.util.*;
import java.util.stream.Collectors;
=======
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import core.utils.components.path.SimpleIPath;

import java.io.IOException;
<<<<<<< HEAD
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
>>>>>>> ef71cb29 (added green and blue portal variants)
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
=======
import core.components.VelocityComponent;
>>>>>>> 24b937b6 (implemented a basic projectile teleportation)
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.*;
import core.utils.components.path.SimpleIPath;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
<<<<<<< HEAD
>>>>>>> d483f6ff (added direction to portals)
=======
>>>>>>> 878b072b (added direction to portals)
>>>>>>> ac8cf0c7 (restructed portal related files)

public class PortalFactory {

  private static Entity bluePortal;
  private static Entity greenPortal;
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
  private static Entity stone;
=======
>>>>>>> cefa46bc (added PortalComponent to avoid unwanted portal on portal interactions)
=======
  private static Entity stone;
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)

  private static Direction bluePortalDirection;
  private static Direction greenPortalDirection;

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> ac8cf0c7 (restructed portal related files)
  public static void createBluePortal(Point point) {
    clearBluePortal();
=======
=======
  private static Entity stone;
<<<<<<< HEAD
>>>>>>> d483f6ff (added direction to portals)
=======
>>>>>>> 878b072b (added direction to portals)
>>>>>>> ac8cf0c7 (restructed portal related files)

  private static Point bluePortalDirection;
  private static Point greenPortalDirection;

<<<<<<< HEAD
  public static void createBluePortal(Point point) {
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
=======
  public static void createBluePortal(Point point, Vector2 currentVelocity, Point projectilePosition) {
>>>>>>> d483f6ff (added direction to portals)
    clearBluePortal();
>>>>>>> d4f46e36 (implemented singleton pattern behaviour for portals)
=======
  public static void createBluePortal(Point point, Vector2 currentVelocity) {
>>>>>>> cefa46bc (added PortalComponent to avoid unwanted portal on portal interactions)
    Entity portal;
    clearBluePortal();
    if (greenPortal != null && greenPortal.fetch(PositionComponent.class).get().position().equals(point)) {
      clearGreenPortal();
    }
    portal = new Entity("blue_portal");
    portal.add(new PositionComponent(point));
    portal.add(new PortalComponent());
=======
>>>>>>> ef71cb29 (added green and blue portal variants)
=======
=======
  public static void createBluePortal(Point point, Vector2 currentVelocity, Point projectilePosition) {
>>>>>>> 878b072b (added direction to portals)
    clearBluePortal();
>>>>>>> bd651d9e (implemented singleton pattern behaviour for portals)
=======
  public static void createBluePortal(Point point, Vector2 currentVelocity) {
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
    Entity portal;
    clearBluePortal();
    if (greenPortal != null && greenPortal.fetch(PositionComponent.class).get().position().equals(point)) {
      clearGreenPortal();
    }
    portal = new Entity("blue_portal");
    portal.add(new PositionComponent(point));
<<<<<<< HEAD
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
    portal.add(new PortalComponent());
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> d483f6ff (added direction to portals)
=======
>>>>>>> 878b072b (added direction to portals)
>>>>>>> ac8cf0c7 (restructed portal related files)
    // checking all Neighbours, for each compare the currentVelocity with the impact direction, put them into a liste
    // get the best score of the list(?), if its a wall go to next best one, the resulting neighbour the the direction where
=======
    // checking all Neighbours, for each compare the currentVelocity with the impact direction, put them into a list
    // get the best score of the list(?), if its a wall go to next best one, the resulting neighbour is the  direction where
<<<<<<< HEAD
>>>>>>> 5d963fb8 (fixed portal creating bug and added directions to the portals to smoothen the transition)
=======
>>>>>>> 355d8064 (fixed portal creating bug and added directions to the portals to smoothen the transition)
>>>>>>> ac8cf0c7 (restructed portal related files)
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
<<<<<<< HEAD
    System.out.println(new Point(point.x()-best.x(), point.y()-best.y()));
<<<<<<< HEAD
<<<<<<< HEAD
=======
<<<<<<< HEAD
=======
>>>>>>> ef71cb29 (added green and blue portal variants)
=======
>>>>>>> 878b072b (added direction to portals)
    try {
      portal.add(new DrawComponent(new SimpleIPath("portals/blue")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
=======
=======
>>>>>>> ac8cf0c7 (restructed portal related files)
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

<<<<<<< HEAD
>>>>>>> 5d963fb8 (fixed portal creating bug and added directions to the portals to smoothen the transition)
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
=======
    portal.add(new DrawComponent(new SimpleIPath("portals/blue_portal")));
>>>>>>> 077375b3 (updated all portal related assets to .json formats and moved them into advancedDungeon)
=======
>>>>>>> 355d8064 (fixed portal creating bug and added directions to the portals to smoothen the transition)
    portal.add(new DrawComponent(new SimpleIPath("portals/blue_portal")));
>>>>>>> ec4153d0 (updated all portal related assets to .json formats and moved them into advancedDungeon)
>>>>>>> ac8cf0c7 (restructed portal related files)
    Game.add(portal);
    bluePortal = portal;
  }

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> d483f6ff (added direction to portals)
  public static void createGreenPortal(Point point, Vector2 currentVelocity, Point projectilePosition) {
=======
  public static void createGreenPortal(Point point) {
>>>>>>> 5d963fb8 (fixed portal creating bug and added directions to the portals to smoothen the transition)
=======
=======
>>>>>>> 878b072b (added direction to portals)
  public static void createGreenPortal(Point point, Vector2 currentVelocity, Point projectilePosition) {
=======
  public static void createGreenPortal(Point point) {
>>>>>>> 355d8064 (fixed portal creating bug and added directions to the portals to smoothen the transition)
>>>>>>> ac8cf0c7 (restructed portal related files)
    clearGreenPortal();
=======
  public static void createGreenPortal(Point point) {
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
    clearGreenPortal();
>>>>>>> d4f46e36 (implemented singleton pattern behaviour for portals)
=======
  public static void createGreenPortal(Point point, Vector2 currentVelocity) {
>>>>>>> cefa46bc (added PortalComponent to avoid unwanted portal on portal interactions)
    Entity portal;
    clearGreenPortal();
    if (bluePortal != null &&  bluePortal.fetch(PositionComponent.class).get().position().equals(point)) {
      clearBluePortal();
    }
    portal = new Entity("green_portal");
    portal.add(new PositionComponent(point));
<<<<<<< HEAD

<<<<<<< HEAD:advancedDungeon/src/produsAdvanced/abstraction/Portal.java

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 077375b3 (updated all portal related assets to .json formats and moved them into advancedDungeon)
=======
=======
>>>>>>> 20f3a7f9 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/PortalFactory.java
=======
    portal.add(new PortalComponent());
>>>>>>> cefa46bc (added PortalComponent to avoid unwanted portal on portal interactions)
=======
>>>>>>> ef71cb29 (added green and blue portal variants)
=======
    clearGreenPortal();
>>>>>>> bd651d9e (implemented singleton pattern behaviour for portals)
=======
  public static void createGreenPortal(Point point, Vector2 currentVelocity) {
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
    Entity portal;
    clearGreenPortal();
    if (bluePortal != null &&  bluePortal.fetch(PositionComponent.class).get().position().equals(point)) {
      clearBluePortal();
    }
    portal = new Entity("green_portal");
    portal.add(new PositionComponent(point));
<<<<<<< HEAD

>>>>>>> ac8cf0c7 (restructed portal related files)
=======
    portal.add(new PortalComponent());
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
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

<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> 5d963fb8 (fixed portal creating bug and added directions to the portals to smoothen the transition)
=======
    portal.add(cc);
>>>>>>> dfc687cf (added direction hitboxes for the portals)
    portal.add(new DrawComponent(new SimpleIPath("portals/green_portal")));
    Game.add(portal);
=======
    portal.add(cc);
    portal.add(new DrawComponent(new SimpleIPath("portals/green_portal")));
    Game.add(portal);
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> ac8cf0c7 (restructed portal related files)
    greenPortal = portal;
  }

  public static void onGreenCollideEnter(Entity portal, Entity other, Direction dir) {
    if (bluePortal != null && !isEntityPortal(other)) {
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
//      System.out.println("Used Green Portal - Teleported " + other.name() + " to " + bluePortal.fetch(PositionComponent.class).get().position().translate(greenPortalDirection));
=======
>>>>>>> 24b937b6 (implemented a basic projectile teleportation)
      PositionComponent pc = other.fetch(PositionComponent.class).get();
      pc.position(bluePortal.fetch(PositionComponent.class).get().position().translate(bluePortalDirection.opposite()));
      handleProjectiles(other, bluePortalDirection.opposite());
    }
<<<<<<< HEAD:advancedDungeon/src/produsAdvanced/abstraction/Portal.java
<<<<<<< HEAD
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
=======
//    System.out.println("Green Portal entered");
>>>>>>> dfc687cf (added direction hitboxes for the portals)
=======
=======
      System.out.println("Used Green Portal - Teleported " + other.name() + " to " + bluePortal.fetch(PositionComponent.class).get().position().translate(greenPortalDirection));
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
=======
//      System.out.println("Used Green Portal - Teleported " + other.name() + " to " + bluePortal.fetch(PositionComponent.class).get().position().translate(greenPortalDirection));
>>>>>>> 25d26bca (added projectile handling)
      PositionComponent pc = other.fetch(PositionComponent.class).get();
      pc.position(bluePortal.fetch(PositionComponent.class).get().position().translate(bluePortalDirection.opposite()));
      handleProjectiles(other);
    }
<<<<<<<< HEAD:advancedDungeon/src/produsAdvanced/abstraction/Portal.java
<<<<<<< HEAD
<<<<<<< HEAD
=======
    bluePortal = portal;
=======
    greenPortal = portal;
>>>>>>> bd651d9e (implemented singleton pattern behaviour for portals)
  }

  public static void onGreenCollideEnter(Entity portal, Entity other, Direction dir) {
>>>>>>> ef71cb29 (added green and blue portal variants)
=======
>>>>>>> 878b072b (added direction to portals)
    System.out.println("Green Portal entered");
=======
//    System.out.println("Green Portal entered");
>>>>>>> 18a2e62d (added direction hitboxes for the portals)
>>>>>>> ac8cf0c7 (restructed portal related files)
  }

  public static void onBlueCollideEnter(Entity portal, Entity other, Direction dir) {
<<<<<<< HEAD
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> d483f6ff (added direction to portals)
=======
>>>>>>> 878b072b (added direction to portals)
>>>>>>> ac8cf0c7 (restructed portal related files)
    Entity hero = Game.hero().get();
    if (other.equals(hero) && greenPortal != null) {
      PositionComponent pc = hero.fetch(PositionComponent.class).get();
      pc.position(greenPortal.fetch(PositionComponent.class).get().position().translate(bluePortalDirection));
    }

<<<<<<< HEAD
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
>>>>>>> d483f6ff (added direction to portals)
    System.out.println("Blue Portal entered");
=======
//    System.out.println("Blue Portal entered");
>>>>>>> dfc687cf (added direction hitboxes for the portals)
=======
=======
>>>>>>> ef71cb29 (added green and blue portal variants)
=======
>>>>>>> 878b072b (added direction to portals)
    System.out.println("Blue Portal entered");
=======
//    System.out.println("Blue Portal entered");
>>>>>>> 18a2e62d (added direction hitboxes for the portals)
========
>>>>>>> ac8cf0c7 (restructed portal related files)
  }

  public static void onBlueCollideEnter(Entity portal, Entity other, Direction dir) {
    if (greenPortal != null && !isEntityPortal(other)) {
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
//      System.out.println("Used Blue Portal - Teleported " + other.name() + " to " + greenPortal.fetch(PositionComponent.class).get().position().translate(bluePortalDirection));
=======
>>>>>>> 24b937b6 (implemented a basic projectile teleportation)
      PositionComponent pc = other.fetch(PositionComponent.class).get();
      pc.position(greenPortal.fetch(PositionComponent.class).get().position().translate(greenPortalDirection.opposite()));
      handleProjectiles(other, greenPortalDirection.opposite());
    }
>>>>>>> 20f3a7f9 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/PortalFactory.java
  }

  public static void handleProjectiles(Entity projectile, Direction direction) {
    if (!projectile.isPresent(ProjectileComponent.class)) {
      return;
    }
    Entity entity = new Entity();
    for (Component component : projectile.componentStream().toList()) {
      entity.add(component);
    }
    Game.remove(projectile);

    VelocityComponent vc = projectile.fetch(VelocityComponent.class).get();
    if (direction == bluePortalDirection.opposite()) {
      System.out.println("blue");
      vc.currentVelocity(rotateVelocityThroughPortals(vc.currentVelocity(), bluePortalDirection, greenPortalDirection));
    } else {
      System.out.println("green");
      vc.currentVelocity(rotateVelocityThroughPortals(vc.currentVelocity(), greenPortalDirection, bluePortalDirection));
    }
    PositionComponent pc = projectile.fetch(PositionComponent.class).get();
    pc.rotation((float) direction.angleDeg());
    Game.add(entity);
  }
=======
=======
      System.out.println("Used Blue Portal - Teleported " + other.name() + " to " + greenPortal.fetch(PositionComponent.class).get().position().translate(bluePortalDirection));
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
=======
//      System.out.println("Used Blue Portal - Teleported " + other.name() + " to " + greenPortal.fetch(PositionComponent.class).get().position().translate(bluePortalDirection));
>>>>>>> 25d26bca (added projectile handling)
      PositionComponent pc = other.fetch(PositionComponent.class).get();
      pc.position(greenPortal.fetch(PositionComponent.class).get().position().translate(greenPortalDirection.opposite()));
      handleProjectiles(other);
    }
>>>>>>>> ac8cf0c7 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/PortalFactory.java
  }

<<<<<<< HEAD
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
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
>>>>>>> 25d26bca (added projectile handling)

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
<<<<<<< HEAD
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> d4f46e36 (implemented singleton pattern behaviour for portals)
    if (bluePortal != null) {
      System.out.println("Blue Portal removed");
<<<<<<< HEAD
=======
>>>>>>> bd651d9e (implemented singleton pattern behaviour for portals)
    if (bluePortal != null) {
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
      Game.remove(bluePortal);
      bluePortal = null;
    }
<<<<<<< HEAD
  }

  public static void clearGreenPortal() {
    if (greenPortal != null) {
<<<<<<< HEAD
      System.out.println("Green Portal removed");
=======
>>>>>>> ac8cf0c7 (restructed portal related files)
      Game.remove(greenPortal);
      greenPortal = null;
    }
=======
    bluePortal = null;
  }

  public static void clearGreenPortal() {
    greenPortal = null;
<<<<<<< HEAD
>>>>>>> 36adc3c1 (added green and blue portal variants)
=======
>>>>>>> ef71cb29 (added green and blue portal variants)
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
  }

  public static void clearGreenPortal() {
    if (greenPortal != null) {
      System.out.println("Green Portal removed");
      Game.remove(greenPortal);
      greenPortal = null;
    }
<<<<<<< HEAD
>>>>>>> d4f46e36 (implemented singleton pattern behaviour for portals)
  }

  private static boolean isEntityPortal(Entity entity) {
    return entity.isPresent(PortalComponent.class);
=======
>>>>>>> bd651d9e (implemented singleton pattern behaviour for portals)
  }

  private static boolean isEntityPortal(Entity entity) {
<<<<<<< HEAD
    if (Objects.equals(entity.name(), BluePortalSkill.SKILL_NAME + "_projectile") || Objects.equals(entity.name(), GreenPortalSkill.SKILL_NAME + "_projectile")) {
      System.out.println("NOT ALLOWED");
      Game.remove(entity);
      return true;
    }
    return false;
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
    return entity.isPresent(PortalComponent.class);
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
  }

}
