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
import org.junit.jupiter.api.BeforeEach;
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

  private CollisionSystem cs;

  /**
   * Helper to clean up used Class Attributes to avoid interfering with other tests.
   *
   * <p>all Systems add themselves to the Class Attribute SystemController of the Game. To Check the
   * correct processing of the CollisionSystemController the entities are added to the entities
   * list.
   */
  private static void cleanUpEnvironment() {
    Game.removeAllEntities();
    Game.currentLevel(null);
    Game.removeAllSystems();
  }

  /** Creating a clean Systemcontroller to avoid interferences. */
  @BeforeEach
  public void prepareEnvironment() {
    cs = new CollisionSystem();
    Game.add(cs);
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
    cleanUpEnvironment();
  }

  /**
   * Check if the Collision is detected when the hit box A is on the left of hit box B.
   *
   * <p>Left means the Position of B is higher on the x-axis.
   */
  @Test
  public void checkForCollisionRight() {
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
  public void checkForCollisionTopWithIntersection() {
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
    assertEquals(Direction.UP, cs.checkDirectionOfCollision(e1, hb1, e2, hb2));
    cleanUpEnvironment();
  }

  /**
   * Check if no Collision is detected when the hit box A is above hit box B and not colliding.
   *
   * <p>Above means the Position of B is higher on the y-axis not colliding means there is no
   * possible intersection between A and B and there is A gap between to avoid float inaccuracy.
   */
  @Test
  public void checkForCollisionTopWithNoIntersection() {
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
    assertEquals(Direction.UP, cs.checkDirectionOfCollision(e1, hb1, e2, hb2));
    cleanUpEnvironment();
  }

  /**
   * Check if the Collision is detected when the hit box A is below hit box B.
   *
   * <p>Below means the Position of B is lower on the y-axis.
   */
  @Test
  public void checkForCollisionBottomWithIntersection() {
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
    assertEquals(Direction.DOWN, cs.checkDirectionOfCollision(e1, hb1, e2, hb2));
    cleanUpEnvironment();
  }

  /**
   * Check if no Collision is detected when the hit box A is below hit box B and not colliding.
   *
   * <p>Below means the Position of B is lower on the y-axis not colliding means there is no
   * possible intersection between A and B and there is A gap between to avoid float inaccuracy.
   */
  @Test
  public void checkForCollisionBottomNoIntersection() {
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
    assertEquals(Direction.DOWN, cs.checkDirectionOfCollision(e1, hb1, e2, hb2));
    cleanUpEnvironment();
  }

  /**
   * Check if the Collision is detected when the hit box A is bigger and every Corner is around hit
   * box B.
   */
  @Test
  public void checkForCollisionBoxAAroundB() {
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
    cs.execute();
    cleanUpEnvironment();
  }

  /** Checks that the System is still working when there is no Entity with A hit box component. */
  @Test
  public void checkUpdateNoEntitiesWithHitboxComponent() {
    prepareEntityWithPosition(new Point(0, 0));
    cs.execute();
    cleanUpEnvironment();
  }

  /**
   * Checks that there is no call off the collider Methods when there is only one hit box entity.
   */
  @Test
  public void checkUpdateOneEntityWithHitboxComponent() {
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

  //  /**
  //   * Checks if two solid entities collide then the collision will be resolved by moving one of
  // them out.
  //   * This case currently always fails, because we dont instantiate a level, so any
  // CollisionUtils.isCollidingWithLevel will always return true.
  //   */
  //  @Test
  //  public void checkSolidCollideWithMass(){
  //    Entity e1 = prepareEntityWithPosition(new Point(0, 0));
  //    e1.add(new CollideComponent(Vector2.ZERO, Vector2.ONE).isSolid(true));
  //    VelocityComponent vc1 = new VelocityComponent(5f);
  //    vc1.mass(5f);
  //    e1.add(vc1);
  //    Game.add(e1);
  //
  //    Entity e2 = prepareEntityWithPosition(new Point(0.9f, 0.0f));
  //    e2.add(new CollideComponent(Vector2.ZERO, Vector2.ONE).isSolid(true));
  //    VelocityComponent vc2 = new VelocityComponent(5f);
  //    e2.add(vc2);
  //    vc2.mass(1f);
  //    Game.add(e2);
  //
  //    cs.execute();
  //
  //    // e2 should be moved to the right
  //    PositionComponent pc1 = e1.fetch(PositionComponent.class).orElseThrow();
  //    PositionComponent pc2 = e2.fetch(PositionComponent.class).orElseThrow();
  //
  //    assertEquals(new Point(0, 0), pc1.position());
  //
  //    // e2 should be at exactly 1.0 + CollisionSystem.COLLIDE_SET_DISTANCE. beware floating point
  // inaccuracies
  //    assertTrue(Math.abs(pc2.position().x() - (1.0f + CollisionSystem.COLLIDE_SET_DISTANCE)) <
  // 0.0001f, "Actual x pos: "+pc2.position().x());
  //    assertEquals(0.0f, pc2.position().y());
  //  }
}
