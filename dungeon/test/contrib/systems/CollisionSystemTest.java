package contrib.systems;

import static org.junit.jupiter.api.Assertions.*;

import contrib.components.CollideComponent;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import testingUtils.SimpleCounter;

/** WTF? . */
public class CollisionSystemTest {

  private static final String DIRECTION_MESSAGE = "The Direction of the Collision should be.";
  private static final String NO_COLLISION_DETECTION_MESSAGE =
      "No Collision between the two hit boxes should be detected.";
  private static final String COLLISION_DETECTED_MESSSAGE =
      "Collision between the two hit boxes should be detected.";
  private static final String MISSING_POSITION_COMPONENT =
      "PositionComponent did get removed Test no longer valid";

  /**
   * Helper to clean up used Class Attributes to avoid interfering with other tests.
   *
   * <p>all Systems add themselves to the Class Attribute SystemController of the Game. To Check the
   * correct processing of the CollisionSystemController the entities are added to the entities
   * list.
   */
  private static void cleanUpEnvironment() {
    Game.removeAllEntities();
    Game.removeAllSystems();
  }

  /** Creating a clean Systemcontroller to avoid interferences. */
  private static void prepareEnvironment() {
    cleanUpEnvironment();
  }

  /**
   * Helper to create an Entity and keep Testcode a bit more clean.
   *
   * @param point1 Position of the newly created Entity
   * @return thr configured Entity
   */
  private static Entity prepareEntityWithPosition(Point point1) {
    Entity e1 = new Entity();
    e1.add(new PositionComponent(point1));

    return e1;
  }

  /** WTF? . */
  @AfterEach
  public void cleanup() {
    Game.removeAllEntities();
    Game.currentLevel(null);
    Game.removeAllSystems();
  }

  /**
   * Check if the Collision is detected when the hit box A is on the left of hit box B.
   *
   * <p>Left means the Position of B is higher on the x-axis.
   */
  @Test
  public void checkForCollisionRight() {
    prepareEnvironment();
    CollisionSystem cs = new CollisionSystem();
    Game.add(cs);
    Vector2 offset = Vector2.of(0, 0);
    Vector2 size = Vector2.of(1, 1);
    Entity e1 = prepareEntityWithPosition(new Point(0, 0));
    TriConsumer<Entity, Entity, Direction> collider = (a, b, c) -> {};
    CollideComponent hb1 =
        new CollideComponent(Vector2.of(offset), Vector2.of(size), collider, collider);

    Entity e2 = prepareEntityWithPosition(new Point(.5f, 0));

    CollideComponent hb2 =
        new CollideComponent(Vector2.of(offset), Vector2.of(size), collider, collider);

    e1.add(hb1);
    e2.add(hb2);
    Game.add(e1);
    Game.add(e2);
    assertTrue(cs.checkForCollision(e1, hb1, e2, hb2));
    assertEquals(Direction.RIGHT, cs.checkDirectionOfCollision(e1, hb1, e2, hb2));
    cleanUpEnvironment();
  }

  /**
   * Check if the Collision is detected when the hit box A is on the left of hit box B and not
   * colliding.
   *
   * <p>Left means the Position of B is higher on the x-axis not colliding means there is no
   * possible intersection between A and B and there is A gap between to avoid float inaccuracy.
   */
  @Test
  public void checkForCollisionRightNoIntersection() {
    prepareEnvironment();
    CollisionSystem cs = new CollisionSystem();
    Game.add(cs);
    Vector2 offset = Vector2.of(0, 0);
    Vector2 size = Vector2.of(1, 1);
    Entity e1 = prepareEntityWithPosition(new Point(0, 0));

    TriConsumer<Entity, Entity, Direction> collider = (a, b, c) -> {};
    CollideComponent hb1 =
        new CollideComponent(Vector2.of(offset), Vector2.of(size), collider, collider);

    Entity e2 = prepareEntityWithPosition(new Point(1.5f, 0));

    CollideComponent hb2 =
        new CollideComponent(Vector2.of(offset), Vector2.of(size), collider, collider);

    e1.add(hb1);
    e2.add(hb2);
    Game.add(e1);
    Game.add(e2);
    assertFalse(cs.checkForCollision(e1, hb1, e2, hb2));
    assertEquals(Direction.RIGHT, cs.checkDirectionOfCollision(e1, hb1, e2, hb2));
    cleanUpEnvironment();
  }

