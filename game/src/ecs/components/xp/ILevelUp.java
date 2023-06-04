package ecs.components.xp;

import java.io.Serializable;

public interface ILevelUp extends Serializable {

    /**
     * Implements the LevelUp behavior of a XPComponent having entity
     *
     * @param nexLevel is the new level of the entity
     */
    void onLevelUp(long nexLevel);
}
