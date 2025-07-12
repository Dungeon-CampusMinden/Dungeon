package core.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.Entity;
import core.utils.Vector2;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the {@link VelocityComponent} class. */
public class VelocityComponentTest {

  private final Vector2 initialVelocity = Vector2.of(3f, 3f);
  private VelocityComponent velocityComponent;

  @BeforeEach
  void setUp() {
    velocityComponent = new VelocityComponent(initialVelocity);
  }

  /** Tests that the default constructor creates a component with zero velocity. */
  @Test
  void testDefaultConstructor() {
    VelocityComponent defaultComponent = new VelocityComponent();
    assertEquals(Vector2.ZERO, defaultComponent.velocity());
    assertEquals(Vector2.ZERO, defaultComponent.currentVelocity());
    assertFalse(defaultComponent.canEnterOpenPits());
    assertNotNull(defaultComponent.onWallHit());
  }

  /** Tests that the single-parameter constructor sets velocity correctly with default values. */
  @Test
  void testSingleParameterConstructor() {
    Vector2 testVelocity = Vector2.of(5f, 2f);
    VelocityComponent component = new VelocityComponent(testVelocity);

    assertEquals(testVelocity, component.velocity());
    assertEquals(Vector2.ZERO, component.currentVelocity());
    assertFalse(component.canEnterOpenPits());
    assertNotNull(component.onWallHit());
  }

  /** Tests that the full constructor sets all parameters correctly. */
  @Test
  void testFullConstructor() {
    Vector2 testVelocity = Vector2.of(4f, 6f);
    AtomicBoolean callbackExecuted = new AtomicBoolean(false);

    VelocityComponent component =
        new VelocityComponent(testVelocity, entity -> callbackExecuted.set(true), true);

    assertEquals(testVelocity, component.velocity());
    assertEquals(Vector2.ZERO, component.currentVelocity());
    assertTrue(component.canEnterOpenPits());

    // Test callback
    component.onWallHit().accept(new Entity());
    assertTrue(callbackExecuted.get());
  }

  /** Tests getting and setting the current velocity vector. */
  @Test
  void testCurrentVelocityGetterAndSetter() {
    assertEquals(Vector2.ZERO, velocityComponent.currentVelocity());

    Vector2 newCurrentVelocity = Vector2.of(1.5f, -2.5f);
    velocityComponent.currentVelocity(newCurrentVelocity);

    assertEquals(newCurrentVelocity, velocityComponent.currentVelocity());
  }

  /** Tests getting and setting the maximum velocity vector. */
  @Test
  void testVelocityGetterAndSetter() {
    assertEquals(initialVelocity, velocityComponent.velocity());

    Vector2 newVelocity = Vector2.of(8f, 4f);
    velocityComponent.velocity(newVelocity);

    assertEquals(newVelocity, velocityComponent.velocity());
  }

  /** Tests getting and setting the previous velocity vector. */
  @Test
  void testPreviousVelocityGetterAndSetter() {
    assertEquals(Vector2.ZERO, velocityComponent.previouXVelocity());

    Vector2 previousVelocity = Vector2.of(2f, 3f);
    velocityComponent.previousVelocity(previousVelocity);

    assertEquals(previousVelocity, velocityComponent.previouXVelocity());
  }

  /** Tests getting and setting the wall hit callback function. */
  @Test
  void testOnWallHitGetterAndSetter() {
    AtomicBoolean originalCallbackExecuted = new AtomicBoolean(false);
    AtomicBoolean newCallbackExecuted = new AtomicBoolean(false);

    // Test setting new callback
    velocityComponent.onWallHit(entity -> newCallbackExecuted.set(true));

    // Execute the callback
    velocityComponent.onWallHit().accept(new Entity());

    assertFalse(originalCallbackExecuted.get());
    assertTrue(newCallbackExecuted.get());
  }

  /** Tests that velocity vectors with different components are handled correctly. */
  @Test
  void testVectorOperationsWithDifferentComponents() {
    Vector2 asymmetricVelocity = Vector2.of(10f, 5f);
    velocityComponent.velocity(asymmetricVelocity);

    assertEquals(10f, velocityComponent.velocity().x());
    assertEquals(5f, velocityComponent.velocity().y());

    Vector2 negativeVelocity = Vector2.of(-3f, -7f);
    velocityComponent.currentVelocity(negativeVelocity);

    assertEquals(-3f, velocityComponent.currentVelocity().x());
    assertEquals(-7f, velocityComponent.currentVelocity().y());
  }

  /** Tests that zero vectors are handled correctly in all velocity properties. */
  @Test
  void testZeroVectorHandling() {
    velocityComponent.velocity(Vector2.ZERO);
    velocityComponent.currentVelocity(Vector2.ZERO);
    velocityComponent.previousVelocity(Vector2.ZERO);

    assertEquals(Vector2.ZERO, velocityComponent.velocity());
    assertEquals(Vector2.ZERO, velocityComponent.currentVelocity());
    assertEquals(Vector2.ZERO, velocityComponent.previouXVelocity());
  }

  /** Tests that very small velocity values are preserved without precision loss. */
  @Test
  void testSmallVelocityValues() {
    Vector2 smallVelocity = Vector2.of(0.001f, 0.0001f);
    velocityComponent.velocity(smallVelocity);

    assertEquals(0.001f, velocityComponent.velocity().x(), 0.0001f);
    assertEquals(0.0001f, velocityComponent.velocity().y(), 0.00001f);
  }

  /** Tests that large velocity values are handled correctly without overflow. */
  @Test
  void testLargeVelocityValues() {
    Vector2 largeVelocity = Vector2.of(1000f, 999f);
    velocityComponent.velocity(largeVelocity);

    assertEquals(1000f, velocityComponent.velocity().x());
    assertEquals(999f, velocityComponent.velocity().y());
  }
}
