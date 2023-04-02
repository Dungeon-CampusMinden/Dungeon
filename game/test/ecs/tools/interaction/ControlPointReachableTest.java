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

public class ControlPointReachableTest {
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
     * <p>All normal Floors except the second column which has from row 2-4 Walls
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
        Game.getEntities().clear();
    }

    private void setup() {
        cleanup();
        Game.currentLevel = new TileLevel(testLayout, DesignLabel.DEFAULT);
    }

    /** Check when the Entities are on top of each other */
    @Test
    public void controlDefault() {
        setup();

        IReachable i = new ControlPointReachable();

        Point e1Point = new Point(0, 0);
        PreparedEntityWithCounter first = getEntityCounter(e1Point);

        Point e2Point = new Point(0, 0);
        PreparedEntityWithCounter second = getEntityCounter(e2Point);

        Point unitDirectionalVector =
                Point.getUnitDirectionalVector(first.pc.getPosition(), second.pc.getPosition());
        int dist = 0;
        boolean reachable =
                i.checkReachable(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertTrue("should be true the Points are on the same Tile.", reachable);

        cleanup();
    }

    /** Check when the Entities are in a straight line without anything between */
    @Test
    public void controlStraightNotBlocked() {
        setup();

        IReachable i = new ControlPointReachable();

        Point e1Point = new Point(0, 0);
        PreparedEntityWithCounter first = getEntityCounter(e1Point);

        Point e2Point = new Point(2, 0);
        PreparedEntityWithCounter second = getEntityCounter(e2Point);

        Point unitDirectionalVector =
                Point.getUnitDirectionalVector(second.pc.getPosition(), first.pc.getPosition());
        float dist = Point.calculateDistance(first.pc.getPosition(), second.pc.getPosition());
        boolean reachable =
                i.checkReachable(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertTrue("The Points have a line of sight and are close enough", reachable);
    }

    /** check if the Entities are diagonal of each other */
    @Test
    public void controlsDiagonalNotBlocked() {
        setup();

        IReachable i = new ControlPointReachable();

        Point e1Point = new Point(2, 0);
        PreparedEntityWithCounter first = getEntityCounter(e1Point);

        Point e2Point = new Point(4, 2);
        PreparedEntityWithCounter second = getEntityCounter(e2Point);

        Point unitDirectionalVector =
                Point.getUnitDirectionalVector(second.pc.getPosition(), first.pc.getPosition());
        float dist = Point.calculateDistance(first.pc.getPosition(), second.pc.getPosition());
        boolean reachable =
                i.checkReachable(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertTrue("Distance is close enough and the Points have a line of sight", reachable);
        cleanup();
    }

    /** check if the return value is false when between the Entities is a nonAccessible Tile */
    @Test
    public void controlDiagonalBlocked() {
        setup();

        IReachable i = new ControlPointReachable();

        Point e1Point = new Point(0, 0);
        PreparedEntityWithCounter first = getEntityCounter(e1Point);

        Point e2Point = new Point(2, 2);
        PreparedEntityWithCounter second = getEntityCounter(e2Point);

        Point unitDirectionalVector =
                Point.getUnitDirectionalVector(second.pc.getPosition(), first.pc.getPosition());
        float dist = Point.calculateDistance(first.pc.getPosition(), second.pc.getPosition());
        boolean reachable =
                i.checkReachable(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertFalse("Distance is close enough but there is a Wall between the Points", reachable);
        cleanup();
    }

    /** check if the return value is false when between the Entities is a nonAccessible Tile */
    @Test
    public void controlStraightBlocked() {
        setup();

        IReachable i = new ControlPointReachable();

        Point e1Point = new Point(0, 1);
        PreparedEntityWithCounter first = getEntityCounter(e1Point);

        Point e2Point = new Point(2, 1);
        PreparedEntityWithCounter second = getEntityCounter(e2Point);

        Point unitDirectionalVector =
                Point.getUnitDirectionalVector(second.pc.getPosition(), first.pc.getPosition());
        float dist = Point.calculateDistance(first.pc.getPosition(), second.pc.getPosition());
        boolean reachable =
                i.checkReachable(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertFalse("Distance is close enough but there is a Wall between the Points", reachable);
        cleanup();
    }

    /**
     * check if the return value is false when between the Entities is a nonAccessible Tile and the
     * interaction-radius is smaller than the distance
     */
    @Test
    public void controlStraightBlockedOutOfRadius() {
        setup();

        IReachable i = new ControlPointReachable();

        Point e1Point = new Point(0, 1);
        PreparedEntityWithCounter first = getEntityCounter(e1Point);

        Point e2Point = new Point(4, 1);
        PreparedEntityWithCounter second = getEntityCounter(e2Point);

        Point unitDirectionalVector =
                Point.getUnitDirectionalVector(second.pc.getPosition(), first.pc.getPosition());
        float dist = Point.calculateDistance(first.pc.getPosition(), second.pc.getPosition());
        boolean reachable =
                i.checkReachable(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertFalse("Distance not close enough and no line of sight.", reachable);
        cleanup();
    }

    /**
     * check if the return value is false when the distance between the Entities is bigger then the
     * interaction-radius
     */
    @Test
    public void controlStraightNotBlockedOutOfRAnge() {
        setup();

        IReachable i = new ControlPointReachable();

        Point e1Point = new Point(0, 0);
        PreparedEntityWithCounter first = getEntityCounter(e1Point);

        Point e2Point = new Point(4, 0);
        PreparedEntityWithCounter second = getEntityCounter(e2Point);

        Point unitDirectionalVector =
                Point.getUnitDirectionalVector(second.pc.getPosition(), first.pc.getPosition());
        float dist = Point.calculateDistance(first.pc.getPosition(), second.pc.getPosition());
        boolean reachable =
                i.checkReachable(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertFalse(reachable);
    }
}
