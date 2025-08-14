package core.systems;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.graphics.Texture;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.draw.TextureMap;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.DirectionalState;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Tests for the {@link VelocitySystem} class. */
public class VelocitySystemTest {

  private final ILevel level = Mockito.mock(ILevel.class);
  private final Tile tile = Mockito.mock(Tile.class);
  private final Vector2 velocity = Vector2.of(1, 2);
  private final Vector2 currentVelocity = Vector2.of(1, 2);
  private final float startXPosition = 2f;
  private final float startYPosition = 4f;
  private VelocitySystem velocitySystem;
  private PositionComponent positionComponent;
  private VelocityComponent velocityComponent;
  private DrawComponent animationComponent;

  /** Sets up the test environment with mock level, entity, and components. */
  @BeforeEach
  public void setup() throws IOException {
    // Create file system handle. WARNING: This will assume all future paths to be relative to the
    // working directory (probably the root of the project)
    Gdx.files = new LwjglFiles();
    TextureMap.instance().clear(); // reset any existing mappings

    // Replace internal map logic to skip real texture loading
    TextureMap instance = TextureMap.instance();
    Texture dummyTexture = Mockito.mock(Texture.class);

    // Trick: preload the keys you need for your test with dummy textures. Paths must be relative to
    // working directory.
    String assetKey = "test_assets/textures/test_hero/test_hero.png";
    instance.put(assetKey, dummyTexture);

    // Remaining test logic
    Game.add(new LevelSystem(() -> {}));
    Game.currentLevel(level);
    Mockito.when(tile.friction()).thenReturn(0.75f);
    Mockito.when(level.tileAt((Point) Mockito.any())).thenReturn(tile);
    Entity entity = new Entity();
    velocitySystem = new VelocitySystem();
    Game.add(velocitySystem);
    velocityComponent = new VelocityComponent(velocity);
    positionComponent = new PositionComponent(new Point(startXPosition, startYPosition));

    Map<String, Animation> animationMap =
        Animation.loadAnimationSpritesheet(new SimpleIPath("test_assets/textures/test_hero"));
    State stIdle = new DirectionalState("idle", animationMap);
    State stMove = new DirectionalState("move", animationMap, "run");
    State stDead = new State("dead", animationMap.get("die"));
    StateMachine sm = new StateMachine(Arrays.asList(stIdle, stMove, stDead));
    sm.addTransition(stIdle, "move", stMove);
    sm.addTransition(stMove, "move", stMove);
    sm.addTransition(stMove, "idle", stIdle);
    sm.addTransition(stIdle, "died", stDead);
    sm.addTransition(stMove, "died", stDead);
    animationComponent = new DrawComponent(sm);

    entity.add(velocityComponent);
    entity.add(positionComponent);
    entity.add(animationComponent);
    Game.add(entity);
  }

  /** Cleans up the test environment by removing all entities, level, and systems. */
  @AfterEach
  public void cleanup() {
    Game.removeAllEntities();
    Game.currentLevel(null);
    Game.removeAllSystems();
  }

  /** Tests that entity moves correctly when the target tile is accessible. */
  @Test
  public void testValidMove() {
    Mockito.when(tile.isAccessible()).thenReturn(true);
    velocityComponent.currentVelocity(currentVelocity);

    Vector2 expectedVelocity = currentVelocity;
    float maxSpeed =
        Math.max(
            Math.abs(velocityComponent.velocity().x()), Math.abs(velocityComponent.velocity().y()));
    if (expectedVelocity.length() > maxSpeed) {
      expectedVelocity = expectedVelocity.normalize().scale(maxSpeed);
    }

    velocitySystem.execute();
    Point position = positionComponent.position();

    assertEquals(startXPosition + expectedVelocity.x(), position.x(), 0.001);
    assertEquals(startYPosition + expectedVelocity.y(), position.y(), 0.001);

    Vector2 expectedFinalVelocity = expectedVelocity.scale(1.0f - tile.friction());
    assertEquals(expectedFinalVelocity.x(), velocityComponent.currentVelocity().x(), 0.001);
    assertEquals(expectedFinalVelocity.y(), velocityComponent.currentVelocity().y(), 0.001);
  }

  /** Tests that entity moves correctly with negative velocity values. */
  @Test
  public void testValidMoveWithNegativeVelocity() {
    Mockito.when(tile.isAccessible()).thenReturn(true);
    velocityComponent.velocity(Vector2.of(4, 8));
    Vector2 negativeVelocity = Vector2.of(-4, -8);
    velocityComponent.currentVelocity(negativeVelocity);

    Vector2 expectedVelocity = negativeVelocity;
    float maxSpeed =
        Math.max(
            Math.abs(velocityComponent.velocity().x()), Math.abs(velocityComponent.velocity().y()));
    if (expectedVelocity.length() > maxSpeed) {
      expectedVelocity = expectedVelocity.normalize().scale(maxSpeed);
    }

    velocitySystem.execute();
    Point position = positionComponent.position();

    assertEquals(startXPosition + expectedVelocity.x(), position.x(), 0.001);
    assertEquals(startYPosition + expectedVelocity.y(), position.y(), 0.001);

    Vector2 expectedFinalVelocity = expectedVelocity.scale(1.0f - tile.friction());
    assertEquals(expectedFinalVelocity.x(), velocityComponent.currentVelocity().x(), 0.001);
    assertEquals(expectedFinalVelocity.y(), velocityComponent.currentVelocity().y(), 0.001);
  }

