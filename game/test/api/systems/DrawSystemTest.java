package api.systems;

import static org.junit.Assert.assertThrows;

import api.Entity;
import api.Game;
import api.components.DrawComponent;
import api.components.PositionComponent;
import api.utils.Point;
import api.utils.component_utils.MissingComponentException;
import api.utils.component_utils.drawComponent.Animation;
import api.utils.component_utils.drawComponent.Painter;
import api.utils.controller.SystemController;
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
