package ecs.tools.interaction;

import static org.junit.Assert.*;

import ecs.components.InteractionComponent;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import level.elements.TileLevel;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import org.junit.Test;
import starter.Game;
import testinghelper.SimpleCounter;
import tools.Point;

public class ControllPointReachableTest {
    private record PreparedEntityWithCounter(
            Entity e, SimpleCounter sc, PositionComponent pc, InteractionComponent ic) {}

    private static PreparedEntityWithCounter getEntityCounter(Point e1Point) {
        SimpleCounter s1 = new SimpleCounter();
        Entity e1 = new Entity();
        PositionComponent e1PC = new PositionComponent(e1, e1Point);
        InteractionComponent e1IC = new InteractionComponent(e1, 3, false, (e) -> s1.inc());
        PreparedEntityWithCounter first = new PreparedEntityWithCounter(e1, s1, e1PC, e1IC);
        return first;
    }

    /**
     * testlayout for the Level this Reach check uses the currentLevel to check for walls or not
     * accessible Tiles
     *
     * <p>F F F F F F W F F F F W F F F F W F F F F F F F F
     */
    private LevelElement[][] testLayout =
            new LevelElement[][] {
                new LevelElement[] {
                    LevelElement.FLOOR,
                    LevelElement.FLOOR,
                    LevelElement.FLOOR,
                    LevelElement.FLOOR,
                    LevelElement.FLOOR
                },
                new LevelElement[] {
                    LevelElement.FLOOR,
                    LevelElement.WALL,
                    LevelElement.FLOOR,
                    LevelElement.FLOOR,
                    LevelElement.FLOOR
                },
                new LevelElement[] {
                    LevelElement.FLOOR,
                    LevelElement.WALL,
                    LevelElement.FLOOR,
                    LevelElement.FLOOR,
                    LevelElement.FLOOR
                },
                new LevelElement[] {
                    LevelElement.FLOOR,
                    LevelElement.WALL,
                    LevelElement.FLOOR,
                    LevelElement.FLOOR,
                    LevelElement.FLOOR
                },
                new LevelElement[] {
                    LevelElement.FLOOR,
                    LevelElement.FLOOR,
                    LevelElement.FLOOR,
                    LevelElement.FLOOR,
                    LevelElement.FLOOR
                }
            };

    private void cleanup() {
        Game.currentLevel = null;
        Game.entities.clear();
    }

    private void setup() {
        cleanup();
        Game.currentLevel = new TileLevel(testLayout, DesignLabel.DEFAULT);
    }

    /** Check when the Entities are on top of each other */
    @Test
    public void controllDefault() {
        setup();

        IReachable i = new ControllPointReachable();

        Point e1Point = new Point(0, 0);
        PreparedEntityWithCounter first = getEntityCounter(e1Point);

        Point e2Point = new Point(0, 0);
        PreparedEntityWithCounter second = getEntityCounter(e2Point);

        Point unitDirectionalVector =
                Point.getUnitDirectionalVector(first.pc.getPosition(), second.pc.getPosition());
        int dist = 0;
        boolean b =
                i.checkReachable(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertTrue(b);

        cleanup();
    }

    /** Check when the Entities are in a straight line without anything between */
    @Test
    public void controllStraightNotBlocked() {
        setup();

        IReachable i = new ControllPointReachable();

        Point e1Point = new Point(0, 0);
        PreparedEntityWithCounter first = getEntityCounter(e1Point);

        Point e2Point = new Point(2, 0);
        PreparedEntityWithCounter second = getEntityCounter(e2Point);

        Point unitDirectionalVector =
                Point.getUnitDirectionalVector(second.pc.getPosition(), first.pc.getPosition());
        float dist = Point.calculateDistance(first.pc.getPosition(), second.pc.getPosition());
        boolean b =
                i.checkReachable(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertTrue(b);
    }

    /** checkif the Entities are diagonal of each other */
    @Test
    public void controllDiagonaltNotBlocked() {
        setup();

        IReachable i = new ControllPointReachable();

        Point e1Point = new Point(2, 0);
        PreparedEntityWithCounter first = getEntityCounter(e1Point);

        Point e2Point = new Point(4, 2);
        PreparedEntityWithCounter second = getEntityCounter(e2Point);

        Point unitDirectionalVector =
                Point.getUnitDirectionalVector(second.pc.getPosition(), first.pc.getPosition());
        float dist = Point.calculateDistance(first.pc.getPosition(), second.pc.getPosition());
        boolean b =
                i.checkReachable(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertTrue(b);
        cleanup();
    }

    /** check if the return value is false when between the Entities is a nonAccessible Tile */
    @Test
    public void controllDiagonaltBlocked() {
        setup();

        IReachable i = new ControllPointReachable();

        Point e1Point = new Point(0, 0);
        PreparedEntityWithCounter first = getEntityCounter(e1Point);

        Point e2Point = new Point(2, 2);
        PreparedEntityWithCounter second = getEntityCounter(e2Point);

        Point unitDirectionalVector =
                Point.getUnitDirectionalVector(second.pc.getPosition(), first.pc.getPosition());
        float dist = Point.calculateDistance(first.pc.getPosition(), second.pc.getPosition());
        boolean b =
                i.checkReachable(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertFalse(b);
        cleanup();
    }

    /** check if the return value is false when between the Entities is a nonAccessible Tile */
    @Test
    public void controllStraightBlocked() {
        setup();

        IReachable i = new ControllPointReachable();

        Point e1Point = new Point(0, 1);
        PreparedEntityWithCounter first = getEntityCounter(e1Point);

        Point e2Point = new Point(2, 1);
        PreparedEntityWithCounter second = getEntityCounter(e2Point);

        Point unitDirectionalVector =
                Point.getUnitDirectionalVector(second.pc.getPosition(), first.pc.getPosition());
        float dist = Point.calculateDistance(first.pc.getPosition(), second.pc.getPosition());
        boolean b =
                i.checkReachable(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertFalse(b);
        cleanup();
    }

    /**
     * check if the return value is false when between the Entities is a nonAccessible Tile and the
     * interactionradius is smaller then the distance
     */
    @Test
    public void controllStraightBlockedOutOfRadius() {
        setup();

        IReachable i = new ControllPointReachable();

        Point e1Point = new Point(0, 1);
        PreparedEntityWithCounter first = getEntityCounter(e1Point);

        Point e2Point = new Point(4, 1);
        PreparedEntityWithCounter second = getEntityCounter(e2Point);

        Point unitDirectionalVector =
                Point.getUnitDirectionalVector(second.pc.getPosition(), first.pc.getPosition());
        float dist = Point.calculateDistance(first.pc.getPosition(), second.pc.getPosition());
        boolean b =
                i.checkReachable(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertFalse(b);
        cleanup();
    }

    /**
     * check if the return value is false when the distance between the Entities is bigger then the
     * interactionradius
     */
    @Test
    public void controllStraightNotBlockedOutOfRAnge() {
        setup();

        IReachable i = new ControllPointReachable();

        Point e1Point = new Point(0, 0);
        PreparedEntityWithCounter first = getEntityCounter(e1Point);

        Point e2Point = new Point(4, 0);
        PreparedEntityWithCounter second = getEntityCounter(e2Point);

        Point unitDirectionalVector =
                Point.getUnitDirectionalVector(second.pc.getPosition(), first.pc.getPosition());
        float dist = Point.calculateDistance(first.pc.getPosition(), second.pc.getPosition());
        boolean b =
                i.checkReachable(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertFalse(b);
    }
}