  /**
   * Check if the Collision is detected when the hit box A is on the right of hit box B.
   *
   * <p>Right means the Position of B is lower on the x-axis not colliding means there is no
   * possible intersection between A and B and there is A gap between to avoid float inaccuracy.
   */
  @Test
  public void checkForCollisionLeft() {
    prepareEnvironment();
    CollisionSystem cs = new CollisionSystem();
    Game.add(cs);
    Vector2 offset = Vector2.of(0, 0);
    Vector2 size = Vector2.of(1, 1);
    Entity e1 = prepareEntityWithPosition(new Point(0, 0));
    TriConsumer<Entity, Entity, Direction> collider = (a, b, c) -> {};
    CollideComponent hb1 =
        new CollideComponent(Vector2.of(offset), Vector2.of(size), collider, collider);

    Entity e2 = prepareEntityWithPosition(new Point(-.5f, 0));
    CollideComponent hb2 =
        new CollideComponent(Vector2.of(offset), Vector2.of(size), collider, collider);

    e1.add(hb1);
    e2.add(hb2);
    Game.add(e1);
    Game.add(e2);
    assertTrue(cs.checkForCollision(e1, hb1, e2, hb2));
    assertEquals(Direction.LEFT, cs.checkDirectionOfCollision(e1, hb1, e2, hb2));
    cleanUpEnvironment();
  }

  /**
   * Check if the Collision is detected when the hit box A is on the right of hit box B and not
   * colliding.
   *
   * <p>Right means the Position of B is lower on the x-axis not colliding means there is no
   * possible intersection between A and B and there is A gap between to avoid float inaccuracy.
   */
  @Test
  public void checkForCollisionLeftNoIntersection() {
    prepareEnvironment();
    CollisionSystem cs = new CollisionSystem();
    Game.add(cs);
    Entity e1 = prepareEntityWithPosition(new Point(0, 0));

    Vector2 offset = Vector2.of(0, 0);
    Vector2 size = Vector2.of(1, 1);
    TriConsumer<Entity, Entity, Direction> collider = (a, b, c) -> {};
    CollideComponent hb1 =
        new CollideComponent(Vector2.of(offset), Vector2.of(size), collider, collider);

    Entity e2 = prepareEntityWithPosition(new Point(-1.5f, 0));

    CollideComponent hb2 =
        new CollideComponent(Vector2.of(offset), Vector2.of(size), collider, collider);

    e1.add(hb1);
    e2.add(hb2);
    Game.add(e1);
    Game.add(e2);
    assertFalse(cs.checkForCollision(e1, hb1, e2, hb2));
    assertEquals(Direction.LEFT, cs.checkDirectionOfCollision(e1, hb1, e2, hb2));
    cleanUpEnvironment();
  }

  /**
   * Check if the Collision is detected when the hit box A is above hit box B.
   *
   * <p>above means the Position of B is higher on the y-axis.
   */
  @Test
  public void checkForCollisionBottomWithIntersection() {
    prepareEnvironment();
    CollisionSystem cs = new CollisionSystem();
    Game.add(cs);
    Vector2 offset = Vector2.of(0, 0);
    Vector2 size = Vector2.of(1, 1);
    Entity e1 = prepareEntityWithPosition(new Point(0, 0));
    TriConsumer<Entity, Entity, Direction> collider = (a, b, c) -> {};
    CollideComponent hb1 =
        new CollideComponent(Vector2.of(offset), Vector2.of(size), collider, collider);

    Entity e2 = prepareEntityWithPosition(new Point(0, .5f));
    CollideComponent hb2 =
        new CollideComponent(Vector2.of(offset), Vector2.of(size), collider, collider);

    e1.add(hb1);
    e2.add(hb2);
    Game.add(e1);
    Game.add(e2);
    assertTrue(cs.checkForCollision(e1, hb1, e2, hb2));
    assertEquals(Direction.DOWN, cs.checkDirectionOfCollision(e1, hb1, e2, hb2));
    cleanUpEnvironment();
  }

