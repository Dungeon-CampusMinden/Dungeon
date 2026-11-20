package engine.utils.components.draw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.utils.Vector2;
import org.junit.jupiter.api.Test;

// tests for @link DrawConfig
class DrawConfigTest {

  @Test
  void defaultConstructorUsesNeutralValues() {
    DrawConfig config = new DrawConfig();
    assertEquals(Vector2.ZERO, config.offset());
    assertEquals(Vector2.ONE, config.size());
    assertEquals(Vector2.ONE, config.scale());
    assertEquals(-1, config.tintColor());
    assertFalse(config.mirrored());
    assertEquals(0f, config.rotation());
  }

  @Test
  void missingVectorsAreReplacedByDefaults() {
    DrawConfig config = new DrawConfig(null, null, null, 7, true, 90f);
    assertEquals(Vector2.ZERO, config.offset());
    assertEquals(Vector2.ONE, config.size());
    assertEquals(Vector2.ONE, config.scale());
    assertEquals(7, config.tintColor());
    assertTrue(config.mirrored());
    assertEquals(90f, config.rotation());
  }

  @Test
  void offsetConstructorKeepsDefaultSizeAndScale() {
    DrawConfig config = new DrawConfig(2f, 3f, 7);
    assertEquals(Vector2.of(2, 3), config.offset());
    assertEquals(Vector2.ONE, config.size());
    assertEquals(Vector2.ONE, config.scale());
    assertEquals(7, config.tintColor());
    assertFalse(config.mirrored());
    assertEquals(0f, config.rotation());
  }

  @Test
  void offsetAndSizeConstructorKeepsDefaultScale() {
    DrawConfig config = new DrawConfig(2f, 3f, 4f, 5f, 7);
    assertEquals(Vector2.of(2, 3), config.offset());
    assertEquals(Vector2.of(4, 5), config.size());
    assertEquals(Vector2.ONE, config.scale());
    assertEquals(7, config.tintColor());
  }

  @Test
  void withMethodsChangeOnlyOneValue() {
    DrawConfig base = new DrawConfig(2f, 3f, 4f, 5f, 7);
    Vector2 offset = base.offset();
    Vector2 size = base.size();
    Vector2 scale = Vector2.of(2, 2);
    assertEquals(
        new DrawConfig(offset, size, scale, 7, false, 0f), base.withScale(Vector2.of(2, 2)));
    assertEquals(new DrawConfig(offset, size, Vector2.ONE, 9, false, 0f), base.withTintColor(9));
    assertEquals(new DrawConfig(offset, size, Vector2.ONE, 7, true, 0f), base.withMirrored(true));
    assertEquals(new DrawConfig(offset, size, Vector2.ONE, 7, false, 45f), base.withRotation(45f));
  }

  @Test
  void withMethodsDoNotChangeTheOriginal() {
    DrawConfig base = new DrawConfig();
    base.withScale(Vector2.of(2, 2));
    base.withTintColor(9);
    base.withMirrored(true);
    base.withRotation(45f);
    assertEquals(new DrawConfig(), base);
  }

  @Test
  void withScaleNullFallsBackToOne() {
    assertEquals(Vector2.ONE, new DrawConfig().withScale(null).scale());
  }
}
