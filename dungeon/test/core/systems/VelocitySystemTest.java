package core.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import core.Entity;
import core.Game;
import core.components.*;
import core.utils.Direction;
import core.utils.Vector2;
import core.utils.components.draw.Animation;
import core.utils.components.draw.CoreAnimationPriorities;
import core.utils.components.draw.CoreAnimations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
  void setUp() {
    entity = new Entity();
    vc = new VelocityComponent(speed);
    pc = new PositionComponent();
    dc = new DrawComponent(mock(Animation.class));
    entity.add(vc);
    entity.add(pc);
    entity.add(dc);
    entity.add(new MassComponent(mass));
    Game.add(entity);
    system = new VelocitySystem();
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
   * Tests that when a single force is applied to an entity with a mass of 1 (default), the velocity
   * is correctly updated by adding the acceleration (force / mass). After the calculation, the
   * forces should be cleared.
   */
  @Test
  void calculateVelocitySingleForceNoMass() {
    Vector2 force = Vector2.of(4, 3);
    entity.remove(MassComponent.class);
    vc.applyForce("testforce", force);
    vc.currentVelocity(Vector2.ZERO);
    system.execute();
    Vector2 expectedVelocity = force;
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
   * Tests that when a single negative force is applied to an entity without a MassComponent, the
   * velocity is updated correctly assuming the default mass of 1. Forces should be cleared after
   * execution.
   */
  @Test
  void calculateVelocitySingleNegativeForceNoMass() {
    // Remove any MassComponent from entity to simulate no mass
    entity.remove(MassComponent.class);
    Vector2 negativeForce = Vector2.of(-7, -4);
    vc.applyForce("negForce", negativeForce);
    vc.currentVelocity(Vector2.ZERO);

    system.execute();

    Vector2 expectedVelocity = negativeForce; // mass defaults to 1
    assertEquals(expectedVelocity, vc.currentVelocity());
    assertEquals(0, vc.appliedForcesStream().count());
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

  /**
   * Tests that when multiple forces are applied to an entity without a MassComponent, the resulting
   * velocity is the sum of all forces (mass defaults to 1). Forces should be cleared after
   * execution.
   */
  @Test
  void calculateVelocityMultipleForceNoMass() {
    entity.remove(MassComponent.class);

    Vector2 force1 = Vector2.of(2, 5);
    Vector2 force2 = Vector2.of(-3, 1);

    vc.applyForce("force1", force1);
    vc.applyForce("force2", force2);
    vc.currentVelocity(Vector2.ZERO);

    system.execute();

    Vector2 expectedVelocity = force1.add(force2); // mass defaults to 1, so no scaling
    assertEquals(expectedVelocity, vc.currentVelocity());
    assertEquals(0, vc.appliedForcesStream().count());
  }

  /**
   * Tests that when an entity has a negative mass value, the mass is treated as 1 (minimum), so
   * velocity is calculated accordingly. Forces should be cleared after execution.
   */
  @Test
  void calculateVelocityNegativeMass() {
    // Set a negative mass on the entity
    entity.remove(MassComponent.class);
    entity.add(new MassComponent(-3f)); // negative mass

    Vector2 force = Vector2.of(6, 4);
    vc.applyForce("force", force);
    vc.currentVelocity(Vector2.ZERO);

    system.execute();

    // Since mass <= 0, it defaults to 1, so velocity = force * 1/1 = force
    Vector2 expectedVelocity = force;
    assertEquals(expectedVelocity, vc.currentVelocity());
    assertEquals(0, vc.appliedForcesStream().count());
  }

  /** Tests that when the entity is idle and facing UP, the correct idle animations are queued. */
  @Test
  void setAnimationIdleUp() {
    vc.currentVelocity(Vector2.ZERO);
    pc.viewDirection(Direction.UP);

    DrawComponent mockDraw = mock(DrawComponent.class);
    entity.remove(DrawComponent.class);
    entity.add(mockDraw);

    system.execute();

    verify(mockDraw)
        .queueAnimation(
            eq(1),
            eq(CoreAnimations.IDLE_UP),
            eq(CoreAnimations.IDLE),
            eq(CoreAnimations.IDLE_DOWN),
            eq(CoreAnimations.IDLE_LEFT),
            eq(CoreAnimations.IDLE_RIGHT));
  }

  /** Tests that when the entity is idle and facing LEFT, the correct idle animations are queued. */
  @Test
  void setAnimationIdleLeft() {
    vc.currentVelocity(Vector2.ZERO);
    pc.viewDirection(Direction.LEFT);

    DrawComponent mockDraw = mock(DrawComponent.class);
    entity.remove(DrawComponent.class);
    entity.add(mockDraw);

    system.execute();

    verify(mockDraw)
        .queueAnimation(
            eq(1),
            eq(CoreAnimations.IDLE_LEFT),
            eq(CoreAnimations.IDLE),
            eq(CoreAnimations.IDLE_RIGHT),
            eq(CoreAnimations.IDLE_DOWN),
            eq(CoreAnimations.IDLE_UP));
  }

  /** Tests that when the entity is idle and facing DOWN, the correct idle animations are queued. */
  @Test
  void setAnimationIdleDown() {
    vc.currentVelocity(Vector2.ZERO);
    pc.viewDirection(Direction.DOWN);

    DrawComponent mockDraw = mock(DrawComponent.class);
    entity.remove(DrawComponent.class);
    entity.add(mockDraw);

    system.execute();

    verify(mockDraw)
        .queueAnimation(
            eq(1),
            eq(CoreAnimations.IDLE_DOWN),
            eq(CoreAnimations.IDLE),
            eq(CoreAnimations.IDLE_UP),
            eq(CoreAnimations.IDLE_LEFT),
            eq(CoreAnimations.IDLE_RIGHT));
  }

  /**
   * Tests that when the entity is idle and facing RIGHT, the correct idle animations are queued.
   */
  @Test
  void setAnimationIdleRight() {
    vc.currentVelocity(Vector2.ZERO);
    pc.viewDirection(Direction.RIGHT);

    DrawComponent mockDraw = mock(DrawComponent.class);
    entity.remove(DrawComponent.class);
    entity.add(mockDraw);

    system.execute();

    verify(mockDraw)
        .queueAnimation(
            eq(1),
            eq(CoreAnimations.IDLE_RIGHT),
            eq(CoreAnimations.IDLE),
            eq(CoreAnimations.IDLE_LEFT),
            eq(CoreAnimations.IDLE_DOWN),
            eq(CoreAnimations.IDLE_UP));
  }

  /**
   * Tests that when the entity has a positive horizontal velocity (moving right), the
   * VelocitySystem queues the RUN_RIGHT animation and sets the view direction to RIGHT.
   */
  @Test
  void queueRunAnimationWhenMovingRight() {
    // Set velocity to move right only
    vc.currentVelocity(Vector2.of(5, 0));

    // Initial direction can be anything, e.g., NONE or UP
    pc.viewDirection(Direction.NONE);

    // Replace DrawComponent with a mock to verify animation calls
    DrawComponent mockDraw = mock(DrawComponent.class);
    entity.remove(DrawComponent.class);
    entity.add(mockDraw);

    system.execute();

    // Verify that RUN_RIGHT animation is queued
    verify(mockDraw).deQueueByPriority(CoreAnimationPriorities.RUN.priority());
    verify(mockDraw).deQueueByPriority(CoreAnimationPriorities.IDLE.priority());
    verify(mockDraw).queueAnimation(CoreAnimations.RUN_RIGHT, CoreAnimations.RUN);

    // Verify that view direction is set to RIGHT
    assertEquals(Direction.RIGHT, pc.viewDirection());
  }

  /**
   * Tests that when the entity has a negative horizontal velocity (moving left), the VelocitySystem
   * queues the RUN_LEFT animation and sets the view direction to LEFT.
   */
  @Test
  void queueRunAnimationWhenMovingLeft() {
    vc.currentVelocity(Vector2.of(-5, 0));
    pc.viewDirection(Direction.NONE);

    DrawComponent mockDraw = mock(DrawComponent.class);
    entity.remove(DrawComponent.class);
    entity.add(mockDraw);

    system.execute();

    verify(mockDraw).deQueueByPriority(CoreAnimationPriorities.RUN.priority());
    verify(mockDraw).deQueueByPriority(CoreAnimationPriorities.IDLE.priority());
    verify(mockDraw).queueAnimation(CoreAnimations.RUN_LEFT, CoreAnimations.RUN);

    assertEquals(Direction.LEFT, pc.viewDirection());
  }

  /**
   * Tests that when the entity has a positive vertical velocity (moving up), the VelocitySystem
   * queues the RUN_UP animation and sets the view direction to UP.
   */
  @Test
  void queueRunAnimationWhenMovingUp() {
    vc.currentVelocity(Vector2.of(0, 5));
    pc.viewDirection(Direction.NONE);

    DrawComponent mockDraw = mock(DrawComponent.class);
    entity.remove(DrawComponent.class);
    entity.add(mockDraw);

    system.execute();

    verify(mockDraw).deQueueByPriority(CoreAnimationPriorities.RUN.priority());
    verify(mockDraw).deQueueByPriority(CoreAnimationPriorities.IDLE.priority());
    verify(mockDraw).queueAnimation(CoreAnimations.RUN_UP, CoreAnimations.RUN);

    assertEquals(Direction.UP, pc.viewDirection());
  }

  /**
   * Tests that when the entity has a negative vertical velocity (moving down), the VelocitySystem
   * queues the RUN_DOWN animation and sets the view direction to DOWN.
   */
  @Test
  void queueRunAnimationWhenMovingDown() {
    vc.currentVelocity(Vector2.of(0, -5));
    pc.viewDirection(Direction.NONE);

    DrawComponent mockDraw = mock(DrawComponent.class);
    entity.remove(DrawComponent.class);
    entity.add(mockDraw);

    system.execute();

    verify(mockDraw).deQueueByPriority(CoreAnimationPriorities.RUN.priority());
    verify(mockDraw).deQueueByPriority(CoreAnimationPriorities.IDLE.priority());
    verify(mockDraw).queueAnimation(CoreAnimations.RUN_DOWN, CoreAnimations.RUN);

    assertEquals(Direction.DOWN, pc.viewDirection());
  }

  /**
   * Tests that when the vertical velocity magnitude is higher than horizontal, the VelocitySystem
   * queues the vertical run animation (up or down) accordingly and sets the correct view direction.
   */
  @Test
  void queueRunAnimationWhenVerticalVelocityDominates() {
    vc.currentVelocity(Vector2.of(3, 5)); // Vertical > Horizontal
    pc.viewDirection(Direction.NONE);

    DrawComponent mockDraw = mock(DrawComponent.class);
    entity.remove(DrawComponent.class);
    entity.add(mockDraw);

    system.execute();

    verify(mockDraw).deQueueByPriority(CoreAnimationPriorities.RUN.priority());
    verify(mockDraw).deQueueByPriority(CoreAnimationPriorities.IDLE.priority());
    verify(mockDraw).queueAnimation(CoreAnimations.RUN_UP, CoreAnimations.RUN);

    assertEquals(Direction.UP, pc.viewDirection());

    // Also test for downward vertical dominance
    vc.currentVelocity(Vector2.of(2, -6)); // Vertical > Horizontal (down)
    pc.viewDirection(Direction.NONE);
    system.execute();

    verify(mockDraw, times(2)).deQueueByPriority(CoreAnimationPriorities.RUN.priority());
    verify(mockDraw, times(2)).deQueueByPriority(CoreAnimationPriorities.IDLE.priority());
    verify(mockDraw).queueAnimation(CoreAnimations.RUN_DOWN, CoreAnimations.RUN);

    assertEquals(Direction.DOWN, pc.viewDirection());
  }

  /**
   * Tests that when the horizontal velocity magnitude is higher than vertical, the VelocitySystem
   * queues the horizontal run animation (left or right) accordingly and sets the correct view
   * direction.
   */
  @Test
  void queueRunAnimationWhenHorizontalVelocityDominates() {
    vc.currentVelocity(Vector2.of(7, 4)); // Horizontal > Vertical
    pc.viewDirection(Direction.NONE);

    DrawComponent mockDraw = mock(DrawComponent.class);
    entity.remove(DrawComponent.class);
    entity.add(mockDraw);

    system.execute();

    verify(mockDraw).deQueueByPriority(CoreAnimationPriorities.RUN.priority());
    verify(mockDraw).deQueueByPriority(CoreAnimationPriorities.IDLE.priority());
    verify(mockDraw).queueAnimation(CoreAnimations.RUN_RIGHT, CoreAnimations.RUN);

    assertEquals(Direction.RIGHT, pc.viewDirection());

    // Also test for left dominance
    vc.currentVelocity(Vector2.of(-8, 3)); // Horizontal > Vertical (left)
    pc.viewDirection(Direction.NONE);
    system.execute();

    verify(mockDraw, times(2)).deQueueByPriority(CoreAnimationPriorities.RUN.priority());
    verify(mockDraw, times(2)).deQueueByPriority(CoreAnimationPriorities.IDLE.priority());
    verify(mockDraw).queueAnimation(CoreAnimations.RUN_LEFT, CoreAnimations.RUN);

    assertEquals(Direction.LEFT, pc.viewDirection());
  }
}
