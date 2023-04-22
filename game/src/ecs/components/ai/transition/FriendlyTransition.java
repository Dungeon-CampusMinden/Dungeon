package ecs.components.ai.transition;

import ecs.entities.Entity;

/**
 * Class that determines an Entity as friendly - it won't enter Combat mode if near the hero
 *
 *
 */
public class FriendlyTransition implements ITransition{
    @Override
    public boolean isInFightMode(Entity entity) {
        return false;
    }
}
