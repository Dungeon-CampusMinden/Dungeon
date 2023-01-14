package ecs.components;

import static org.junit.Assert.*;

import ecs.entities.Entity;
import org.junit.Before;
import org.junit.Test;

public class PlayableComponentTest {

    private Entity entity;

    @Before
    public void setup() {
        entity = new Entity();
    }

    @Test
    public void testIsPlayable() {
        PlayableComponent component = new PlayableComponent(entity);
        assertTrue(component.isPlayable());

        component.setPlayable(false);
        assertFalse(component.isPlayable());
    }
}
