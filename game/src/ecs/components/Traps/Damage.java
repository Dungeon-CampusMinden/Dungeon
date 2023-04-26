package game.src.ecs.components.Traps;

import ecs.components.HealthComponent;
import ecs.entities.Entity;
import ecs.damage.DamageType;

public class Damage implements ITrigger {

    private static final int DAMAGE_AMOUNT = 40;
    private static final DamageType DAMAGE_TYPE = DamageType.PHYSICAL;
    private static final ecs.damage.Damage DAMAGE_DONE = new ecs.damage.Damage(DAMAGE_AMOUNT, DAMAGE_TYPE, null);
    
    public void trigger(Entity entity) {
        
        if (entity.getComponent(HealthComponent.class).isPresent()) {
            ((HealthComponent) entity.getComponent(HealthComponent.class).get()).receiveHit(DAMAGE_DONE);
        }
        
    }

}
