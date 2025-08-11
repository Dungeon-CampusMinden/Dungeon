package contrib.components;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PressurePlateComponent}.
 *
 * <p>Tests correctness of increasing, decreasing, and querying the total mass on a pressure plate,
 * as well as the triggered state based on the mass threshold.
 */
class PressurePlateComponentTest {

  private PressurePlateComponent pressurePlate;

  /** Initializes a new PressurePlateComponent before each test with default mass trigger. */
  @BeforeEach
  void setUp() {
    pressurePlate = new PressurePlateComponent();
  }

  /** Tests that initial mass is zero and pressure plate is not triggered. */
  @Test
  void initialMassIsZeroAndNotTriggered() {
    assertEquals(0f, pressurePlate.currentMass(), 0.0001f, "Initial mass should be 0");
    assertFalse(pressurePlate.isTriggered(), "Pressure plate should not be triggered initially");
  }

  /** Tests that increasing mass adds correctly and may trigger plate if threshold reached. */
  @Test
  void increaseMassIncrementsCurrentMass() {
    pressurePlate = new PressurePlateComponent(1.0f);
    pressurePlate.increase(0.5f);
    assertEquals(0.5f, pressurePlate.currentMass(), 0.0001f);
    assertFalse(pressurePlate.isTriggered(), "Plate should not trigger below threshold");

    pressurePlate.increase(0.6f);
    assertEquals(1.1f, pressurePlate.currentMass(), 0.0001f);
    assertTrue(pressurePlate.isTriggered(), "Plate should trigger at or above threshold");
  }

  /** Tests that decreasing mass reduces current mass but not below zero. */
  @Test
  void decreaseMassReducesCurrentMassNotBelowZero() {
    pressurePlate.increase(1.5f);
    pressurePlate.decrease(0.4f);
    assertEquals(1.1f, pressurePlate.currentMass(), 0.0001f);

    pressurePlate.decrease(2f); // Try to decrease more than current mass
    assertEquals(0f, pressurePlate.currentMass(), 0.0001f, "Mass should not go below zero");
    assertFalse(pressurePlate.isTriggered(), "Plate should not trigger when mass is zero");
  }

  /** Tests that isTriggered() reflects correct triggered state based on mass threshold. */
  @Test
  void isTriggeredReflectsThresholdCorrectly() {
    float threshold = pressurePlate.massTrigger();

    pressurePlate.increase(threshold - 0.01f);
    assertFalse(pressurePlate.isTriggered(), "Just below threshold should not trigger");

    pressurePlate.increase(0.01f);
    assertTrue(pressurePlate.isTriggered(), "At threshold should trigger");

    pressurePlate.decrease(0.1f);
    assertFalse(pressurePlate.isTriggered(), "Below threshold after decrease should not trigger");
  }

  /** Tests that massTrigger() returns the configured mass threshold. */
  @Test
  void massTriggerReturnsConfiguredThreshold() {
    float defaultTrigger = PressurePlateComponent.DEFAULT_MASS_TRIGGER;
    assertEquals(defaultTrigger, pressurePlate.massTrigger(), 0.0001f);

    PressurePlateComponent custom = new PressurePlateComponent(5.0f);
    assertEquals(5.0f, custom.massTrigger(), 0.0001f);
  }
}
