package core.game.render.level;

import core.game.render.effects.OrderedEffectRegistry;
import java.util.Optional;

/**
 * Registry facade for level-pass effects.
 *
 * <p>The ordering and toggle mechanics are provided by {@link OrderedEffectRegistry}; this class
 * preserves the level-specific public API and toggleable marker contract.
 */
public final class LevelEffectRegistry {

  private final OrderedEffectRegistry<LevelEffect> effects =
    new OrderedEffectRegistry<>(
      LevelEffect::enabled,
      ToggleableLevelEffect.class::isInstance,
      (effect, enabled) -> ((ToggleableLevelEffect) effect).enabled(enabled));

  /**
   * Adds a level effect to the registry with the specified identifier and priority.
   *
   * @param identifier unique identifier for this effect
   * @param effect level effect to add
   * @param priority priority level; lower values are processed earlier
   * @return true if the effect was added, false if the identifier already exists
   */
  public boolean add(String identifier, LevelEffect effect, int priority) {
    return effects.add(identifier, effect, priority);
  }

  /**
   * Adds a level effect to the registry with default priority 0.
   *
   * @param identifier unique identifier for this effect
   * @param effect level effect to add
   * @return true if the effect was added, false if the identifier already exists
   */
  public boolean add(String identifier, LevelEffect effect) {
    return effects.add(identifier, effect);
  }

  /**
   * Removes the level effect with the specified identifier.
   *
   * @param identifier unique identifier of the effect to remove
   * @return true if an effect was removed, false if no effect exists for the identifier
   */
  public boolean remove(String identifier) {
    return effects.remove(identifier);
  }

  /**
   * Gets the level effect with the specified identifier.
   *
   * @param identifier unique identifier of the effect
   * @return effect if present
   */
  public Optional<LevelEffect> get(String identifier) {
    return effects.get(identifier);
  }

  /**
   * Changes the priority of an existing effect.
   *
   * @param identifier unique identifier of the effect to update
   * @param newPriority new priority level
   * @return true if the priority was changed, false if no effect exists
   */
  public boolean changePriority(String identifier, int newPriority) {
    return effects.changePriority(identifier, newPriority);
  }

  /**
   * Checks whether any level effects in this registry are currently enabled.
   *
   * @return true if at least one effect is enabled
   */
  public boolean hasEnabledEffects() {
    return effects.hasEnabledEffects();
  }

  /**
   * Sets the enabled state of all toggleable level effects.
   *
   * @param enabled true to enable all effects, false to disable them
   */
  public void enableAll(boolean enabled) {
    effects.enableAll(enabled);
  }

  /** Enables all toggleable level effects. */
  public void enableAll() {
    effects.enableAll();
  }

  /** Disables all toggleable level effects. */
  public void disableAll() {
    effects.disableAll();
  }

  /**
   * Checks whether all toggleable level effects are enabled.
   *
   * @return true if at least one toggleable effect exists and all toggleable effects are enabled
   */
  public boolean allEnabled() {
    return effects.allEnabled();
  }

  /**
   * Toggles the enabled state of all toggleable level effects.
   *
   * @return the new enabled state after toggling
   */
  public boolean toggleAll() {
    return effects.toggleAll();
  }

  /** Clears all effects from this registry. */
  public void clear() {
    effects.clear();
  }

  /**
   * Checks whether this registry contains no effects.
   *
   * @return true if the registry is empty
   */
  public boolean isEmpty() {
    return effects.isEmpty();
  }

  /**
   * Gets all level effects sorted by priority and insertion order.
   *
   * @param onlyEnabled whether only enabled effects should be returned
   * @return iterable of effects in priority order
   */
  public Iterable<LevelEffect> getSorted(boolean onlyEnabled) {
    return effects.getSorted(onlyEnabled);
  }

  /**
   * Gets all enabled level effects sorted by priority and insertion order.
   *
   * @return iterable of enabled effects in priority order
   */
  public Iterable<LevelEffect> getEnabledSorted() {
    return effects.getEnabledSorted();
  }

  /**
   * Optional helper contract for mutable level-pass effects.
   */
  public interface ToggleableLevelEffect extends LevelEffect {
    /**
     * Sets the enabled state of this effect.
     *
     * @param enabled true to enable the effect, false to disable it
     */
    void enabled(boolean enabled);
  }
}