  /**
   * Check if no Collision is detected when the hit box A is above hit box B and not colliding.
   *
   * <p>Above means the Position of B is higher on the y-axis not colliding means there is no
   * possible intersection between A and B and there is A gap between to avoid float inaccuracy.
   */
  @Test
  public void checkForCollisionBottomWithNoIntersection() {
    prepareEnvironment();
    CollisionSystem cs = new CollisionSystem();
    Game.add(cs);
    Vector2 offset = Vector2.of(0, 0);
    Vector2 size = Vector2.of(1, 1);
    Entity e1 = prepareEntityWithPosition(new Point(0, 0));
    TriConsumer<Entity, Entity, Direction> collider = (a, b, c) -> {};
    CollideComponent hb1 =
        new CollideComponent(Vector2.of(offset), Vector2.of(size), collider, collider);

    Entity e2 = prepareEntityWithPosition(new Point(0, 1.5f));
    CollideComponent hb2 =
        new CollideComponent(Vector2.of(offset), Vector2.of(size), collider, collider);

    e1.add(hb1);
    e2.add(hb2);
    Game.add(e1);
    Game.add(e2);
    assertFalse(cs.checkForCollision(e1, hb1, e2, hb2));
    assertEquals(Direction.DOWN, cs.checkDirectionOfCollision(e1, hb1, e2, hb2));
    cleanUpEnvironment();
  }

  /**
   * Check if the Collision is detected when the hit box A is below hit box B.
   *
   * <p>Below means the Position of B is lower on the y-axis.
   */
  @Test
  public void checkForCollisionTopWithIntersection() {
    prepareEnvironment();
    CollisionSystem cs = new CollisionSystem();
    Game.add(cs);
    Vector2 offset = Vector2.of(0, 0);
    Vector2 size = Vector2.of(1, 1);
    Entity e1 = prepareEntityWithPosition(new Point(0, 0));
    TriConsumer<Entity, Entity, Direction> collider = (a, b, c) -> {};
    CollideComponent hb1 =
        new CollideComponent(Vector2.of(offset), Vector2.of(size), collider, collider);

    Entity e2 = prepareEntityWithPosition(new Point(0, -0.5f));
    CollideComponent hb2 =
        new CollideComponent(Vector2.of(offset), Vector2.of(size), collider, collider);

    e1.add(hb1);
    e2.add(hb2);
    Game.add(e1);
    Game.add(e2);
    assertTrue(cs.checkForCollision(e1, hb1, e2, hb2));
    assertEquals(Direction.UP, cs.checkDirectionOfCollision(e1, hb1, e2, hb2));
    cleanUpEnvironment();
  }

  /**
   * Check if no Collision is detected when the hit box A is below hit box B and not colliding.
   *
   * <p>Below means the Position of B is lower on the y-axis not colliding means there is no
   * possible intersection between A and B and there is A gap between to avoid float inaccuracy.
   */
  @Test
  public void checkForCollisionTopNoIntersection() {
    prepareEnvironment();
    CollisionSystem cs = new CollisionSystem();
    Game.add(cs);
    Vector2 offset = Vector2.of(0, 0);
    Vector2 size = Vector2.of(1, 1);
    Entity e1 = prepareEntityWithPosition(new Point(0, 0));
    TriConsumer<Entity, Entity, Direction> collider = (a, b, c) -> {};
    CollideComponent hb1 =
        new CollideComponent(Vector2.of(offset), Vector2.of(size), collider, collider);

    Entity e2 = prepareEntityWithPosition(new Point(0, -1.5f));
    CollideComponent hb2 =
        new CollideComponent(Vector2.of(offset), Vector2.of(size), collider, collider);

    e1.add(hb1);
    e2.add(hb2);
    Game.add(e1);
    Game.add(e2);
    assertFalse(cs.checkForCollision(e1, hb1, e2, hb2));
    assertEquals(Direction.UP, cs.checkDirectionOfCollision(e1, hb1, e2, hb2));
    cleanUpEnvironment();
  }

