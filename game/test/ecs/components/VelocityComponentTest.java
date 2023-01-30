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
    private VelocityComponent component;

    @Before
    public void setup() {
        component = new VelocityComponent(new Entity(), 3.0f, 4.0f, moveLeft, moveRight);
    }

    @Test
    public void testSetX() {
        component.setCurrentXVelocity(5.0f);
        assertEquals(5.0f, component.getCurrentXVelocity(), 0.001);
    }

    @Test
    public void testSetY() {
        component.setCurrentYVelocity(6.0f);
        assertEquals(6.0f, component.getCurrentYVelocity(), 0.001);
    }

    @Test
    public void testSetXSpeed() {
        assertEquals(3.0f, component.getXVelocity(), 0.001);
        component.setXVelocity(6.0f);
        assertEquals(6.0f, component.getXVelocity(), 0.001);
    }

    @Test
    public void testSetYSpeed() {
        assertEquals(4.0f, component.getYVelocity(), 0.001);
        component.setYVelocity(6.0f);
        assertEquals(6.0f, component.getYVelocity(), 0.001);
    }

    @Test
    public void testGetAnimation() {
        assertEquals(moveLeft, component.getMoveLeftAnimation());
        assertEquals(moveRight, component.getMoveRightAnimation());
    }
}
