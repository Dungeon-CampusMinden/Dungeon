package core.game.render.sprite.effects;

import core.game.render.effects.OrderedEffectRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Registry facade for sprite effects.
 *
 * <p>The ordering and toggle mechanics are provided by {@link OrderedEffectRegistry}; this class
 * preserves the sprite-specific public API and toggleable marker contract.
 */
public final class SpriteEffectRegistry {

  private final OrderedEffectRegistry<SpriteEffect> effects =
    new OrderedEffectRegistry<>(
      SpriteEffect::enabled,
      ToggleableSpriteEffect.class::isInstance,
      (effect, enabled) -> ((ToggleableSpriteEffect) effect).enabled(enabled));

  /**
   * Adds a sprite effect to the registry with the specified identifier and priority.
   *
   * @param identifier unique identifier for this effect
   * @param effect sprite effect to add
   * @param priority priority level; lower values are processed earlier
   * @return true if the effect was added, false if the identifier already exists
   */
  public boolean add(String identifier, SpriteEffect effect, int priority) {
    return effects.add(identifier, effect, priority);
  }

  /**
   * Adds a sprite effect to the registry with default priority 0.
   *
   * @param identifier unique identifier for this effect
   * @param effect sprite effect to add
   * @return true if the effect was added, false if the identifier already exists
   */
  public boolean add(String identifier, SpriteEffect effect) {
    return effects.add(identifier, effect);
  }

  /**
   * Removes the sprite effect with the specified identifier.
   *
   * @param identifier unique identifier of the effect to remove
   * @return removed effect if present
   */
  public Optional<SpriteEffect> remove(String identifier) {
    Optional<SpriteEffect> removed = effects.get(identifier);
    effects.remove(identifier);
    return removed;
  }

  /**
   * Gets the sprite effect with the specified identifier.
   *
   * @param identifier unique identifier of the effect
   * @return effect if present
   */
  public Optional<SpriteEffect> get(String identifier) {
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
   * Checks whether any sprite effects in this registry are currently enabled.
   *
   * @return true if at least one effect is enabled
   */
  public boolean hasEnabledEffects() {
    return effects.hasEnabledEffects();
  }

  /**
   * Sets the enabled state of all toggleable sprite effects.
   *
   * @param enabled true to enable all effects, false to disable them
   */
  public void enableAll(boolean enabled) {
    effects.enableAll(enabled);
  }

  /** Enables all toggleable sprite effects. */
  public void enableAll() {
    effects.enableAll();
  }

  /** Disables all toggleable sprite effects. */
  public void disableAll() {
    effects.disableAll();
  }

  /**
   * Checks whether all toggleable sprite effects are enabled.
   *
   * @return true if at least one toggleable effect exists and all toggleable effects are enabled
   */
  public boolean allEnabled() {
    return effects.allEnabled();
  }

  /**
   * Toggles the enabled state of all toggleable sprite effects.
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
   * Gets all sprite effects sorted by priority and insertion order.
   *
   * @param onlyEnabled whether only enabled effects should be returned
   * @return iterable of effects in priority order
   */
  public Iterable<SpriteEffect> getSorted(boolean onlyEnabled) {
    return effects.getSorted(onlyEnabled);
  }

  /**
   * Gets all enabled sprite effects sorted by priority and insertion order.
   *
   * @return list of enabled effects in priority order
   */
  public List<SpriteEffect> getEnabledSorted() {
    List<SpriteEffect> sorted = new ArrayList<>();
    effects.getEnabledSorted().forEach(sorted::add);
    return sorted;
  }
}