  /**
   * Check if the Collision is detected when the hit box A is bigger and every Corner is around hit
   * box B.
   */
  @Test
  public void checkForCollisionBoxAAroundB() {
    prepareEnvironment();
    CollisionSystem cs = new CollisionSystem();
    Game.add(cs);
    Entity e1 = prepareEntityWithPosition(new Point(-.1f, -.1f));
    TriConsumer<Entity, Entity, Direction> collider = (a, b, c) -> {};
    CollideComponent hb1 =
        new CollideComponent(
            Vector2.of(Vector2.of(0, 0)), Vector2.of(Vector2.of(1.2f, 1.2f)), collider, collider);

    Entity e2 = prepareEntityWithPosition(new Point(0, 0f));
    CollideComponent hb2 =
        new CollideComponent(
            Vector2.of(Vector2.of(0, 0)), Vector2.of(Vector2.of(1, 1)), collider, collider);

    e1.add(hb1);
    e2.add(hb2);
    Game.add(e1);
    Game.add(e2);
    assertTrue(cs.checkForCollision(e1, hb1, e2, hb2));
    cleanUpEnvironment();
  }

  /**
   * Check if the Collision is detected when the hit box B is bigger and every Corner is around hit
   * box A.
   */
  @Test
  public void checkForCollisionBoxBAroundA() {
    prepareEnvironment();
    CollisionSystem cs = new CollisionSystem();
    Game.add(cs);
    Entity e1 = prepareEntityWithPosition(new Point(0, 0));
    TriConsumer<Entity, Entity, Direction> collider = (a, b, c) -> {};
    CollideComponent hb1 =
        new CollideComponent(
            Vector2.of(Vector2.of(0, 0)), Vector2.of(Vector2.of(1, 1)), collider, collider);

    Entity e2 = prepareEntityWithPosition(new Point(-.1f, -.1f));
    CollideComponent hb2 =
        new CollideComponent(
            Vector2.of(Vector2.of(0, 0)), Vector2.of(Vector2.of(1.2f, 1.2f)), collider, collider);

    e1.add(hb1);
    e2.add(hb2);
    Game.add(e1);
    Game.add(e2);
    assertTrue(cs.checkForCollision(e1, hb1, e2, hb2));
    cleanUpEnvironment();
  }

  /** Checks if the System is still Working even if there is no Entity. */
  @Test
  public void checkUpdateNoEntities() {
    prepareEnvironment();
    CollisionSystem cs = new CollisionSystem();
    Game.add(cs);
    cs.execute();
    cleanUpEnvironment();
  }

  /** Checks that the System is still working when there is no Entity with A hit box component. */
  @Test
  public void checkUpdateNoEntitiesWithHitboxComponent() {
    prepareEnvironment();
    CollisionSystem cs = new CollisionSystem();
    Game.add(cs);
    prepareEntityWithPosition(new Point(0, 0));
    cs.execute();
    cleanUpEnvironment();
  }

  /**
   * Checks that there is no call off the collider Methods when there is only one hit box entity.
   */
  @Test
  public void checkUpdateOneEntityWithHitboxComponent() {
    prepareEnvironment();
    CollisionSystem cs = new CollisionSystem();
    Game.add(cs);
    Entity e1 = prepareEntityWithPosition(new Point(0, 0));
    SimpleCounter sc1OnEnter = new SimpleCounter();
    SimpleCounter sc1OnLeave = new SimpleCounter();
    e1.add(
        new CollideComponent(
            Vector2.of(0, 0),
            Vector2.of(1, 1),
            (a, b, c) -> sc1OnEnter.inc(),
            (a, b, c) -> sc1OnLeave.inc()));
    cs.execute();
    assertEquals(0, sc1OnEnter.getCount());
    assertEquals(0, sc1OnLeave.getCount());
    cleanUpEnvironment();
  }

  /** Checks that there is no call off the collider Methods when there is no Collision. */
  @Test
  public void checkUpdateTwoEntitiesWithHitboxComponentNonColliding() {
    prepareEnvironment();
    CollisionSystem cs = new CollisionSystem();
    Game.add(cs);
    Entity e1 = prepareEntityWithPosition(new Point(0, 0));
    SimpleCounter sc1OnEnter = new SimpleCounter();
    SimpleCounter sc1OnLeave = new SimpleCounter();
    e1.add(
        new CollideComponent(
            Vector2.of(0, 0),
            Vector2.of(1, 1),
            (a, b, c) -> sc1OnEnter.inc(),
            (a, b, c) -> sc1OnLeave.inc()));
    Entity e2 = prepareEntityWithPosition(new Point(1, 1));
    SimpleCounter sc2OnEnter = new SimpleCounter();
    SimpleCounter sc2OnLeave = new SimpleCounter();
    e2.add(
        new CollideComponent(
            Vector2.of(0, 0),
            Vector2.of(1, 1),
            (a, b, c) -> sc2OnEnter.inc(),
            (a, b, c) -> sc2OnLeave.inc()));
    cs.execute();
    assertEquals(0, sc1OnEnter.getCount());
    assertEquals(0, sc1OnLeave.getCount());
    assertEquals(0, sc2OnEnter.getCount());
    assertEquals(0, sc2OnLeave.getCount());

    cleanUpEnvironment();
  }

