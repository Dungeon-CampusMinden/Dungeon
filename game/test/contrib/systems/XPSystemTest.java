package contrib.systems;

import static org.junit.Assert.*;

import contrib.components.XPComponent;

import core.Entity;
import core.Game;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.function.Consumer;

public class XPSystemTest {

    @After
    public void cleanup() {
        Game.removeAllEntities();
        Game.currentLevel(null);
        Game.removeAllSystems();
    }

    /** Test if the xp component ist initialized with zero xp and level zero. */
    @Test
    public void testStartingWithZero() {
        /* Prepare */
        Game.removeAllEntities();

        Entity entity = new Entity();
        Consumer<Entity> levelUp = Mockito.mock(Consumer.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();
        Game.add(xpSystem);
        xpSystem.showEntity(entity);
        assertEquals(0, xpComponent.currentXP());
        assertEquals(0, xpComponent.characterLevel());
        xpSystem.execute();
        assertEquals(0, xpComponent.currentXP());
        assertEquals(0, xpComponent.characterLevel());
    }

    /** Test if level up is not triggered if the xp is not enough. */
    @Test
    public void testNoLevelUp() {
        /* Prepare */
        Game.removeAllEntities();
        Entity entity = new Entity();

        Consumer<Entity> levelUp = Mockito.mock(Consumer.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();
        Game.add(xpSystem);
        xpSystem.showEntity(entity);
        /* Test */
        xpComponent.addXP(99); // First level is reached with 100 XP
        xpSystem.execute();
        assertEquals(0, xpComponent.characterLevel());
    }

    /** Test if level up is triggered if the xp is exact the needed amount. */
    @Test
    public void testLevelUpExact() {
        /* Prepare */
        Game.removeAllEntities();
        Entity entity = new Entity();

        Consumer<Entity> levelUp = Mockito.mock(Consumer.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();
        Game.add(xpSystem);
        xpSystem.showEntity(entity);
        /* Test */
        xpComponent.addXP(100); // First level is reached with 100 XP

        xpSystem.execute();
        assertEquals(1, xpComponent.characterLevel());
        assertEquals(0, xpComponent.currentXP());
    }

    /** Test if level up is triggered if the xp is more than the needed amount. */
    @Test
    public void testLevelUpOverflow() {
        /* Prepare */
        Game.removeAllEntities();
        Entity entity = new Entity();

        Consumer<Entity> levelUp = Mockito.mock(Consumer.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();
        Game.add(xpSystem);
        xpSystem.showEntity(entity);
        /* Test */
        xpComponent.addXP(120); // First level is reached with 100 XP
        xpSystem.execute();
        assertEquals(1, xpComponent.characterLevel());
        assertEquals(20, xpComponent.currentXP());
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

        Consumer<Entity> levelUp = Mockito.mock(Consumer.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();
        Game.add(xpSystem);
        xpSystem.showEntity(entity);
        /* Test */
        xpComponent.addXP(201);
        xpSystem.showEntity(entity);

        xpSystem.execute();
        assertEquals(2, xpComponent.characterLevel());
        assertEquals(0, xpComponent.currentXP());
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

        Consumer<Entity> levelUp = Mockito.mock(Consumer.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();
        Game.add(xpSystem);
        xpSystem.showEntity(entity);
        /* Test */

        xpComponent.addXP(221);
        xpSystem.execute();
        assertEquals(2, xpComponent.characterLevel());
        assertEquals(20, xpComponent.currentXP());
    }

    /** Test if negative xp is not allowed. */
    @Test
    public void testNegativeXP() {
        /* Prepare */
        Game.removeAllEntities();
        Entity entity = new Entity();

        Consumer<Entity> levelUp = Mockito.mock(Consumer.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();
        Game.add(xpSystem);
        xpSystem.showEntity(entity);
        /* Test */
        xpComponent.addXP(-1);
        xpSystem.execute();
        assertEquals(0, xpComponent.currentXP());
    }
}
