package core.systems;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Texture;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Vector2;
import core.utils.components.draw.TextureMap;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.DirectionalState;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Unit tests for the {@link VelocitySystem}. */
public class VelocitySystemTest {

  private Entity entity;
  private VelocityComponent vc;
  private PositionComponent pc;
  private DrawComponent dc;

  private float speed = 10f;

  private float mass = 2f;
  private VelocitySystem system;

  /**
   * Sets up a fresh entity with the necessary components before each test, and adds it to the game
   * world.
   */
  @BeforeEach
  public void setup() {
    // Create file system handle. WARNING: This will assume all future paths to be relative to the
    // working directory (probably the root of the project)
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
    //    Game.currentLevel(level);
    //    Mockito.when(tile.friction()).thenReturn(0.75f);
    //    Mockito.when(level.tileAt((Point) Mockito.any())).thenReturn(tile);
    system = new VelocitySystem();
    Game.add(system);

    Entity entity = new Entity();
    vc = new VelocityComponent(speed);
    vc.mass(mass);
    pc = new PositionComponent();
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
    dc = new DrawComponent(sm);
    entity.add(vc);
    entity.add(pc);
    entity.add(dc);
    Game.add(entity);
  }

  @AfterEach
  void cleanUp() {
    Game.removeAllSystems();
    Game.removeAllEntities();
  }

  /**
   * Tests that the VelocitySystem only processes entities that have all required components:
   * VelocityComponent, PositionComponent, and DrawComponent.
   *
   * <p>Entities missing one or more of these components must not be included in the system's
   * filtered stream.
   */
  @Test
  void onlyWorkOnCorrectEntities() {
    // Create invalid entity
    Game.add(new Entity());
    // Collect the filtered stream
    long count = system.filteredEntityStream().count();

    // Assert that only one (the valid one) is processed
    assertEquals(1, count);
  }

  /**
   * Tests that when a single force is applied, velocity is updated by acceleration (force / mass).
   * Forces cleared after execution.
   */
  @Test
  void calculateVelocitySingleForce() {
    Vector2 force = Vector2.of(4, 3);
    vc.applyForce("testforce", force);
    vc.currentVelocity(Vector2.ZERO);

    system.execute();

    Vector2 expectedVelocity = force.scale(1f / mass); // <-- scale by 1/mass
    assertEquals(expectedVelocity, vc.currentVelocity());
    assertEquals(0, vc.appliedForcesStream().count());
  }

  /**
   * Tests that when a single negative force is applied to an entity with a specified mass, the
   * velocity is correctly updated by adding the negative acceleration (force / mass). After the
   * calculation, the forces should be cleared.
   */
  @Test
  void calculateVelocitySingleNegativeForce() {
    Vector2 negativeForce = Vector2.of(-5, -2);
    vc.applyForce("negativeForce", negativeForce);
    vc.currentVelocity(Vector2.ZERO);

    system.execute();

    Vector2 expectedVelocity = negativeForce.scale(1f / mass); // acceleration = force / mass
    assertEquals(expectedVelocity, vc.currentVelocity());
    assertEquals(0, vc.appliedForcesStream().count()); // forces cleared
  }

  /**
   * Tests that when multiple forces are applied to an entity with a specified mass, the resulting
   * velocity is the sum of all forces divided by the mass. Forces should be cleared after
   * execution.
   */
  @Test
  void calculateVelocityMultipleForce() {
    Vector2 force1 = Vector2.of(3, 2);
    Vector2 force2 = Vector2.of(1, 4);
    Vector2 force3 = Vector2.of(-1, -1);

    vc.applyForce("force1", force1);
    vc.applyForce("force2", force2);
    vc.applyForce("force3", force3);
    vc.currentVelocity(Vector2.ZERO);

    system.execute();

    Vector2 expectedVelocity = force1.add(force2).add(force3).scale(1f / mass); // scale by 1/mass
    assertEquals(expectedVelocity, vc.currentVelocity());
    assertEquals(0, vc.appliedForcesStream().count());
  }

