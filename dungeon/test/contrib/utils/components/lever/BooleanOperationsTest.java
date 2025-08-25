package contrib.utils.components.lever;

import static org.junit.jupiter.api.Assertions.*;

import contrib.components.LeverComponent;
import contrib.utils.ICommand;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link BooleanOperations} utility class.
 *
 * <p>Each test method verifies the correct behavior of a specific boolean logic operation applied
 * to {@link LeverComponent} instances.
 */
class BooleanOperationsTest {

  /**
   * Creates a {@link LeverComponent} with the given initial state and a no-operation command.
   *
   * @param isOn Initial lever state.
   * @return A new LeverComponent instance.
   */
  private LeverComponent lever(boolean isOn) {
    return new LeverComponent(isOn, ICommand.NOOP);
  }

  /**
   * Tests that {@link BooleanOperations#and(LeverComponent, LeverComponent)} returns {@code true}
   * only when both levers are on.
   */
  @Test
  void testAnd() {
    assertTrue(BooleanOperations.and(lever(true), lever(true)));
    assertFalse(BooleanOperations.and(lever(true), lever(false)));
    assertFalse(BooleanOperations.and(lever(false), lever(true)));
    assertFalse(BooleanOperations.and(lever(false), lever(false)));
  }

  /**
   * Tests that {@link BooleanOperations#or(LeverComponent, LeverComponent)} returns {@code true}
   * when at least one lever is on.
   */
  @Test
  void testOr() {
    assertTrue(BooleanOperations.or(lever(true), lever(true)));
    assertTrue(BooleanOperations.or(lever(true), lever(false)));
    assertTrue(BooleanOperations.or(lever(false), lever(true)));
    assertFalse(BooleanOperations.or(lever(false), lever(false)));
  }

  /**
   * Tests that {@link BooleanOperations#xor(LeverComponent, LeverComponent)} returns {@code true}
   * only when exactly one lever is on.
   */
  @Test
  void testXor() {
    assertFalse(BooleanOperations.xor(lever(true), lever(true)));
    assertTrue(BooleanOperations.xor(lever(true), lever(false)));
    assertTrue(BooleanOperations.xor(lever(false), lever(true)));
    assertFalse(BooleanOperations.xor(lever(false), lever(false)));
  }

  /**
   * Tests that {@link BooleanOperations#nand(LeverComponent, LeverComponent)} returns {@code true}
   * when not both levers are on.
   */
  @Test
  void testNand() {
    assertFalse(BooleanOperations.nand(lever(true), lever(true)));
    assertTrue(BooleanOperations.nand(lever(true), lever(false)));
    assertTrue(BooleanOperations.nand(lever(false), lever(true)));
    assertTrue(BooleanOperations.nand(lever(false), lever(false)));
  }

  /**
   * Tests that {@link BooleanOperations#nor(LeverComponent, LeverComponent)} returns {@code true}
   * only when both levers are off.
   */
  @Test
  void testNor() {
    assertFalse(BooleanOperations.nor(lever(true), lever(true)));
    assertFalse(BooleanOperations.nor(lever(true), lever(false)));
    assertFalse(BooleanOperations.nor(lever(false), lever(true)));
    assertTrue(BooleanOperations.nor(lever(false), lever(false)));
  }

  /**
   * Tests that {@link BooleanOperations#xnor(LeverComponent, LeverComponent)} returns {@code true}
   * only when both levers are in the same state (both on or both off).
   */
  @Test
  void testXnor() {
    assertTrue(BooleanOperations.xnor(lever(true), lever(true)));
    assertFalse(BooleanOperations.xnor(lever(true), lever(false)));
    assertFalse(BooleanOperations.xnor(lever(false), lever(true)));
    assertTrue(BooleanOperations.xnor(lever(false), lever(false)));
  }

  /**
   * Tests that {@link BooleanOperations#not(LeverComponent)} returns {@code true} only when the
   * given lever is off.
   */
  @Test
  void testNot() {
    assertFalse(BooleanOperations.not(lever(true)));
    assertTrue(BooleanOperations.not(lever(false)));
  }
}
