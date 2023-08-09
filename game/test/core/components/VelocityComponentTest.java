package core.components;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class VelocityComponentTest {

    private final float xVelocityAtStart = 3f;
    private final float yVelocityAtStart = 3f;
    private VelocityComponent velocityComponent;

    @Before
    public void setup() {
        velocityComponent = new VelocityComponent(xVelocityAtStart, yVelocityAtStart);
    }

    @Test
    public void setCurrentXVelocity() {
        velocityComponent.currentXVelocity(5.0f);
        assertEquals(5.0f, velocityComponent.currentXVelocity(), 0.001);
    }

    @Test
    public void setCurrentYVelocity() {
        velocityComponent.currentYVelocity(6.0f);
        assertEquals(6.0f, velocityComponent.currentYVelocity(), 0.001);
    }

    @Test
    public void setXVelocity() {
        assertEquals(xVelocityAtStart, velocityComponent.xVelocity(), 0.001);
        velocityComponent.xVelocity(6.0f);
        assertEquals(6.0f, velocityComponent.xVelocity(), 0.001);
    }

    @Test
    public void setYVelocity() {
        assertEquals(yVelocityAtStart, velocityComponent.yVelocity(), 0.001);
        velocityComponent.yVelocity(6.0f);
        assertEquals(6.0f, velocityComponent.yVelocity(), 0.001);
    }
}
