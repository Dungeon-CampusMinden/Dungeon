package core.game.render.effects;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/** Tests for shared color-grade effect configuration behavior. */
class BaseColorGradeEffectTest {

  @Test
  void saturationMultiplierSetterIsFluent() {
    TestColorGradeEffect effect = new TestColorGradeEffect();

    assertSame(effect, effect.saturationMultiplier(0.5f));
  }

  @Test
  void valueMultiplierSetterIsFluent() {
    TestColorGradeEffect effect = new TestColorGradeEffect();

    assertSame(effect, effect.valueMultiplier(0.5f));
  }

  @Test
  void enabledSetterIsFluent() {
    TestColorGradeEffect effect = new TestColorGradeEffect();

    assertSame(effect, effect.enabled(false));
  }

  private static final class TestColorGradeEffect
    extends BaseColorGradeEffect<TestColorGradeEffect> {

    @Override
    protected TestColorGradeEffect self() {
      return this;
    }
  }
}
