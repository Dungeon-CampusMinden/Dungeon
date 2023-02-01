package ecs.components;

import static org.junit.Assert.*;

import ecs.entities.Entity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class PlayableComponentTest {

    private PlayableComponent playableComponent;

    @Before
    public void setup() {
        playableComponent = new PlayableComponent(Mockito.mock(Entity.class));
    }

    @Test
    public void isPlayable() {
        assertTrue(playableComponent.isPlayable());
        playableComponent.setPlayable(false);
        assertFalse(playableComponent.isPlayable());
    }
}
