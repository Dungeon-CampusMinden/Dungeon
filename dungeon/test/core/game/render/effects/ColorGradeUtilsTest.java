package core.game.render.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Tests for the shared HSV-style color grading helpers. */
class ColorGradeUtilsTest {

  @Test
  void normalizeTargetHueKeepsNegativeHueAndWrapsPositiveHue() {
    assertEquals(-1.0f, ColorGradeUtils.normalizeTargetHue(-0.25f));
    assertEquals(0.25f, ColorGradeUtils.normalizeTargetHue(1.25f));
    assertEquals(0.75f, ColorGradeUtils.normalizeTargetHue(-0.25f + 1.0f));
  }

  @Test
  void clampMultiplierOnlyClampsNegativeValues() {
    assertEquals(0.0f, ColorGradeUtils.clampMultiplier(-0.5f));
    assertEquals(1.5f, ColorGradeUtils.clampMultiplier(1.5f));
  }

  @Test
  void gradeArgbAppliesTargetHue() {
    int red = 0xFFFF0000;

    assertEquals(0xFF0000FF, ColorGradeUtils.gradeArgb(red, 2.0f / 3.0f, 1.0f, 1.0f));
  }

  @Test
  void gradeArgbKeepsHueWhenTargetHueIsNegative() {
    int red = 0xFFFF0000;

    assertEquals(0xFFFFFFFF, ColorGradeUtils.gradeArgb(red, -1.0f, 0.0f, 1.0f));
  }

  @Test
  void gradeArgbClearsTransparentPixels() {
    assertEquals(0x00000000, ColorGradeUtils.gradeArgb(0x00123456, 0.5f, 1.0f, 1.0f));
  }
}
