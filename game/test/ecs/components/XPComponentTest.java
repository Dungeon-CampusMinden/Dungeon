package ecs.components;

import static org.junit.Assert.*;

import ecs.components.xp.XPComponent;
import ecs.entities.Entity;
import ecs.systems.SystemController;
import mydungeon.ECS;
import org.junit.Test;

public class XPComponentTest {

    /** Test if the xp component ist initialized with zero xp and level zero. */
    @Test
    public void testStartXP() {
        /* Prepare */
        ECS.entities.clear();
        ECS.systems = new SystemController();
        Entity entity = new Entity();
        XPComponent xpComponent = new XPComponent(entity, null);

        /* Test */
        assertEquals(0, xpComponent.getCurrentXP());
        assertEquals(0, xpComponent.getCurrentLevel());
    }

    /** Test if xp is added correctly. */
    @Test
    public void testAddXP2() {
        /* Prepare */
        ECS.entities.clear();
        ECS.systems = new SystemController();
        Entity entity = new Entity();
        XPComponent xpComponent = new XPComponent(entity, null);

        /* Test */
        xpComponent.addXP(10);
        assertEquals(10, xpComponent.getCurrentXP());
    }

    /** Test if xp is added correctly. */
    @Test
    public void testAddXP3() {
        /* Prepare */
        ECS.entities.clear();
        ECS.systems = new SystemController();
        Entity entity = new Entity();
        XPComponent xpComponent = new XPComponent(entity, null);

        /* Test */
        xpComponent.addXP(10);
        xpComponent.addXP(10);
        assertEquals(20, xpComponent.getCurrentXP());
    }

    @Test
    public void testXPToNextLevel1() {
        /* Prepare */
        ECS.entities.clear();
        ECS.systems = new SystemController();
        Entity entity = new Entity();
        XPComponent xpComponent = new XPComponent(entity, null);

        /* Test */
        xpComponent.addXP(10);
        assertEquals(90, xpComponent.getXPToNextLevel());
    }

    @Test
    public void testXPToNextLevel2() {
        /* Prepare */
        ECS.entities.clear();
        ECS.systems = new SystemController();
        Entity entity = new Entity();
        XPComponent xpComponent = new XPComponent(entity, null);

        /* Test */
        xpComponent.addXP(100);
        assertEquals(0, xpComponent.getXPToNextLevel());
    }

    @Test
    public void testXPToNextLevel3() {
        /* Prepare */
        ECS.entities.clear();
        ECS.systems = new SystemController();
        Entity entity = new Entity();
        XPComponent xpComponent = new XPComponent(entity, null);

        /* Test */
        xpComponent.addXP(120);
        assertEquals(-20, xpComponent.getXPToNextLevel());
    }

}
