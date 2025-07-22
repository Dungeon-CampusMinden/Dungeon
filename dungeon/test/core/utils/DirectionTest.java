package core.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link Direction} enum.
 *
 * <p>This test suite covers Direction-specific functionality including:
 *
 * <ul>
 *   <li>Direction transformations (opposite, turns)
 *   <li>Relative direction applications
 *   <li>Random direction generation
 *   <li>String conversion
 *   <li>Vector2 interface implementation
 * </ul>
 *
 * <p>Vector2 operations are tested in {@link Vector2Test} and are not duplicated here.
 */
public class DirectionTest {

  private static final double DELTA = 1e-6;

  /** Tests that each direction has the correct x and y coordinates as documented. */
  @Test
  public void testDirectionCoordinates() {
    assertEquals(0.0f, Direction.UP.x(), DELTA);
    assertEquals(1.0f, Direction.UP.y(), DELTA);

    assertEquals(1.0f, Direction.RIGHT.x(), DELTA);
    assertEquals(0.0f, Direction.RIGHT.y(), DELTA);

    assertEquals(0.0f, Direction.DOWN.x(), DELTA);
    assertEquals(-1.0f, Direction.DOWN.y(), DELTA);

    assertEquals(-1.0f, Direction.LEFT.x(), DELTA);
    assertEquals(0.0f, Direction.LEFT.y(), DELTA);

    assertEquals(0.0f, Direction.NONE.x(), DELTA);
    assertEquals(0.0f, Direction.NONE.y(), DELTA);
  }

  /** Tests the opposite() method for all cardinal directions. */
  @Test
  public void testOpposite() {
    assertEquals(Direction.DOWN, Direction.UP.opposite());
    assertEquals(Direction.UP, Direction.DOWN.opposite());
    assertEquals(Direction.RIGHT, Direction.LEFT.opposite());
    assertEquals(Direction.LEFT, Direction.RIGHT.opposite());
    assertEquals(Direction.NONE, Direction.NONE.opposite());
  }

  /** Tests the turnLeft() method for 90-degree counter-clockwise rotations. */
  @Test
  public void testTurnLeft() {
    assertEquals(Direction.LEFT, Direction.UP.turnLeft());
    assertEquals(Direction.DOWN, Direction.LEFT.turnLeft());
    assertEquals(Direction.RIGHT, Direction.DOWN.turnLeft());
    assertEquals(Direction.UP, Direction.RIGHT.turnLeft());
    assertEquals(Direction.NONE, Direction.NONE.turnLeft());
  }

  /** Tests the turnRight() method for 90-degree clockwise rotations. */
  @Test
  public void testTurnRight() {
    assertEquals(Direction.RIGHT, Direction.UP.turnRight());
    assertEquals(Direction.DOWN, Direction.RIGHT.turnRight());
    assertEquals(Direction.LEFT, Direction.DOWN.turnRight());
    assertEquals(Direction.UP, Direction.LEFT.turnRight());
    assertEquals(Direction.NONE, Direction.NONE.turnRight());
  }

  /** Tests that four consecutive left turns return to the original direction. */
  @Test
  public void testFourLeftTurns() {
    Direction original = Direction.UP;
    Direction result = original.turnLeft().turnLeft().turnLeft().turnLeft();
    assertEquals(original, result);
  }

  /** Tests that four consecutive right turns return to the original direction. */
  @Test
  public void testFourRightTurns() {
    Direction original = Direction.LEFT;
    Direction result = original.turnRight().turnRight().turnRight().turnRight();
    assertEquals(original, result);
  }

  /** Tests that left and right turns are inverses of each other. */
  @Test
  public void testTurnInverses() {
    for (Direction dir :
        new Direction[] {Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT}) {
      assertEquals(dir, dir.turnLeft().turnRight());
      assertEquals(dir, dir.turnRight().turnLeft());
    }
  }

  /** Tests the applyRelative() method with DOWN (back) transformation. */
  @Test
  public void testApplyRelativeBack() {
    assertEquals(Direction.DOWN, Direction.UP.applyRelative(Direction.DOWN));
    assertEquals(Direction.LEFT, Direction.RIGHT.applyRelative(Direction.DOWN));
    assertEquals(Direction.UP, Direction.DOWN.applyRelative(Direction.DOWN));
    assertEquals(Direction.RIGHT, Direction.LEFT.applyRelative(Direction.DOWN));
  }

  /** Tests the applyRelative() method with LEFT transformation. */
  @Test
  public void testApplyRelativeLeft() {
    assertEquals(Direction.LEFT, Direction.UP.applyRelative(Direction.LEFT));
    assertEquals(Direction.UP, Direction.RIGHT.applyRelative(Direction.LEFT));
    assertEquals(Direction.RIGHT, Direction.DOWN.applyRelative(Direction.LEFT));
    assertEquals(Direction.DOWN, Direction.LEFT.applyRelative(Direction.LEFT));
  }

  /** Tests the applyRelative() method with RIGHT transformation. */
  @Test
  public void testApplyRelativeRight() {
    assertEquals(Direction.RIGHT, Direction.UP.applyRelative(Direction.RIGHT));
    assertEquals(Direction.DOWN, Direction.RIGHT.applyRelative(Direction.RIGHT));
    assertEquals(Direction.LEFT, Direction.DOWN.applyRelative(Direction.RIGHT));
    assertEquals(Direction.UP, Direction.LEFT.applyRelative(Direction.RIGHT));
  }

