package ecs.entities;

import ecs.components.Component;
import ecs.components.SkillComponent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import mydungeon.ECS;

/** Entity is a unique identifier for an object in the game world */
public class Entity {
    private static int nextId = 0;
    public final int id = nextId++;
    private HashMap<String, Component> components;
    private Set<SkillComponent> skills;

    public Entity() {
        components = new HashMap<>();
        skills = new HashSet<>();
        ECS.entities.add(this);
    }

    /**
     * Add a new component to this entity
     *
     * @param name Name of the component
     * @param component The component
     */
    public void addComponent(String name, Component component) {
        if (name.equals(SkillComponent.name)) addSkill((SkillComponent) component);
        else components.put(name, component);
    }

    public void addSkill(SkillComponent component) {
        skills.add(component);
    }

    public void removeComponent(String name) {
        components.remove(name);
    }

    public void removeSkill(SkillComponent component) {
        skills.remove(component);
    }

    public Set<SkillComponent> getSkills() {
        return skills;
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
