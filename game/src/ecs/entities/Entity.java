package ecs.entities;

import ecs.components.Component;
import java.util.HashMap;
import java.util.Optional;
import mydungeon.ECS;
import semanticAnalysis.types.DSLContextPush;
import semanticAnalysis.types.DSLType;

/** Entity is a unique identifier for an object in the game world */
@DSLType(name = "game_object")
@DSLContextPush(name = "entity")
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

    public void removeComponent(String name) {
        components.remove(name);
    }

    /**
     * Get the component
     *
     * @param name Name of the component
     * @return Optional that can contain the requested component
     */
    public Optional<Component> getComponent(String name) {
        return Optional.ofNullable(components.get(name));
    }
}
