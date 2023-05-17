package core.systems;

import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
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
        Game.removeAllEntities();
        Game.systems = new SystemController();
        drawSystem = new DrawSystem(painter);
        entity = new Entity();
        new DrawComponent(entity, animation);
        new PositionComponent(entity, new Point(3, 3));
    }

    @Test
    public void update() {
        /*
         * This method can not be tested because we can not mock the internal PainterConfig Object
         * in the DrawSystem. The PainterConfig needs libGDX setup
         */
    }

    @Test
    public void updateWithoutDrawComponent() {
        entity.removeComponent(DrawComponent.class);
        Mockito.verifyNoMoreInteractions(painter);
        drawSystem.update();
        Game.removeAllEntities();
    }
}
