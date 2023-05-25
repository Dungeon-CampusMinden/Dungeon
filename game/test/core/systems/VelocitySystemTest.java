package core.systems;

import static junit.framework.TestCase.assertTrue;

import static org.junit.Assert.assertEquals;

import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.utils.Point;
import core.utils.components.draw.CoreAnimations;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class VelocitySystemTest {

    private VelocitySystem velocitySystem;
    private final ILevel level = Mockito.mock(ILevel.class);
    private final Tile tile = Mockito.mock(Tile.class);

    private final float xVelocity = 1f;
    private final float yVelocity = 2f;
    private final float startXPosition = 2f;
    private final float startYPosition = 4f;
    private PositionComponent positionComponent;
    private VelocityComponent velocityComponent;

    private DrawComponent animationComponent;
    private Entity entity;

    @Before
    public void setup() throws IOException {
        Game.currentLevel = level;
        Mockito.when(level.getTileAt(Mockito.any())).thenReturn(tile);
        Game.removeEntity(entity);
        entity = new Entity();
        velocitySystem = new VelocitySystem();
        velocityComponent = new VelocityComponent(entity, xVelocity, yVelocity);
        positionComponent =
                new PositionComponent(entity, new Point(startXPosition, startYPosition));
        animationComponent = new DrawComponent(entity, "character/knight");
        velocitySystem.showEntity(entity);
    }

    @Test
    public void updateValidMove() {
        Mockito.when(tile.isAccessible()).thenReturn(true);
        velocityComponent.setCurrentXVelocity(xVelocity);
        velocityComponent.setCurrentYVelocity(yVelocity);
        velocitySystem.execute();
        Point position = positionComponent.getPosition();
        assertEquals(startXPosition + xVelocity, position.x, 0.001);
        assertEquals(startYPosition + yVelocity, position.y, 0.001);
        assertEquals(0, velocityComponent.getCurrentXVelocity(), 0.001);
        assertEquals(0, velocityComponent.getCurrentYVelocity(), 0.001);
    }

    @Test
    public void updateValidMoveWithNegativeVelocity() {
        Mockito.when(tile.isAccessible()).thenReturn(true);
        velocityComponent.setCurrentXVelocity(-4);
        velocityComponent.setCurrentYVelocity(-8);
        velocitySystem.execute();
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
        velocitySystem.execute();
        Point position = positionComponent.getPosition();
        assertEquals(startXPosition, position.x, 0.001);
        assertEquals(startYPosition, position.y, 0.001);
        assertEquals(0, velocityComponent.getCurrentXVelocity(), 0.001);
        assertEquals(0, velocityComponent.getCurrentYVelocity(), 0.001);
    }

    @Test
    public void updateUnValidMoveWithNegativeVelocity() {
        Mockito.when(tile.isAccessible()).thenReturn(false);
        velocityComponent.setCurrentXVelocity(-4);
        velocityComponent.setCurrentYVelocity(-8);
        velocitySystem.execute();
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
        velocitySystem.execute();
        assertTrue(animationComponent.isCurrentAnimation(CoreAnimations.RUN_RIGHT));

        // idleRight
        velocityComponent.setCurrentXVelocity(0);
        velocityComponent.setCurrentYVelocity(0);

        velocitySystem.execute();
        assertTrue(animationComponent.isCurrentAnimation(CoreAnimations.IDLE_RIGHT));

        // left
        velocityComponent.setCurrentXVelocity(-1);
        velocityComponent.setCurrentYVelocity(0);
        velocitySystem.execute();
        assertTrue(animationComponent.isCurrentAnimation(CoreAnimations.RUN_LEFT));

        // idleLeft
        velocityComponent.setCurrentXVelocity(0);
        velocityComponent.setCurrentYVelocity(0);

        velocitySystem.execute();
        assertTrue(animationComponent.isCurrentAnimation(CoreAnimations.IDLE_LEFT));
    }
}
