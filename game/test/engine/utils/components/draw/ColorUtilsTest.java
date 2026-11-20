package engine.utils.components.draw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import com.badlogic.gdx.graphics.Color;
import org.junit.jupiter.api.Test;

// tests for @link ColorUtils
class ColorUtilsTest {

  private static final float DELTA = 1e-6f;

  private static void assertColor(float r, float g, float b, float a, Color actual) {
    assertEquals(r, actual.r, DELTA, "red");
    assertEquals(g, actual.g, DELTA, "green");
    assertEquals(b, actual.b, DELTA, "blue");
    assertEquals(a, actual.a, DELTA, "alpha");
  }

  @Test
  void pmaColorMultipliesRgbWithAlpha() {
    Color pma = ColorUtils.pmaColor(new Color(1f, 0.5f, 0.2f, 0.5f));
    assertColor(0.5f, 0.25f, 0.1f, 0.5f, pma);
  }

  @Test
  void pmaColorKeepsAnOpaqueColor() {
    Color pma = ColorUtils.pmaColor(new Color(0.3f, 0.6f, 0.9f, 1f));
    assertColor(0.3f, 0.6f, 0.9f, 1f, pma);
  }

  @Test
  void pmaColorOfAFullyTransparentColorIsEmpty() {
    Color pma = ColorUtils.pmaColor(new Color(0.3f, 0.6f, 0.9f, 0f));
    assertColor(0f, 0f, 0f, 0f, pma);
  }

  @Test
  void pmaColorReturnsANewColor() {
    Color original = new Color(1f, 1f, 1f, 0.5f);
    Color pma = ColorUtils.pmaColor(original);
    assertNotSame(original, pma);
    assertColor(1f, 1f, 1f, 0.5f, original);
  }

  @Test
  void pmaColorReadsRgba8888() {
    // red 255, green 0, blue 0, alpha 128
    Color pma = ColorUtils.pmaColor(0xFF000080);
    float alpha = 128 / 255f;
    assertColor(alpha, 0f, 0f, alpha, pma);
  }

  @Test
  void withAlphaReplacesOnlyTheAlpha() {
    Color original = new Color(0.2f, 0.4f, 0.6f, 1f);
    Color result = ColorUtils.withAlpha(original, 0.3f);
    assertColor(0.2f, 0.4f, 0.6f, 0.3f, result);
    assertColor(0.2f, 0.4f, 0.6f, 1f, original);
  }
}
