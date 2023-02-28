package ecs.systems;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import ecs.components.Component;
import ecs.components.InteractionComponent;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import ecs.entities.Hero;
import java.util.Optional;
import mydungeon.ECS;
import org.junit.Test;
import tools.Point;

public class InteractionSystemTest {
    private static final class SimpleCounter {
        private int count = 0;

        public void inc() {
            count++;
        }

        public int getCount() {
            return count;
        }
    }

    private static Hero fullMockedHero(boolean havingpc) {
        Hero mock = mock(Hero.class);
        Optional<Component> pc;
        if (havingpc) {
            pc = Optional.of(new PositionComponent(mock, new Point(0, 0)));
        } else {
            pc = Optional.empty();
        }
        when(mock.getComponent(PositionComponent.class)).thenReturn(pc);
        return mock;
    }

    /** Tests the functionality when the Hero does not have the PositionComponent */
    @Test
    public void interactWithClosestInteractableHeroMissingPositionComponent() {
        ECS.hero = fullMockedHero(false);

        MissingComponentException e =
                assertThrows(
                        MissingComponentException.class,
                        InteractionSystem::interactWithClosestInteractable);
        assertTrue(e.getMessage().contains(InteractionSystem.class.getName()));
        assertTrue(e.getMessage().contains(Hero.class.getName()));
        assertTrue(e.getMessage().contains(PositionComponent.class.getName()));
        cleanup();
    }

    private static void cleanup() {
        ECS.entities.clear();
        ECS.hero = null;
    }

    /** Tests the functionality when there are no Entities in the ECS */
    @Test
    public void interactWithClosestInteractableNoEntities() {
        ECS.hero = fullMockedHero(true);
        InteractionSystem.interactWithClosestInteractable();
        cleanup();
    }

    /**
     * Tests the functionality when there are no Entities with the interactionComponent in the ECS
     */
    @Test
    public void interactWithClosestInteractableNoInteractable() {
        ECS.hero = fullMockedHero(true);
        ECS.entities.add(ECS.hero);
        InteractionSystem.interactWithClosestInteractable();
        cleanup();
    }

    /**
     * Tests the functionality when there is exactly one Entity in the ECS with the
     * InteractionComponent and not in Radius
     */
    @Test
    public void interactWithClosestInteractableOneInteractableOutOfRange() {
        ECS.hero = fullMockedHero(true);
        ECS.entities.add(ECS.hero);

        Entity e = new Entity();
        new PositionComponent(e, new Point(10, 10));

        SimpleCounter sc_e = new SimpleCounter();
        new InteractionComponent(e, 5f, false, (x) -> sc_e.inc());

        InteractionSystem.interactWithClosestInteractable();
        assertEquals("No interaction should happen", 0, sc_e.getCount());

        cleanup();
    }

    /**
     * Tests the functionality when there is exactly one Entity in the ECS with the
     * InteractionComponent and n range
     */
    @Test
    public void interactWithClosestInteractableOneInteractableInRange() {
        ECS.hero = fullMockedHero(true);
        ECS.entities.add(ECS.hero);

        Entity e = new Entity();
        new PositionComponent(e, new Point(3, 0));

        SimpleCounter sc_e = new SimpleCounter();
        new InteractionComponent(e, 5f, false, (x) -> sc_e.inc());

        InteractionSystem.interactWithClosestInteractable();
        assertEquals("One interaction should happen", 1, sc_e.getCount());

        cleanup();
    }

    /** Test if the interactable is missing the PositionComponent */
    @Test
    public void interactWithClosestInteractableOneInteractableInRangeMissingPosition() {
        ECS.hero = fullMockedHero(true);

        ECS.entities.add(ECS.hero);

        Entity e = new Entity();

        SimpleCounter sc_e = new SimpleCounter();
        new InteractionComponent(e, 5f, false, (x) -> sc_e.inc());

        MissingComponentException exception =
                assertThrows(
                        MissingComponentException.class,
                        InteractionSystem::interactWithClosestInteractable);
        assertTrue(exception.getMessage().contains(InteractionSystem.class.getName()));
        assertTrue(exception.getMessage().contains(e.getClass().getName()));
        assertTrue(exception.getMessage().contains(PositionComponent.class.getName()));
        assertEquals("No interaction should happen", 0, sc_e.getCount());

        cleanup();
    }

    /**
     * Test if the interaction happens with the closest entity closer Entity is first in
     * ECS.entities
     */
    @Test
    public void interactWithClosestInteractableClosestEntityFirst() {
        ECS.hero = fullMockedHero(true);

        // distance 2
        Entity eClose = new Entity();
        new PositionComponent(eClose, new Point(2, 0));

        SimpleCounter sc_eClose = new SimpleCounter();
        new InteractionComponent(eClose, 5f, false, (x) -> sc_eClose.inc());

        // distance 3
        Entity eFar = new Entity();
        new PositionComponent(eFar, new Point(3, 0));

        SimpleCounter sc_eFar = new SimpleCounter();
        new InteractionComponent(eFar, 5f, false, (x) -> sc_eFar.inc());

        InteractionSystem.interactWithClosestInteractable();
        assertEquals("One interaction should happen", 1, sc_eClose.getCount());
        assertEquals("No interaction should happen", 0, sc_eFar.getCount());

        cleanup();
    }

    /**
     * Test if the interaction happens with the closest entity closer Entity is last in ECS.entities
     */
    @Test
    public void interactWithClosestInteractableClosestEntityLast() {

        ECS.hero = fullMockedHero(true);

        // distance 3
        Entity eFar = new Entity();
        PositionComponent pc2 = new PositionComponent(eFar, new Point(3, 0));

        SimpleCounter sc_eFar = new SimpleCounter();
        new InteractionComponent(eFar, 5f, false, (x) -> sc_eFar.inc());

        // distance 2
        Entity eClose = new Entity();
        PositionComponent pc = new PositionComponent(eClose, new Point(2, 0));

        SimpleCounter sc_eClose = new SimpleCounter();
        new InteractionComponent(eClose, 5f, false, (x) -> sc_eClose.inc());

        InteractionSystem.interactWithClosestInteractable();
        assertEquals("One interaction should happen", 1, sc_eClose.getCount());
        assertEquals("No interaction should happen", 0, sc_eFar.getCount());

        cleanup();
    }
}
