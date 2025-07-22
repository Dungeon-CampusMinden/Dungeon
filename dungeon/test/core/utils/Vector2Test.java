package core.utils;

import static org.junit.jupiter.api.Assertions.*;

import core.level.utils.Coordinate;
import org.junit.jupiter.api.Test;

/** Tests for the {@link Vector2} class. */
public class Vector2Test {

  private static final double DELTA = 1e-6;

  /** Tests the creation of a vector from x and y coordinates. */
  @Test
  public void testOfXY() {
    Vector2 v = Vector2.of(3.0f, 4.0f);
    assertEquals(3.0f, v.x(), DELTA);
    assertEquals(4.0f, v.y(), DELTA);
  }

  /** Tests the creation of a vector from another vector. */
  @Test
  public void testOfVector() {
    Vector2 original = Vector2.of(5.0f, 6.0f);
    Vector2 copy = Vector2.of(original);
    assertEquals(original.x(), copy.x(), DELTA);
    assertEquals(original.y(), copy.y(), DELTA);
    assertNotSame(original, copy);
  }

  /** Tests the creation of a vector from a point. */
  @Test
  public void testOfPoint() {
    Point p = new Point(7.0f, 8.0f);
    Vector2 v = Vector2.of(p);
    assertEquals(p.x(), v.x(), DELTA);
    assertEquals(p.y(), v.y(), DELTA);
  }

  /** Tests the creation of a vector from a coordinate. */
  @Test
  public void testOfCoordinate() {
    Coordinate c = new Coordinate(9, 10);
    Vector2 v = Vector2.of(c);
    assertEquals(9.0f, v.x(), DELTA);
    assertEquals(10.0f, v.y(), DELTA);
  }

  /** Tests the correctness of the predefined vector constants. */
  @Test
  public void testConstants() {
    assertEquals(0.0f, Vector2.ZERO.x(), DELTA);
    assertEquals(0.0f, Vector2.ZERO.y(), DELTA);

    assertEquals(1.0f, Vector2.ONE.x(), DELTA);
    assertEquals(1.0f, Vector2.ONE.y(), DELTA);
  }

  /** Tests the addition of two vectors. */
  @Test
  public void testAdd() {
    Vector2 v1 = Vector2.of(1.0f, 2.0f);
    Vector2 v2 = Vector2.of(3.0f, 4.0f);
    Vector2 result = v1.add(v2);
    assertEquals(4.0f, result.x(), DELTA);
    assertEquals(6.0f, result.y(), DELTA);
  }

  /** Tests the subtraction of one vector from another. */
  @Test
  public void testSubtract() {
    Vector2 v1 = Vector2.of(5.0f, 6.0f);
    Vector2 v2 = Vector2.of(1.0f, 2.0f);
    Vector2 result = v1.subtract(v2);
    assertEquals(4.0f, result.x(), DELTA);
    assertEquals(4.0f, result.y(), DELTA);
  }

  /** Tests scaling a vector by a single scalar. */
  @Test
  public void testScale() {
    Vector2 v = Vector2.of(2.0f, 3.0f);
    Vector2 result = v.scale(2.0);
    assertEquals(4.0f, result.x(), DELTA);
    assertEquals(6.0f, result.y(), DELTA);
  }

  /** Tests scaling a vector by different x and y scalars. */
  @Test
  public void testScaleXY() {
    Vector2 v = Vector2.of(2.0f, 3.0f);
    Vector2 result = v.scale(Vector2.of(2.0, 3.0));
    assertEquals(4.0f, result.x(), DELTA);
    assertEquals(9.0f, result.y(), DELTA);
  }

  /** Tests the calculation of the vector's length. */
  @Test
  public void testLength() {
    Vector2 v = Vector2.of(3.0f, 4.0f);
    assertEquals(5.0, v.length(), DELTA);
  }

  /** Tests if a vector is the zero vector. */
  @Test
  public void testIsZero() {
    assertTrue(Vector2.ZERO.isZero());
    assertFalse(Vector2.ONE.isZero());
  }

  /** Tests the normalization of a vector. */
  @Test
  public void testNormalize() {
    Vector2 v = Vector2.of(3.0f, 4.0f);
    Vector2 normalized = v.normalize();
    assertEquals(1.0, normalized.length(), DELTA);
    assertEquals(0.6f, normalized.x(), DELTA);
    assertEquals(0.8f, normalized.y(), DELTA);
  }

  /** Tests the normalization of a zero vector. */
  @Test
  public void testNormalizeZero() {
    Vector2 normalized = Vector2.ZERO.normalize();
    assertTrue(normalized.isZero());
  }

  /** Tests the dot product of two vectors. */
  @Test
  public void testDot() {
    Vector2 v1 = Vector2.of(1.0f, 2.0f);
    Vector2 v2 = Vector2.of(3.0f, 4.0f);
    assertEquals(11.0, v1.dot(v2), DELTA);
  }

  /** Tests the calculation of the squared length of a vector. */
  @Test
  public void testLengthSquared() {
    Vector2 v = Vector2.of(3.0f, 4.0f);
    assertEquals(25.0, v.lengthSquared(), DELTA);
  }

  /** Tests the distance calculation between two vectors. */
  @Test
  public void testDistance() {
    Vector2 v1 = Vector2.of(1.0f, 2.0f);
    Vector2 v2 = Vector2.of(4.0f, 6.0f);
    assertEquals(5.0, v1.distance(v2), DELTA);
  }

  /** Tests the rotation of a vector by degrees. */
  @Test
  public void testRotateDeg() {
    Vector2 v = Vector2.of(1.0f, 0.0f);
    Vector2 rotated = v.rotateDeg(90.0);
    assertEquals(0.0f, rotated.x(), DELTA);
    assertEquals(1.0f, rotated.y(), DELTA);
  }

  /** Tests the rotation of a vector by radians. */
  @Test
  public void testRotateRad() {
    Vector2 v = Vector2.of(1.0f, 0.0f);
    Vector2 rotated = v.rotateRad(Math.PI / 2.0);
    assertEquals(0.0f, rotated.x(), DELTA);
    assertEquals(1.0f, rotated.y(), DELTA);
  }
}