  /*
   * Checks the call of the onEnterCollider when the Collision started happening.
   *
   * <p>The collision between A and B was happening in between CollisionSystem#update calls.
   *
   * <p>Since we cant update the {@link Game#entities} from outside the game loop, this is testcase
   * cant be tested.
   */
  /* @Test
  public void checkUpdateTwoEntitiesWithHitboxComponentColliding() {
      prepareEnvironment();
      CollisionSystem cs = new CollisionSystem();
      Entity e1 = prepareEntityWithPosition(new Point(0, 0));
      SimpleCounter sc1OnEnter = new SimpleCounter();
      SimpleCounter sc1OnLeave = new SimpleCounter();
      new CollideComponent(
              e1,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc1OnEnter.inc(),
              (a, b, c) -> sc1OnLeave.inc());
      Entity e2 = prepareEntityWithPosition(new Point(0, 0));
      SimpleCounter sc2OnEnter = new SimpleCounter();
      SimpleCounter sc2OnLeave = new SimpleCounter();
      new CollideComponent(
              e2,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc2OnEnter.inc(),
              (a, b, c) -> sc2OnLeave.inc());
      Entity e3 = prepareEntityWithPosition(new Point(1, 2));
      SimpleCounter sc3OnEnter = new SimpleCounter();
      SimpleCounter sc3OnLeave = new SimpleCounter();
      new CollideComponent(
              e3,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc3OnEnter.inc(),
              (a, b, c) -> sc3OnLeave.inc());

      cs.showEntity(e1);
      cs.showEntity(e2);
      cs.showEntity(e3);

      cs.execute();
      assertEquals("Only one interaction begins for e1", 1, sc1OnEnter.getCount());
      assertEquals("No interaction ends for e1", 0, sc1OnLeave.getCount());
      assertEquals("Only one interaction begins for e2", 1, sc2OnEnter.getCount());
      assertEquals("No interaction ends for e2", 0, sc2OnLeave.getCount());
      assertEquals("No interaction begins for e3", 0, sc3OnEnter.getCount());
      assertEquals("No interaction ends for e3", 0, sc3OnLeave.getCount());
      cleanUpEnvironment();
  }*/

  /*
   * Checks the call of the onEnterCollider when the Collision started happening only being called
   * once.
   *
   * <p>The collision between A and B was happening in between CollisionSystem#update calls.
   *
   * <p>Since we cant update the {@link Game#entities} from outside the game loop, this is testcase
   * cant be tested.
   */

  /*
  @Test
  public void checkUpdateTwoEntitiesWithHitboxComponentCollidingOnlyOnce() {
      prepareEnvironment();
      CollisionSystem cs = new CollisionSystem();
      Entity e1 = prepareEntityWithPosition(new Point(0, 0));
      SimpleCounter sc1OnEnter = new SimpleCounter();
      SimpleCounter sc1OnLeave = new SimpleCounter();
      new CollideComponent(
              e1,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc1OnEnter.inc(),
              (a, b, c) -> sc1OnLeave.inc());
      Entity e2 = prepareEntityWithPosition(new Point(0, 0));
      SimpleCounter sc2OnEnter = new SimpleCounter();
      SimpleCounter sc2OnLeave = new SimpleCounter();
      new CollideComponent(
              e2,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc2OnEnter.inc(),
              (a, b, c) -> sc2OnLeave.inc());
      Entity e3 = prepareEntityWithPosition(new Point(1, 2));
      SimpleCounter sc3OnEnter = new SimpleCounter();
      SimpleCounter sc3OnLeave = new SimpleCounter();
      new CollideComponent(
              e3,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc3OnEnter.inc(),
              (a, b, c) -> sc3OnLeave.inc());

      cs.execute();
      cs.execute();
      assertEquals("Only one interaction begins for e1", 1, sc1OnEnter.getCount());
      assertEquals("No interaction ends for e1", 0, sc1OnLeave.getCount());
      assertEquals("Only one interaction begins for e2", 1, sc2OnEnter.getCount());
      assertEquals("No interaction ends for e2", 0, sc2OnLeave.getCount());
      assertEquals("No interaction begins for e3", 0, sc3OnEnter.getCount());
      assertEquals("No interaction ends for e3", 0, sc3OnLeave.getCount());
      cleanUpEnvironment();
  }*/

