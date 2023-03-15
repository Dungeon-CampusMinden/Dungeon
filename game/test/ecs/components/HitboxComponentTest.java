package ecs.components;

import static org.junit.Assert.*;

import ecs.components.collision.ICollide;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import org.junit.Test;
import tools.Point;

public class HitboxComponentTest {

    private static final double DELTA = 0.0001;

    /** on enter no method given */
    @Test
    public void onEnterNoMethod() {
        Entity e1 = new Entity();
        HitboxComponent hb1 = new HitboxComponent(e1, null, null);
        Entity e2 = new Entity();
        HitboxComponent hb2 = new HitboxComponent(e2, null, null);
        hb1.onEnter(hb2, Tile.Direction.N);
    }

    /** on enter method given */
    @Test
    public void onEnterCheckCall() {
        Entity e1 = new Entity();
        SimpleCounter counterE1Enter = new SimpleCounter();
        SimpleCounter counterE1Leave = new SimpleCounter();
        SimpleCounter counterE2Enter = new SimpleCounter();
        SimpleCounter counterE2Leave = new SimpleCounter();
        HitboxComponent hb1 =
                new HitboxComponent(
                        e1, (a, b, c) -> counterE1Enter.inc(), (a, b, c) -> counterE1Leave.inc());
        Entity e2 = new Entity();
        HitboxComponent hb2 =
                new HitboxComponent(
                        e2, (a, b, c) -> counterE2Enter.inc(), (a, b, c) -> counterE2Leave.inc());
        hb1.onEnter(hb2, Tile.Direction.N);
        assertEquals("Der Counter von Entität 1 Enter soll ", 1, counterE1Enter.getCount());
        assertEquals("Der Counter von Entität 1 Leave soll ", 0, counterE1Leave.getCount());
        assertEquals("Der Counter von Entität 2 Enter soll ", 0, counterE2Enter.getCount());
        assertEquals("Der Counter von Entität 2 Leave soll ", 0, counterE2Leave.getCount());
    }

    /** on leave no method given */
    @Test
    public void onLeaveNoMethod() {
        Entity e1 = new Entity();
        HitboxComponent hb1 = new HitboxComponent(e1, null, null);
        Entity e2 = new Entity();
        HitboxComponent hb2 = new HitboxComponent(e2, null, null);
        hb1.onLeave(hb2, Tile.Direction.N);
    }

    /** on leave method given */
    @Test
    public void onLeaveCheckCall() {
        Entity e1 = new Entity();
        SimpleCounter counterE1Enter = new SimpleCounter();
        SimpleCounter counterE1Leave = new SimpleCounter();
        SimpleCounter counterE2Enter = new SimpleCounter();
        SimpleCounter counterE2Leave = new SimpleCounter();
        HitboxComponent hb1 =
                new HitboxComponent(
                        e1, (a, b, c) -> counterE1Enter.inc(), (a, b, c) -> counterE1Leave.inc());
        Entity e2 = new Entity();
        HitboxComponent hb2 =
                new HitboxComponent(
                        e2, (a, b, c) -> counterE2Enter.inc(), (a, b, c) -> counterE2Leave.inc());
        hb1.onLeave(hb2, Tile.Direction.N);
        assertEquals("Der Counter von Entität 1 Enter soll ", 0, counterE1Enter.getCount());
        assertEquals("Der Counter von Entität 1 Leave soll ", 1, counterE1Leave.getCount());
        assertEquals("Der Counter von Entität 2 Enter soll ", 0, counterE2Enter.getCount());
        assertEquals("Der Counter von Entität 2 Leave soll ", 0, counterE2Leave.getCount());
    }

    @Test
    public void setiCollideEnterNull() {
        Entity e1 = new Entity();
        SimpleCounter counterE1Enter = new SimpleCounter();
        HitboxComponent hb1 = new HitboxComponent(e1, (a, b, c) -> counterE1Enter.inc(), null);
        Entity e2 = new Entity();
        HitboxComponent hb2 = new HitboxComponent(e2, null, null);
        hb1.setiCollideEnter(null);
        hb1.onEnter(hb2, Tile.Direction.N);
        assertEquals(
                "Die alte Collide darf nicht mehr aufgerufen werden ",
                0,
                counterE1Enter.getCount());
    }

