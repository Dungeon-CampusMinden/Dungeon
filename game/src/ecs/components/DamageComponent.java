package ecs.components;

import java.util.logging.Logger;

import ecs.entities.Entity;

/**
 * The entire purpose of this class is that skills deal damage depending on the
 * entity that uses it.
 */
public class DamageComponent extends Component {

    private int damage;

    private final transient Logger damageLogger = Logger.getLogger(DamageComponent.class.getName());

    /**
     * Construct a DamageComponent
     * 
     * @implNote Give this to evreything that deals damage
     */
    public DamageComponent(Entity entity, int damage) {
        super(entity);
        this.damage = damage;
        damageLogger.info("New DamageComponent: " + damage + " by: " + entity);
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }

}
