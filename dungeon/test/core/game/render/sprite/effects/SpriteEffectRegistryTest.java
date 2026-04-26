package core.game.render.sprite.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.game.render.effects.ToggleableEffect;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for the sprite-specific ordered render-effect registry facade. */
class SpriteEffectRegistryTest {

  @Test
  void getEnabledSortedUsesPriorityThenInsertionOrder() {
    SpriteEffectRegistry registry = new SpriteEffectRegistry();

    registry.add("late", new TestEffect("late", true), 10);
    registry.add("early", new TestEffect("early", true), -1);
    registry.add("same", new TestEffect("same", true), -1);
    registry.add("disabled", new TestEffect("disabled", false), -10);

    assertEquals(List.of("early", "same", "late"), names(registry.getEnabledSorted()));
    assertEquals(List.of("disabled", "early", "same", "late"), names(registry.getSorted(false)));
  }

  @Test
  void removeReturnsWhetherEffectWasPresentAndUpdatesSortedView() {
    SpriteEffectRegistry registry = new SpriteEffectRegistry();
    TestEffect first = new TestEffect("first", true);
    TestEffect second = new TestEffect("second", true);

    registry.add("first", first, 0);
    registry.add("second", second, 1);

    assertSame(second, registry.get("second").orElseThrow());
    assertTrue(registry.remove("second"));
    assertTrue(registry.get("second").isEmpty());
    assertFalse(registry.remove("missing"));
    assertEquals(List.of("first"), names(registry.getEnabledSorted()));
  }

  @Test
  void changePriorityDelegatesToSharedRegistry() {
    SpriteEffectRegistry registry = new SpriteEffectRegistry();

    registry.add("first", new TestEffect("first", true), 0);
    registry.add("second", new TestEffect("second", true), 1);

    assertTrue(registry.changePriority("second", -1));
    assertFalse(registry.changePriority("missing", -1));
    assertEquals(List.of("second", "first"), names(registry.getEnabledSorted()));
  }

  @Test
  void toggleAllOnlyChangesToggleableSpriteEffects() {
    SpriteEffectRegistry registry = new SpriteEffectRegistry();
    ToggleableTestEffect toggleable = new ToggleableTestEffect("toggleable", false);
    TestEffect immutable = new TestEffect("immutable", false);

    registry.add("toggleable", toggleable);
    registry.add("immutable", immutable);

    assertFalse(registry.allEnabled());
    assertTrue(registry.toggleAll());
    assertTrue(toggleable.enabled());
    assertFalse(immutable.enabled());
    assertTrue(registry.allEnabled());

    registry.disableAll();
    assertFalse(toggleable.enabled());
    assertFalse(immutable.enabled());
  }

  private static List<String> names(Iterable<SpriteEffect> effects) {
    List<String> names = new ArrayList<>();
    effects.forEach(effect -> names.add(((NamedEffect) effect).name()));
    return names;
  }

  private interface NamedEffect extends SpriteEffect {
    String name();
  }

  private static class TestEffect implements NamedEffect {
    private final String name;
    protected boolean enabled;

    private TestEffect(String name, boolean enabled) {
      this.name = name;
      this.enabled = enabled;
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public boolean enabled() {
      return enabled;
    }

    @Override
    public BufferedImage apply(BufferedImage input, long nowMs) {
      return input;
    }
  }

  private static final class ToggleableTestEffect extends TestEffect
      implements ToggleableEffect<ToggleableTestEffect> {

    private ToggleableTestEffect(String name, boolean enabled) {
      super(name, enabled);
    }

    @Override
    public ToggleableTestEffect enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }
  }
}
