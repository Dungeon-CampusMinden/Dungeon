package ecs.components;

import static org.junit.Assert.*;

import ecs.components.xp.XPComponent;
import ecs.entities.Entity;
import ecs.systems.SystemController;
import mydungeon.ECS;
import org.junit.Before;
import org.junit.Test;

public class XPComponentTest {

    private XPComponent xpComponent;

    @Before
    public void setup() {
        ECS.entities.clear();
        ECS.systems = new SystemController();
        Entity entity = new Entity();
        xpComponent = new XPComponent(entity, null);
    }

    @Test
    public void startXP() {
        assertEquals(0, xpComponent.getCurrentXP());
    }

    @Test
    public void addXP2() {
        assertEquals(0, xpComponent.getCurrentXP());
        xpComponent.addXP(10);
        assertEquals(10, xpComponent.getCurrentXP());
    }

    @Test
    public void addXP3() {
        assertEquals(0, xpComponent.getCurrentXP());
        xpComponent.addXP(10);
        xpComponent.addXP(10);
        assertEquals(20, xpComponent.getCurrentXP());
    }
}
