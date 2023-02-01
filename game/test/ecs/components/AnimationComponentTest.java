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
    private AnimationComponent animationComponent;

    @Before
    public void setup() {
        animationComponent =
                new AnimationComponent(Mockito.mock(Entity.class), idleLeft, idleRight);
    }

    @Test
    public void setCurrentAnimation() {
        Animation currentAnimation = animationComponent.getCurrentAnimation();
        // Ensure that the current animation is initially set to the expected value
        assertEquals(currentAnimation, animationComponent.getCurrentAnimation());

        // Set a new animation and ensure that it is correctly set
        animationComponent.setCurrentAnimation(idleLeft);
        assertEquals(idleLeft, animationComponent.getCurrentAnimation());
    }

    @Test
    public void getAnimations() {
        assertEquals(idleLeft, animationComponent.getIdleLeft());
        assertEquals(idleRight, animationComponent.getIdleRight());
    }
}