  /** Tests the applyRelative() method with UP and NONE (no change) transformations. */
  @Test
  public void testApplyRelativeNoChange() {
    Direction[] directions = {
      Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT, Direction.NONE
    };

    for (Direction dir : directions) {
      assertEquals(dir, dir.applyRelative(Direction.UP));
      assertEquals(dir, dir.applyRelative(Direction.NONE));
    }
  }

  /** Tests that the random() method returns only cardinal directions. */
  @Test
  public void testRandomReturnsCardinalDirections() {
    Direction[] cardinals = {Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT};

    // Test multiple times to increase confidence
    for (int i = 0; i < 100; i++) {
      Direction random = Direction.random();
      boolean isCardinal = false;
      for (Direction cardinal : cardinals) {
        if (random == cardinal) {
          isCardinal = true;
          break;
        }
      }
      assertTrue(isCardinal, "Random direction should be a cardinal direction, but was: " + random);
    }
  }

  /** Tests that random() never returns NONE. */
  @Test
  public void testRandomNeverReturnsNone() {
    // Test multiple times to increase confidence
    for (int i = 0; i < 100; i++) {
      Direction random = Direction.random();
      assertNotEquals(Direction.NONE, random, "Random direction should never be NONE");
    }
  }

  /** Tests the fromString() method with valid direction strings. */
  @Test
  public void testFromStringValid() {
    assertEquals(Direction.UP, Direction.fromString("up"));
    assertEquals(Direction.UP, Direction.fromString("UP"));
    assertEquals(Direction.UP, Direction.fromString("Up"));

    assertEquals(Direction.RIGHT, Direction.fromString("right"));
    assertEquals(Direction.RIGHT, Direction.fromString("RIGHT"));

    assertEquals(Direction.DOWN, Direction.fromString("down"));
    assertEquals(Direction.DOWN, Direction.fromString("DOWN"));

    assertEquals(Direction.LEFT, Direction.fromString("left"));
    assertEquals(Direction.LEFT, Direction.fromString("LEFT"));

    assertEquals(Direction.NONE, Direction.fromString("none"));
    assertEquals(Direction.NONE, Direction.fromString("NONE"));
  }

  /** Tests that fromString() throws IllegalArgumentException for invalid strings. */
  @Test
  public void testFromStringInvalid() {
    assertThrows(IllegalArgumentException.class, () -> Direction.fromString("invalid"));
    assertThrows(IllegalArgumentException.class, () -> Direction.fromString("north"));
    assertThrows(IllegalArgumentException.class, () -> Direction.fromString(""));
    assertThrows(IllegalArgumentException.class, () -> Direction.fromString("forward"));
  }

  /** Tests that fromString() is case-insensitive. */
  @Test
  public void testFromStringCaseInsensitive() {
    String[] cases = {"up", "UP", "Up", "uP"};
    for (String testCase : cases) {
      assertEquals(Direction.UP, Direction.fromString(testCase));
    }
  }

  /** Tests that all directions can be converted to string and back correctly. */
  @Test
  public void testStringRoundTrip() {
    for (Direction dir : Direction.values()) {
      Direction converted = Direction.fromString(dir.name());
      assertEquals(dir, converted);
    }
  }

  /** Tests that Direction implements Vector2 interface correctly. */
  @Test
  public void testVector2Interface() {
    // Direction should be usable as Vector2
    Vector2 upVector = Direction.UP;
    assertEquals(0.0f, upVector.x(), DELTA);
    assertEquals(1.0f, upVector.y(), DELTA);

    // Test that Direction can be used in Vector2 operations
    Vector2 result = Direction.UP.add(Direction.RIGHT);
    assertEquals(1.0f, result.x(), DELTA);
    assertEquals(1.0f, result.y(), DELTA);
  }

  /** Tests that directions are unit vectors (except NONE). */
  @Test
  public void testUnitVectors() {
    assertEquals(1.0, Direction.UP.length(), DELTA);
    assertEquals(1.0, Direction.RIGHT.length(), DELTA);
    assertEquals(1.0, Direction.DOWN.length(), DELTA);
    assertEquals(1.0, Direction.LEFT.length(), DELTA);
    assertEquals(0.0, Direction.NONE.length(), DELTA);
  }

  /** Tests that NONE direction is the zero vector. */
  @Test
  public void testNoneIsZeroVector() {
    assertTrue(Direction.NONE.isZero());
    assertFalse(Direction.UP.isZero());
    assertFalse(Direction.RIGHT.isZero());
    assertFalse(Direction.DOWN.isZero());
    assertFalse(Direction.LEFT.isZero());
  }

  /** Tests that cardinal directions are orthogonal to their adjacent directions. */
  @Test
  public void testOrthogonalDirections() {
    assertEquals(0.0, Direction.UP.dot(Direction.RIGHT), DELTA);
    assertEquals(0.0, Direction.RIGHT.dot(Direction.DOWN), DELTA);
    assertEquals(0.0, Direction.DOWN.dot(Direction.LEFT), DELTA);
    assertEquals(0.0, Direction.LEFT.dot(Direction.UP), DELTA);
  }

  /** Tests that opposite directions have dot product of -1. */
  @Test
  public void testOppositeDirectionsDotProduct() {
    assertEquals(-1.0, Direction.UP.dot(Direction.DOWN), DELTA);
    assertEquals(-1.0, Direction.RIGHT.dot(Direction.LEFT), DELTA);
    assertEquals(-1.0, Direction.DOWN.dot(Direction.UP), DELTA);
    assertEquals(-1.0, Direction.LEFT.dot(Direction.RIGHT), DELTA);
  }
}
