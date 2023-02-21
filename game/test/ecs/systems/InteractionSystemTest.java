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
import org.mockito.Mockito;
import tools.Point;

public class InteractionSystemTest {

    /** Tests the functionality when the Hero does not have the PositionComponent */
    @Test
    public void interactWithClosestInteractableHeroMissingPositionComponent() {
        ECS.hero = Mockito.mock(Hero.class);

        Entity e2 = new Entity();
        Optional<Component> opt = Optional.empty();
        Mockito.when(ECS.hero.getComponent(PositionComponent.class)).thenReturn(opt);
        MissingComponentException e =
                assertThrows(
                        MissingComponentException.class,
                        InteractionSystem::interactWithClosestInteractable);
        assertTrue(e.getMessage().contains(InteractionSystem.class.getName()));
        assertTrue(e.getMessage().contains(Hero.class.getName()));
        assertTrue(e.getMessage().contains(PositionComponent.class.getName()));
        ECS.hero = null;
    }

    /** Tests the functionality when there are no Entities in the ECS */
    @Test
    public void interactWithClosestInteractableNoEntities() {
        ECS.hero = Mockito.mock(Hero.class);
        Entity e2 = new Entity();
        Optional<Component> opt = Optional.of(new PositionComponent(e2, new Point(0, 0)));
        Mockito.when(ECS.hero.getComponent(PositionComponent.class)).thenReturn(opt);

        InteractionSystem.interactWithClosestInteractable();
        ECS.hero = null;
    }

    /**
     * Tests the functionality when there are no Entities with the interactionComponent in the ECS
     */
    @Test
    public void interactWithClosestInteractableNoInteractable() {
        ECS.hero = Mockito.mock(Hero.class);
        Optional<Component> opt = Optional.of(new PositionComponent(ECS.hero, new Point(0, 0)));
        Mockito.when(ECS.hero.getComponent(PositionComponent.class)).thenReturn(opt);
        ECS.entities.add(ECS.hero);
        InteractionSystem.interactWithClosestInteractable();
        ECS.entities.clear();
        ECS.hero = null;
    }

    /**
     * Tests the functionality when there is exactly one Entity in the ECS with the
     * InteractionComponent and not in Radius
     */
    @Test
    public void interactWithClosestInteractableOneInteractableOutOfRange() {
        ECS.hero = Mockito.mock(Hero.class);
        Optional<Component> opt = Optional.of(new PositionComponent(ECS.hero, new Point(0, 0)));
        Mockito.when(ECS.hero.getComponent(PositionComponent.class)).thenReturn(opt);
        ECS.entities.add(ECS.hero);

        Entity e = new Entity();
        new PositionComponent(e, new Point(10, 10));
        InteractionComponent interactionComponent = Mockito.mock(InteractionComponent.class);
        when(interactionComponent.getRadius()).thenReturn(5f);
        e.addComponent(interactionComponent);
        ECS.entities.add(e);

        InteractionSystem.interactWithClosestInteractable();
        verify(interactionComponent, never()).triggerInteraction();

        ECS.entities.clear();
        ECS.hero = null;
    }

    /**
     * Tests the functionality when there is exactly one Entity in the ECS with the
     * InteractionComponent and n range
     */
    @Test
    public void interactWithClosestInteractableOneInteractableInRange() {
        ECS.hero = Mockito.mock(Hero.class);
        Optional<Component> opt = Optional.of(new PositionComponent(ECS.hero, new Point(0, 0)));
        Mockito.when(ECS.hero.getComponent(PositionComponent.class)).thenReturn(opt);
        ECS.entities.add(ECS.hero);

        Entity e = Mockito.mock(Entity.class);

        PositionComponent pc = new PositionComponent(e, new Point(2, 0));
        when(e.getComponent(PositionComponent.class)).thenReturn(Optional.of(pc));

        InteractionComponent interactionComponent = Mockito.mock(InteractionComponent.class);
        when(interactionComponent.getRadius()).thenReturn(5f);
        when(e.getComponent(InteractionComponent.class))
                .thenReturn(Optional.of(interactionComponent));

        ECS.entities.add(e);

        InteractionSystem.interactWithClosestInteractable();
        verify(interactionComponent).triggerInteraction();

        ECS.entities.clear();
        ECS.hero = null;
    }

