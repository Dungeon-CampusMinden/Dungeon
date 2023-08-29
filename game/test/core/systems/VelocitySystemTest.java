package core.systems;

import static junit.framework.TestCase.assertTrue;

import static org.junit.Assert.assertEquals;

import com.badlogic.gdx.math.Vector2;

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
        Game.add(new LevelSystem(null, null, () -> {}));
        Game.currentLevel(level);
        Mockito.when(tile.friction()).thenReturn(0.75f);
        Mockito.when(level.tileAt((Point) Mockito.any())).thenReturn(tile);
        entity = new Entity();
        velocitySystem = new VelocitySystem();
        Game.add(velocitySystem);
        velocityComponent = new VelocityComponent(xVelocity, yVelocity);
        positionComponent = new PositionComponent(new Point(startXPosition, startYPosition));
        animationComponent = new DrawComponent("textures/test_hero");
        entity.addComponent(velocityComponent);
        entity.addComponent(positionComponent);
        entity.addComponent(animationComponent);
        Game.add(entity);
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

        Vector2 velocity =
                new Vector2(
                        velocityComponent.currentXVelocity(), velocityComponent.currentYVelocity());
        if (velocity.len()
                > Math.max(
                        Math.abs(velocityComponent.xVelocity()),
                        Math.abs(velocityComponent.yVelocity()))) {
            velocity.setLength(
                    Math.max(
                            Math.abs(velocityComponent.xVelocity()),
                            Math.abs(velocityComponent.yVelocity())));
        }

        velocitySystem.execute();
        Point position = positionComponent.position();

        assertEquals(startXPosition + velocity.x, position.x, 0.001);
        assertEquals(startYPosition + velocity.y, position.y, 0.001);
        assertEquals(
                xVelocity * (1.0f - tile.friction()), velocityComponent.currentXVelocity(), 0.001);
        assertEquals(
                yVelocity * (1.0f - tile.friction()), velocityComponent.currentYVelocity(), 0.001);
    }

    @Test
    public void updateValidMoveWithNegativeVelocity() {
        Mockito.when(tile.isAccessible()).thenReturn(true);
        velocityComponent.xVelocity(4);
        velocityComponent.yVelocity(8);
        velocityComponent.currentXVelocity(-4);
        velocityComponent.currentYVelocity(-8);

        Vector2 velocity =
                new Vector2(
                        velocityComponent.currentXVelocity(), velocityComponent.currentYVelocity());
        if (velocity.len()
                > Math.max(
                        Math.abs(velocityComponent.xVelocity()),
                        Math.abs(velocityComponent.yVelocity()))) {
            velocity.setLength(
                    Math.max(
                            Math.abs(velocityComponent.xVelocity()),
                            Math.abs(velocityComponent.yVelocity())));
        }

        velocitySystem.execute();
        System.out.println(tile.friction());
        Point position = positionComponent.position();

        assertEquals(startXPosition + velocity.x, position.x, 0.001);
        assertEquals(startYPosition + velocity.y, position.y, 0.001);
        assertEquals(-4 * (1.0f - tile.friction()), velocityComponent.currentXVelocity(), 0.001);
        assertEquals(-8 * (1.0f - tile.friction()), velocityComponent.currentYVelocity(), 0.001);
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
        assertEquals(
                xVelocity * (1.0f - tile.friction()), velocityComponent.currentXVelocity(), 0.001);
        assertEquals(
                yVelocity * (1.0f - tile.friction()), velocityComponent.currentYVelocity(), 0.001);
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
        assertEquals(-4 * (1.0f - tile.friction()), velocityComponent.currentXVelocity(), 0.001);
        assertEquals(-8 * (1.0f - tile.friction()), velocityComponent.currentYVelocity(), 0.001);
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
