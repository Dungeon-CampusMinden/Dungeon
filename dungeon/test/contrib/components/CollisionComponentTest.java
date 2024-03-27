package contrib.components;

import static org.junit.Assert.*;

import core.Entity;
import core.components.PositionComponent;
import core.level.Tile;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.MissingComponentException;
import org.junit.Test;
import testingUtils.SimpleCounter;

/** Collision component tests. */
public class CollisionComponentTest {

  private static final double DELTA = 0.0001;

  /** On enter no method given. */
  @Test
  public void onEnterNoMethod() {
    Entity e1 = new Entity();
    CollideComponent hb1 = new CollideComponent(null, null);
    e1.add(hb1);
    Entity e2 = new Entity();
    CollideComponent hb2 = new CollideComponent(null, null);
    e2.add(hb2);
    hb1.onEnter(e1, e2, Tile.Direction.N);
  }

  /** On enter method given. */
  @Test
  public void onEnterCheckCall() {
    Entity e1 = new Entity();
    SimpleCounter counterE1Enter = new SimpleCounter();
    SimpleCounter counterE1Leave = new SimpleCounter();
    SimpleCounter counterE2Enter = new SimpleCounter();
    SimpleCounter counterE2Leave = new SimpleCounter();
    CollideComponent hb1 =
        new CollideComponent((a, b, c) -> counterE1Enter.inc(), (a, b, c) -> counterE1Leave.inc());
    Entity e2 = new Entity();
    CollideComponent hb2 =
        new CollideComponent((a, b, c) -> counterE2Enter.inc(), (a, b, c) -> counterE2Leave.inc());

    e1.add(hb1);
    e2.add(hb2);
    hb1.onEnter(e1, e2, Tile.Direction.N);

    assertEquals("Der Counter von Entität 1 Enter soll ", 1, counterE1Enter.getCount());
    assertEquals("Der Counter von Entität 1 Leave soll ", 0, counterE1Leave.getCount());
    assertEquals("Der Counter von Entität 2 Enter soll ", 0, counterE2Enter.getCount());
    assertEquals("Der Counter von Entität 2 Leave soll ", 0, counterE2Leave.getCount());
  }

  /** On leave no method given. */
  @Test
  public void onLeaveNoMethod() {
    Entity e1 = new Entity();
    CollideComponent hb1 = new CollideComponent(null, null);
    Entity e2 = new Entity();
    CollideComponent hb2 = new CollideComponent(null, null);

    e1.add(hb1);
    e2.add(hb2);
    hb1.onLeave(e1, e2, Tile.Direction.N);
  }

  /** On leave method given. */
  @Test
  public void onLeaveCheckCall() {
    Entity e1 = new Entity();
    SimpleCounter counterE1Enter = new SimpleCounter();
    SimpleCounter counterE1Leave = new SimpleCounter();
    SimpleCounter counterE2Enter = new SimpleCounter();
    SimpleCounter counterE2Leave = new SimpleCounter();
    CollideComponent hb1 =
        new CollideComponent((a, b, c) -> counterE1Enter.inc(), (a, b, c) -> counterE1Leave.inc());
    Entity e2 = new Entity();
    CollideComponent hb2 =
        new CollideComponent((a, b, c) -> counterE2Enter.inc(), (a, b, c) -> counterE2Leave.inc());

    e1.add(hb1);
    e2.add(hb2);
    hb1.onLeave(e1, e2, Tile.Direction.N);
    assertEquals("Der Counter von Entität 1 Enter soll ", 0, counterE1Enter.getCount());
    assertEquals("Der Counter von Entität 1 Leave soll ", 1, counterE1Leave.getCount());
    assertEquals("Der Counter von Entität 2 Enter soll ", 0, counterE2Enter.getCount());
    assertEquals("Der Counter von Entität 2 Leave soll ", 0, counterE2Leave.getCount());
  }

