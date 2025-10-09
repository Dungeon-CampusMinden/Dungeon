package contrib.components;

import static org.junit.jupiter.api.Assertions.*;

import contrib.systems.CollisionSystem;
import core.Entity;
import core.Game;
import core.utils.Direction;
import core.utils.Vector2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testingUtils.SimpleCounter;

/** Collision component tests. */
public class CollisionComponentTest {

  private static final double DELTA = 0.0001;

  private CollisionSystem collisionSystem;
  private Entity e1, e2;
  private CollideComponent hb1, hb2;

  /** Setup before each test. */
  @BeforeEach
  public void setUp() {
    collisionSystem = new CollisionSystem();
    Game.add(collisionSystem);

    e1 = new Entity();
    hb1 = new CollideComponent(Vector2.ZERO, Vector2.ONE, null, null);
    e1.add(hb1);
    Game.add(e1);

    e2 = new Entity();
    hb2 = new CollideComponent(Vector2.ZERO, Vector2.ONE, null, null);
    e2.add(hb2);
    Game.add(e2);
  }

  /** Teardown after each test. */
  @AfterEach
  public void tearDown() {
    Game.removeAllEntities();
    Game.removeAllSystems();
  }

  /** On enter no method given. */
  @Test
  public void onEnterNoMethod() {
    hb1.onEnter(e1, e2, Direction.UP);
  }

  /** On enter method given. */
  @Test
  public void onEnterCheckCall() {
    SimpleCounter counterE1Enter = new SimpleCounter();
    SimpleCounter counterE1Leave = new SimpleCounter();
    SimpleCounter counterE2Enter = new SimpleCounter();
    SimpleCounter counterE2Leave = new SimpleCounter();
    hb1.collideEnter((a, b, c) -> counterE1Enter.inc());
    hb1.collideLeave((a, b, c) -> counterE1Leave.inc());
    hb2.collideEnter((a, b, c) -> counterE2Enter.inc());
    hb2.collideLeave((a, b, c) -> counterE2Leave.inc());

    hb1.onEnter(e1, e2, Direction.UP);

    assertEquals(1, counterE1Enter.getCount());
    assertEquals(0, counterE1Leave.getCount());
    assertEquals(0, counterE2Enter.getCount());
    assertEquals(0, counterE2Leave.getCount());
  }

  /** On leave no method given. */
  @Test
  public void onLeaveNoMethod() {
    hb1.onLeave(e1, e2, Direction.UP);
  }

  /** On leave method given. */
  @Test
  public void onLeaveCheckCall() {
    SimpleCounter counterE1Enter = new SimpleCounter();
    SimpleCounter counterE1Leave = new SimpleCounter();
    SimpleCounter counterE2Enter = new SimpleCounter();
    SimpleCounter counterE2Leave = new SimpleCounter();
    hb1.collideEnter((a, b, c) -> counterE1Enter.inc());
    hb1.collideLeave((a, b, c) -> counterE1Leave.inc());
    hb2.collideEnter((a, b, c) -> counterE2Enter.inc());
    hb2.collideLeave((a, b, c) -> counterE2Leave.inc());

    hb1.onLeave(e1, e2, Direction.UP);

    assertEquals(0, counterE1Enter.getCount());
    assertEquals(1, counterE1Leave.getCount());
    assertEquals(0, counterE2Enter.getCount());
    assertEquals(0, counterE2Leave.getCount());
  }

  /** WTF? . */
  @Test
  public void setiCollideEnterNull() {
    SimpleCounter counterE1Enter = new SimpleCounter();
    hb1.collideEnter((a, b, c) -> counterE1Enter.inc());
    hb1.collideEnter(null);
    hb1.onEnter(e1, e2, Direction.UP);
    assertEquals(0, counterE1Enter.getCount());
  }

  /** WTF? . */
  @Test
  public void setiCollideEnterValidCollider() {
    SimpleCounter counterE1Enter = new SimpleCounter();
    SimpleCounter newCounterE1Enter = new SimpleCounter();
    hb1.collideEnter((a, b, c) -> counterE1Enter.inc());
    hb1.collideEnter((a, b, c) -> newCounterE1Enter.inc());
    hb1.onEnter(e1, e2, Direction.UP);
    assertEquals(0, counterE1Enter.getCount());
    assertEquals(1, newCounterE1Enter.getCount());
  }

  /** WTF? . */
  @Test
  public void setiCollideLeaveNull() {
    Entity e1 = new Entity();
    SimpleCounter counterE1Enter = new SimpleCounter();
    CollideComponent hb1 = new CollideComponent(null, (a, b, c) -> counterE1Enter.inc());
    Entity e2 = new Entity();
    CollideComponent hb2 = new CollideComponent(Vector2.ZERO, Vector2.ONE, null, null);
    e1.add(hb1);
    e2.add(hb2);
    hb1.collideLeave(null);
    hb1.onLeave(e1, e2, Direction.UP);
    assertEquals(0, counterE1Enter.getCount());
  }

  /** WTF? . */
  @Test
  public void setiCollideLeaveValidCollider() {
    SimpleCounter counterE1Leave = new SimpleCounter();
    SimpleCounter newCounterE1Leave = new SimpleCounter();
    hb1.collideLeave((a, b, c) -> counterE1Leave.inc());
    hb1.collideLeave((a, b, c) -> newCounterE1Leave.inc());
    hb1.onLeave(e1, e2, Direction.UP);
    assertEquals(0, counterE1Leave.getCount());
    assertEquals(1, newCounterE1Leave.getCount());
  }
}
