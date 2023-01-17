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
        component =
                new VelocityComponent(new Entity(), 1.0f, 2.0f, 3.0f, 4.0f, moveLeft, moveRight);
    }

    @Test
    public void testSetX() {
        assertEquals(1.0f, component.getX(), 0.001);
        component.setX(5.0f);
        assertEquals(5.0f, component.getX(), 0.001);
    }

    @Test
    public void testSetY() {
        assertEquals(2.0f, component.getY(), 0.001);
        component.setY(6.0f);
        assertEquals(6.0f, component.getY(), 0.001);
    }

    @Test
    public void testSetXSpeed() {
        assertEquals(3.0f, component.getxSpeed(), 0.001);
        component.setxSpeed(6.0f);
        assertEquals(6.0f, component.getxSpeed(), 0.001);
    }

    @Test
    public void testSetYSpeed() {
        assertEquals(4.0f, component.getySpeed(), 0.001);
        component.setySpeed(6.0f);
        assertEquals(6.0f, component.getySpeed(), 0.001);
    }

    @Test
    public void testGetAnimation() {
        assertEquals(moveLeft, component.getMoveLeftAnimation());
        assertEquals(moveRight, component.getMoveRightAnimation());
    }
}
