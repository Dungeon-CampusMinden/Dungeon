package contrib.components;

import contrib.utils.components.health.DamageType;
import contrib.utils.components.stats.DamageModifier;

import core.Component;

/**
 * Add resistances and vulnerabilities for {@link contrib.utils.components.health.DamageType}s to
 * the associated entity.
 *
 * <p>Resistances refer to the ability of an entity to mitigate or reduce the damage taken from
 * specific {@link DamageType}. Vulnerabilities indicate specific weaknesses of an entity, making
 * them more susceptible to increased damage or negative effects from certain {@link DamageType}.
 *
 * <p>To set resistances and vulnerabilities for a damage type, use {@link #multiplier(DamageType,
 * float)}. For resistances, use values less than 1.0, for vulnerabilities use values greater than
 * 1.0. If you use a value less than 0.0f, the damage will become negative, resulting in healing the
 * entity.
 *
 * <p>The modifier values are taken into account as multiplier by the {@link
 * contrib.systems.HealthSystem} when calculating damage, allowing for defining resistances and
 * vulnerabilities for different damage types.
 *
 * @see contrib.utils.components.stats.DamageModifier
 */
public final class StatsComponent implements Component {

    private final DamageModifier damageModifier = new DamageModifier();

    /**
     * Get multiplier for a given damage type
     *
     * <p>If no modifier is set for the given type, the return value will be 1.0.
     *
     * @param type damage type
     * @return multiplier (1.0 is default, values greater than 1.0 increase damage, values less than
     *     1.0 decrease damage, value less than 0.0 will make the damage to a heal)
     */
    public float multiplierFor(final DamageType type) {
        return damageModifier.multiplierFor(type);
    }

    /**
     * Set multiplier for a given damage type
     *
     * @param type damage type
     * @param multiplier multiplier (1 is default, values greater than 1 increase damage, values
     *     less than 1 decrease damage, value less than 0.0 will make the damage to a heal)
     */
    public void multiplier(final DamageType type, float multiplier) {
        damageModifier.setMultiplier(type, multiplier);
    }
}
