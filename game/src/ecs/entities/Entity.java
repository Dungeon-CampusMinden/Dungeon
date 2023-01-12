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

    public void addComponent(String name, Component c) {
        components.put(name, c);
    }

    public Component getComponent(String name) {
        return components.get(name);
    }
}
