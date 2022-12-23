package ecs.systems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.entitys.Entity;
import java.util.HashMap;
import level.elements.ILevel;
import level.elements.tile.Tile;
import mydungeon.ECS;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import tools.Point;

public class MovementSystemTest {

    private MovementSystem system;
    private ILevel level;
    private Tile tile;
    private Entity entity;
    private VelocityComponent velocityComponent;
    private PositionComponent positionComponent;

    @Before
    public void setup() {
        ECS.velocityComponentMap = new HashMap<>();
        ECS.positionComponentMap = new HashMap<>();
        ECS.systems = new SystemController();
        system = new MovementSystem();
        entity = new Entity();
        positionComponent = new PositionComponent(entity, new Point(1, 2));
        velocityComponent = new VelocityComponent(entity, 1, 2, 1, 2);
        level = Mockito.mock(ILevel.class);
        ECS.currentLevel = level;
        tile = Mockito.mock(Tile.class);
        Mockito.when(level.getTileAt(Mockito.any())).thenReturn(tile);
    }

    @Test
    public void constructorTest() {
        assertTrue(ECS.systems.contains(system));
    }

    @Test
    public void updateValidMove() {
        Mockito.when(tile.isAccessible()).thenReturn(true);
        system.update();
        Point position = ECS.positionComponentMap.get(entity).getPosition();
        assertEquals(2, position.x, 0.001);
        assertEquals(4, position.y, 0.001);

        velocityComponent.setX(-4);
        velocityComponent.setY(-8);
        system.update();
        position = ECS.positionComponentMap.get(entity).getPosition();
        assertEquals(-2, position.x, 0.001);
        assertEquals(-4, position.y, 0.001);
    }

    @Test
    public void updateUnValidMove() {
        Mockito.when(tile.isAccessible()).thenReturn(false);
        system.update();
        Point position = ECS.positionComponentMap.get(entity).getPosition();
        assertEquals(1, position.x, 0.001);
        assertEquals(2, position.y, 0.001);
    }

    @Test
    public void testUpdateWithoutVelocityComponent() {
        ECS.velocityComponentMap = new HashMap<>();
        Mockito.when(tile.isAccessible()).thenReturn(false);
        system.update();
        Point position = ECS.positionComponentMap.get(entity).getPosition();
        assertEquals(1, position.x, 0.001);
        assertEquals(2, position.y, 0.001);
    }
}
