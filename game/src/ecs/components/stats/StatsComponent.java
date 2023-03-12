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
     * Get the DamageModifier object of the entity
     *
     * @return {@link ecs.components.stats.DamageModifier} object
     */
    public DamageModifier getDamageModifiers() {
        return this.damageModifier;
    }

    /**
     * Overwrite the DamageModifier object of the entity
     *
     * @param damageModifier new {@link ecs.components.stats.DamageModifier} object
     */
    public void setDamageModifier(DamageModifier damageModifier) {
        this.damageModifier = damageModifier;
    }
}
