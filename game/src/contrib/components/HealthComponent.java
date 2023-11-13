package contrib.components;

import com.badlogic.gdx.utils.Null;

import contrib.systems.HealthSystem;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;

import core.Component;
import core.Entity;
import core.utils.logging.CustomLogLevel;

import dsl.semanticanalysis.types.DSLCallback;
import dsl.semanticanalysis.types.DSLType;
import dsl.semanticanalysis.types.DSLTypeMember;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Allow an associated entity to take damage and to die.
 *
 * <p>The component also tracks the received damage via the {@link #receiveHit(Damage) receiveHit}
 * method. The damage is not applied immediately but is temporarily stored in an internal list until
 * it is applied, and can be retrieved via the {@link #calculateDamageOf(DamageType)
 * calculateDamageOf} method.
 *
 * <p>To calculate the damage received, the {@link HealthSystem} calls the {@link
 * #calculateDamageOf(DamageType)} method for each {@link DamageType} and calculates the sum of the
 * damage. Next, the {@link HealthSystem} reduces the {@link #currentHealthpoints} by this value and
 * calls {@link #clearDamage()} to clear the internal list afterwards. When the health points drop
 * to 0 or less, the system calls {@link #triggerOnDeath(Entity)}.
 *
 * <p>To determine the last cause of damage, the {@link #lastDamageCause()} method can be used.
 */
@DSLType(name = "health_component")
public final class HealthComponent implements Component {
    private final List<Damage> damageToGet;
    private @DSLCallback(name = "on_death") final Consumer<Entity> onDeath;
    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());
    private @DSLTypeMember(name = "max_health") int maximalHealthpoints;
    private @DSLTypeMember(name = "start_health") int currentHealthpoints;
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
        this.maximalHealthpoints = maximalHitPoints;
        this.currentHealthpoints = maximalHitPoints;
        this.onDeath = onDeath;
        damageToGet = new ArrayList<>();
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
     * <p>The {@link HealthSystem} will reduce the current health points based on the received
     * damage.
     *
     * @param damage Damage that should be inflicted
     */
    public void receiveHit(Damage damage) {
        damageToGet.add(damage);
        this.lastCause = damage.cause() != null ? damage.cause() : this.lastCause;
    }

    /**
     * Trigger the onDeath function
     *
     * @param entity associated entity of this component.
     */
    public void triggerOnDeath(final Entity entity) {
        onDeath.accept(entity);
    }

    /**
     * Calculate the amount of damage of a certain type
     *
     * @param dt Type of damage object that still need to be accounted for
     * @return Sum of all damage objects of type dt (default: 0)
     */
    public int calculateDamageOf(final DamageType dt) {
        int damageSum =
                damageToGet.stream()
                        .filter(d -> d.damageType() == dt)
                        .mapToInt(Damage::damageAmount)
                        .sum();

        LOGGER.log(
                CustomLogLevel.DEBUG,
                this.getClass().getSimpleName() + " processed damage: '" + damageSum);

        return damageSum;
    }

    /**
     * Clear the damage list.
     *
     * <p>The damage list is used to determine the damage the entity should receive on next tick.
     */
    public void clearDamage() {
        damageToGet.clear();
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
     * <p>If the new maximum health points are less than the current health points, the current
     * health points are set to the new maximum health points.
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
        return currentHealthpoints <= 0;
    }

    /**
     * Activate or deactivate the god mode-
     *
     * <p>in god mode the entity can not die.
     *
     * @param status true to actiavte, false to deactivate.
     */
    public void godMode(boolean status) {
        this.godMode = status;
    }
}
