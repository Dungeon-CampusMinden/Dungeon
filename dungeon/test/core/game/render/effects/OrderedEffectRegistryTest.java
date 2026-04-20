package core.game.render.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for the shared ordered render-effect registry. */
class OrderedEffectRegistryTest {

  @Test
  void getEnabledSortedUsesPriorityThenInsertionOrder() {
    OrderedEffectRegistry<TestEffect> registry = newRegistry();

    registry.add("late", new TestEffect("late", true, true), 10);
    registry.add("early", new TestEffect("early", true, true), -1);
    registry.add("same", new TestEffect("same", true, true), -1);
    registry.add("disabled", new TestEffect("disabled", false, true), -10);

    assertEquals(List.of("early", "same", "late"), names(registry.getEnabledSorted()));
    assertEquals(
      List.of("disabled", "early", "same", "late"), names(registry.getSorted(false)));
  }

  @Test
  void toggleAllOnlyChangesToggleableEffects() {
    OrderedEffectRegistry<TestEffect> registry = newRegistry();
    TestEffect toggleable = new TestEffect("toggleable", false, true);
    TestEffect immutable = new TestEffect("immutable", false, false);

    registry.add("toggleable", toggleable);
    registry.add("immutable", immutable);

    assertFalse(registry.allEnabled());
    assertTrue(registry.toggleAll());
    assertTrue(toggleable.enabled());
    assertFalse(immutable.enabled());
    assertTrue(registry.allEnabled());

    assertFalse(registry.toggleAll());
    assertFalse(toggleable.enabled());
    assertFalse(immutable.enabled());
  }

  @Test
  void changePriorityAndRemoveUpdateSortedView() {
    OrderedEffectRegistry<TestEffect> registry = newRegistry();

    registry.add("first", new TestEffect("first", true, true), 0);
    registry.add("second", new TestEffect("second", true, true), 1);

    assertTrue(registry.changePriority("second", -1));
    assertEquals(List.of("second", "first"), names(registry.getEnabledSorted()));

    assertTrue(registry.remove("second"));
    assertTrue(registry.get("second").isEmpty());
    assertEquals(List.of("first"), names(registry.getEnabledSorted()));
  }

  private static OrderedEffectRegistry<TestEffect> newRegistry() {
    return new OrderedEffectRegistry<>(
      TestEffect::enabled,
      TestEffect::toggleable,
      (effect, enabled) -> effect.enabled(enabled));
  }

  private static List<String> names(Iterable<TestEffect> effects) {
    List<String> names = new ArrayList<>();
    effects.forEach(effect -> names.add(effect.name()));
    return names;
  }

  private static final class TestEffect {
    private final String name;
    private final boolean toggleable;
    private boolean enabled;

    private TestEffect(String name, boolean enabled, boolean toggleable) {
      this.name = name;
      this.enabled = enabled;
      this.toggleable = toggleable;
    }

    private String name() {
      return name;
    }

    private boolean enabled() {
      return enabled;
    }

    private void enabled(boolean enabled) {
      this.enabled = enabled;
    }

    private boolean toggleable() {
      return toggleable;
    }
  }
}
