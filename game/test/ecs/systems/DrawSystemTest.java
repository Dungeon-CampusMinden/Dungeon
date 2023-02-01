package ecs.systems;

import static org.junit.Assert.assertThrows;

import ecs.components.AnimationComponent;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import graphic.Animation;
import graphic.Painter;
import mydungeon.ECS;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import tools.Point;

public class DrawSystemTest {

    private final Animation animation = Mockito.mock(Animation.class);
    private final Painter painter = Mockito.mock(Painter.class);
    private DrawSystem drawSystem;
    private Entity entity;

    @Before
    public void setup() {
        ECS.systems = Mockito.mock(SystemController.class);
        ECS.entities.clear();
        drawSystem = new DrawSystem(painter);
        entity = new Entity();
        entity.addComponent(AnimationComponent.name, new AnimationComponent(entity, animation));
        entity.addComponent(PositionComponent.name, new PositionComponent(entity, new Point(3, 3)));
    }

    @Test
    public void testUpdate() {
        /*
         * This method can not be tested because we can not mock the internal PainterConfig Object
         * in the DrawSystem. The PainterConfig needs libGDX setup
         */
    }

    @Test
    public void testUpdateWithoutPositionComponent() {
        entity.removeComponent(PositionComponent.name);
        Mockito.verifyNoMoreInteractions(painter);
        assertThrows(
                MissingComponentException.class,
                () -> {
                    drawSystem.update();
                });
    }

    @Test
    public void testUpdateWithoutAnimationComponent() {
        entity.removeComponent(AnimationComponent.name);
        Mockito.verifyNoMoreInteractions(painter);
        drawSystem.update();
    }
}