  /*
   * Checks the call of the onLeaveCollider when the Collision is no longer happening.
   *
   * <p>The collision between A and B was brocken up in between CollisionSystem#update calls.
   *
   * <p>Since we cant update the {@link Game#entities} from outside the gameloop, this is testcase
   * cant be tested.
   */
  /*
  @Test
  public void checkUpdateTwoEntitiesWithHitboxComponentNoLongerColliding() {
      prepareEnvironment();
      CollisionSystem cs = new CollisionSystem();
      Entity e1 = prepareEntityWithPosition(new Point(0, 0));
      SimpleCounter sc1OnEnter = new SimpleCounter();
      SimpleCounter sc1OnLeave = new SimpleCounter();
      new CollideComponent(
              e1,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc1OnEnter.inc(),
              (a, b, c) -> sc1OnLeave.inc());
      Entity e2 = prepareEntityWithPosition(new Point(0, 0));
      SimpleCounter sc2OnEnter = new SimpleCounter();
      SimpleCounter sc2OnLeave = new SimpleCounter();
      new CollideComponent(
              e2,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc2OnEnter.inc(),
              (a, b, c) -> sc2OnLeave.inc());
      Entity e3 = prepareEntityWithPosition(new Point(1, 2));
      SimpleCounter sc3OnEnter = new SimpleCounter();
      SimpleCounter sc3OnLeave = new SimpleCounter();
      new CollideComponent(
              e3,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc3OnEnter.inc(),
              (a, b, c) -> sc3OnLeave.inc());
      cs.showEntity(e1);
      cs.showEntity(e2);
      cs.showEntity(e3);

      cs.execute();
      e1.getComponent(PositionComponent.class)
              .map(PositionComponent.class::cast)
              .ifPresent(x -> x.getPosition().x += 2);
      cs.execute();
      assertEquals("Only one interaction begins for e1", 1, sc1OnEnter.getCount());
      assertEquals("One interaction ends for e1", 1, sc1OnLeave.getCount());
      assertEquals("Only one interaction begins for e2", 1, sc2OnEnter.getCount());
      assertEquals("One interaction ends for e2", 1, sc2OnLeave.getCount());
      assertEquals("No interaction begins for e3", 0, sc3OnEnter.getCount());
      assertEquals("No interaction ends for e3", 0, sc3OnLeave.getCount());
      cleanUpEnvironment();
  }*/

