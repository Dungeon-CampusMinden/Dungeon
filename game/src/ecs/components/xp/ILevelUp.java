package ecs.components.xp;

public interface ILevelUp {

    /**
     * Implements the LevelUp behavior of a XPComponent having entity
     *
     * @param nexLevel is the new level of the entity
     */
    void onLevelUp(long nexLevel);
}
