package ecs.systems;

import ecs.components.AnimationComponent;
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
    private DrawSystem system;
    private Entity entity;

    @Before
    public void setup() {
        ECS.systems = new SystemController();
        system = new DrawSystem(painter);
        entity = new Entity();
        entity.addComponent(PositionComponent.name, new PositionComponent(entity, new Point(3, 3)));
        entity.addComponent(AnimationComponent.name, new AnimationComponent(entity, animation));
    }

    @Test
    public void testUpdate() {
        /*
         * This method can not be tested because we can not mock the internal PainterConfig Object
         * in the DrawSystem. The PainterConfig needs libGDX setup
         */
    }

    @Test
    public void testUpdateWithoutPoisitionComponent() {
        entity.removeComponent(PositionComponent.name);
        Mockito.verifyNoMoreInteractions(painter);
        system.update();
    }

    @Test
    public void testUpdateWithoutAnimationComponent() {
        entity.removeComponent(AnimationComponent.name);
        Mockito.verifyNoMoreInteractions(painter);
        system.update();
    }
}
