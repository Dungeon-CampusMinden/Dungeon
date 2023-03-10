package ecs.components.stats;

import ecs.damage.DamageType;
import java.util.HashMap;
import java.util.Map;

public class DamageModifier {

    private final Map<DamageType, Float> damageMultipliers = new HashMap<>();

    /**
     * Get the multiplier for a given damage type
     *
     * @param type damage type
     * @return multiplier (1 is default, values greater than 1 increase damage, values less than 1
     *     decrease damage)
     */
    public float getMultiplier(DamageType type) {
        return this.damageMultipliers.getOrDefault(type, 1f);
    }

    /**
     * Set the multiplier for a given damage type
     *
     * @param type damage type
     * @param multiplier multiplier (1 is default, values greater than 1 increase damage, values
     *     less than 1 decrease damage)
     */
    public void setMultiplier(DamageType type, float multiplier) {
        this.damageMultipliers.put(type, multiplier);
    }
}
