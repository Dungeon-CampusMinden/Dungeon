package core.components;

import static org.junit.jupiter.api.Assertions.*;

import core.Entity;
import core.utils.Vector2;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for the {@link VelocityComponent} class. */
class VelocityComponentTest {

  private VelocityComponent component;

  /** Initializes the test object before each test. */
  @BeforeEach
  void setUp() {
    component = new VelocityComponent(5.0f, e -> {}, true);
  }

  /** Verifies that the default constructor sets all fields to their default values. */
  @Test
  void testDefaultConstructor() {
    VelocityComponent vc = new VelocityComponent();
    assertEquals(0f, vc.maxSpeed());
    assertEquals(Vector2.ZERO, vc.currentVelocity());
    assertFalse(vc.canEnterOpenPits());
    assertNotNull(vc.onWallHit());
    assertEquals(vc.mass(), 1);
  }

  /** Verifies that the constructor with maxSpeed sets the speed correctly and defaults the rest. */
  @Test
  void testConstructorWithMaxSpeedOnly() {
    VelocityComponent vc = new VelocityComponent(3.5f);
    assertEquals(3.5f, vc.maxSpeed());
    assertEquals(Vector2.ZERO, vc.currentVelocity());
    assertFalse(vc.canEnterOpenPits());
    assertNotNull(vc.onWallHit());
    assertEquals(1, vc.mass());
  }

  /** Verifies that the constructor sets all fields correctly. */
  @Test
  void testConstructorWithoutMass() {
    Consumer<Entity> callback = e -> {};
    VelocityComponent vc = new VelocityComponent(4.2f, callback, true);
    assertEquals(4.2f, vc.maxSpeed());
    assertTrue(vc.canEnterOpenPits());
    assertEquals(callback, vc.onWallHit());
    assertEquals(1f, vc.mass());
  }

  /** Verifies that the full constructor sets all fields correctly. */
  @Test
  void testConstructorWithFullArguments() {
    Consumer<Entity> callback = e -> {};
    VelocityComponent vc = new VelocityComponent(4.2f, 2f, callback, true);
    assertEquals(4.2f, vc.maxSpeed());
    assertTrue(vc.canEnterOpenPits());
    assertEquals(callback, vc.onWallHit());
    assertEquals(2f, vc.mass());
  }

  /**
   * Tests that the VelocityComponent constructor throws an IllegalArgumentException when given a
   * negative mass value.
   *
   * <p>This ensures that invalid physical parameters are not accepted.
   */
  @Test
  void testConstructorWithFullArgumentsNegativeMass() {
    Consumer<Entity> callback = e -> {};

    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new VelocityComponent(4.2f, -1, callback, true);
            });

    assertEquals("Mass cannot be 0 or less", thrown.getMessage());
  }

  /**
   * Tests that the VelocityComponent constructor throws an IllegalArgumentException when given a
   * zero mass value.
   *
   * <p>This ensures that invalid physical parameters are not accepted.
   */
  @Test
  void testConstructorWithFullArgumentsZeroMass() {
    Consumer<Entity> callback = e -> {};

    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new VelocityComponent(4.2f, 0, callback, true);
            });

    assertEquals("Mass cannot be 0 or less", thrown.getMessage());
  }

  /** Tests getter and setter for current velocity. */
  @Test
  void testCurrentVelocitySetterAndGetter() {
    Vector2 velocity = Vector2.of(2, -3);
    component.currentVelocity(velocity);
    assertEquals(velocity, component.currentVelocity());
  }

  /** Tests getter and setter for max speed. */
  @Test
  void testMaxSpeedSetterAndGetter() {
    component.maxSpeed(10f);
    assertEquals(10f, component.maxSpeed());
  }

  /** Tests getter and setter for mass. */
  @Test
  void testMassSetterAndGetter() {
    component.mass(10f);
    assertEquals(10f, component.mass());
  }

  /** Tests getter and setter for mass with negative mass. */
  @Test
  void testMassSetterAndGetterNegativeMass() {
    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              component.mass(-10);
            });

    assertEquals("Mass cannot be 0 or less", thrown.getMessage());
  }

  /** Tests getter and setter for mass with negative mass. */
  @Test
  void testMassSetterAndGetterZeroMass() {
    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              component.mass(0);
            });

    assertEquals("Mass cannot be 0 or less", thrown.getMessage());
  }

  /** Tests enabling and disabling the canEnterOpenPits flag. */
  @Test
  void testCanEnterOpenPitsSetterAndGetter() {
    component.canEnterOpenPits(false);
    assertFalse(component.canEnterOpenPits());

    component.canEnterOpenPits(true);
    assertTrue(component.canEnterOpenPits());
  }

  /** Tests that the wall hit callback can be set and is correctly invoked. */
  @Test
  void testOnWallHitSetterAndInvocation() {
    AtomicBoolean wasCalled = new AtomicBoolean(false);
    Consumer<Entity> hitCallback = e -> wasCalled.set(true);
    component.onWallHit(hitCallback);
    component.onWallHit().accept(new Entity());
    assertTrue(wasCalled.get());
  }

  /** Tests that a force can be applied and retrieved. */
  @Test
  void testApplyForceAndRetrieve() {
    Vector2 force = Vector2.ONE;
    component.applyForce("gravity", force);

    Optional<Vector2> retrieved = component.force("gravity");
    assertTrue(retrieved.isPresent());
    assertEquals(force, retrieved.get());
  }

  /** Tests that applying a force with an existing ID replaces the previous one. */
  @Test
  void testApplyForceReplacesPreviousForceWithSameId() {
    component.applyForce("friction", Vector2.ONE);
    component.applyForce("friction", Vector2.ZERO);

    assertEquals(Vector2.ZERO, component.force("friction").get());
  }

  /** Tests that a force can be removed. */
  @Test
  void testRemoveForce() {
    component.applyForce("wind", Vector2.of(1, 0));
    component.removeForce("wind");

    assertFalse(component.force("wind").isPresent());
  }

  /** Tests that all forces can be cleared at once. */
  @Test
  void testClearForces() {
    component.applyForce("a", Vector2.of(1, 0));
    component.applyForce("b", Vector2.of(0, 1));

    component.clearForces();

    assertTrue(component.appliedForces().isEmpty());
  }

  /** Tests that the appliedForcesStream correctly counts the number of forces. */
  @Test
  void testAppliedForcesStream() {
    component.applyForce("a", Vector2.of(1, 0));
    component.applyForce("b", Vector2.of(0, 1));

    long count = component.appliedForcesStream().count();
    assertEquals(2, count);
  }

  /**
   * Tests that the applied forces map is returned as a copy and not modifiable from the outside.
   */
  @Test
  void testAppliedForcesReturnsCopy() {
    component.applyForce("external", Vector2.ONE);
    var forces = component.appliedForces();
    forces.clear(); // Should not affect internal state

    assertFalse(component.appliedForces().isEmpty());
  }

  /** Tests that requesting a non-existent force returns an empty Optional. */
  @Test
  void testForceReturnsEmptyIfNotPresent() {
    assertTrue(component.force("nonexistent").isEmpty());
  }
}
