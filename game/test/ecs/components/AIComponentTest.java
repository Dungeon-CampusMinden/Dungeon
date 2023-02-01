package ecs.components;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import ecs.components.ai.AIComponent;
import ecs.components.ai.fight.IFightAI;
import ecs.components.ai.idle.IIdleAI;
import ecs.components.ai.transition.ITransition;
import ecs.entities.Entity;
import mydungeon.ECS;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class AIComponentTest {

    private AIComponent aiComponent;
    private IFightAI mockFightAI;
    private IIdleAI mockIdleAI;
    private ITransition mockTransition;
    private Entity entity;

    @Before
    public void setUp() {
        ECS.entities.clear();
        mockFightAI = mock(IFightAI.class);
        mockIdleAI = mock(IIdleAI.class);
        mockTransition = mock(ITransition.class);
        entity = new Entity();
        aiComponent = new AIComponent(entity, mockFightAI, mockIdleAI, mockTransition);
    }

    @Test
    public void testExecuteFight() {
        when(mockTransition.isInFightMode(entity)).thenReturn(true);
        aiComponent.execute();
        verify(mockFightAI, times(1)).fight(entity);
        verify(mockIdleAI, never()).idle(entity);
    }

    @Test
    public void testExecuteIdle() {
        when(mockTransition.isInFightMode(entity)).thenReturn(false);
        aiComponent.execute();
        verify(mockFightAI, never()).fight(entity);
        verify(mockIdleAI, times(1)).idle(entity);
    }

    @Test
    public void testSetFightAI() {
        IFightAI newAI = Mockito.mock(IFightAI.class);
        aiComponent.setFightAI(newAI);
        assertEquals(newAI, aiComponent.getFightAI());
    }

    @Test
    public void testSetIdleAI() {
        IIdleAI newAI = Mockito.mock(IIdleAI.class);
        aiComponent.setIdleAI(newAI);
        assertEquals(newAI, aiComponent.getIdleAI());
    }

    @Test
    public void testSetTransitionAI() {
        ITransition newAI = Mockito.mock(ITransition.class);
        aiComponent.setTransitionAI(newAI);
        assertEquals(newAI, aiComponent.getTransitionAI());
    }
}
