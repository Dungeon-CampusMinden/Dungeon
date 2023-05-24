package contrib.systems;

import static org.junit.Assert.*;

import contrib.components.XPComponent;

import core.Entity;
import core.Game;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.function.LongConsumer;

public class XPSystemTest {

    /** Test if the xp component ist initialized with zero xp and level zero. */
    @Test
    public void testStartingWithZero() {
        /* Prepare */
        Game.removeAllEntities();

        Entity entity = new Entity();
        LongConsumer levelUp = Mockito.mock(LongConsumer.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();
        xpSystem.showEntity(entity);
        assertEquals(0, xpComponent.getCurrentXP());
        assertEquals(0, xpComponent.getCurrentLevel());
        xpSystem.execute();
        assertEquals(0, xpComponent.getCurrentXP());
        assertEquals(0, xpComponent.getCurrentLevel());
    }

    /** Test if level up is not triggered if the xp is not enough. */
    @Test
    public void testNoLevelUp() {
        /* Prepare */
        Game.removeAllEntities();
        Entity entity = new Entity();

        LongConsumer levelUp = Mockito.mock(LongConsumer.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();
        xpSystem.showEntity(entity);
        /* Test */
        xpComponent.addXP(99); // First level is reached with 100 XP
        xpSystem.execute();
        assertEquals(0, xpComponent.getCurrentLevel());
    }

    /** Test if level up is triggered if the xp is exact the needed amount. */
    @Test
    public void testLevelUpExact() {
        /* Prepare */
        Game.removeAllEntities();
        Entity entity = new Entity();

        LongConsumer levelUp = Mockito.mock(LongConsumer.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();
        xpSystem.showEntity(entity);
        /* Test */
        xpComponent.addXP(100); // First level is reached with 100 XP

        xpSystem.execute();
        assertEquals(1, xpComponent.getCurrentLevel());
        assertEquals(0, xpComponent.getCurrentXP());
    }

    /** Test if level up is triggered if the xp is more than the needed amount. */
    @Test
    public void testLevelUpOverflow() {
        /* Prepare */
        Game.removeAllEntities();
        Entity entity = new Entity();

        LongConsumer levelUp = Mockito.mock(LongConsumer.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();
        xpSystem.showEntity(entity);
        /* Test */
        xpComponent.addXP(120); // First level is reached with 100 XP
        xpSystem.execute();
        assertEquals(1, xpComponent.getCurrentLevel());
        assertEquals(20, xpComponent.getCurrentXP());
    }

    /**
     * Test if two level ups are triggered if the xp is exact the needed amount for two levels.
     * First Level is reached with 100 XP, second level is reached with 101 XP (100 + 101 = 201)
     */
    @Test
    public void testLevelUpMultipleExact() {
        /* Prepare */
        Game.removeAllEntities();
        Entity entity = new Entity();

        LongConsumer levelUp = Mockito.mock(LongConsumer.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();
        xpSystem.showEntity(entity);
        /* Test */
        xpComponent.addXP(201);
        xpSystem.showEntity(entity);

        xpSystem.execute();
        assertEquals(2, xpComponent.getCurrentLevel());
        assertEquals(0, xpComponent.getCurrentXP());
    }

    /**
     * Test if two level ups are triggered if the xp is more than the needed amount for two levels.
     * First Level is reached with 100 XP, second level is reached with 141 XP (100 + 101 = 201)
     */
    @Test
    public void testLevelUpMultipleOverflow() {
        /* Prepare */
        Game.removeAllEntities();
        Entity entity = new Entity();

        LongConsumer levelUp = Mockito.mock(LongConsumer.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();
        xpSystem.showEntity(entity);
        /* Test */

        xpComponent.addXP(221);
        xpSystem.execute();
        assertEquals(2, xpComponent.getCurrentLevel());
        assertEquals(20, xpComponent.getCurrentXP());
    }

    /** Test if negative xp is not allowed. */
    @Test
    public void testNegativeXP() {
        /* Prepare */
        Game.removeAllEntities();
        Entity entity = new Entity();

        LongConsumer levelUp = Mockito.mock(LongConsumer.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();
        xpSystem.showEntity(entity);
        /* Test */
        xpComponent.addXP(-1);
        xpSystem.execute();
        assertEquals(0, xpComponent.getCurrentXP());
    }
}
