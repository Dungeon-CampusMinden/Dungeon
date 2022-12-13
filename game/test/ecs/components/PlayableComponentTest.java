package ecs.components;

import static org.junit.Assert.*;

import ecs.entitys.Entity;
import java.util.HashMap;
import mydungeon.ECS;
import org.junit.Before;
import org.junit.Test;

public class PlayableComponentTest {

    private Entity entity;

    @Before
    public void setup() {
        ECS.playableComponentMap = new HashMap<>();
        entity = new Entity();
    }

    @Test
    public void testConstructor() {
        PlayableComponent component = new PlayableComponent(entity);
        assertNotNull(component);
        assertNotNull(ECS.playableComponentMap.get(entity));
    }

    @Test
    public void testIsPlayable() {
        PlayableComponent component = new PlayableComponent(entity);
        assertTrue(component.isPlayable());

        component.setPlayable(false);
        assertFalse(component.isPlayable());
    }
}
