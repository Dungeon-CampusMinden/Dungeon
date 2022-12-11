package ecs.entitys;

/** Entity is a unique identifier for an object in the game world */
public class Entity {
    private static int nextId = 0;
    public final int id = nextId++;
}
