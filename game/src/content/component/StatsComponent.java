package content.component;

import api.Component;
import api.Entity;
import content.utils.componentUtils.statsComponent.DamageModifier;

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
     * @return {@link DamageModifier} object
     */
    public DamageModifier getDamageModifiers() {
        return this.damageModifier;
    }

    /**
     * Overwrite the DamageModifier object of the entity
     *
     * @param damageModifier new {@link DamageModifier} object
     */
    public void setDamageModifier(DamageModifier damageModifier) {
        this.damageModifier = damageModifier;
    }
}
