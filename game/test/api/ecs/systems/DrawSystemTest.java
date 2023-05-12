package api.ecs.systems;

import static org.junit.Assert.assertThrows;

import api.controller.SystemController;
import api.ecs.components.AnimationComponent;
import api.ecs.components.MissingComponentException;
import api.ecs.components.PositionComponent;
import api.ecs.entities.Entity;
import api.graphic.Animation;
import api.graphic.Painter;
import api.tools.Point;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import starter.Game;

public class DrawSystemTest {

    private final Animation animation = Mockito.mock(Animation.class);
    private final Painter painter = Mockito.mock(Painter.class);
    private DrawSystem drawSystem;
    private Entity entity;

    @Before
    public void setup() {
        Game.systems = Mockito.mock(SystemController.class);
        Game.getDelayedEntitySet().clear();
        drawSystem = new DrawSystem(painter);
        entity = new Entity();
        new AnimationComponent(entity, animation);
        new PositionComponent(entity, new Point(3, 3));
        Game.getDelayedEntitySet().update();
    }

    @Test
    public void update() {
        /*
         * This method can not be tested because we can not mock the internal PainterConfig Object
         * in the DrawSystem. The PainterConfig needs libGDX setup
         */
    }

    @Test
    public void updateWithoutPositionComponent() {
        entity.removeComponent(PositionComponent.class);
        Mockito.verifyNoMoreInteractions(painter);
        assertThrows(MissingComponentException.class, () -> drawSystem.update());
    }

    @Test
    public void updateWithoutAnimationComponent() {
        entity.removeComponent(AnimationComponent.class);
        Mockito.verifyNoMoreInteractions(painter);
        drawSystem.update();
    }
}