    @Test
    public void setiCollideEnterValidCollider() {
        Entity e1 = new Entity();
        SimpleCounter counterE1Enter = new SimpleCounter();
        SimpleCounter newCounterE1Enter = new SimpleCounter();
        HitboxComponent hb1 = new HitboxComponent(e1, (a, b, c) -> counterE1Enter.inc(), null);
        Entity e2 = new Entity();
        HitboxComponent hb2 = new HitboxComponent(e2, null, null);
        hb1.setiCollideEnter((a, b, c) -> newCounterE1Enter.inc());
        hb1.onEnter(hb2, Tile.Direction.N);
        assertEquals(
                "Die alte Collide darf nicht mehr aufgerufen werden ",
                0,
                counterE1Enter.getCount());
        assertEquals("Die neue Collide muss aufgerufen werden ", 1, newCounterE1Enter.getCount());
    }

    @Test
    public void setiCollideLeaveNull() {
        Entity e1 = new Entity();
        SimpleCounter counterE1Enter = new SimpleCounter();
        HitboxComponent hb1 = new HitboxComponent(e1, null, (a, b, c) -> counterE1Enter.inc());
        Entity e2 = new Entity();
        HitboxComponent hb2 = new HitboxComponent(e2, null, null);
        hb1.setiCollideLeave(null);
        hb1.onLeave(hb2, Tile.Direction.N);
        assertEquals(
                "Die alte Collide darf nicht mehr aufgerufen werden ",
                0,
                counterE1Enter.getCount());
    }

    @Test
    public void setiCollideLeaveValidCollider() {
        Entity e1 = new Entity();
        SimpleCounter counterE1Leave = new SimpleCounter();
        SimpleCounter newCounterE1Leave = new SimpleCounter();
        HitboxComponent hb1 = new HitboxComponent(e1, null, (a, b, c) -> counterE1Leave.inc());
        Entity e2 = new Entity();
        HitboxComponent hb2 = new HitboxComponent(e2, null, null);
        hb1.setiCollideLeave((a, b, c) -> newCounterE1Leave.inc());
        hb1.onLeave(hb2, Tile.Direction.N);
        assertEquals(
                "Die alte Collide darf nicht mehr aufgerufen werden ",
                0,
                counterE1Leave.getCount());
        assertEquals("Die neue Collide muss aufgerufen werden ", 1, newCounterE1Leave.getCount());
    }

