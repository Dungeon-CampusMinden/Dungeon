package ecs.components.ai.transition;

import ecs.entities.Entity;
import savegame.IFieldSerializing;

/** Determines when an ai switches between idle and fight */
public interface ITransition extends IFieldSerializing {

    /**
     * Function that determines whether an entity should be in combat mode
     *
     * @param entity associated entity
     * @return if the entity should fight
     */
    boolean isInFightMode(Entity entity);
}
