package core.components;

import static org.junit.Assert.assertEquals;

import core.Entity;
import core.utils.components.draw.Animation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DrawComponentTest {

    private final Animation idleLeft = Mockito.mock(Animation.class);
    private final Animation idleRight = Mockito.mock(Animation.class);
    private DrawComponent animationComponent;

    @Before
    public void setup() {
        animationComponent = new DrawComponent(new Entity(), idleLeft, idleRight);
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
