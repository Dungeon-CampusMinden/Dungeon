package produsAdvanced.abstraction;

import contrib.components.CollideComponent;
import contrib.entities.EntityFactory;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import core.utils.components.path.SimpleIPath;

import java.io.IOException;

public class Portal {

  private static Entity bluePortal;
  private static Entity greenPortal;


  public static void createBluePortal(Point point) {
    clearBluePortal();
    Entity portal;
    portal = new Entity("blue_portal");
    portal.add(new PositionComponent(point));
    CollideComponent cc = new CollideComponent(CollideComponent.DEFAULT_OFFSET, Vector2.of(1.05,1.05), Portal::onBlueCollideEnter, CollideComponent.DEFAULT_COLLIDER);
    portal.add(cc);

    try {
      portal.add(new DrawComponent(new SimpleIPath("portals/blue")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.add(portal);
    bluePortal = portal;
  }

  public static void createGreenPortal(Point point) {
    clearGreenPortal();
    Entity portal;
    portal = new Entity("green_portal");
    portal.add(new PositionComponent(point));
    CollideComponent cc = new CollideComponent(CollideComponent.DEFAULT_OFFSET, Vector2.of(1.05,1.05), Portal::onGreenCollideEnter, CollideComponent.DEFAULT_COLLIDER);
    portal.add(cc);

    try {
      portal.add(new DrawComponent(new SimpleIPath("portals/green")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.add(portal);
    greenPortal = portal;
  }

  public static void onGreenCollideEnter(Entity portal, Entity other, Direction dir) {
    System.out.println("Green Portal entered");
  }

  public static void onBlueCollideEnter(Entity portal, Entity other, Direction dir) {
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
