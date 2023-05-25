package core.components;

import static org.junit.Assert.assertEquals;

import core.Entity;

import org.junit.Before;
import org.junit.Test;

public class VelocityComponentTest {

    private final float xVelocityAtStart = 3f;
    private final float yVelocityAtStart = 3f;
    private VelocityComponent velocityComponent;

    @Before
    public void setup() {
        velocityComponent = new VelocityComponent(new Entity(), xVelocityAtStart, yVelocityAtStart);
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
}
