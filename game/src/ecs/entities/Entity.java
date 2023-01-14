package ecs.entities;

import ecs.components.Component;
import java.util.HashMap;
import mydungeon.ECS;

/** Entity is a unique identifier for an object in the game world */
public class Entity {
    private static int nextId = 0;
    public final int id = nextId++;
    private HashMap<String, Component> components;

    public Entity() {
        components = new HashMap<>();
        ECS.entities.add(this);
    }

    /**
     * Add a new component to this entity
     *
     * @param name Name of the component
     * @param component The component
     */
    public void addComponent(String name, Component component) {
        components.put(name, component);
    }

    /**
     * Get the component
     *
     * @param name Name of the component
     * @return The component with the given name associated with this entity, can be null
     */
    public Component getComponent(String name) {
        return components.get(name);
    }
}
