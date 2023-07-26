package contrib.utils.components.interaction;

import static org.junit.Assert.*;

import contrib.components.InteractionComponent;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.TileLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.systems.LevelSystem;
import core.utils.position.Point;
import core.utils.position.Position;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import testingUtils.SimpleCounter;

import java.util.function.Function;

public class ControlPositionReachableTest {
    /**
     * testlayout for the Level this Reach check uses the currentLevel to check for walls or not
     * accessible Tiles
     *
     * <p>All normal Floors except the second column which has from row 2-4 Walls
     */
    private final LevelElement[][] testLayout =
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

    private static PreparedEntityWithCounter getEntityCounter(Position e1Position) {
        SimpleCounter s1 = new SimpleCounter();
        Entity e1 = new Entity();
        PositionComponent e1PC = new PositionComponent(e1, e1Position);
        InteractionComponent e1IC = new InteractionComponent(e1, 3, false, (e) -> s1.inc());
        PreparedEntityWithCounter first = new PreparedEntityWithCounter(e1, s1, e1PC, e1IC);
        return first;
    }

    @Before
    public void setup() {
        Game.addSystem(new LevelSystem(null, null, () -> {}));
        Game.currentLevel(new TileLevel(testLayout, DesignLabel.DEFAULT));
    }

    @After
    public void cleanup() {
        Game.currentLevel(null);
        Game.removeAllEntities();
        Game.removeAllSystems();
    }

    /** Check when the Entities are on top of each other */
    @Test
    public void controlDefault() {
        Function<InteractionData, Boolean> i = new ControlPointReachable();

        Position e1Position = new Point(0, 0);
        PreparedEntityWithCounter first = getEntityCounter(e1Position);

        Position e2Position = new Point(0, 0);
        PreparedEntityWithCounter second = getEntityCounter(e2Position);

        Position unitDirectionalVector =
                Position.unitDirectionalVector(first.pc.position(), second.pc.position());
        int dist = 0;
        boolean reachable =
                i.apply(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertTrue("should be true the Points are on the same Tile.", reachable);
    }

    /** Check when the Entities are in a straight line without anything between */
    @Test
    public void controlStraightNotBlocked() {
        Function<InteractionData, Boolean> i = new ControlPointReachable();

        Position e1Position = new Point(0, 0);
        PreparedEntityWithCounter first = getEntityCounter(e1Position);

        Position e2Position = new Point(2, 0);
        PreparedEntityWithCounter second = getEntityCounter(e2Position);

        Position unitDirectionalVector =
                Position.unitDirectionalVector(second.pc.position(), first.pc.position());
        float dist = Position.calculateDistance(first.pc.position(), second.pc.position());
        boolean reachable =
                i.apply(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertTrue("The Points have a line of sight and are close enough", reachable);
    }

    /** check if the Entities are diagonal of each other */
    @Test
    public void controlsDiagonalNotBlocked() {
        Function<InteractionData, Boolean> i = new ControlPointReachable();

        Position e1Position = new Point(2, 0);
        PreparedEntityWithCounter first = getEntityCounter(e1Position);

        Position e2Position = new Point(4, 2);
        PreparedEntityWithCounter second = getEntityCounter(e2Position);

        Position unitDirectionalVector =
                Position.unitDirectionalVector(second.pc.position(), first.pc.position());
        float dist = Position.calculateDistance(first.pc.position(), second.pc.position());
        boolean reachable =
                i.apply(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertTrue("Distance is close enough and the Points have a line of sight", reachable);
    }

    /** check if the return value is false when between the Entities is a nonAccessible Tile */
    @Test
    public void controlDiagonalBlocked() {
        Function<InteractionData, Boolean> i = new ControlPointReachable();

        Position e1Position = new Point(0, 0);
        PreparedEntityWithCounter first = getEntityCounter(e1Position);

        Position e2Position = new Point(2, 2);
        PreparedEntityWithCounter second = getEntityCounter(e2Position);

        Position unitDirectionalVector =
                Position.unitDirectionalVector(second.pc.position(), first.pc.position());
        float dist = Position.calculateDistance(first.pc.position(), second.pc.position());
        boolean reachable =
                i.apply(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertFalse("Distance is close enough but there is a Wall between the Points", reachable);
    }

    /** check if the return value is false when between the Entities is a nonAccessible Tile */
    @Test
    public void controlStraightBlocked() {
        Function<InteractionData, Boolean> i = new ControlPointReachable();

        Position e1Position = new Point(0, 1);
        PreparedEntityWithCounter first = getEntityCounter(e1Position);

        Position e2Position = new Point(2, 1);
        PreparedEntityWithCounter second = getEntityCounter(e2Position);

        Position unitDirectionalVector =
                Position.unitDirectionalVector(second.pc.position(), first.pc.position());
        float dist = Position.calculateDistance(first.pc.position(), second.pc.position());
        boolean reachable =
                i.apply(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertFalse("Distance is close enough but there is a Wall between the Points", reachable);
    }

    /**
     * check if the return value is false when between the Entities is a nonAccessible Tile and the
     * interaction-radius is smaller than the distance
     */
    @Test
    public void controlStraightBlockedOutOfRadius() {

        Function<InteractionData, Boolean> i = new ControlPointReachable();

        Position e1Position = new Point(0, 1);
        PreparedEntityWithCounter first = getEntityCounter(e1Position);

        Position e2Position = new Point(4, 1);
        PreparedEntityWithCounter second = getEntityCounter(e2Position);

        Position unitDirectionalVector =
                Position.unitDirectionalVector(second.pc.position(), first.pc.position());
        float dist = Position.calculateDistance(first.pc.position(), second.pc.position());
        boolean reachable =
                i.apply(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertFalse("Distance not close enough and no line of sight.", reachable);
    }

    /**
     * check if the return value is false when the distance between the Entities is bigger then the
     * interaction-radius
     */
    @Test
    public void controlStraightNotBlockedOutOfRAnge() {
        Function<InteractionData, Boolean> i = new ControlPointReachable();

        Position e1Position = new Point(0, 0);
        PreparedEntityWithCounter first = getEntityCounter(e1Position);

        Position e2Position = new Point(4, 0);
        PreparedEntityWithCounter second = getEntityCounter(e2Position);

        Position unitDirectionalVector =
                Position.unitDirectionalVector(second.pc.position(), first.pc.position());
        float dist = Position.calculateDistance(first.pc.position(), second.pc.position());
        boolean reachable =
                i.apply(
                        new InteractionData(
                                first.e, first.pc, first.ic, dist, unitDirectionalVector));
        assertEquals(0, first.sc.getCount());
        assertEquals(0, second.sc.getCount());
        assertFalse(reachable);
    }

    private record PreparedEntityWithCounter(
            Entity e, SimpleCounter sc, PositionComponent pc, InteractionComponent ic) {}
}