    /** Test if the interactable is missing the PositionComponent */
    @Test
    public void interactWithClosestInteractableOneInteractableInRangeMissingPosition() {
        ECS.hero = Mockito.mock(Hero.class);
        Optional<Component> opt = Optional.of(new PositionComponent(ECS.hero, new Point(0, 0)));
        Mockito.when(ECS.hero.getComponent(PositionComponent.class)).thenReturn(opt);
        ECS.entities.add(ECS.hero);

        Entity e = Mockito.mock(Entity.class);

        when(e.getComponent(PositionComponent.class)).thenReturn(Optional.empty());

        InteractionComponent interactionComponent = Mockito.mock(InteractionComponent.class);
        when(interactionComponent.getRadius()).thenReturn(5f);
        when(e.getComponent(InteractionComponent.class))
                .thenReturn(Optional.of(interactionComponent));

        ECS.entities.add(e);

        MissingComponentException exception =
                assertThrows(
                        MissingComponentException.class,
                        InteractionSystem::interactWithClosestInteractable);
        assertTrue(exception.getMessage().contains(InteractionSystem.class.getName()));
        assertTrue(exception.getMessage().contains(e.getClass().getName()));
        assertTrue(exception.getMessage().contains(PositionComponent.class.getName()));
        verify(interactionComponent, never()).triggerInteraction();

        ECS.entities.clear();
        ECS.hero = null;
    }

    /**
     * Test if the interaction happens with the closest entity closer Entity is first in
     * ECS.entities
     */
    @Test
    public void interactWithClosestInteractableClosestEntityFirst() {
        ECS.hero = Mockito.mock(Hero.class);
        Optional<Component> opt = Optional.of(new PositionComponent(ECS.hero, new Point(0, 0)));
        Mockito.when(ECS.hero.getComponent(PositionComponent.class)).thenReturn(opt);

        // distance 2
        Entity eClose = Mockito.mock(Entity.class);
        PositionComponent pc = new PositionComponent(eClose, new Point(2, 0));
        when(eClose.getComponent(PositionComponent.class)).thenReturn(Optional.of(pc));

        InteractionComponent ic1 = Mockito.mock(InteractionComponent.class);
        when(ic1.getRadius()).thenReturn(5f);
        when(eClose.getComponent(InteractionComponent.class)).thenReturn(Optional.of(ic1));

        ECS.entities.add(eClose);

        // distance 3
        Entity eFar = Mockito.mock(Entity.class);
        PositionComponent pc2 = new PositionComponent(eFar, new Point(3, 0));
        when(eFar.getComponent(PositionComponent.class)).thenReturn(Optional.of(pc2));

        InteractionComponent ic2 = Mockito.mock(InteractionComponent.class);
        when(ic2.getRadius()).thenReturn(5f);
        when(eFar.getComponent(InteractionComponent.class)).thenReturn(Optional.of(ic2));

        ECS.entities.add(eFar);

        InteractionSystem.interactWithClosestInteractable();
        verify(ic1).triggerInteraction();
        verify(ic2, never()).triggerInteraction();

        ECS.entities.clear();
        ECS.hero = null;
    }

    /**
     * Test if the interaction happens with the closest entity closer Entity is last in ECS.entities
     */
    @Test
    public void interactWithClosestInteractableClosestEntityLast() {

        ECS.hero = Mockito.mock(Hero.class);
        Optional<Component> opt = Optional.of(new PositionComponent(ECS.hero, new Point(0, 0)));
        Mockito.when(ECS.hero.getComponent(PositionComponent.class)).thenReturn(opt);

        // distance 3
        Entity eFar = Mockito.mock(Entity.class);
        PositionComponent pc2 = new PositionComponent(eFar, new Point(3, 0));
        when(eFar.getComponent(PositionComponent.class)).thenReturn(Optional.of(pc2));

        InteractionComponent ic2 = Mockito.mock(InteractionComponent.class);
        when(ic2.getRadius()).thenReturn(5f);
        when(eFar.getComponent(InteractionComponent.class)).thenReturn(Optional.of(ic2));

        ECS.entities.add(eFar);

        // distance 2
        Entity eClose = Mockito.mock(Entity.class);
        PositionComponent pc = new PositionComponent(eClose, new Point(2, 0));
        when(eClose.getComponent(PositionComponent.class)).thenReturn(Optional.of(pc));

        InteractionComponent ic1 = Mockito.mock(InteractionComponent.class);
        when(ic1.getRadius()).thenReturn(5f);
        when(eClose.getComponent(InteractionComponent.class)).thenReturn(Optional.of(ic1));

        ECS.entities.add(eClose);

        InteractionSystem.interactWithClosestInteractable();
        verify(ic1).triggerInteraction();
        verify(ic2, never()).triggerInteraction();

        ECS.entities.clear();
        ECS.hero = null;
    }
}