  /** WTF? . */
  @Test
  public void setiCollideEnterNull() {
    Entity e1 = new Entity();
    SimpleCounter counterE1Enter = new SimpleCounter();
    CollideComponent hb1 = new CollideComponent((a, b, c) -> counterE1Enter.inc(), null);
    Entity e2 = new Entity();
    CollideComponent hb2 = new CollideComponent(null, null);
    e1.add(hb1);
    e2.add(hb2);
    hb1.collideEnter(null);
    hb1.onEnter(e1, e2, Tile.Direction.N);
    assertEquals(
        "Die alte Collide darf nicht mehr aufgerufen werden ", 0, counterE1Enter.getCount());
  }

  /** WTF? . */
  @Test
  public void setiCollideEnterValidCollider() {
    Entity e1 = new Entity();
    SimpleCounter counterE1Enter = new SimpleCounter();
    SimpleCounter newCounterE1Enter = new SimpleCounter();
    CollideComponent hb1 = new CollideComponent((a, b, c) -> counterE1Enter.inc(), null);
    Entity e2 = new Entity();
    CollideComponent hb2 = new CollideComponent(null, null);
    e1.add(hb1);
    e2.add(hb2);
    hb1.collideEnter((a, b, c) -> newCounterE1Enter.inc());
    hb1.onEnter(e1, e2, Tile.Direction.N);
    assertEquals(
        "Die alte Collide darf nicht mehr aufgerufen werden ", 0, counterE1Enter.getCount());
    assertEquals("Die neue Collide muss aufgerufen werden ", 1, newCounterE1Enter.getCount());
  }

  /** WTF? . */
  @Test
  public void setiCollideLeaveNull() {
    Entity e1 = new Entity();
    SimpleCounter counterE1Enter = new SimpleCounter();
    CollideComponent hb1 = new CollideComponent(null, (a, b, c) -> counterE1Enter.inc());
    Entity e2 = new Entity();
    CollideComponent hb2 = new CollideComponent(null, null);
    e1.add(hb1);
    e2.add(hb2);
    hb1.collideLeave(null);
    hb1.onLeave(e1, e2, Tile.Direction.N);
    assertEquals(
        "Die alte Collide darf nicht mehr aufgerufen werden ", 0, counterE1Enter.getCount());
  }

  /** WTF? . */
  @Test
  public void setiCollideLeaveValidCollider() {
    Entity e1 = new Entity();
    SimpleCounter counterE1Leave = new SimpleCounter();
    SimpleCounter newCounterE1Leave = new SimpleCounter();
    CollideComponent hb1 = new CollideComponent(null, (a, b, c) -> counterE1Leave.inc());
    Entity e2 = new Entity();
    CollideComponent hb2 = new CollideComponent(null, null);
    e1.add(hb1);
    e2.add(hb2);
    hb1.collideLeave((a, b, c) -> newCounterE1Leave.inc());
    hb1.onLeave(e1, e2, Tile.Direction.N);
    assertEquals(
        "Die alte Collide darf nicht mehr aufgerufen werden ", 0, counterE1Leave.getCount());
    assertEquals("Die neue Collide muss aufgerufen werden ", 1, newCounterE1Leave.getCount());
  }

  /** Missing PositionComponent. */
  @Test
  public void getCenterMissingPositionComponent() {
    Entity e = new Entity();
    CollideComponent hb =
        new CollideComponent(new Point(0, 0), new Point(0, 0), (a, b, c) -> {}, (a, b, c) -> {});
    e.add(hb);
    MissingComponentException missingComponentException =
        assertThrows(MissingComponentException.class, () -> hb.center(e));
    assertTrue(missingComponentException.getMessage().contains(PositionComponent.class.getName()));
  }

  /*
     interesting Values could be
     position(0, 0), offset(0, 0),  size( 1, 1), result(.5f, .5f),
     position(1, 1), offset(0, 0), size(1, 1), result(1.5f, 1.5f),
     position(-1, -1), offset(0, 0), size(1, 1), result(-.5f, -.5f),
     position(.5f, .5f), offset(0, 0), size(1, 1), result(1, 1),
     position(.5f, .5f), offset(-1, -1), size(1, 1), result(0, 0)
     position(-.5, .5), offset(0, 0), size(2,2), result(.5, 1.5)
  */

