package contrib.components;

import contrib.systems.HealthSystem;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Component;
import core.Entity;
import core.Game;
import core.utils.logging.CustomLogLevel;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Allow an associated entity to take damage and to die.
 *
 * <p>The component also tracks the received damage via the {@link #receiveHit(Damage) receiveHit}
 * method. The damage is not applied immediately, instead it is enqueued in the {@link HealthSystem}
 * and processed there at the end of the tick.
 *
 * <p>To calculate the damage received, the {@link HealthSystem} calls the {@link
 * #calculateDamageOf(DamageType)} method for each {@link DamageType} and calculates the sum of the
 * damage. Next, the {@link HealthSystem} reduces the {@link #currentHealthpoints} by this value and
 * calls {@link #clearDamage()} to discard all pendig damage for this component. When the health
 * points drop to 0 or less, the system calls {@link #triggerOnDeath(Entity)}.
 *
 * <p>To determine the last cause of damage, the {@link #lastDamageCause()} method can be used.
 */
public final class HealthComponent implements Component {
  private static final Consumer<Entity> REMOVE_DEAD_ENTITY = Game::remove;
  private Consumer<Entity> onDeath;
  private final Logger LOGGER = Logger.getLogger(this.getClass().getName());
  private int maximalHealthpoints;
  private int currentHealthpoints;
  private boolean godMode = false;

  /**
   * Create a new HealthComponent.
   *
   * @param maximalHitPoints Maximum amount of health points; currentHitPoints cannot be greater
   *     than that
   * @param onDeath Function that gets called when this entity dies
   */
  public HealthComponent(int maximalHitPoints, final Consumer<Entity> onDeath) {
    this.maximalHealthpoints = maximalHitPoints;
    this.currentHealthpoints = maximalHitPoints;
    this.onDeath = onDeath;
  }

  /**
   * Create a new HealthComponent.
   *
   * <p>onDeath function will remove the entity from * the game.
   *
   * @param maximalHitPoints Maximum amount of health points; currentHitPoints cannot be greater
   *     than that
   */
  public HealthComponent(int maximalHitPoints) {
    this(maximalHitPoints, REMOVE_DEAD_ENTITY);
  }

  /**
   * Create a HealthComponent with default values.
   *
   * <p>The maximum health points are set to 1, and the onDeath function will remove the entity from
   * the game.
   */
  public HealthComponent() {
    this(1, REMOVE_DEAD_ENTITY);
  }

  /**
   * Add damage, which is accounted for by the {@link HealthSystem}.
   *
   * <p>The {@link HealthSystem} will reduce the current health points based on the received damage.
   * The damage will not be applied immediately. Instead, it is enqueued in the system and processed
   * during the next execution cycle.
   *
   * @param damage Damage that should be inflicted
   */
  public void receiveHit(Damage damage) {
    HealthSystem.enqueueDamage(this, damage);
  }

  /**
   * Trigger the onDeath function.
   *
   * @param entity associated entity of this component.
   */
  public void triggerOnDeath(final Entity entity) {
    onDeath.accept(entity);
  }

  /**
   * Set the onDeath function.
   *
   * <p>This function will be called when the associated entity dies.
   *
   * @param onDeath A Consumer function that takes an Entity as input.
   */
  public void onDeath(Consumer<Entity> onDeath) {
    this.onDeath = onDeath;
  }

  /**
   * Calculate the amount of damage to a certain type.
   *
   * <p>Delegates to the {@link HealthSystem}, which aggregates all queued {@link Damage} for this
   * component.
   *
   * @param dt Type of damage object that still need to be accounted for
   * @return Sum of all damage objects of type dt (default: 0)
   */
  public int calculateDamageOf(final DamageType dt) {
    int damageSum = HealthSystem.calculateDamageOf(this, dt);
    LOGGER.log(
        CustomLogLevel.DEBUG, this.getClass().getSimpleName() + " processed damage: '" + damageSum);
    return damageSum;
  }

  /**
   * Clear all pending damage for this component.
   *
   * <p>This delegates to the {@link HealthSystem} and removes the queue entry for this component.
   */
  public void clearDamage() {
    HealthSystem.removePendingDamage(this);
  }

  /**
   * Set the current health points.
   *
   * <p>If the new current health points are greater than the maximum health points of this
   * component, the current health points will be set to the maximum health points amount.
   *
   * @param amount New amount of current health points
   */
  public void currentHealthpoints(int amount) {
    this.currentHealthpoints = Math.min(maximalHealthpoints, amount);
    if (godMode) this.currentHealthpoints = Math.max(currentHealthpoints, 1);
  }

  /**
   * Set the value of the maximum health points.
   *
   * <p>If the new maximum health points are less than the current health points, the current health
   * points are set to the new maximum health points.
   *
   * @param amount New amount of maximal health points
   */
  public void maximalHealthpoints(int amount) {
    this.maximalHealthpoints = amount;
    currentHealthpoints = Math.min(currentHealthpoints, maximalHealthpoints);
  }

  /**
   * Get current health-points.
   *
   * @return The current health-points the associated entity has.
   */
  public int currentHealthpoints() {
    return currentHealthpoints;
  }

  /**
   * Get the maximal health-points.
   *
   * @return The maximal health-points the associated entity can have.
   */
  public int maximalHealthpoints() {
    return maximalHealthpoints;
  }

  /**
   * Get the last entity that caused damage to the associated entity.
   *
   * <p>The value is derived from the last {@link Damage} queued for this component in the {@link
   * HealthSystem}.
   *
   * @return {@link Optional} containing the last damage cause, or {@link Optional#empty()} if none
   */
  public Optional<Entity> lastDamageCause() {
    return HealthSystem.lastDamageCauseOf(this);
  }

  /**
   * Check if the current health-points are 0 or less.
   *
   * @return true if the current health-points are 0 or less, false if they are more than 0.
   */
  public boolean isDead() {
    return currentHealthpoints <= 0;
  }

  /**
   * Activate or deactivate the god mode-
   *
   * <p>in god mode the entity can not die.
   *
   * @param status true to activate, false to deactivate.
   */
  public void godMode(boolean status) {
    this.godMode = status;
  }

  /**
   * Check if god mode is activated.
   *
   * @return true if god mode is activated, false otherwise.
   */
  public boolean godMode() {
    return this.godMode;
  }
}
