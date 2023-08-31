package core.systems;

import static org.junit.Assert.*;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.elements.tile.FloorTile;
import core.level.utils.LevelElement;
import core.utils.Point;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class PositionSystemTest {

    private PositionSystem system;
    private Tile mock = Mockito.mock(FloorTile.class);
    private Point point = new Point(3, 3);
    private ILevel level = Mockito.mock(ILevel.class);
    private Entity entity;
    private PositionComponent pc;

    @Before
    public void setup() {
        pc = new PositionComponent();
        Game.add(new LevelSystem(null, null, () -> {}));
        Game.currentLevel(level);
        system = new PositionSystem();
        Game.add(system);
        entity = new Entity();
        Game.add(entity);

        entity.addComponent(pc);

        Mockito.when(level.randomTile(LevelElement.FLOOR)).thenReturn(mock);
        Mockito.when(mock.position()).thenReturn(point);
    }

    @After
    public void cleanup() {
        Game.removeAllSystems();
        Game.removeAllEntities();
        Game.currentLevel(null);
    }

    @Test
    public void test_illegalPosition() {
        pc.position(PositionComponent.ILLEGAL_POSITION);
        system.execute();
        assertTrue(pc.position().equals(point));
    }

    @Test
    public void test_legalPosition() {
        pc.position(new Point(2, 2));
        system.execute();
        assertFalse("Nothing should have changed", pc.position().equals(point));
    }
}