  /** Tests that when the entity is idle the correct idle animations are played. */
  @Test
  void setAnimationIdle() {
    vc.currentVelocity(Vector2.ZERO);
    pc.viewDirection(Direction.UP);

    // Test right movement
    vc.currentVelocity(Vector2.of(1, 1));
    system.execute();
    assertEquals("move", dc.currentState().name);
    assertEquals(Direction.RIGHT, dc.currentState().getData());

    // Test idle right
    vc.currentVelocity(Vector2.ZERO);
    system.execute();
    assertEquals("idle", dc.currentState().name);
    assertEquals(Direction.RIGHT, dc.currentState().getData());

    // Test left movement
    vc.currentVelocity(Vector2.of(-1, 0));
    system.execute();
    assertEquals("move", dc.currentState().name);
    assertEquals(Direction.LEFT, dc.currentState().getData());

    // Test idle left
    vc.currentVelocity(Vector2.ZERO);
    system.execute();
    assertEquals("idle", dc.currentState().name);
    assertEquals(Direction.LEFT, dc.currentState().getData());
  }

  /**
   * Tests that when the entity has a positive horizontal velocity (moving right), the
   * VelocitySystem queues the RUN_RIGHT animation and sets the view direction to RIGHT.
   */
  @Test
  void setAnimationRunning() {
    // MOVE RIGHT
    vc.currentVelocity(Vector2.of(5, 0));
    pc.viewDirection(Direction.NONE);

    system.execute();

    assertEquals("move", dc.currentStateName());
    assertEquals(Direction.RIGHT, dc.currentStateData());
    assertEquals(Direction.RIGHT, pc.viewDirection());

    // MOVE LEFT
    vc.currentVelocity(Vector2.of(-5, 0));
    pc.viewDirection(Direction.NONE);

    system.execute();

    assertEquals("move", dc.currentStateName());
    assertEquals(Direction.LEFT, dc.currentStateData());
    assertEquals(Direction.LEFT, pc.viewDirection());

    // MOVE UP
    vc.currentVelocity(Vector2.of(0, 5));
    pc.viewDirection(Direction.NONE);

    system.execute();

    assertEquals("move", dc.currentStateName());
    assertEquals(Direction.UP, dc.currentStateData());
    assertEquals(Direction.UP, pc.viewDirection());

    // MOVE DOWN
    vc.currentVelocity(Vector2.of(0, -5));
    pc.viewDirection(Direction.NONE);

    system.execute();

    assertEquals("move", dc.currentStateName());
    assertEquals(Direction.DOWN, dc.currentStateData());
    assertEquals(Direction.DOWN, pc.viewDirection());
  }

  /**
   * Tests that when the vertical velocity magnitude is higher than horizontal, the VelocitySystem
   * queues the vertical run animation (up or down) accordingly and sets the correct view direction.
   */
  @Test
  void setAnimationRunWhenVerticalVelocityDominates() {
    vc.currentVelocity(Vector2.of(3, 5)); // Vertical > Horizontal
    pc.viewDirection(Direction.NONE);

    system.execute();

    assertEquals("move", dc.currentStateName());
    assertEquals(Direction.UP, dc.currentStateData());
    assertEquals(Direction.UP, pc.viewDirection());

    // Also test for downward vertical dominance
    vc.currentVelocity(Vector2.of(2, -6)); // Vertical > Horizontal (down)
    pc.viewDirection(Direction.NONE);
    system.execute();

    assertEquals("move", dc.currentStateName());
    assertEquals(Direction.DOWN, dc.currentStateData());
    assertEquals(Direction.DOWN, pc.viewDirection());
  }

  /**
   * Tests that when the horizontal velocity magnitude is higher than vertical, the VelocitySystem
   * queues the horizontal run animation (left or right) accordingly and sets the correct view
   * direction.
   */
  @Test
  void setAnimationRunWhenHorizontalVelocityDominates() {
    vc.currentVelocity(Vector2.of(7, 4)); // Horizontal > Vertical
    pc.viewDirection(Direction.NONE);

    system.execute();

    assertEquals("move", dc.currentStateName());
    assertEquals(Direction.RIGHT, dc.currentStateData());
    assertEquals(Direction.RIGHT, pc.viewDirection());

    // Also test for left dominance
    vc.currentVelocity(Vector2.of(-8, 3)); // Horizontal > Vertical (left)
    pc.viewDirection(Direction.NONE);
    system.execute();

    assertEquals("move", dc.currentStateName());
    assertEquals(Direction.LEFT, dc.currentStateData());
    assertEquals(Direction.LEFT, pc.viewDirection());
  }
}
