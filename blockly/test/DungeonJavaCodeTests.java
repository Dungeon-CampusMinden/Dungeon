import core.utils.Point;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DungeonJavaCodeTests extends DungeonCompilerTestBase {
  @Test
  void testHelloWorld() throws InterruptedException, IOException {
    sendCode(
        """
          public static void main() {
            IO.println("Hello, world!");
            Hero.move();
        }
        """);
    waitForCompletion();
  }

  @Test
  void runInCircleFiveTimes() throws InterruptedException, IOException {
    Point start = playerPosition();
    var startDirection = playerDirection();
    sendCode(
        """
            public static void main() {
              for (int i = 0; i < 25; i++) {
                Hero.move();
                Hero.turnLeft();
              }
            }
          """);
    waitForCompletion();
    assertNear(
        start.translate(startDirection.x(), startDirection.y()),
        playerPosition(),
        "move-then-turnLeft should end exactly one tile in the hero's initial facing direction");
  }

  @Test
  void turnThenMoveAlsoWorks() throws InterruptedException, IOException {
    Point start = playerPosition();
    var expectedDirection = playerDirection().turnLeft();

    sendCode(
        """
            public static void main() {
              for (int i = 0; i < 25; i++) {
                Hero.turnLeft();
                Hero.move();
              }
            }
          """);
    waitForCompletion();

    assertNear(
        start.translate(expectedDirection.x(), expectedDirection.y()),
        playerPosition(),
        "turnLeft-then-move should end exactly one tile in the turned direction");
  }

  @Test
  void complexMovement() throws InterruptedException, IOException {
    Point start = playerPosition();
    var expectedDirection = playerDirection().turnLeft().turnLeft();
    sendCode(
        """
            public static void main() {
              for (int x = 0; x < 4; x++) {
                for (int i = 0; i < 5; i++) {
                  Hero.move();
                  Hero.turnLeft();
                }
                for (int i = 0; i < 5; i++) {
                  Hero.turnLeft();
                  Hero.move();
                }
              }
              Hero.rest();
            }
          """);
    waitForCompletion();
    assertNear(
        start,
        playerPosition(),
        "After a complex movement pattern, the hero should end near the starting position");
  }

  private static void assertNear(Point expected, Point actual, String message) {
    assertTrue(
        actual.distance(expected) <= 0.05,
        message + " (expected " + expected + ", actual " + actual + ")");
  }
}