  /** Check Center for position(0, 0), offset(0, 0), size( 1, 1), result(.5f, .5f). */
  @Test
  public void getCenterFirst() {
    Entity e = new Entity();
    Point position = new Point(0, 0);
    Point offset = new Point(0, 0);
    Point size = new Point(1, 1);
    TriConsumer<Entity, Entity, Tile.Direction> iCollide = (a, b, c) -> {};
    e.add(new PositionComponent(position));
    CollideComponent hb = new CollideComponent(offset, size, iCollide, iCollide);
    e.add(hb);

    Point center = hb.center(e);
    assertEquals(0.5f, center.x, DELTA);
    assertEquals(0.5f, center.y, DELTA);
  }

  /** Check Center for position(1, 1), offset(0, 0), size(1, 1), result(1.5f, 1.5f). */
  @Test
  public void getCenterSecond() {
    Entity e = new Entity();
    Point position = new Point(1, 1);
    Point offset = new Point(0, 0);
    Point size = new Point(1, 1);
    TriConsumer<Entity, Entity, Tile.Direction> iCollide = (a, b, c) -> {};
    e.add(new PositionComponent(position));
    CollideComponent hb = new CollideComponent(offset, size, iCollide, iCollide);
    e.add(hb);

    Point center = hb.center(e);
    assertEquals(1.5f, center.x, DELTA);
    assertEquals(1.5f, center.y, DELTA);
  }

  /** Check Center for position(-1, -1), offset(0, 0), size(1, 1), result(-.5f, -.5f). */
  @Test
  public void getCenterThird() {
    Entity e = new Entity();
    Point position = new Point(-1, -1);
    Point offset = new Point(0, 0);
    Point size = new Point(1, 1);
    TriConsumer<Entity, Entity, Tile.Direction> iCollide = (a, b, c) -> {};
    e.add(new PositionComponent(position));
    CollideComponent hb = new CollideComponent(offset, size, iCollide, iCollide);
    e.add(hb);
    Point center = hb.center(e);

    assertEquals(-.5f, center.x, DELTA);
    assertEquals(-.5f, center.y, DELTA);
  }

  /** Check Center for position(.5f, .5f), offset(0, 0), size(1, 1), result(1, 1). */
  @Test
  public void getCenterFourth() {
    Entity e = new Entity();
    Point position = new Point(.5f, .5f);
    Point offset = new Point(0, 0);
    Point size = new Point(1, 1);
    TriConsumer<Entity, Entity, Tile.Direction> iCollide = (a, b, c) -> {};
    e.add(new PositionComponent(position));
    CollideComponent hb = new CollideComponent(offset, size, iCollide, iCollide);
    e.add(hb);
    Point center = hb.center(e);

    assertEquals(1, center.x, DELTA);
    assertEquals(1, center.y, DELTA);
  }

  /** Check Center for position(.5f, .5f), offset(-1, -1), size(1, 1), result(0, 0). */
  @Test
  public void getCenterFifth() {
    Entity e = new Entity();
    Point position = new Point(.5f, .5f);
    Point offset = new Point(-1, -1);
    Point size = new Point(1, 1);
    TriConsumer<Entity, Entity, Tile.Direction> iCollide = (a, b, c) -> {};
    e.add(new PositionComponent(position));
    CollideComponent hb = new CollideComponent(offset, size, iCollide, iCollide);
    e.add(hb);
    Point center = hb.center(e);

    assertEquals(0, center.x, DELTA);
    assertEquals(0, center.y, DELTA);
  }

