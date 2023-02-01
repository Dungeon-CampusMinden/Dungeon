package ecs.components;

import static org.junit.Assert.assertEquals;

import ecs.entities.Entity;
import graphic.Animation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class VelocityComponentTest {

    private final Animation moveLeft = Mockito.mock(Animation.class);
    private final Animation moveRight = Mockito.mock(Animation.class);

    private final float xVelocityAtStart = 3f;
    private final float yVelocityAtStart = 3f;
    private VelocityComponent velocityComponent;

    @Before
    public void setup() {
        velocityComponent =
                new VelocityComponent(
                        Mockito.mock(Entity.class),
                        xVelocityAtStart,
                        yVelocityAtStart,
                        moveLeft,
                        moveRight);
    }

    @Test
    public void setCurrentXVelocity() {
        velocityComponent.setCurrentXVelocity(5.0f);
        assertEquals(5.0f, velocityComponent.getCurrentXVelocity(), 0.001);
    }

    @Test
    public void setCurrentYVelocity() {
        velocityComponent.setCurrentYVelocity(6.0f);
        assertEquals(6.0f, velocityComponent.getCurrentYVelocity(), 0.001);
    }

    @Test
    public void setXVelocity() {
        assertEquals(xVelocityAtStart, velocityComponent.getXVelocity(), 0.001);
        velocityComponent.setXVelocity(6.0f);
        assertEquals(6.0f, velocityComponent.getXVelocity(), 0.001);
    }

    @Test
    public void setYVelocity() {
        assertEquals(yVelocityAtStart, velocityComponent.getYVelocity(), 0.001);
        velocityComponent.setYVelocity(6.0f);
        assertEquals(6.0f, velocityComponent.getYVelocity(), 0.001);
    }

    @Test
    public void getAnimation() {
        assertEquals(moveLeft, velocityComponent.getMoveLeftAnimation());
        assertEquals(moveRight, velocityComponent.getMoveRightAnimation());
    }
}
