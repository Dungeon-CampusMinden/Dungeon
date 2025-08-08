package contrib.components;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PressurePlateComponent}.
 *
 * <p>Tests the correctness of increasing, decreasing, and querying the entity count on a pressure
 * plate.
 */
class PressurePlateComponentTest {

  private PressurePlateComponent pressurePlate;

  /** Initializes a new PressurePlateComponent before each test. */
  @BeforeEach
  void setUp() {
    pressurePlate = new PressurePlateComponent();
  }

  /** Tests that the initial standing count is zero and atLeastOne() returns false. */
  @Test
  void initialStandingCountIsZero() {
    assertEquals(0, pressurePlate.standingCount(), "Initial count should be 0");
    assertFalse(pressurePlate.atLeastOne(), "Initial atLeastOne should be false");
  }

  /**
   * Tests that calling increase() once increments the standing count to 1 and atLeastOne() returns
   * true.
   */
  @Test
  void increaseIncrementsCount() {
    pressurePlate.increase();
    assertEquals(1, pressurePlate.standingCount());
    assertTrue(pressurePlate.atLeastOne());
  }

  /** Tests that multiple calls to increase() increment the count correctly. */
  @Test
  void multipleIncreaseIncrementsCountCorrectly() {
    pressurePlate.increase();
    pressurePlate.increase();
    pressurePlate.increase();
    assertEquals(3, pressurePlate.standingCount());
    assertTrue(pressurePlate.atLeastOne());
  }

  /** Tests that decrease() reduces the count after increasing it. */
  @Test
  void decreaseDecrementsCount() {
    pressurePlate.increase();
    pressurePlate.increase();
    pressurePlate.decrease();
    assertEquals(1, pressurePlate.standingCount());
    assertTrue(pressurePlate.atLeastOne());
  }

  /** Tests that the standing count reaches zero and atLeastOne() becomes false. */
  @Test
  void decreaseToZero() {
    pressurePlate.increase();
    pressurePlate.decrease();
    assertEquals(0, pressurePlate.standingCount());
    assertFalse(pressurePlate.atLeastOne());
  }

  /** Tests that calling decrease() when count is already zero keeps it at zero. */
  @Test
  void decreaseDoesNotGoBelowZero() {
    pressurePlate.decrease(); // nothing to decrease
    assertEquals(0, pressurePlate.standingCount());
    assertFalse(pressurePlate.atLeastOne());
  }

  /**
   * Tests that {@link PressurePlateComponent#atLeastOne()} returns false when no entities are
   * standing on the pressure plate.
   */
  @Test
  void atLeastOneReturnsFalseWhenZero() {
    assertFalse(pressurePlate.atLeastOne(), "atLeastOne() should return false when count is zero");
  }

  /**
   * Tests that {@link PressurePlateComponent#atLeastOne()} returns true when one or more entities
   * are standing on the pressure plate.
   */
  @Test
  void atLeastOneReturnsTrueWhenOneOrMore() {
    pressurePlate.increase();
    assertTrue(
        pressurePlate.atLeastOne(), "atLeastOne() should return true when count is one or more");

    pressurePlate.increase();
    assertTrue(
        pressurePlate.atLeastOne(),
        "atLeastOne() should still return true when count is greater than one");
  }

  /**
   * Tests that {@link PressurePlateComponent#atLeastOne()} returns false after entities leave and
   * the count decreases back to zero.
   */
  @Test
  void atLeastOneReturnsFalseAfterDecreaseToZero() {
    pressurePlate.increase();
    pressurePlate.decrease();
    assertFalse(
        pressurePlate.atLeastOne(),
        "atLeastOne() should return false after count decreases back to zero");
  }
}
