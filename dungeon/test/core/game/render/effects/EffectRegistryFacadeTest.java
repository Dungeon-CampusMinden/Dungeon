package core.game.render.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for the typed render-effect registry facade. */
class EffectRegistryFacadeTest {

  @Test
  void exposesOrderedRegistryApiForTypedFacades() {
    TestRegistry registry = new TestRegistry();
    TestEffect early = new TestEffect("early", true, true);
    TestEffect late = new TestEffect("late", true, true);
    TestEffect disabled = new TestEffect("disabled", false, true);

    assertTrue(registry.add("late", late, 10));
    assertTrue(registry.add("early", early, -1));
    assertTrue(registry.add("disabled", disabled, -10));
    assertFalse(registry.add("early", new TestEffect("duplicate", true, true)));

    assertSame(early, registry.get("early").orElseThrow());
    assertEquals(List.of("early", "late"), names(registry.getEnabledSorted()));

    assertTrue(registry.changePriority("late", -2));
    assertFalse(registry.changePriority("missing", 0));
    assertEquals(List.of("late", "early"), names(registry.getEnabledSorted()));

    assertTrue(registry.remove("late"));
    assertTrue(registry.get("late").isEmpty());
    assertEquals(List.of("disabled", "early"), names(registry.getSorted(false)));
  }

  @Test
  void togglesOnlyEffectsAcceptedByTypedFacade() {
    TestRegistry registry = new TestRegistry();
    TestEffect toggleable = new TestEffect("toggleable", false, true);
    TestEffect immutable = new TestEffect("immutable", false, false);

    registry.add("toggleable", toggleable);
    registry.add("immutable", immutable);

    assertFalse(registry.hasEnabledEffects());
    assertTrue(registry.toggleAll());
    assertTrue(toggleable.enabled());
    assertFalse(immutable.enabled());
    assertTrue(registry.allEnabled());

    registry.disableAll();
    assertFalse(toggleable.enabled());
    assertFalse(immutable.enabled());
  }

  private static List<String> names(Iterable<TestEffect> effects) {
    List<String> names = new ArrayList<>();
    effects.forEach(effect -> names.add(effect.name()));
    return names;
  }

  private static final class TestRegistry extends EffectRegistryFacade<TestEffect> {
    private TestRegistry() {
      super(TestEffect::enabled, TestEffect::toggleable, TestEffect::enabled);
    }
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
