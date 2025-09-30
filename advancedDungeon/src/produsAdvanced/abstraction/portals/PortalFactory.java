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
import contrib.components.SkillComponent;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.projectileSkill.ProjectileSkill;
<<<<<<< HEAD
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
=======
>>>>>>> 36f09545 (apply spotless)
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
<<<<<<< HEAD
import produsAdvanced.abstraction.portals.components.PortalComponent;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> d483f6ff (added direction to portals)
=======
>>>>>>> 878b072b (added direction to portals)
>>>>>>> ac8cf0c7 (restructed portal related files)

=======
=======
import java.util.*;
import java.util.stream.Collectors;
import produsAdvanced.abstraction.portals.components.PortalComponent;

>>>>>>> 36f09545 (apply spotless)
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
>>>>>>> fdb15be0 (added comments and refactored the code to make it more readable)
public class PortalFactory {

<<<<<<< HEAD
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
=======
  private static Entity bluePortal = null;
  private static Entity greenPortal = null;
>>>>>>> f8ecc099 (fixed pÃrojectiles and rotation)

  private static Direction bluePortalDirection;
  private static Direction greenPortalDirection;

<<<<<<< HEAD
<<<<<<< HEAD
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
=======
=======
  /**
   * Creates a portal of the given color at the specified position.
   *
   * <p>If a portal of the same color already exists, it will be replaced. If the new portal
   * overlaps with the other portal, the other portal will be removed.
   *
   * @param point the position where the portal should be placed
   * @param color the portal color, see {@link PortalColor}
   */
>>>>>>> fdb15be0 (added comments and refactored the code to make it more readable)
  public static void createPortal(Point point, PortalColor color) {
    Entity portal = preparePortal(point, color);

    portal.add(new DrawComponent(new SimpleIPath(getPortalPath(color))));

>>>>>>> c71f73d8 (decluttered portal create method)
    PositionComponent pc = new PositionComponent(point);
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
=======
    Direction dir = setPortalDirection(point, color);
    pc.viewDirection(dir);
>>>>>>> c71f73d8 (decluttered portal create method)

    CollideComponent cc = setCollideComponent(dir, getCollideHandler(color));
    cc.isSolid(false);

    portal.add(pc);
    portal.add(cc);

    assignPortalReference(color, portal);

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

  /**
   * Removes the given portal if it exists at the specified position.
   *
   * @param portal the portal entity to check
   * @param point the position to check against
   * @param clearAction the action to execute if overlap is detected
   */
  private static void removeIfOverlap(Entity portal, Point point, Runnable clearAction) {
    if (portal != null && portal.fetch(PositionComponent.class).get().position().equals(point)) {
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
      case BLUE -> "portals/blue_portal";
      case GREEN -> "portals/green_portal";
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
   * Assigns the corresponding portal reference.
   *
   * @param color the portal color
   * @param portal the portal entity
   */
  private static void assignPortalReference(PortalColor color, Entity portal) {
    if (color == PortalColor.BLUE) {
      bluePortal = portal;
    } else {
      greenPortal = portal;
    }
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

<<<<<<< HEAD

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
    Direction dir = toDirection(pointDirection);
=======
    Point nearestTile = list.getFirst().a();
    Point pointDirection = new Point(point.x() - nearestTile.x(), point.y() - nearestTile.y());
    Direction direction = toDirection(pointDirection).opposite();
>>>>>>> fdb15be0 (added comments and refactored the code to make it more readable)

    if (color == PortalColor.GREEN) {
      greenPortalDirection = direction;
    } else {
      bluePortalDirection = direction;
    }

<<<<<<< HEAD
<<<<<<< HEAD
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
<<<<<<< HEAD
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
=======
    PositionComponent pc = new PositionComponent(point);
>>>>>>> 8dba5349 (commit for help)
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
      greenPortalDirection = Direction.UP;
    } else if (pointDirection.equals(new Point(0,-1))) {
      greenPortalDirection = Direction.DOWN;
    } else if (pointDirection.equals(new Point(1,0))) {
      greenPortalDirection = Direction.RIGHT;
    } else {
      greenPortalDirection = Direction.LEFT;
    }
<<<<<<< HEAD
<<<<<<< HEAD

<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> 5d963fb8 (fixed portal creating bug and added directions to the portals to smoothen the transition)
=======
    portal.add(cc);
>>>>>>> dfc687cf (added direction hitboxes for the portals)
    portal.add(new DrawComponent(new SimpleIPath("portals/green_portal")));
    Game.add(portal);
=======
=======
    cc.isSolid(false);
>>>>>>> deaec5ba (fixed random teleport bug + adjusted for new collidesystem)
=======
    pc.viewDirection(greenPortalDirection);
    cc = setCollideComponent(greenPortalDirection, PortalFactory::onGreenCollideEnter);
    cc.isSolid(false);
    portal.add(pc);
>>>>>>> 8dba5349 (commit for help)
    portal.add(cc);
    portal.add(new DrawComponent(new SimpleIPath("portals/green_portal")));
    Game.add(portal);
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> ac8cf0c7 (restructed portal related files)
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
=======
    return dir;
=======
    return direction;
>>>>>>> fdb15be0 (added comments and refactored the code to make it more readable)
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
>>>>>>> c71f73d8 (decluttered portal create method)
  }

  /**
   * Teleports an entity that collides with the green portal to the corresponding blue portal.
   *
   * @param portal the green portal entity
   * @param other the entity entering the portal
   * @param dir the direction of collision
   */
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
      pc.position(
          bluePortal
              .fetch(PositionComponent.class)
              .get()
              .position()
              .translate(bluePortalDirection.scale(1.2)));
      handleProjectiles(other, greenPortalDirection, bluePortalDirection);
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

  /**
   * Teleports an entity that collides with the blue portal to the corresponding green portal.
   *
   * @param portal the blue portal entity
   * @param other the entity entering the portal
   * @param dir the direction of collision
   */
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
      pc.position(
          greenPortal
              .fetch(PositionComponent.class)
              .get()
              .position()
              .translate(greenPortalDirection.scale(1.2)));
      handleProjectiles(other, bluePortalDirection, greenPortalDirection);
    }
>>>>>>> 20f3a7f9 (restructed portal related files):advancedDungeon/src/produsAdvanced/abstraction/portals/PortalFactory.java
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
            CollideComponent.DEFAULT_COLLIDER);
      }
      case UP -> {
        return new CollideComponent(
            Vector2.of(offsetX, offsetY),
            Vector2.of(hitboxX, hitboxY),
            onCollideEnter,
            CollideComponent.DEFAULT_COLLIDER);
      }
      case LEFT -> {
        return new CollideComponent(
            Vector2.of(offsetX, offsetX),
            Vector2.of(hitboxY, hitboxX),
            onCollideEnter,
            CollideComponent.DEFAULT_COLLIDER);
      }
      case RIGHT -> {
        return new CollideComponent(
            Vector2.of(offsetY, offsetX),
            Vector2.of(hitboxY, hitboxX),
            onCollideEnter,
            CollideComponent.DEFAULT_COLLIDER);
      }
      default -> {
        return new CollideComponent();
      }
    }
  }

  /** Removes both the blue and green portals from the game, if present. */
  public static void clearAllPortals() {
    clearBluePortal();
    clearGreenPortal();
  }

  /** Removes the blue portal from the game, if present. */
  public static void clearBluePortal() {
<<<<<<< HEAD
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> d4f46e36 (implemented singleton pattern behaviour for portals)
    if (bluePortal != null) {
<<<<<<< HEAD
      System.out.println("Blue Portal removed");
<<<<<<< HEAD
=======
>>>>>>> bd651d9e (implemented singleton pattern behaviour for portals)
    if (bluePortal != null) {
>>>>>>> ac8cf0c7 (restructed portal related files)
=======
>>>>>>> efe893f0 (added PortalComponent to avoid unwanted portal on portal interactions)
=======
>>>>>>> fdb15be0 (added comments and refactored the code to make it more readable)
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

  /** Removes the green portal from the game, if present. */
  public static void clearGreenPortal() {
    if (greenPortal != null) {
      Game.remove(greenPortal);
      greenPortal = null;
    }
<<<<<<< HEAD
>>>>>>> d4f46e36 (implemented singleton pattern behaviour for portals)
  }

  /**
   * Checks if the given entity is a portal.
   *
   * @param entity the entity to check
   * @return true if the entity is a portal, false otherwise
   */
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

  /**
   * Returns the blue portal, if it exists.
   *
   * @return an {@link Optional} containing the blue portal entity, or empty if none exists
   */
  public static Optional<Entity> getBluePortal() {
    return Optional.ofNullable(bluePortal);
  }

  /**
   * Returns the green portal, if it exists.
   *
   * @return an {@link Optional} containing the green portal entity, or empty if none exists
   */
  public static Optional<Entity> getGreenPortal() {
    return Optional.ofNullable(greenPortal);
  }
}
