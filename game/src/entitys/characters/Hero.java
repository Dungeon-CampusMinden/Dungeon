package entitys.characters;

import components.HealthComponent;
import entitys.Entity;


/**
 * Player character
 *
 * DUMMY TO TEST THE ECS
 */
public class Hero extends Entity {

    public Hero(int health) {
        addComponent(new HealthComponent(health, health));
    }
}
