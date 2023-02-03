package controller;

import static org.mockito.Mockito.CALLS_REAL_METHODS;

import basiselements.DungeonElement;
import hamster.HamsterSimulator;
import hamster.elements.Loot;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;
import tools.Point;

/**
 * Tests for the hamster simulator game instance
 *
 * @author Maxim Fruendt
 */
public class HamsterSimulatorTest {

    private HamsterSimulator hamsterSimulator;
    private DungeonElement testElement1;
    private DungeonElement testElement2;
    private DungeonElement testElement3;
    private DungeonElement testElement4;
    private final Point pos1 = new Point(0, 0);
    private final Point pos2 = new Point(1, 1);
    private final Point pos3 = new Point(2, 2);
    private final Point pos4 = new Point(10, 10);
    private static final float COLLISION_RANGE = 2f;

    /** Setup the tests */
    @Before
    public void setup() {
        testElement1 = new Loot();
        testElement2 = new Loot();
        testElement3 = new Loot();
        testElement4 = new Loot();
        testElement1.setPosition(pos1);
        testElement2.setPosition(pos2);
        testElement3.setPosition(pos3);
        testElement4.setPosition(pos4);

        hamsterSimulator =
                PowerMockito.mock(
                        HamsterSimulator.class,
                        Mockito.withSettings().defaultAnswer(CALLS_REAL_METHODS));
        PowerMockito.when(hamsterSimulator.isLevelPosAccessible(ArgumentMatchers.any()))
                .thenReturn(true);
        Whitebox.setInternalState(hamsterSimulator, "entityController", new EntityController(null));
        Whitebox.setInternalState(hamsterSimulator, "entityLock", new Object());
    }

    /** Verify that no collisions occur when no entity was registered */
    @Test
    public void test_collision_no_entities() {
        List<DungeonElement> collidingEntities =
                hamsterSimulator.getCollidingEntitiesForEntity(testElement1, pos1, COLLISION_RANGE);
        Assert.assertEquals(0, collidingEntities.size());
    }

    /** Verify that one entity doesn't collide with itself */
    @Test
    public void test_no_collision_one_entities() {
        hamsterSimulator.addDungeonElement(testElement1);
        boolean isAccessible =
                hamsterSimulator.isPosAccessibleForEntity(testElement1, pos1, COLLISION_RANGE);
        Assert.assertTrue(isAccessible);
    }

    /** Verify that two entities within reach of each other collide */
    @Test
    public void test_collision_two_entities() {
        hamsterSimulator.addDungeonElement(testElement1);
        hamsterSimulator.addDungeonElement(testElement2);
        List<DungeonElement> collidingEntities =
                hamsterSimulator.getCollidingEntitiesForEntity(testElement1, pos1, COLLISION_RANGE);
        Assert.assertEquals(1, collidingEntities.size());
        Assert.assertEquals(testElement2, collidingEntities.get(0));
    }

    /** Verify that multiple entities can collide with each other, when in reach */
    @Test
    public void test_collision_multiple_entities() {
        hamsterSimulator.addDungeonElement(testElement1);
        hamsterSimulator.addDungeonElement(testElement2);
        hamsterSimulator.addDungeonElement(testElement3);
        hamsterSimulator.addDungeonElement(testElement4);
        List<DungeonElement> collidingEntities =
                hamsterSimulator.getCollidingEntitiesForEntity(testElement2, pos2, COLLISION_RANGE);
        Assert.assertEquals(2, collidingEntities.size());
        Assert.assertEquals(testElement1, collidingEntities.get(0));
        Assert.assertEquals(testElement3, collidingEntities.get(1));
    }
}
