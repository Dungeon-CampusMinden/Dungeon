package core.game.render.scene;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/** Tests for {@link PassthroughDebugEffect}. */
class PassthroughDebugEffectTest {

  @Test
  void enabledSetterIsFluent() {
    PassthroughDebugEffect effect = new PassthroughDebugEffect();

    assertSame(effect, effect.enabled(false));
  }
}
