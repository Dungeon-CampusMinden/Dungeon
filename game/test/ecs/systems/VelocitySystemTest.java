package ecs.systems;

import static org.junit.Assert.assertEquals;

import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.entities.Entity;
import graphic.Animation;
import level.elements.ILevel;
import level.elements.tile.Tile;
import mydungeon.ECS;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import tools.Point;

public class VelocitySystemTest {

    private VelocitySystem velocitySystem;
    private final ILevel level = Mockito.mock(ILevel.class);
    private final Animation moveRight = Mockito.mock(Animation.class);
    private final Animation moveLeft = Mockito.mock(Animation.class);

    private final Animation idleRight = Mockito.mock(Animation.class);
    private final Animation idleLeft = Mockito.mock(Animation.class);
    private final Tile tile = Mockito.mock(Tile.class);
    private Entity entity;

    @Before
    public void setup() {
        ECS.systems = Mockito.mock(SystemController.class);
        ECS.entities.clear();
        velocitySystem = new VelocitySystem();
        entity = new Entity();
        entity.addComponent(PositionComponent.name, new PositionComponent(entity, new Point(2, 4)));
        entity.addComponent(
                VelocityComponent.name, new VelocityComponent(entity, 1, 2, moveLeft, moveRight));
        entity.addComponent(
                AnimationComponent.name, new AnimationComponent(entity, idleLeft, idleRight));
        ECS.currentLevel = level;
        Mockito.when(level.getTileAt(Mockito.any())).thenReturn(tile);
    }

    @Test
    public void updateValidMove() {

        Mockito.when(tile.isAccessible()).thenReturn(true);

        PositionComponent positionComponent =
                (PositionComponent) entity.getComponent(PositionComponent.name).orElseThrow();
        VelocityComponent velocityComponent =
                (VelocityComponent) entity.getComponent(VelocityComponent.name).orElseThrow();
        velocitySystem.update();
        Point position = positionComponent.getPosition();
        assertEquals(2, position.x, 0.001);
        assertEquals(4, position.y, 0.001);

        velocityComponent.setCurrentXVelocity(-4);
        velocityComponent.setCurrentYVelocity(-8);
        velocitySystem.update();
        position = positionComponent.getPosition();
        assertEquals(-2, position.x, 0.001);
        assertEquals(-4, position.y, 0.001);
    }

    @Test
    public void updateUnValidMove() {
        Mockito.when(tile.isAccessible()).thenReturn(false);
        velocitySystem.update();
        PositionComponent positionComponent =
                (PositionComponent) entity.getComponent(PositionComponent.name).orElseThrow();
        Point position = positionComponent.getPosition();
        assertEquals(2, position.x, 0.001);
        assertEquals(4, position.y, 0.001);
    }

    @Test
    public void changeAnimation() {
        Mockito.when(tile.isAccessible()).thenReturn(true);
        VelocityComponent velocityComponent =
                (VelocityComponent) entity.getComponent(VelocityComponent.name).orElseThrow();
        AnimationComponent animationComponent =
                (AnimationComponent) entity.getComponent(AnimationComponent.name).orElseThrow();
        // right
        velocityComponent.setCurrentXVelocity(1);
        velocityComponent.setCurrentYVelocity(0);
        velocitySystem.update();
        assertEquals(moveRight, animationComponent.getCurrentAnimation());

        // idleRight
        velocityComponent.setCurrentXVelocity(0);
        velocityComponent.setCurrentYVelocity(0);

        velocitySystem.update();
        assertEquals(idleRight, animationComponent.getCurrentAnimation());

        // left
        velocityComponent.setCurrentXVelocity(-1);
        velocityComponent.setCurrentYVelocity(0);
        velocitySystem.update();
        assertEquals(moveLeft, animationComponent.getCurrentAnimation());

        // idleLeft
        velocityComponent.setCurrentXVelocity(0);
        velocityComponent.setCurrentYVelocity(0);

        velocitySystem.update();
        assertEquals(idleLeft, animationComponent.getCurrentAnimation());
        ;
    }
}
