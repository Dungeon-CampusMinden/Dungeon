package ecs.systems;

import static org.junit.Assert.*;

import controller.SystemController;
import ecs.components.xp.ILevelUp;
import ecs.components.xp.XPComponent;
import ecs.entities.Entity;
import org.junit.Test;
import org.mockito.Mockito;
import starter.Game;

public class XPSystemTest {

    /** Test if the xp component ist initialized with zero xp and level zero. */
    @Test
    public void testStartingWithZero() {
        /* Prepare */
        Game.getEntities().clear();
        Game.getEntitiesToAdd().clear();
        Game.getEntitiesToRemove().clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        ILevelUp levelUp = Mockito.mock(ILevelUp.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();

        assertEquals(0, xpComponent.getCurrentXP());
        assertEquals(0, xpComponent.getCurrentLevel());
        xpSystem.update();
        assertEquals(0, xpComponent.getCurrentXP());
        assertEquals(0, xpComponent.getCurrentLevel());
    }

    /** Test if level up is not triggered if the xp is not enough. */
    @Test
    public void testNoLevelUp() {
        /* Prepare */
        Game.getEntities().clear();
        Game.getEntitiesToAdd().clear();
        Game.getEntitiesToRemove().clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        ILevelUp levelUp = Mockito.mock(ILevelUp.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();

        /* Test */
        xpComponent.addXP(99); // First level is reached with 100 XP
        xpSystem.update();
        assertEquals(0, xpComponent.getCurrentLevel());
    }

    /** Test if level up is triggered if the xp is exact the needed amount. */
    @Test
    public void testLevelUpExact() {
        /* Prepare */
        Game.getEntities().clear();
        Game.getEntitiesToAdd().clear();
        Game.getEntitiesToRemove().clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        ILevelUp levelUp = Mockito.mock(ILevelUp.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();
        Game.getEntities().addAll(Game.getEntitiesToAdd());
        Game.getEntitiesToAdd().clear();

        /* Test */
        xpComponent.addXP(100); // First level is reached with 100 XP
        xpSystem.update();
        assertEquals(1, xpComponent.getCurrentLevel());
        assertEquals(0, xpComponent.getCurrentXP());
    }

    /** Test if level up is triggered if the xp is more than the needed amount. */
    @Test
    public void testLevelUpOverflow() {
        /* Prepare */
        Game.getEntities().clear();
        Game.getEntitiesToAdd().clear();
        Game.getEntitiesToRemove().clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        ILevelUp levelUp = Mockito.mock(ILevelUp.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();
        Game.getEntities().addAll(Game.getEntitiesToAdd());
        Game.getEntitiesToAdd().clear();

        /* Test */
        xpComponent.addXP(120); // First level is reached with 100 XP
        xpSystem.update();
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
        Game.getEntities().clear();
        Game.getEntitiesToAdd().clear();
        Game.getEntitiesToRemove().clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        ILevelUp levelUp = Mockito.mock(ILevelUp.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();
        Game.getEntities().addAll(Game.getEntitiesToAdd());
        Game.getEntitiesToAdd().clear();

        /* Test */
        xpComponent.addXP(201);
        xpSystem.update();
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
        Game.getEntities().clear();
        Game.getEntitiesToAdd().clear();
        Game.getEntitiesToRemove().clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        ILevelUp levelUp = Mockito.mock(ILevelUp.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();

        /* Test */
        Game.getEntities().addAll(Game.getEntitiesToAdd());
        Game.getEntitiesToAdd().clear();
        xpComponent.addXP(221);
        xpSystem.update();
        assertEquals(2, xpComponent.getCurrentLevel());
        assertEquals(20, xpComponent.getCurrentXP());
    }

    /** Test if negative xp is not allowed. */
    @Test
    public void testNegativeXP() {
        /* Prepare */
        Game.getEntities().clear();
        Game.getEntitiesToAdd().clear();
        Game.getEntitiesToRemove().clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        ILevelUp levelUp = Mockito.mock(ILevelUp.class);
        XPComponent xpComponent = new XPComponent(entity, levelUp);
        XPSystem xpSystem = new XPSystem();

        /* Test */
        xpComponent.addXP(-1);
        xpSystem.update();
        assertEquals(0, xpComponent.getCurrentXP());
    }
}
