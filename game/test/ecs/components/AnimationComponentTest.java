package ecs.components;

import static org.junit.Assert.assertEquals;

import ecs.components.animation.AnimationComponent;
import ecs.components.animation.IOnAnimationEnd;
import ecs.entities.Entity;
import graphic.Animation;
import java.util.concurrent.atomic.AtomicInteger;
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

    /** Test that the current animation is correctly set and retrieved */
    @Test
    public void setCurrentAnimation() {
        Animation currentAnimation = animationComponent.getCurrentAnimation();
        // Ensure that the current animation is initially set to the expected value
        assertEquals(currentAnimation, animationComponent.getCurrentAnimation());

        // Set a new animation and ensure that it is correctly set
        animationComponent.setCurrentAnimation(idleLeft);
        assertEquals(idleLeft, animationComponent.getCurrentAnimation());
    }

    /** Test that the idle animations are correctly set and retrieved */
    @Test
    public void getAnimations() {
        assertEquals(idleLeft, animationComponent.getIdleLeft());
        assertEquals(idleRight, animationComponent.getIdleRight());
    }

    /** Test that the OnAnimationEnd is correctly set and retrieved */
    @Test
    public void setOnAnimationEnd() {
        IOnAnimationEnd onAnimationEnd = () -> {};
        animationComponent.setOnAnimationEnd(onAnimationEnd);
        assertEquals(onAnimationEnd, animationComponent.getOnAnimationEnd());
    }

    /** Test that the OnAnimationEnd is correctly triggered and only triggered once. */
    @Test
    public void triggerOnAnimationEnd() {
        AtomicInteger count = new AtomicInteger();
        IOnAnimationEnd onAnimationEnd = count::getAndIncrement;
        animationComponent.setOnAnimationEnd(onAnimationEnd);
        animationComponent.triggerOnAnimationEnd();
        assertEquals("OnAnimationEnd should be triggered once.", 1, count.get());
        animationComponent.triggerOnAnimationEnd();
        assertEquals("OnAnimationEnd should only be triggered once.", 1, count.get());
    }

    /** Test that the OnAnimationEnd is correctly triggered and only triggered once until reset. */
    @Test
    public void resetTriggerOnAnimationEnd() {
        AtomicInteger count = new AtomicInteger();
        IOnAnimationEnd onAnimationEnd = count::getAndIncrement;
        animationComponent.setOnAnimationEnd(onAnimationEnd);
        animationComponent.triggerOnAnimationEnd();
        assertEquals("OnAnimationEnd should be triggered once.", 1, count.get());
        animationComponent.resetTriggerOnAnimationEnd();
        animationComponent.triggerOnAnimationEnd();
        assertEquals("OnAnimationEnd should be triggered twice. Due to reset.", 2, count.get());
    }
}
