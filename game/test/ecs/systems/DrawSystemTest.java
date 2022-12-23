package ecs.systems;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.entitys.Entity;
import graphic.Animation;
import graphic.Painter;
import java.util.Arrays;
import java.util.HashMap;
import mydungeon.ECS;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import tools.Point;

public class DrawSystemTest {

    private final Animation animation = Mockito.mock(Animation.class);
    private Entity entity;
    private Painter painter = Mockito.mock(Painter.class);
    private PositionComponent positionComponent;
    private AnimationComponent animationComponent;

    @Before
    public void setup() {
        ECS.systems = new SystemController();
        ECS.positionComponentMap = new HashMap<>();
        ECS.animationComponentMap = new HashMap<>();
        entity = new Entity();
        positionComponent = new PositionComponent(entity, new Point(3, 3));
        new AnimationComponent(entity, Arrays.asList(animation), animation);
    }

    @Test
    public void constructorTest() {
        DrawSystem system = new DrawSystem(painter);
        assertNotNull(system);
        assertTrue(ECS.systems.contains(system));
    }

    @Test
    public void testUpdate() {
        /**
         * This method can not be tested because we can not mock the internal PainterConfig Object
         * in the DrawSystem. The PainterConfig needs libGDX setup
         */
    }

    @Test
    public void testUpdateWithoutPoisitionComponent() {
        ECS.positionComponentMap = new HashMap<>();
        DrawSystem system = new DrawSystem(painter);
        Mockito.verifyNoMoreInteractions(painter);
        system.update();
    }
}
