package core.game.render.effects;

import java.util.function.Predicate;

/**
 * Shared typed registry base for render effects that may implement {@link ToggleableEffect}.
 *
 * @param <E> effect type stored in the registry
 */
public abstract class ToggleableEffectRegistry<E> extends EffectRegistryFacade<E> {

  /**
   * Creates a typed effect registry for effects with optional toggle support.
   *
   * @param enabledPredicate predicate returning whether an effect is enabled
   */
  protected ToggleableEffectRegistry(Predicate<? super E> enabledPredicate) {
    super(
      enabledPredicate,
      ToggleableEffect.class::isInstance,
      (effect, enabled) -> ((ToggleableEffect<?>) effect).enabled(enabled));
  }
}
