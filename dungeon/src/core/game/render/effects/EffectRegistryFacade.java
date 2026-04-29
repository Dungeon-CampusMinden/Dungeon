package core.game.render.effects;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Typed facade around {@link OrderedEffectRegistry} for render-effect registries.
 *
 * @param <E> effect type stored in the registry
 */
public abstract class EffectRegistryFacade<E> {

  private final OrderedEffectRegistry<E> effects;

  /**
   * Creates a typed effect-registry facade.
   *
   * @param enabledPredicate predicate returning whether an effect is enabled
   * @param toggleablePredicate predicate returning whether an effect can be toggled by the registry
   * @param enabledSetter setter used to toggle mutable effects
   */
  protected EffectRegistryFacade(
      Predicate<? super E> enabledPredicate,
      Predicate<? super E> toggleablePredicate,
      BiConsumer<? super E, Boolean> enabledSetter) {
    this.effects =
        new OrderedEffectRegistry<>(enabledPredicate, toggleablePredicate, enabledSetter);
  }

  /**
   * Adds an effect to the registry with the specified identifier and priority.
   *
   * @param identifier unique effect identifier
   * @param effect effect to add
   * @param priority priority level; lower values are processed earlier
   * @return true if the effect was added, false if the identifier already exists
   */
  public boolean add(String identifier, E effect, int priority) {
    return effects.add(identifier, effect, priority);
  }

  /**
   * Adds an effect to the registry with default priority 0.
   *
   * @param identifier unique effect identifier
   * @param effect effect to add
   * @return true if the effect was added, false if the identifier already exists
   */
  public boolean add(String identifier, E effect) {
    return effects.add(identifier, effect);
  }

  /**
   * Removes an effect by identifier.
   *
   * @param identifier unique effect identifier
   * @return true if an effect was removed, false if no effect exists for the identifier
   */
  public boolean remove(String identifier) {
    return effects.remove(identifier);
  }

  /**
   * Returns an effect by identifier.
   *
   * @param identifier unique effect identifier
   * @return effect if present
   */
  public Optional<E> get(String identifier) {
    return effects.get(identifier);
  }

  /**
   * Changes the priority of an existing effect.
   *
   * @param identifier unique effect identifier
   * @param newPriority new priority level
   * @return true if the priority was changed or already matched, false if no effect exists
   */
  public boolean changePriority(String identifier, int newPriority) {
    return effects.changePriority(identifier, newPriority);
  }

   /**
    * Checks whether any registered effect is enabled.
    *
    * @return true if at least one effect is enabled
    */
   public boolean hasEnabledEffects() {
     return effects.hasEnabledEffects();
   }

   /**
    * Checks whether no registered effects are enabled (all effects are disabled).
    *
    * @return true if all effects are disabled
    */
   public boolean hasNoEnabledEffects() {
     return effects.hasNoEnabledEffects();
   }

  /**
   * Sets the enabled state for all toggleable effects.
   *
   * @param enabled true to enable all toggleable effects, false to disable them
   */
  public void enableAll(boolean enabled) {
    effects.enableAll(enabled);
  }

  /** Disables all toggleable effects. */
  public void disableAll() {
    effects.disableAll();
  }

  /**
   * Checks whether all toggleable effects are enabled.
   *
   * @return true if at least one toggleable effect exists and all toggleable effects are enabled
   */
  public boolean allEnabled() {
    return effects.allEnabled();
  }

  /**
   * Toggles all toggleable effects.
   *
   * @return the new enabled state that was applied
   */
  public boolean toggleAll() {
    return effects.toggleAll();
  }

  /** Removes all effects from this registry. */
  public void clear() {
    effects.clear();
  }

  /**
   * Checks whether the registry is empty.
   *
   * @return true if no effects are registered
   */
  public boolean isEmpty() {
    return effects.isEmpty();
  }

  /**
   * Returns effects sorted by priority and insertion order.
   *
   * @param onlyEnabled whether only enabled effects should be returned
   * @return iterable view over sorted effects
   */
  public Iterable<E> getSorted(boolean onlyEnabled) {
    return effects.getSorted(onlyEnabled);
  }

  /**
   * Returns enabled effects sorted by priority and insertion order.
   *
   * @return iterable view over enabled effects
   */
  public Iterable<E> getEnabledSorted() {
    return effects.getEnabledSorted();
  }
}
