package contrib.components;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import core.Entity;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.function.BiConsumer;

public class InteractionComponentTest {

    /** Tests if the simple ctor sets the attributes to the default values */
    @Test
    public void createSimpleConstructor() {
        Entity e = new Entity();
        InteractionComponent component = new InteractionComponent(e);
        assertEquals(InteractionComponent.DEFAULT_INTERACTION_RADIUS, component.radius(), 0.0001);
    }

    /** Tests if the complex Constructor sets the attributes to the parameter */
    @Test
    public void createComplexConstructor() {
        Entity e = new Entity();
        float radius = 100;
        boolean repeat = true;
        BiConsumer<Entity, Entity> iInteraction = Mockito.mock(BiConsumer.class);

        InteractionComponent component = new InteractionComponent(e, radius, repeat, iInteraction);

        assertEquals(radius, component.radius(), 0.0001);
    }

    /** Checks if the iInteraction is called on triggerInteraction */
    @Test
    public void triggerInteractionOnLinkedEntity() {
        BiConsumer<Entity, Entity> iInteraction = Mockito.mock(BiConsumer.class);
        Entity e = new Entity();
        InteractionComponent component = new InteractionComponent(e, 1, true, iInteraction);
        component.triggerInteraction(null);
        verify(iInteraction).accept(e, null);
        assertTrue(e.fetch(InteractionComponent.class).isPresent());
    }

    /** Checks if after the interaction the component gets removed */
    @Test
    public void triggerInteractionOnLinkedEntityRemovesComponent() {
        BiConsumer<Entity, Entity> iInteraction = Mockito.mock(BiConsumer.class);
        Entity e = new Entity();
        InteractionComponent component = new InteractionComponent(e, 1, false, iInteraction);
        component.triggerInteraction(null);
        verify(iInteraction).accept(e, null);
        assertFalse(e.fetch(InteractionComponent.class).isPresent());
    }

    /** Checks that the interaction only gets triggered for the linked iInteraction */
    @Test
    public void triggerInteractionNonLinkedEntity() {
        BiConsumer<Entity, Entity> iInteraction = Mockito.mock(BiConsumer.class);
        BiConsumer<Entity, Entity> iInteraction2 = Mockito.mock(BiConsumer.class);
        Entity e = new Entity();
        Entity e2 = new Entity();
        InteractionComponent component = new InteractionComponent(e, 1, true, iInteraction);
        InteractionComponent component2 = new InteractionComponent(e2, 1, true, iInteraction2);
        component.triggerInteraction(null);
        verify(iInteraction2, never()).accept(e, null);
    }

    /** Checks that the Component does not ge removed when the interaction was not triggered */
    @Test
    public void triggerInteractionNonLinkedEntityComponentNotRemoved() {
        BiConsumer<Entity, Entity> iInteraction = Mockito.mock(BiConsumer.class);
        BiConsumer<Entity, Entity> iInteraction2 = Mockito.mock(BiConsumer.class);
        Entity e = new Entity();
        Entity e2 = new Entity();
        InteractionComponent component = new InteractionComponent(e, 1, false, iInteraction);
        InteractionComponent component2 = new InteractionComponent(e2, 1, false, iInteraction2);
        component.triggerInteraction(null);
        assertTrue(e2.fetch(InteractionComponent.class).isPresent());
    }
}