  /** Check Center for position(-.5, .5), offset(0, 0), size(2,2), result(.5, 1.5). */
  @Test
  public void getCenterSixth() {
    Entity e = new Entity();
    Point position = new Point(-.5f, .5f);
    Point offset = new Point(0, 0);
    Point size = new Point(2, 2);
    TriConsumer<Entity, Entity, Tile.Direction> iCollide = (a, b, c) -> {};
    e.add(new PositionComponent(position));
    CollideComponent hb = new CollideComponent(offset, size, iCollide, iCollide);
    e.add(hb);
    Point center = hb.center(e);

    assertEquals(.5, center.x, DELTA);
    assertEquals(1.5, center.y, DELTA);
  }

  /** Missing Position Component when calling getBottomLeft should throw an exception. */
  @Test
  public void getTopRightMissingPosition() {
    Entity e = new Entity();
    CollideComponent hb = new CollideComponent();
    e.add(hb);
    MissingComponentException missingComponentException =
        assertThrows(MissingComponentException.class, () -> hb.topRight(e));
    assertTrue(missingComponentException.getMessage().contains(PositionComponent.class.getName()));
  }

  /** Position and offset stay in origin (0,0). */
  @Test
  public void getTopRightOrigin() {
    Entity e = new Entity();
    Point position = new Point(0, 0);
    Point offset = new Point(0, 0);
    Point size = new Point(1, 1);
    TriConsumer<Entity, Entity, Tile.Direction> iCollide = (a, b, c) -> {};
    e.add(new PositionComponent(position));
    CollideComponent hb = new CollideComponent(offset, size, iCollide, iCollide);
    e.add(hb);
    Point center = hb.topRight(e);

    assertEquals(1, center.x, DELTA);
    assertEquals(1, center.y, DELTA);
  }

  /** Position and offset stay in origin (0,0) size changed to(2,2). */
  @Test
  public void getTopRightOriginSizeChange() {
    Entity e = new Entity();
    Point position = new Point(0, 0);
    Point offset = new Point(0, 0);
    Point size = new Point(2, 2);
    TriConsumer<Entity, Entity, Tile.Direction> iCollide = (a, b, c) -> {};
    e.add(new PositionComponent(position));
    CollideComponent hb = new CollideComponent(offset, size, iCollide, iCollide);
    e.add(hb);
    Point center = hb.topRight(e);

    assertEquals(2, center.x, DELTA);
    assertEquals(2, center.y, DELTA);
  }

  /** Position moved to (2,1) offset is still (0,0). */
  @Test
  public void getTopRightPositionMoved() {
    Entity e = new Entity();
    Point position = new Point(2, 1);
    Point offset = new Point(0, 0);
    Point size = new Point(1, 1);
    TriConsumer<Entity, Entity, Tile.Direction> iCollide = (a, b, c) -> {};
    e.add(new PositionComponent(position));
    CollideComponent hb = new CollideComponent(offset, size, iCollide, iCollide);
    e.add(hb);
    Point center = hb.topRight(e);

    assertEquals(3, center.x, DELTA);
    assertEquals(2, center.y, DELTA);
  }

  /** Position in origin (0,0) and offset moved to (1,2). */
  @Test
  public void getTopRightOriginOffsetMoved() {
    Entity e = new Entity();
    Point position = new Point(0, 0);
    Point offset = new Point(1, 2);
    Point size = new Point(1, 1);
    TriConsumer<Entity, Entity, Tile.Direction> iCollide = (a, b, c) -> {};
    e.add(new PositionComponent(position));
    CollideComponent hb = new CollideComponent(offset, size, iCollide, iCollide);
    e.add(hb);
    Point center = hb.topRight(e);

    assertEquals(2, center.x, DELTA);
    assertEquals(3, center.y, DELTA);
  }

  /** Position moved to (3,1) and offset moved to(2,4). */
  @Test
  public void getTopRightPositionMovedOffsetMoved() {
    Entity e = new Entity();
    Point position = new Point(3, 1);
    Point offset = new Point(2, 4);
    Point size = new Point(1, 1);
    TriConsumer<Entity, Entity, Tile.Direction> iCollide = (a, b, c) -> {};
    e.add(new PositionComponent(position));
    CollideComponent hb = new CollideComponent(offset, size, iCollide, iCollide);
    e.add(hb);
    Point center = hb.topRight(e);

    assertEquals(6, center.x, DELTA);
    assertEquals(6, center.y, DELTA);
  }

