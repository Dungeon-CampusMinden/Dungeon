package contrib.components;

import contrib.utils.components.health.DamageType;
import contrib.utils.components.stats.DamageModifier;
import core.Component;
import core.Entity;

/**
 * Add resistances and vulnerabilities for {@link contrib.utils.components.health.DamageType}s to
 * the associated entity.
 *
 * <p>To set resistances and vulnerabilities for a damage type, use {@link #multiplier(DamageType,
 * float)}. For resistances, use a float less than 1, for vulnerabilities use a float greater than
 * 1.
 *
 * <p>The modifier values are taken into account by the {@link contrib.systems.HealthSystem} when
 * calculating damage, allowing for defining resistances and vulnerabilities for different damage
 * types.
 *
 * @see contrib.utils.components.stats.DamageModifier
 */
public final class StatsComponent extends Component {

    private final DamageModifier damageModifier = new DamageModifier();

    /**
     * Create a new component and add it to the associated entity.
     *
     * @param entity associated entity
     */
    public StatsComponent(final Entity entity) {
        super(entity);
    }

    /**
     * Get multiplier for a given damage type
     *
     * @param type damage type
     * @return multiplier (1 is default, values greater than 1 increase damage, values less than 1
     * decrease damage)
     */
    public float multiplierFor(DamageType type) {
        return damageModifier.multiplierFor(type);
    }

    /**
     * Set multiplier for a given damage type
     *
     * @param type       damage type
     * @param multiplier multiplier (1 is default, values greater than 1 increase damage, values
     *                   less than 1 decrease damage)
     */
    public void multiplier(DamageType type, float multiplier) {
        damageModifier.setMultiplier(type, multiplier);
    }
}
