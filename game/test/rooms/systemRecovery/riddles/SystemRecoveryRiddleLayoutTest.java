package rooms.systemRecovery.riddles;

import static org.junit.jupiter.api.Assertions.assertEquals;

import engine.utils.Point;
import org.junit.jupiter.api.Test;

/** Guards the distinct matrix layouts used by riddles 8 and 9. */
public class SystemRecoveryRiddleLayoutTest {

  /** Riddle 8 uses the twelve storage cells from the 3x4 custom-point grid. */
  @Test
  public void twoDimensionalStorageUsesThreeByFour_riddle8() {
    assertEquals(3, TwoDimensionalStorageRiddle.ROW_COUNT);
    assertEquals(4, TwoDimensionalStorageRiddle.COLUMN_COUNT);
  }

  /** Riddle 9 creates an inclusive 7x4 matrix from its two corner points. */
  @Test
  public void searchRobotUsesCornerPoints_riddle9() {
    SearchRobotMatrix matrix = SearchRobotMatrix.between(new Point(23, 41), new Point(29, 38));

    assertEquals(4, matrix.rows());
    assertEquals(7, matrix.columns());
    assertEquals(28, matrix.size());
    assertEquals(new Point(23, 41), matrix.pointAt(0));
    assertEquals(new Point(29, 38), matrix.pointAt(matrix.size() - 1));
  }

  /** The final central-computer map uses the designer's inclusive 3x5 corner points. */
  @Test
  public void systemCoreUsesThreeByFiveMap_riddle10() {
    SearchRobotMatrix matrix = SearchRobotMatrix.between(new Point(12, 7), new Point(16, 5));

    assertEquals(3, matrix.rows());
    assertEquals(5, matrix.columns());
    assertEquals(15, matrix.size());
    assertEquals(new Point(12, 7), matrix.pointAt(0));
    assertEquals(new Point(16, 5), matrix.pointAt(matrix.size() - 1));
  }
}
