package ecs.components;

import static org.junit.Assert.assertEquals;

import ecs.entities.Entity;
import graphic.Animation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class AnimationComponentTest {

    private final Animation idleLeft = Mockito.mock(Animation.class);
    private final Animation idleRight = Mockito.mock(Animation.class);
    private Entity entity;
    private AnimationComponent component;

    @Before
    public void setup() {
        entity = new Entity();
        component = new AnimationComponent(entity, idleLeft, idleRight);
    }

    @Test
    public void testSetCurrentAnimation() {
        Animation currentAnimation = component.getCurrentAnimation();
        // Ensure that the current animation is initially set to the expected value
        assertEquals(currentAnimation, component.getCurrentAnimation());

        // Set a new animation and ensure that it is correctly set
        component.setCurrentAnimation(idleLeft);
        assertEquals(idleLeft, component.getCurrentAnimation());
    }

    @Test
    public void testGetAnimations() {
        assertEquals(idleLeft, component.getIdleLeft());
        assertEquals(idleRight, component.getIdleRight());
    }
}
