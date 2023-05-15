package core.systems;

import static org.junit.Assert.assertThrows;

import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;
import core.utils.components.draw.Painter;
import core.utils.controller.SystemController;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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
        new DrawComponent(entity, animation);
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
        entity.removeComponent(DrawComponent.class);
        Mockito.verifyNoMoreInteractions(painter);
        drawSystem.update();
    }
}
