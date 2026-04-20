package core.game.render.depth;

import core.game.render.effects.OrderedEffectRegistry;
import java.util.Optional;

/**
 * Registry facade for depth-layer effects.
 *
 * <p>The ordering and toggle mechanics are provided by {@link OrderedEffectRegistry}; this class
 * preserves the depth-specific public API and toggleable marker contract.
 */
public final class DepthLayerEffectRegistry {

  private final OrderedEffectRegistry<DepthLayerEffect> effects =
    new OrderedEffectRegistry<>(
      DepthLayerEffect::enabled,
      ToggleableDepthLayerEffect.class::isInstance,
      (effect, enabled) -> ((ToggleableDepthLayerEffect) effect).enabled(enabled));

  /**
   * Adds a depth-layer effect to the registry with the specified identifier and priority.
   *
   * @param identifier unique identifier for this effect
   * @param effect depth-layer effect to add
   * @param priority priority level; lower values are processed earlier
   * @return true if the effect was added, false if the identifier already exists
   */
  public boolean add(String identifier, DepthLayerEffect effect, int priority) {
    return effects.add(identifier, effect, priority);
  }

  /**
   * Adds a depth-layer effect to the registry with default priority 0.
   *
   * @param identifier unique identifier for this effect
   * @param effect depth-layer effect to add
   * @return true if the effect was added, false if the identifier already exists
   */
  public boolean add(String identifier, DepthLayerEffect effect) {
    return effects.add(identifier, effect);
  }

  /**
   * Removes the depth-layer effect with the specified identifier.
   *
   * @param identifier unique identifier of the effect to remove
   * @return true if an effect was removed, false if no effect exists for the identifier
   */
  public boolean remove(String identifier) {
    return effects.remove(identifier);
  }

  /**
   * Gets the depth-layer effect with the specified identifier.
   *
   * @param identifier unique identifier of the effect
   * @return effect if present
   */
  public Optional<DepthLayerEffect> get(String identifier) {
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
   * Checks whether any depth-layer effects in this registry are currently enabled.
   *
   * @return true if at least one effect is enabled
   */
  public boolean hasEnabledEffects() {
    return effects.hasEnabledEffects();
  }

  /**
   * Sets the enabled state of all toggleable depth-layer effects.
   *
   * @param enabled true to enable all effects, false to disable them
   */
  public void enableAll(boolean enabled) {
    effects.enableAll(enabled);
  }

  /** Enables all toggleable depth-layer effects. */
  public void enableAll() {
    effects.enableAll();
  }

  /** Disables all toggleable depth-layer effects. */
  public void disableAll() {
    effects.disableAll();
  }

  /**
   * Checks whether all toggleable depth-layer effects are enabled.
   *
   * @return true if at least one toggleable effect exists and all toggleable effects are enabled
   */
  public boolean allEnabled() {
    return effects.allEnabled();
  }

  /**
   * Toggles the enabled state of all toggleable depth-layer effects.
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
   * Gets all depth-layer effects sorted by priority and insertion order.
   *
   * @param onlyEnabled whether only enabled effects should be returned
   * @return iterable of effects in priority order
   */
  public Iterable<DepthLayerEffect> getSorted(boolean onlyEnabled) {
    return effects.getSorted(onlyEnabled);
  }

  /**
   * Gets all enabled depth-layer effects sorted by priority and insertion order.
   *
   * @return iterable of enabled effects in priority order
   */
  public Iterable<DepthLayerEffect> getEnabledSorted() {
    return effects.getEnabledSorted();
  }

  /**
   * Optional helper contract for mutable depth-layer effects.
   */
  public interface ToggleableDepthLayerEffect extends DepthLayerEffect {
    /**
     * Sets the enabled state of this effect.
     *
     * @param enabled true to enable the effect, false to disable it
     */
    void enabled(boolean enabled);
  }
}
