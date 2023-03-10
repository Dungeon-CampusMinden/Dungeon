package ecs.components.stats;

import ecs.components.Component;
import ecs.entities.Entity;

public class StatsComponent extends Component {

    private DamageModifier damageModifier = new DamageModifier();

    /**
     * Create a new component and add it to the associated entity
     *
     * @param entity associated entity
     */
    public StatsComponent(Entity entity) {
        super(entity);
    }

    /**
     * Get the stats object of the entity
     *
     * @return stats object
     */
    public DamageModifier getDamageModifiers() {
        return this.damageModifier;
    }

    /**
     * Overwrite the stats object of the entity
     *
     * @param damageModifier new stats object
     */
    public void setDamageModifier(DamageModifier damageModifier) {
        this.damageModifier = damageModifier;
    }
}
