package ecs.components.stats;

import ecs.components.Component;
import ecs.entities.Entity;

public class StatsComponent extends Component {

    private DamageModifier damageModifier = new DamageModifier();
    private XPModifier xpModifier = new XPModifier(1.0f);

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
     * Get the XPModifier object of the entity
     * 
     * @return {@link ecs.components.stats.XPModifier} object
     */
    public XPModifier getXpModifier() {
        return xpModifier;
    }

    /**
     * Overwrite the DamageModifier object of the entity
     *
     * @param damageModifier new {@link ecs.components.stats.DamageModifier} object
     */
    public void setDamageModifier(DamageModifier damageModifier) {
        this.damageModifier = damageModifier;
    }

    /**
     * Overwrite the XPModifier object of the entity
     * 
     * @param xpModifier new {@link ecs.components.stats.XPModifier} object
     */
    public void setXpModifier(XPModifier xpModifier) {
        this.xpModifier = xpModifier;
    }
}
