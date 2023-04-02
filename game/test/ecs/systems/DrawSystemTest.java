package ecs.systems;

import static org.junit.Assert.assertThrows;

import controller.SystemController;
import ecs.components.AnimationComponent;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import graphic.Animation;
import graphic.Painter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import starter.Game;
import tools.Point;

public class DrawSystemTest {

    private final Animation animation = Mockito.mock(Animation.class);
    private final Painter painter = Mockito.mock(Painter.class);
    private DrawSystem drawSystem;
    private Entity entity;

    @Before
    public void setup() {
        Game.systems = Mockito.mock(SystemController.class);
        Game.getEntities().clear();
        Game.getEntitiesToAdd().clear();
        Game.getEntitiesToRemove().clear();
        drawSystem = new DrawSystem(painter);
        entity = new Entity();
        new AnimationComponent(entity, animation);
        new PositionComponent(entity, new Point(3, 3));
        Game.getEntities().addAll(Game.getEntitiesToAdd());
        Game.getEntitiesToAdd().clear();
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
