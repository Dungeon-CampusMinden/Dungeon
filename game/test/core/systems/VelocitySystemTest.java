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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class VelocitySystemTest {

    private final ILevel level = Mockito.mock(ILevel.class);
    private final Tile tile = Mockito.mock(Tile.class);
    private final float xVelocity = 1f;
    private final float yVelocity = 2f;
    private final float startXPosition = 2f;
    private final float startYPosition = 4f;
    private VelocitySystem velocitySystem;
    private PositionComponent positionComponent;
    private VelocityComponent velocityComponent;

    private DrawComponent animationComponent;
    private Entity entity;

    @Before
    public void setup() throws IOException {
        Game.addSystem(new LevelSystem(null, null, () -> {}));
        Game.currentLevel(level);
        Mockito.when(level.tileAt((Point) Mockito.any())).thenReturn(tile);
        entity = new Entity();
        velocitySystem = new VelocitySystem();
        Game.addSystem(velocitySystem);
        velocityComponent = new VelocityComponent(entity, xVelocity, yVelocity);
        positionComponent =
                new PositionComponent(entity, new Point(startXPosition, startYPosition));
        animationComponent = new DrawComponent(entity, "character/blue_knight");
        velocitySystem.showEntity(entity);
    }

    @After
    public void cleanup() {
        Game.removeAllEntities();
        Game.currentLevel(null);
        Game.removeAllSystems();
    }

    @Test
    public void updateValidMove() {
        Mockito.when(tile.isAccessible()).thenReturn(true);
        velocityComponent.currentXVelocity(xVelocity);
        velocityComponent.currentYVelocity(yVelocity);
        velocitySystem.execute();
        Point position = positionComponent.position();
        assertEquals(startXPosition + xVelocity, position.x, 0.001);
        assertEquals(startYPosition + yVelocity, position.y, 0.001);
        assertEquals(0, velocityComponent.currentXVelocity(), 0.001);
        assertEquals(0, velocityComponent.currentYVelocity(), 0.001);
    }

    @Test
    public void updateValidMoveWithNegativeVelocity() {
        Mockito.when(tile.isAccessible()).thenReturn(true);
        velocityComponent.currentXVelocity(-4);
        velocityComponent.currentYVelocity(-8);
        velocitySystem.execute();
        Point position = positionComponent.position();
        assertEquals(startXPosition - 4, position.x, 0.001);
        assertEquals(startYPosition - 8, position.y, 0.001);
        assertEquals(0, velocityComponent.currentXVelocity(), 0.001);
        assertEquals(0, velocityComponent.currentYVelocity(), 0.001);
    }

    @Test
    public void updateUnValidMove() {
        Mockito.when(tile.isAccessible()).thenReturn(false);
        velocityComponent.currentXVelocity(xVelocity);
        velocityComponent.currentYVelocity(yVelocity);
        velocitySystem.execute();
        Point position = positionComponent.position();
        assertEquals(startXPosition, position.x, 0.001);
        assertEquals(startYPosition, position.y, 0.001);
        assertEquals(0, velocityComponent.currentXVelocity(), 0.001);
        assertEquals(0, velocityComponent.currentYVelocity(), 0.001);
    }

    @Test
    public void updateUnValidMoveWithNegativeVelocity() {
        Mockito.when(tile.isAccessible()).thenReturn(false);
        velocityComponent.currentXVelocity(-4);
        velocityComponent.currentYVelocity(-8);
        velocitySystem.execute();
        Point position = positionComponent.position();
        assertEquals(startXPosition, position.x, 0.001);
        assertEquals(startYPosition, position.y, 0.001);
        assertEquals(0, velocityComponent.currentXVelocity(), 0.001);
        assertEquals(0, velocityComponent.currentYVelocity(), 0.001);
    }

    @Test
    public void changeAnimation() {
        Mockito.when(tile.isAccessible()).thenReturn(true);
        // right
        velocityComponent.currentXVelocity(xVelocity);
        velocityComponent.currentYVelocity(yVelocity);
        velocitySystem.execute();
        assertTrue(animationComponent.isCurrentAnimation(CoreAnimations.RUN_RIGHT));

        // idleRight
        velocityComponent.currentXVelocity(0);
        velocityComponent.currentYVelocity(0);

        velocitySystem.execute();
        assertTrue(animationComponent.isCurrentAnimation(CoreAnimations.IDLE_RIGHT));

        // left
        velocityComponent.currentXVelocity(-1);
        velocityComponent.currentYVelocity(0);
        velocitySystem.execute();
        assertTrue(animationComponent.isCurrentAnimation(CoreAnimations.RUN_LEFT));

        // idleLeft
        velocityComponent.currentXVelocity(0);
        velocityComponent.currentYVelocity(0);

        velocitySystem.execute();
        assertTrue(animationComponent.isCurrentAnimation(CoreAnimations.IDLE_LEFT));
    }
}