  /*
   * Checks the call of the onLeaveCollider when the Collision is no longer happening only .
   *
   * <p>The collision between A and B was brocken up in between CollisionSystem#update calls.
   *
   * <p>Since we cant update the {@link Game#entities} from outside the game loop, this is testcase
   * cant be tested.
   */
  /*@Test
  public void checkUpdateTwoEntitiesWithHitboxComponentNoLongerCollidingOnlyOnce() {
      prepareEnvironment();
      CollisionSystem cs = new CollisionSystem();
      Entity e1 = prepareEntityWithPosition(new Point(0, 0));
      SimpleCounter sc1OnEnter = new SimpleCounter();
      SimpleCounter sc1OnLeave = new SimpleCounter();
      new CollideComponent(
              e1,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc1OnEnter.inc(),
              (a, b, c) -> sc1OnLeave.inc());
      Entity e2 = prepareEntityWithPosition(new Point(0, 0));
      SimpleCounter sc2OnEnter = new SimpleCounter();
      SimpleCounter sc2OnLeave = new SimpleCounter();
      new CollideComponent(
              e2,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc2OnEnter.inc(),
              (a, b, c) -> sc2OnLeave.inc());
      Entity e3 = prepareEntityWithPosition(new Point(1, 2));
      SimpleCounter sc3OnEnter = new SimpleCounter();
      SimpleCounter sc3OnLeave = new SimpleCounter();
      new CollideComponent(
              e3,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc3OnEnter.inc(),
              (a, b, c) -> sc3OnLeave.inc());

      cs.showEntity(e1);
      cs.showEntity(e2);
      cs.showEntity(e3);
      cs.execute();
      e1.getComponent(PositionComponent.class)
              .map(PositionComponent.class::cast)
              .ifPresentOrElse(
                      x -> x.getPosition().x += 2,
                      () -> fail("PositionComponent not available and test not valid "));
      cs.execute();
      cs.execute();
      assertEquals("Only one interaction begins for e1", 1, sc1OnEnter.getCount());
      assertEquals("Only one interaction ends for e1", 1, sc1OnLeave.getCount());
      assertEquals("Only one interaction begins for e2", 1, sc2OnEnter.getCount());
      assertEquals("Only one interaction ends for e2", 1, sc2OnLeave.getCount());
      assertEquals("No interaction begins for  e3", 0, sc3OnEnter.getCount());
      assertEquals("No interaction ends for e3", 0, sc3OnLeave.getCount());
      cleanUpEnvironment();
  }*/

  /*
   * Checks if an Entity can collide Multiple Times.
   *
   * <p>E1 collides with e1 and e3 while e2 and e3 do not.
   *
   * <p>Since we cant update the {@link Game#entities} from outside the game loop, this is testcase
   * cant be tested.
   */
  /*@Test
  public void checkUpdateCollisionNotBlockingOtherCollisions() {
      prepareEnvironment();
      CollisionSystem cs = new CollisionSystem();
      Entity e1 = prepareEntityWithPosition(new Point(0, 0));
      SimpleCounter sc1OnEnter = new SimpleCounter();
      SimpleCounter sc1OnLeave = new SimpleCounter();
      new CollideComponent(
              e1,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc1OnEnter.inc(),
              (a, b, c) -> sc1OnLeave.inc());
      Entity e2 = prepareEntityWithPosition(new Point(.7f, 0));
      SimpleCounter sc2OnEnter = new SimpleCounter();
      SimpleCounter sc2OnLeave = new SimpleCounter();
      new CollideComponent(
              e2,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc2OnEnter.inc(),
              (a, b, c) -> sc2OnLeave.inc());
      Entity e3 = prepareEntityWithPosition(new Point(-.7f, 0));
      SimpleCounter sc3OnEnter = new SimpleCounter();
      SimpleCounter sc3OnLeave = new SimpleCounter();
      new CollideComponent(
              e3,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc3OnEnter.inc(),
              (a, b, c) -> sc3OnLeave.inc());

      cs.showEntity(e1);
      cs.showEntity(e2);
      cs.showEntity(e3);
      cs.execute();
      cs.execute();
      assertEquals("Two interactions begin for e1", 2, sc1OnEnter.getCount());
      assertEquals("No interaction ends for e1", 0, sc1OnLeave.getCount());
      assertEquals("Only one interaction begins for e2", 1, sc2OnEnter.getCount());
      assertEquals("No interaction ends for e2", 0, sc2OnLeave.getCount());
      assertEquals("Only one interaction begins for e3", 1, sc3OnEnter.getCount());
      assertEquals("No interaction ends for e3", 0, sc3OnLeave.getCount());
      cleanUpEnvironment();
  }

  /*
   * Checks if an Entity can stop colliding with one Entity.
   *
   * <p>On first update e1 collides with e1 and e3 while e2 and e3 do not on the second update e1
   * stops colliding with e3.

       // <p> Since we cant update the {@link Game#entities} from outside the game loop, this is testcase cant be tested.</p>

   */

