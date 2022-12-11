package ecs.systems;

/** Marks an Class as a System in the ECS */
public interface ISystem {

    /** Get called every Frame */
    void update();
}