  /** Position moved to (3,1) and offset moved to(2,4) size changed to (3,3). */
  @Test
  public void getTopRightPositionMovedOffsetMovedSizeIncrease() {
    Entity e = new Entity();
    Point position = new Point(3, 1);
    Point offset = new Point(2, 4);
    Point size = new Point(3, 3);
    TriConsumer<Entity, Entity, Tile.Direction> iCollide = (a, b, c) -> {};
    e.add(new PositionComponent(position));
    CollideComponent hb = new CollideComponent(offset, size, iCollide, iCollide);
    e.add(hb);
    Point center = hb.topRight(e);

    assertEquals(8, center.x, DELTA);
    assertEquals(8, center.y, DELTA);
  }

  /** Missing Position Component when calling getBottomLeft should throw an exception. */
  @Test
  public void getBottomLeftMissingPosition() {
    Entity e = new Entity();
    CollideComponent hb = new CollideComponent();
    e.add(hb);
    MissingComponentException missingComponentException =
        assertThrows(MissingComponentException.class, () -> hb.center(e));
    assertTrue(missingComponentException.getMessage().contains(PositionComponent.class.getName()));
  }

  /** Position and offset stay in origin (0,0). */
  @Test
  public void getBottomLeftOrigin() {
    Entity e = new Entity();
    Point position = new Point(0, 0);
    Point offset = new Point(0, 0);
    Point size = new Point(1, 1);
    TriConsumer<Entity, Entity, Tile.Direction> iCollide = (a, b, c) -> {};
    e.add(new PositionComponent(position));
    CollideComponent hb = new CollideComponent(offset, size, iCollide, iCollide);
    e.add(hb);
    Point center = hb.bottomLeft(e);

    assertEquals(0, center.x, DELTA);
    assertEquals(0, center.y, DELTA);
  }

  /** Position moved to (2,1) offset is still (0,0). */
  @Test
  public void getBottomLeftPositionMoved() {
    Entity e = new Entity();
    Point position = new Point(2, 1);
    Point offset = new Point(0, 0);
    Point size = new Point(1, 1);
    TriConsumer<Entity, Entity, Tile.Direction> iCollide = (a, b, c) -> {};
    e.add(new PositionComponent(position));
    CollideComponent hb = new CollideComponent(offset, size, iCollide, iCollide);
    e.add(hb);
    Point center = hb.bottomLeft(e);

    assertEquals(2, center.x, DELTA);
    assertEquals(1, center.y, DELTA);
  }

  /** Position in origin (0,0) and offset moved to (1,2). */
  @Test
  public void getBottomLeftOriginOffsetMoved() {
    Entity e = new Entity();
    Point position = new Point(0, 0);
    Point offset = new Point(1, 2);
    Point size = new Point(1, 1);
    TriConsumer<Entity, Entity, Tile.Direction> iCollide = (a, b, c) -> {};
    e.add(new PositionComponent(position));
    CollideComponent hb = new CollideComponent(offset, size, iCollide, iCollide);
    e.add(hb);
    Point center = hb.bottomLeft(e);

    assertEquals(1, center.x, DELTA);
    assertEquals(2, center.y, DELTA);
  }

  /** Position moved to (3,1) and offset moved to(2,4). */
  @Test
  public void getBottomLeftPositionMovedOffsetMoved() {
    Entity e = new Entity();
    Point position = new Point(3, 1);
    Point offset = new Point(2, 4);
    Point size = new Point(1, 1);
    TriConsumer<Entity, Entity, Tile.Direction> iCollide = (a, b, c) -> {};
    e.add(new PositionComponent(position));
    CollideComponent hb = new CollideComponent(offset, size, iCollide, iCollide);
    e.add(hb);
    Point center = hb.bottomLeft(e);

    assertEquals(5, center.x, DELTA);
    assertEquals(5, center.y, DELTA);
  }
}
