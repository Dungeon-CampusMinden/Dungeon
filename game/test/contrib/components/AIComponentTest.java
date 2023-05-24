package contrib.components;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import core.Entity;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.function.Consumer;
import java.util.function.Function;

public class AIComponentTest {

    private AIComponent aiComponent;
    private final Consumer<Entity> mockFightAI = mock(Consumer.class);

    private final Consumer<Entity> mockIdleAI = mock(Consumer.class);

    private final Function<Entity, Boolean> mockTransition = mock(Function.class);

    private final Entity entity = new Entity();

    @Before
    public void setup() {
        aiComponent = new AIComponent(entity, mockFightAI, mockIdleAI, mockTransition);
    }

    @Test
    public void executeFight() {
        when(mockTransition.apply(entity)).thenReturn(true);
        aiComponent.execute();
        verify(mockFightAI, times(1)).accept(entity);
        verify(mockIdleAI, never()).accept(entity);
    }

    @Test
    public void executeIdle() {
        when(mockTransition.apply(entity)).thenReturn(false);
        aiComponent.execute();
        verify(mockFightAI, never()).accept(entity);
        verify(mockIdleAI, times(1)).accept(entity);
    }

    @Test
    public void setFightAI() {
        Consumer<Entity> newAI = Mockito.mock(Consumer.class);
        aiComponent.setFightAI(newAI);
        assertEquals(newAI, aiComponent.getFightAI());
    }

    @Test
    public void setIdleAI() {
        Consumer<Entity> newAI = Mockito.mock(Consumer.class);
        aiComponent.setIdleAI(newAI);
        assertEquals(newAI, aiComponent.getIdleAI());
    }

    @Test
    public void setTransitionAI() {
        Function<Entity, Boolean> newAI = Mockito.mock(Function.class);
        aiComponent.setTransitionAI(newAI);
        assertEquals(newAI, aiComponent.getTransitionAI());
    }
}
