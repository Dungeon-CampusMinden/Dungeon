package core.game.render.scene;

import static org.junit.jupiter.api.Assertions.assertSame;

import contrib.debug.effects.DebugPassthroughEffect;
import org.junit.jupiter.api.Test;

/** Tests for {@link DebugPassthroughEffect}. */
class DebugPassthroughEffectTest {

  @Test
  void enabledSetterIsFluent() {
    DebugPassthroughEffect effect = new DebugPassthroughEffect();

    assertSame(effect, effect.enabled(false));
  }
}
