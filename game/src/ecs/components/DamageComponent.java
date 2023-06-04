package ecs.components;

import ecs.entities.Entity;

/**
 * The entire purpose of this class is that skills deal damage depending on the
 * entity that uses it.
 */
public class DamageComponent extends Component {

    private int damage;

    /**
     * Construct a DamageComponent
     * 
     * @implNote Give this to evreything that deals damage
     */
    public DamageComponent(Entity entity, int damage) {
        super(entity);
        this.damage = damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }

}
