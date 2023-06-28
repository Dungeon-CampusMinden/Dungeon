package contrib.components;

import com.badlogic.gdx.utils.Null;

import contrib.systems.HealthSystem;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;

import core.Component;
import core.Entity;
import core.utils.logging.CustomLogLevel;

import semanticanalysis.types.DSLContextMember;
import semanticanalysis.types.DSLType;
import semanticanalysis.types.DSLTypeMember;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Allow an associated entity to take damage and to die.
 *
 * <p>The component keeps track of the current and maximum health points of the associated entity.
 *
 * <p>The component also keeps track of the damage received via the {@link #receiveHit(Damage)
 * receiveHit} method. The damage is stored in a list and can be retrieved via the {@link
 * #calculateDamageOf(DamageType) calculateDamageOf} method. The damage is applied, and the list is
 * cleared by the {@link HealthSystem HealthSystem} every frame.
 *
 * <p>To determine the last cause of damage, the {@link #lastDamageCause()} method can be used.
 *
 * <p>If the current health points of an entity go to or below 0, the {@link HealthSystem} will
 * trigger the {@link #onDeath} function stored in this component.
 */
@DSLType(name = "health_component")
public final class HealthComponent extends Component {
    private final List<Damage> damageToGet;
    private @DSLTypeMember(name = "on_death_function") final Consumer<Entity> onDeath;
    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());
    private @DSLTypeMember(name = "maximal_health_points") int maximalHealthpoints;
    private int currentHealthpoints;
    private @Null Entity lastCause = null;

    /**
     * Create a new HealthComponent and add it to the associated entity.
     *
     * @param entity associated entity
     * @param maximalHitPoints Maximum amount of health points; currentHitPoints cannot be greater
     *     than that
     * @param onDeath Function that gets called when this entity dies
     */
    public HealthComponent(
            final Entity entity, int maximalHitPoints, final Consumer<Entity> onDeath) {
        super(entity);
        this.maximalHealthpoints = maximalHitPoints;
        this.currentHealthpoints = maximalHitPoints;
        this.onDeath = onDeath;
        damageToGet = new ArrayList<>();
    }

    /**
     * Create a HealthComponent with default values and add it to the associated entity.
     *
     * <p>The maximum health points are set to 1, and the onDeath function is empty.
     *
     * @param entity associated entity
     */
    public HealthComponent(@DSLContextMember(name = "entity") final Entity entity) {
        this(entity, 1, onDeath -> {});
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

    /** Trigger the onDeath function */
    public void triggerOnDeath() {
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
                this.getClass().getSimpleName()
                        + " processed damage for entity: '"
                        + entity
                        + "': "
                        + damageSum);

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
}
