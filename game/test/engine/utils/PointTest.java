package engine.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.level.utils.Coordinate;
import org.junit.jupiter.api.Test;

/** Tests for {@link Point}. */
class PointTest {

  @Test
  void distanceFromOriginToThreeFourIsFive() {
    Point start = new Point(0, 0);
    Point end = new Point(3, 4);

    assertEquals(5f, Point.calculateDistance(start, end));
    assertEquals(5.0, start.distance(end));
  }

  @Test
  void squaredDistanceFromOriginToThreeFourIsTwentyFive() {
    Point start = new Point(0, 0);
    Point end = new Point(3, 4);

    assertEquals(25.0, start.distanceSquared(end));
  }

  @Test
  void inRangeIncludesTheExactDistance() {
    Point start = new Point(0, 0);
    Point end = new Point(3, 4);

    assertTrue(Point.inRange(start, end, 6));
    assertTrue(Point.inRange(start, end, 5));
    assertFalse(Point.inRange(start, end, 4));
  }

  @Test
  void translationAndVectorToUseTheSameDisplacement() {
    Point start = new Point(1, 2);
    Vector2 step = Vector2.of(3, -1);
    Point end = new Point(4, 1);

    assertEquals(end, start.translate(step));
    assertEquals(end, start.translate(3, -1));
    assertEquals(step, start.vectorTo(end));
  }

  @Test
  void convertsAPointToItsTileCoordinatesAndCenter() {
    Point point = new Point(-1.75f, 2.75f);

    assertEquals(new Coordinate(-1, 2), point.toCoordinate());
    assertEquals(new Point(-0.5f, 2.5f), point.toCenteredPoint());
  }

  @Test
  void floorRoundsNegativeValuesTowardZero() {
    Point point = new Point(-1.75f, 2.75f);

    assertEquals(new Point(-1, 2), point.floor());
  }
}
