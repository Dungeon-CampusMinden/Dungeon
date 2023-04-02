package ecs.systems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import controller.SystemController;
import ecs.components.AnimationComponent;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.entities.Entity;
import graphic.Animation;
import level.elements.ILevel;
import level.elements.tile.Tile;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import starter.Game;
import tools.Point;

public class VelocitySystemTest {

    private VelocitySystem velocitySystem;
    private final ILevel level = Mockito.mock(ILevel.class);
    private final Animation moveRight = Mockito.mock(Animation.class);
    private final Animation moveLeft = Mockito.mock(Animation.class);

    private final Animation idleRight = Mockito.mock(Animation.class);
    private final Animation idleLeft = Mockito.mock(Animation.class);
    private final Tile tile = Mockito.mock(Tile.class);

    private final float xVelocity = 1f;
    private final float yVelocity = 2f;
    private final float startXPosition = 2f;
    private final float startYPosition = 4f;
    private PositionComponent positionComponent;
    private VelocityComponent velocityComponent;

    private AnimationComponent animationComponent;
    private Entity entity;

    @Before
    public void setup() {
        Game.systems = Mockito.mock(SystemController.class);
        Game.currentLevel = level;
        Mockito.when(level.getTileAt(Mockito.any())).thenReturn(tile);
        Game.getEntities().clear();
        Game.getEntitiesToAdd().clear();
        Game.getEntitiesToRemove().clear();
        entity = new Entity();

        velocitySystem = new VelocitySystem();
        velocityComponent =
                new VelocityComponent(entity, xVelocity, yVelocity, moveLeft, moveRight);
        positionComponent =
                new PositionComponent(entity, new Point(startXPosition, startYPosition));
        animationComponent = new AnimationComponent(entity, idleLeft, idleRight);
        Game.getEntities().addAll(Game.getEntitiesToAdd());
        Game.getEntitiesToAdd().clear();
    }

    @Test
    public void updateValidMove() {
        Mockito.when(tile.isAccessible()).thenReturn(true);
        velocityComponent.setCurrentXVelocity(xVelocity);
        velocityComponent.setCurrentYVelocity(yVelocity);
        velocitySystem.update();
        Point position = positionComponent.getPosition();
        assertEquals(startXPosition + xVelocity, position.x, 0.001);
        assertEquals(startYPosition + yVelocity, position.y, 0.001);
        assertEquals(0, velocityComponent.getCurrentXVelocity(), 0.001);
        assertEquals(0, velocityComponent.getCurrentYVelocity(), 0.001);
    }

    @Test
    public void updateValidMoveWithNegativVelocity() {
        Mockito.when(tile.isAccessible()).thenReturn(true);
        velocityComponent.setCurrentXVelocity(-4);
        velocityComponent.setCurrentYVelocity(-8);
        velocitySystem.update();
        Point position = positionComponent.getPosition();
        assertEquals(startXPosition - 4, position.x, 0.001);
        assertEquals(startYPosition - 8, position.y, 0.001);
        assertEquals(0, velocityComponent.getCurrentXVelocity(), 0.001);
        assertEquals(0, velocityComponent.getCurrentYVelocity(), 0.001);
    }

    @Test
    public void updateUnValidMove() {
        Mockito.when(tile.isAccessible()).thenReturn(false);
        velocityComponent.setCurrentXVelocity(xVelocity);
        velocityComponent.setCurrentYVelocity(yVelocity);
        velocitySystem.update();
        Point position = positionComponent.getPosition();
        assertEquals(startXPosition, position.x, 0.001);
        assertEquals(startYPosition, position.y, 0.001);
        assertEquals(0, velocityComponent.getCurrentXVelocity(), 0.001);
        assertEquals(0, velocityComponent.getCurrentYVelocity(), 0.001);
    }

    @Test
    public void updateUnValidMoveWithNegativVelocity() {
        Mockito.when(tile.isAccessible()).thenReturn(false);
        velocityComponent.setCurrentXVelocity(-4);
        velocityComponent.setCurrentYVelocity(-8);
        velocitySystem.update();
        Point position = positionComponent.getPosition();
        assertEquals(startXPosition, position.x, 0.001);
        assertEquals(startYPosition, position.y, 0.001);
        assertEquals(0, velocityComponent.getCurrentXVelocity(), 0.001);
        assertEquals(0, velocityComponent.getCurrentYVelocity(), 0.001);
    }

    @Test
    public void changeAnimation() {
        Mockito.when(tile.isAccessible()).thenReturn(true);
        // right
        velocityComponent.setCurrentXVelocity(xVelocity);
        velocityComponent.setCurrentYVelocity(yVelocity);
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
    }

    @Test
    public void updateWithoutVelocityComponent() {
        entity.removeComponent(VelocityComponent.class);
        velocitySystem.update();
        assertEquals(startXPosition, positionComponent.getPosition().x, 0.001f);
        assertEquals(startYPosition, positionComponent.getPosition().y, 0.001f);
    }

    @Test
    public void updateWithoutPositionComponent() {
        entity.removeComponent(PositionComponent.class);
        assertThrows(MissingComponentException.class, () -> velocitySystem.update());
    }

    @Test
    public void updateWithoutAnimationComponent() {
        Mockito.when(tile.isAccessible()).thenReturn(true);
        entity.removeComponent(AnimationComponent.class);
        assertThrows(MissingComponentException.class, () -> velocitySystem.update());
    }
}
