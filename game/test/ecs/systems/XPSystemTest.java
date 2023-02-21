package ecs.systems;

import static org.junit.Assert.*;

import ecs.components.xp.ILevelUp;
import ecs.components.xp.XPComponent;
import ecs.entities.Entity;
import mydungeon.ECS;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class XPSystemTest {

    private Entity entity;
    private XPComponent xpComponent;
    private XPSystem xpSystem;
    private ILevelUp levelUp;

    @Before
    public void setup() {
        ECS.entities.clear();
        ECS.systems = new SystemController();
        this.entity = new Entity();
        this.levelUp = Mockito.mock(ILevelUp.class);
        this.xpComponent = new XPComponent(entity, this.levelUp);
        this.xpSystem = new XPSystem();
    }

    @Test
    public void noLevelUp() {
        assertEquals(0, this.xpComponent.getCurrentLevel());
        this.xpComponent.addXP(99); //First level is reached with 100 XP
        this.xpSystem.update();
        assertEquals(0, this.xpComponent.getCurrentLevel());
    }

    @Test
    public void levelUpExact() {
        assertEquals(0, this.xpComponent.getCurrentLevel());
        this.xpComponent.addXP(100); //First level is reached with 100 XP
        this.xpSystem.update();
        assertEquals(1, this.xpComponent.getCurrentLevel());
    }

    @Test
    public void levelUpOverflow() {
        assertEquals(0, this.xpComponent.getCurrentLevel());
        this.xpComponent.addXP(120); //First level is reached with 100 XP
        this.xpSystem.update();
        assertEquals(1, this.xpComponent.getCurrentLevel());
        assertEquals(20, this.xpComponent.getCurrentXP());
    }

    @Test
    public void levelUpMultipleExact() {
        assertEquals(0, this.xpComponent.getCurrentLevel());
        this.xpComponent.addXP(241); //First level is reached with 100 XP second level is reached with 141 XP (100 + 141 = 241)
        this.xpSystem.update();
        assertEquals(2, this.xpComponent.getCurrentLevel());
    }

    @Test
    public void levelUpMultipleOverflow() {
        assertEquals(0, this.xpComponent.getCurrentLevel());
        this.xpComponent.addXP(261); //First level is reached with 100 XP second level is reached with 141 XP (100 + 141 = 241)
        this.xpSystem.update();
        assertEquals(2, this.xpComponent.getCurrentLevel());
        assertEquals(20, this.xpComponent.getCurrentXP());
    }


}