  /** Tests that entity doesn't move when the target tile is not accessible. */
  @Test
  public void testInvalidMove() {
    Mockito.when(tile.isAccessible()).thenReturn(false);
    velocityComponent.currentVelocity(currentVelocity);

    velocitySystem.execute();
    Point position = positionComponent.position();

    assertEquals(startXPosition, position.x(), 0.001);
    assertEquals(startYPosition, position.y(), 0.001);

    Vector2 expectedFinalVelocity = currentVelocity.scale(1.0f - tile.friction());
    assertEquals(expectedFinalVelocity.x(), velocityComponent.currentVelocity().x(), 0.001);
    assertEquals(expectedFinalVelocity.y(), velocityComponent.currentVelocity().y(), 0.001);
  }

  /** Tests that entity doesn't move with negative velocity when target tile is not accessible. */
  @Test
  public void testInvalidMoveWithNegativeVelocity() {
    Mockito.when(tile.isAccessible()).thenReturn(false);
    Vector2 negativeVelocity = Vector2.of(-4, -8);
    velocityComponent.currentVelocity(negativeVelocity);

    velocitySystem.execute();
    Point position = positionComponent.position();

    assertEquals(startXPosition, position.x(), 0.001);
    assertEquals(startYPosition, position.y(), 0.001);

    Vector2 expectedFinalVelocity = negativeVelocity.scale(1.0f - tile.friction());
    assertEquals(expectedFinalVelocity.x(), velocityComponent.currentVelocity().x(), 0.001);
    assertEquals(expectedFinalVelocity.y(), velocityComponent.currentVelocity().y(), 0.001);
  }

  /** Tests that the correct animations are queued based on movement direction changes. */
  @Test
  public void testAnimationChanges() {
    Mockito.when(tile.isAccessible()).thenReturn(true);

    // Test right movement
    velocityComponent.currentVelocity(Vector2.of(1, 1));
    velocitySystem.execute();
    assertEquals("move", animationComponent.currentState().name);
    assertEquals(Direction.RIGHT, animationComponent.currentState().getData());

    // Test idle right
    velocityComponent.currentVelocity(Vector2.ZERO);
    velocitySystem.execute();
    assertEquals("idle", animationComponent.currentState().name);
    assertEquals(Direction.RIGHT, animationComponent.currentState().getData());

    // Test left movement
    velocityComponent.currentVelocity(Vector2.of(-1, 0));
    velocitySystem.execute();
    assertEquals("move", animationComponent.currentState().name);
    assertEquals(Direction.LEFT, animationComponent.currentState().getData());

    // Test idle left
    velocityComponent.currentVelocity(Vector2.ZERO);
    velocitySystem.execute();
    assertEquals("idle", animationComponent.currentState().name);
    assertEquals(Direction.LEFT, animationComponent.currentState().getData());
  }

  /** Tests that diagonal movement is properly limited to max speed. */
  @Test
  public void testDiagonalMovementSpeedLimit() {
    Mockito.when(tile.isAccessible()).thenReturn(true);
    velocityComponent.velocity(Vector2.of(5, 5));

    // Set diagonal velocity that would exceed max speed
    Vector2 diagonalVelocity = Vector2.of(5, 5);
    velocityComponent.currentVelocity(diagonalVelocity);

    float maxSpeed =
        Math.max(
            Math.abs(velocityComponent.velocity().x()), Math.abs(velocityComponent.velocity().y()));

    velocitySystem.execute();

    // The actual movement should be limited to maxSpeed
    Point position = positionComponent.position();
    Vector2 actualMovement =
        Vector2.of(position.x() - startXPosition, position.y() - startYPosition);

    assertTrue(
        actualMovement.length() <= maxSpeed + 0.001,
        "Diagonal movement should be limited to max speed");
  }

  /** Tests that zero velocity results in no movement and idle animation. */
  @Test
  public void testZeroVelocity() {
    Mockito.when(tile.isAccessible()).thenReturn(true);
    velocityComponent.currentVelocity(Vector2.ZERO);

    velocitySystem.execute();
    Point position = positionComponent.position();

    assertEquals(startXPosition, position.x(), 0.001);
    assertEquals(startYPosition, position.y(), 0.001);
    assertEquals("idle", animationComponent.currentState().name);
    assertNull(animationComponent.currentState().getData());
  }
}
