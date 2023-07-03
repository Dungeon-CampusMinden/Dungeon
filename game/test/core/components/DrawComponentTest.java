package core.components;

import static org.junit.Assert.*;

import core.Entity;
import core.utils.components.draw.Animation;
import core.utils.components.draw.CoreAnimations;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class DrawComponentTest {

    private DrawComponent animationComponent;

    @Before
    public void setup() throws IOException {
        animationComponent = new DrawComponent(new Entity(), "character/blue_knight");
    }

    @Test
    public void setCurrentAnimation() {
        Animation currentAnimation = animationComponent.currentAnimation();
        // Ensure that the current animation is initially set to the expected value
        assertTrue(animationComponent.isCurrentAnimation(CoreAnimations.IDLE_LEFT));
        // Set a new animation and ensure that it is correctly set
        animationComponent.currentAnimation(CoreAnimations.IDLE_RIGHT);
        assertTrue(animationComponent.isCurrentAnimation(CoreAnimations.IDLE_RIGHT));
    }

    @Test
    public void getAnimations() {
        assertTrue(animationComponent.getAnimation(CoreAnimations.RUN_LEFT).isPresent());
    }

    @Test
    public void hasAnimations() {
        assertTrue(animationComponent.hasAnimation(CoreAnimations.RUN_LEFT));
        assertFalse(animationComponent.hasAnimation(() -> "DUMMY"));
    }
}
