package ecs.components;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import ecs.entities.Entity;
import org.junit.Test;
import org.mockito.Mockito;

public class InteractionComponentTest {

    @Test
    public void createSimpleConstructor() {
        Entity e = new Entity();
        InteractionComponent component = new InteractionComponent(e);
        assertEquals(InteractionComponent.DEFAULT_RADIUS, component.getRadius(), 0.0001);
        assertEquals(InteractionComponent.DEFAULT_REPEATABLE, component.isRepeatable());
    }

    @Test
    public void createComplexConstructor() {
        Entity e = new Entity();
        float radius = 100;
        boolean repeat = true;
        IInteraction iInteraction = Mockito.mock(IInteraction.class);

        InteractionComponent component = new InteractionComponent(e, radius, repeat, iInteraction);

        assertEquals(radius, component.getRadius(), 0.0001);
        assertEquals(repeat, component.isRepeatable());
    }

    @Test
    public void triggerInteractionOnLinkedEntity() {
        IInteraction iInteraction = Mockito.mock(IInteraction.class);
        Entity e = new Entity();
        InteractionComponent component = new InteractionComponent(e, 1, true, iInteraction);
        component.triggerInteraction();
        verify(iInteraction).onInteraction(e);
    }

    @Test
    public void triggerInteractionOnLinkedEntityRemovesComponent() {
        IInteraction iInteraction = Mockito.mock(IInteraction.class);
        Entity e = new Entity();
        InteractionComponent component = new InteractionComponent(e, 1, true, iInteraction);
        component.triggerInteraction();
        verify(iInteraction).onInteraction(e);
        assertFalse(e.getComponent(InteractionComponent.class).isPresent());
    }

    @Test
    public void triggerInteractionNonLinkedEntity() {
        IInteraction iInteraction = Mockito.mock(IInteraction.class);
        IInteraction iInteraction2 = Mockito.mock(IInteraction.class);
        Entity e = new Entity();
        Entity e2 = new Entity();
        InteractionComponent component = new InteractionComponent(e, 1, true, iInteraction);
        InteractionComponent component2 = new InteractionComponent(e, 1, true, iInteraction2);
        component.triggerInteraction();
        verify(iInteraction2, never()).onInteraction(e);
    }

    @Test
    public void triggerInteractionNonLinkedEntityComponentNotRemoved() {
        IInteraction iInteraction = Mockito.mock(IInteraction.class);
        IInteraction iInteraction2 = Mockito.mock(IInteraction.class);
        Entity e = new Entity();
        Entity e2 = new Entity();
        InteractionComponent component = new InteractionComponent(e, 1, false, iInteraction);
        InteractionComponent component2 = new InteractionComponent(e, 1, false, iInteraction2);
        component.triggerInteraction();
        assertTrue(e2.getComponent(InteractionComponent.class).isPresent());
    }
}
