package ecs.components;

import ecs.entitys.Entity;
import java.util.HashMap;
import java.util.Map;

/** ComponentStore is a store for components, indexed by entity */
public class ComponentStore {
    private final Map<Entity, Component> store;

    public ComponentStore() {
        store = new HashMap<>();
    }

    /**
     * Ads a component to the store
     *
     * @param component Component to add
     */
    public void addComponent(Component component) {
        store.put(component.getEntity(), component);
    }

    /**
     * Gets a component from the store
     *
     * @param entity associated entity
     * @return the component from the store
     */
    public Component getComponent(Entity entity) {
        return store.get(entity);
    }

    /**
     * @return The stored HashMap
     */
    public Map<Entity, Component> getStore() {
        return store;
    }
}