  /*@Test
  public void checkUpdateCollisionNotCallingEveryOnLeaveCollider() {
      prepareEnvironment();
      CollisionSystem cs = new CollisionSystem();
      Entity e1 = prepareEntityWithPosition(new Point(0, 0));
      SimpleCounter sc1OnEnter = new SimpleCounter();
      SimpleCounter sc1OnLeave = new SimpleCounter();
      new CollideComponent(
              e1,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc1OnEnter.inc(),
              (a, b, c) -> sc1OnLeave.inc());
      Entity e2 = prepareEntityWithPosition(new Point(.7f, 0));
      SimpleCounter sc2OnEnter = new SimpleCounter();
      SimpleCounter sc2OnLeave = new SimpleCounter();
      new CollideComponent(
              e2,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc2OnEnter.inc(),
              (a, b, c) -> sc2OnLeave.inc());
      Entity e3 = prepareEntityWithPosition(new Point(-.7f, 0));
      SimpleCounter sc3OnEnter = new SimpleCounter();
      SimpleCounter sc3OnLeave = new SimpleCounter();
      new CollideComponent(
              e3,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc3OnEnter.inc(),
              (a, b, c) -> sc3OnLeave.inc());

      cs.showEntity(e1);
      cs.showEntity(e2);
      cs.showEntity(e3);
      cs.execute();
      e1.getComponent(PositionComponent.class)
              .map(PositionComponent.class::cast)
              .ifPresentOrElse(
                      x -> x.getPosition().x += 1, () -> fail(MISSING_POSITION_COMPONENT));
      cs.execute();
      assertEquals("Two interactions begin for e1", 2, sc1OnEnter.getCount());
      assertEquals("No interaction ends for e1", 1, sc1OnLeave.getCount());
      assertEquals("Only one interaction begins for e2", 1, sc2OnEnter.getCount());
      assertEquals("No interaction ends for e2", 0, sc2OnLeave.getCount());
      assertEquals("Only one interaction begins for e3", 1, sc3OnEnter.getCount());
      assertEquals("One interaction ends for e3", 1, sc3OnLeave.getCount());
      cleanUpEnvironment();
  }*/

  /*
   * Checks if all Entity can stop colliding with each other.
   *
   * <p>Since we cant update the {@link Game#entities} from outside the game loop, this is testcase
   * cant be tested.
   */
  /*@Test
  public void checkUpdateCollisionCallingEveryOnLeaveCollider() {
      prepareEnvironment();
      CollisionSystem cs = new CollisionSystem();
      Entity e1 = prepareEntityWithPosition(new Point(0, 0));
      SimpleCounter sc1OnEnter = new SimpleCounter();
      SimpleCounter sc1OnLeave = new SimpleCounter();
      new CollideComponent(
              e1,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc1OnEnter.inc(),
              (a, b, c) -> sc1OnLeave.inc());
      Entity e2 = prepareEntityWithPosition(new Point(.7f, 0));
      SimpleCounter sc2OnEnter = new SimpleCounter();
      SimpleCounter sc2OnLeave = new SimpleCounter();
      new CollideComponent(
              e2,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc2OnEnter.inc(),
              (a, b, c) -> sc2OnLeave.inc());
      Entity e3 = prepareEntityWithPosition(new Point(-.7f, 0));
      SimpleCounter sc3OnEnter = new SimpleCounter();
      SimpleCounter sc3OnLeave = new SimpleCounter();
      new CollideComponent(
              e3,
              new Point(0, 0),
              new Point(1, 1),
              (a, b, c) -> sc3OnEnter.inc(),
              (a, b, c) -> sc3OnLeave.inc());

      cs.showEntity(e1);
      cs.showEntity(e2);
      cs.showEntity(e3);
      cs.execute();
      e1.getComponent(PositionComponent.class)
              .map(PositionComponent.class::cast)
              .ifPresentOrElse(
                      x -> x.getPosition().y += 2, () -> fail(MISSING_POSITION_COMPONENT));

      cs.execute();
      assertEquals("Two interactions begin for e1", 2, sc1OnEnter.getCount());
      assertEquals("Two interactions end for e1", 2, sc1OnLeave.getCount());
      assertEquals("Only one interaction begins for e2", 1, sc2OnEnter.getCount());
      assertEquals("One interaction ends for e2", 1, sc2OnLeave.getCount());
      assertEquals("Only one interaction begins for e3", 1, sc3OnEnter.getCount());
      assertEquals("One interaction ends for e3", 1, sc3OnLeave.getCount());
      cleanUpEnvironment();
  }*/
}
