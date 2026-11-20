package engine.utils.components.draw.animation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

// tests for @link SpritesheetConfig
class SpritesheetConfigTest {

  private static void assertConfig(
      int x, int y, int rows, int columns, int width, int height, SpritesheetConfig actual) {
    assertEquals(x, actual.x(), "x");
    assertEquals(y, actual.y(), "y");
    assertEquals(rows, actual.rows(), "rows");
    assertEquals(columns, actual.columns(), "columns");
    assertEquals(width, actual.spriteWidth(), "spriteWidth");
    assertEquals(height, actual.spriteHeight(), "spriteHeight");
  }

  @Test
  void defaultConstructorUsesOneSpriteOf16x16() {
    assertConfig(0, 0, 1, 1, 16, 16, new SpritesheetConfig());
  }

  // parameter order of the constructor: x, y, rows, columns, width, height
  @Test
  void fullConstructorKeepsAllValues() {
    assertConfig(1, 2, 3, 4, 5, 6, new SpritesheetConfig(1, 2, 3, 4, 5, 6));
  }

  @Test
  void gridConstructorUsesDefaultSpriteSize() {
    assertConfig(1, 2, 3, 4, 16, 16, new SpritesheetConfig(1, 2, 3, 4));
  }

  @Test
  void offsetConstructorUsesDefaultGridAndSpriteSize() {
    assertConfig(1, 2, 1, 1, 16, 16, new SpritesheetConfig(1, 2));
  }

  @Test
  void settersChangeValuesAndAllowChaining() {
    SpritesheetConfig config = new SpritesheetConfig();
    SpritesheetConfig returned = config.x(1).y(2).rows(3).columns(4).spriteWidth(5).spriteHeight(6);
    assertSame(config, returned);
    assertConfig(1, 2, 3, 4, 5, 6, config);
  }

  @Test
  void cloneIsAnIndependentCopy() throws CloneNotSupportedException {
    SpritesheetConfig original = new SpritesheetConfig(1, 2, 3, 4, 5, 6);
    SpritesheetConfig copy = original.clone();
    assertNotSame(original, copy);
    assertConfig(1, 2, 3, 4, 5, 6, copy);
    copy.x(99);
    assertEquals(1, original.x());
  }
}
