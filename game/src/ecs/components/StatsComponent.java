package ecs.components;

import ecs.damage.DamageType;
import ecs.entities.Entity;

import java.util.HashMap;
import java.util.Map;

public class StatsComponent extends Component{

    private final Map<DamageType, Float> multipliers = new HashMap<>();

    /**
     * Create a new component and add it to the associated entity
     *
     * @param entity associated entity
     */
    public StatsComponent(Entity entity) {
        super(entity);
    }

    /**
     * Get the multiplier for a given damage type
     * @param type damage type
     * @return multiplier (1 is default, values greater than 1 increase damage, values less than 1 decrease damage)
     */
    public float getMultiplier(DamageType type) {
        return this.multipliers.getOrDefault(type, 1f);
    }

    /**
     * Set the multiplier for a given damage type
     * @param type damage type
     * @param multiplier multiplier (1 is default, values greater than 1 increase damage, values less than 1 decrease damage)
     */
    public void setMultiplier(DamageType type, float multiplier) {
        this.multipliers.put(type, multiplier);
    }


}