    /** missing PositionComponent */
    @Test
    public void getCenterMissingPositionComponent() {
        Entity e = new Entity();
        HitboxComponent hb =
                new HitboxComponent(
                        e, new Point(0, 0), new Point(0, 0), (a, b, c) -> {}, (a, b, c) -> {});
        MissingComponentException missingComponentException =
                assertThrows(MissingComponentException.class, hb::getCenter);
        assertTrue(
                missingComponentException.getMessage().contains(PositionComponent.class.getName()));
        assertTrue(
                missingComponentException.getMessage().contains(HitboxComponent.class.getName()));
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
    /** check Center for position(0, 0), offset(0, 0), size( 1, 1), result(.5f, .5f) */
    @Test
    public void getCenterFirst() {
        Entity e = new Entity();
        Point position = new Point(0, 0);
        Point offset = new Point(0, 0);
        Point size = new Point(1, 1);
        ICollide iCollide = (a, b, c) -> {};
        new PositionComponent(e, position);
        HitboxComponent hb = new HitboxComponent(e, offset, size, iCollide, iCollide);

        Point center = hb.getCenter();
        assertEquals(0.5f, center.x, DELTA);
        assertEquals(0.5f, center.y, DELTA);
    }

    /** check Center for position(1, 1), offset(0, 0), size(1, 1), result(1.5f, 1.5f) */
    @Test
    public void getCenterSecond() {
        Entity e = new Entity();
        Point position = new Point(1, 1);
        Point offset = new Point(0, 0);
        Point size = new Point(1, 1);
        ICollide iCollide = (a, b, c) -> {};
        new PositionComponent(e, position);
        HitboxComponent hb = new HitboxComponent(e, offset, size, iCollide, iCollide);

        Point center = hb.getCenter();
        assertEquals(1.5f, center.x, DELTA);
        assertEquals(1.5f, center.y, DELTA);
    }

    /** check Center for position(-1, -1), offset(0, 0), size(1, 1), result(-.5f, -.5f) */
    @Test
    public void getCenterThird() {
        Entity e = new Entity();
        Point position = new Point(-1, -1);
        Point offset = new Point(0, 0);
        Point size = new Point(1, 1);
        ICollide iCollide = (a, b, c) -> {};
        new PositionComponent(e, position);
        HitboxComponent hb = new HitboxComponent(e, offset, size, iCollide, iCollide);
        Point center = hb.getCenter();

        assertEquals(-.5f, center.x, DELTA);
        assertEquals(-.5f, center.y, DELTA);
    }

    /** check Center for position(.5f, .5f), offset(0, 0), size(1, 1), result(1, 1) */
    @Test
    public void getCenterFourth() {
        Entity e = new Entity();
        Point position = new Point(.5f, .5f);
        Point offset = new Point(0, 0);
        Point size = new Point(1, 1);
        ICollide iCollide = (a, b, c) -> {};
        new PositionComponent(e, position);
        HitboxComponent hb = new HitboxComponent(e, offset, size, iCollide, iCollide);
        Point center = hb.getCenter();

        assertEquals(1, center.x, DELTA);
        assertEquals(1, center.y, DELTA);
    }

    /** check Center for position(.5f, .5f), offset(-1, -1), size(1, 1), result(0, 0) */
    @Test
    public void getCenterFifth() {
        Entity e = new Entity();
        Point position = new Point(.5f, .5f);
        Point offset = new Point(-1, -1);
        Point size = new Point(1, 1);
        ICollide iCollide = (a, b, c) -> {};
        new PositionComponent(e, position);
        HitboxComponent hb = new HitboxComponent(e, offset, size, iCollide, iCollide);
        Point center = hb.getCenter();

        assertEquals(0, center.x, DELTA);
        assertEquals(0, center.y, DELTA);
    }

    /** check Center for position(-.5, .5), offset(0, 0), size(2,2), result(.5, 1.5) */
    @Test
    public void getCenterSixth() {
        Entity e = new Entity();
        Point position = new Point(-.5f, .5f);
        Point offset = new Point(0, 0);
        Point size = new Point(2, 2);
        ICollide iCollide = (a, b, c) -> {};
        new PositionComponent(e, position);
        HitboxComponent hb = new HitboxComponent(e, offset, size, iCollide, iCollide);
        Point center = hb.getCenter();

        assertEquals(.5, center.x, DELTA);
        assertEquals(1.5, center.y, DELTA);
    }

    /** Missing Position Component when calling getBottomLeft should throw an exception */
    @Test
    public void getTopRightMissingPosition() {
        Entity e = new Entity();
        HitboxComponent hb = new HitboxComponent(e);
        MissingComponentException missingComponentException =
                assertThrows(MissingComponentException.class, hb::getTopRight);
        assertTrue(
                missingComponentException.getMessage().contains(PositionComponent.class.getName()));
        assertTrue(
                missingComponentException.getMessage().contains(HitboxComponent.class.getName()));
    }

    /** Position and offset stay in origin (0,0) */
    @Test
    public void getTopRightOrigin() {
        Entity e = new Entity();
        Point position = new Point(0, 0);
        Point offset = new Point(0, 0);
        Point size = new Point(1, 1);
        ICollide iCollide = (a, b, c) -> {};
        new PositionComponent(e, position);
        HitboxComponent hb = new HitboxComponent(e, offset, size, iCollide, iCollide);
        Point center = hb.getTopRight();

        assertEquals(1, center.x, DELTA);
        assertEquals(1, center.y, DELTA);
    }

    /** Position and offset stay in origin (0,0) size changed to(2,2) */
    @Test
    public void getTopRightOriginSizeChange() {
        Entity e = new Entity();
        Point position = new Point(0, 0);
        Point offset = new Point(0, 0);
        Point size = new Point(2, 2);
        ICollide iCollide = (a, b, c) -> {};
        new PositionComponent(e, position);
        HitboxComponent hb = new HitboxComponent(e, offset, size, iCollide, iCollide);
        Point center = hb.getTopRight();

        assertEquals(2, center.x, DELTA);
        assertEquals(2, center.y, DELTA);
    }

    /** Position moved to (2,1) offset is still (0,0) */
    @Test
    public void getTopRightPositionMoved() {
        Entity e = new Entity();
        Point position = new Point(2, 1);
        Point offset = new Point(0, 0);
        Point size = new Point(1, 1);
        ICollide iCollide = (a, b, c) -> {};
        new PositionComponent(e, position);
        HitboxComponent hb = new HitboxComponent(e, offset, size, iCollide, iCollide);
        Point center = hb.getTopRight();

        assertEquals(3, center.x, DELTA);
        assertEquals(2, center.y, DELTA);
    }

    /** Position in origin (0,0) and offset moved to (1,2) */
    @Test
    public void getTopRightOriginOffsetMoved() {
        Entity e = new Entity();
        Point position = new Point(0, 0);
        Point offset = new Point(1, 2);
        Point size = new Point(1, 1);
        ICollide iCollide = (a, b, c) -> {};
        new PositionComponent(e, position);
        HitboxComponent hb = new HitboxComponent(e, offset, size, iCollide, iCollide);
        Point center = hb.getTopRight();

        assertEquals(2, center.x, DELTA);
        assertEquals(3, center.y, DELTA);
    }

    /** Position moved to (3,1) and offset moved to(2,4) */
    @Test
    public void getTopRightPositionMovedOffsetMoved() {
        Entity e = new Entity();
        Point position = new Point(3, 1);
        Point offset = new Point(2, 4);
        Point size = new Point(1, 1);
        ICollide iCollide = (a, b, c) -> {};
        new PositionComponent(e, position);
        HitboxComponent hb = new HitboxComponent(e, offset, size, iCollide, iCollide);
        Point center = hb.getTopRight();

        assertEquals(6, center.x, DELTA);
        assertEquals(6, center.y, DELTA);
    }

    /** Position moved to (3,1) and offset moved to(2,4) size changed to (3,3) */
    @Test
    public void getTopRightPositionMovedOffsetMovedSizeIncrease() {
        Entity e = new Entity();
        Point position = new Point(3, 1);
        Point offset = new Point(2, 4);
        Point size = new Point(3, 3);
        ICollide iCollide = (a, b, c) -> {};
        new PositionComponent(e, position);
        HitboxComponent hb = new HitboxComponent(e, offset, size, iCollide, iCollide);
        Point center = hb.getTopRight();

        assertEquals(8, center.x, DELTA);
        assertEquals(8, center.y, DELTA);
    }

    /** Missing Position Component when calling getBottomLeft should throw an exception */
    @Test
    public void getBottomLeftMissingPosition() {
        Entity e = new Entity();
        HitboxComponent hb = new HitboxComponent(e);
        MissingComponentException missingComponentException =
                assertThrows(MissingComponentException.class, hb::getCenter);
        assertTrue(
                missingComponentException.getMessage().contains(PositionComponent.class.getName()));
        assertTrue(
                missingComponentException.getMessage().contains(HitboxComponent.class.getName()));
    }

    /** Position and offset stay in origin (0,0) */
    @Test
    public void getBottomLeftOrigin() {
        Entity e = new Entity();
        Point position = new Point(0, 0);
        Point offset = new Point(0, 0);
        Point size = new Point(1, 1);
        ICollide iCollide = (a, b, c) -> {};
        new PositionComponent(e, position);
        HitboxComponent hb = new HitboxComponent(e, offset, size, iCollide, iCollide);
        Point center = hb.getBottomLeft();

        assertEquals(0, center.x, DELTA);
        assertEquals(0, center.y, DELTA);
    }

    /** Position moved to (2,1) offset is still (0,0) */
    @Test
    public void getBottomLeftPositionMoved() {
        Entity e = new Entity();
        Point position = new Point(2, 1);
        Point offset = new Point(0, 0);
        Point size = new Point(1, 1);
        ICollide iCollide = (a, b, c) -> {};
        new PositionComponent(e, position);
        HitboxComponent hb = new HitboxComponent(e, offset, size, iCollide, iCollide);
        Point center = hb.getBottomLeft();

        assertEquals(2, center.x, DELTA);
        assertEquals(1, center.y, DELTA);
    }

    /** Position in origin (0,0) and offset moved to (1,2) */
    @Test
    public void getBottomLeftOriginOffsetMoved() {
        Entity e = new Entity();
        Point position = new Point(0, 0);
        Point offset = new Point(1, 2);
        Point size = new Point(1, 1);
        ICollide iCollide = (a, b, c) -> {};
        new PositionComponent(e, position);
        HitboxComponent hb = new HitboxComponent(e, offset, size, iCollide, iCollide);
        Point center = hb.getBottomLeft();

        assertEquals(1, center.x, DELTA);
        assertEquals(2, center.y, DELTA);
    }

    /** Position moved to (3,1) and offset moved to(2,4) */
    @Test
    public void getBottomLeftPositionMovedOffsetMoved() {
        Entity e = new Entity();
        Point position = new Point(3, 1);
        Point offset = new Point(2, 4);
        Point size = new Point(1, 1);
        ICollide iCollide = (a, b, c) -> {};
        new PositionComponent(e, position);
        HitboxComponent hb = new HitboxComponent(e, offset, size, iCollide, iCollide);
        Point center = hb.getBottomLeft();

        assertEquals(5, center.x, DELTA);
        assertEquals(5, center.y, DELTA);
    }
}
