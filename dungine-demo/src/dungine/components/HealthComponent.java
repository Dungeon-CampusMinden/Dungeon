package dungine.components;

import de.fwatermann.dungine.ecs.Component;
import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.utils.annotations.Null;
import dungine.systems.HealthSystem;
import dungine.util.health.Damage;
import dungine.util.health.DamageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class HealthComponent extends Component {

  private static final Logger LOGGER = LogManager.getLogger(HealthComponent.class);

  private final List<Damage> damageToGet;
  private BiConsumer<Entity, Damage> onHit = (entity, damage) -> {};
  private Consumer<Entity> onDeath;
  private int maximalHealthpoints;
  private int currentHealthpoints;
  private @Null Entity lastCause = null;

  private boolean godMode = false;

  /**
   * Create a new HealthComponent.
   *
   * @param maximalHitPoints Maximum amount of health points; currentHitPoints cannot be greater
   *     than that
   * @param onDeath Function that gets called when this entity dies
   */
  public HealthComponent(int maximalHitPoints, final Consumer<Entity> onDeath) {
    super(false);
    this.maximalHealthpoints = maximalHitPoints;
    this.currentHealthpoints = maximalHitPoints;
    this.onDeath = onDeath;
    this.damageToGet = new ArrayList<>();
  }

  /**
   * Create a HealthComponent with default values.
   *
   * <p>The maximum health points are set to 1, and the onDeath function is empty.
   */
  public HealthComponent() {
    this(1, onDeath -> {});
  }

  /**
   * Add damage, which is accounted for by the {@link HealthSystem}.
   *
   * <p>The {@link HealthSystem} will reduce the current health points based on the received damage.
   *
   * @param damage Damage that should be inflicted
   */
  public void receiveHit(Damage damage) {
    this.onHit.accept(damage.cause(), damage);
    this.damageToGet.add(damage);
    this.lastCause = damage.cause() != null ? damage.cause() : this.lastCause;
  }

  /**
   * Trigger the onDeath function.
   *
   * @param entity associated entity of this component.
   */
  public void triggerOnDeath(final Entity entity) {
    this.onDeath.accept(entity);
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
   * Set the onHit function.
   *
   * <p>This function will be called when the associated entity receives damage.
   *
   * @param onHit A BiConsumer function that takes an Entity that caused the damage (can be null)
   *     and the Damage object.
   */
  public void onHit(BiConsumer<Entity, Damage> onHit) {
    this.onHit = onHit;
  }

  /**
   * Calculate the amount of damage to a certain type.
   *
   * @param dt Type of damage object that still need to be accounted for
   * @return Sum of all damage objects of type dt (default: 0)
   */
  public int calculateDamageOf(final DamageType dt) {
    int damageSum =
      this.damageToGet.stream().filter(d -> d.damageType() == dt).mapToInt(Damage::damageAmount).sum();
    LOGGER.debug("{} processed damage: '{}", this.getClass().getSimpleName(), damageSum);

    return damageSum;
  }

  /**
   * Clear the damage list.
   *
   * <p>The damage list is used to determine the damage the entity should receive on next tick.
   */
  public void clearDamage() {
    this.damageToGet.clear();
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
    this.currentHealthpoints = Math.min(this.maximalHealthpoints, amount);
    if (this.godMode) this.currentHealthpoints = Math.max(this.currentHealthpoints, 1);
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
    this.currentHealthpoints = Math.min(this.currentHealthpoints, this.maximalHealthpoints);
  }

  /**
   * Get current health-points.
   *
   * @return The current health-points the associated entity has.
   */
  public int currentHealthpoints() {
    return this.currentHealthpoints;
  }

  /**
   * Get the maximal health-points.
   *
   * @return The maximal health-points the associated entity can have.
   */
  public int maximalHealthpoints() {
    return this.maximalHealthpoints;
  }

  /**
   * Get last entity that caused damage to the associated entity.
   *
   * @return The last entity that caused damage to the associated entity.
   */
  public Optional<Entity> lastDamageCause() {
    return Optional.ofNullable(this.lastCause);
  }

  /**
   * Check if the current health-points are 0 or less.
   *
   * @return true if the current health-points are 0 or less, false if they are more than 0.
   */
  public boolean isDead() {
    return this.currentHealthpoints <= 0;
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
}
