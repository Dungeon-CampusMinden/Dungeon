package entitys;

import components.Component;
import java.util.HashSet;
import java.util.Set;

/** Entitys are Objects in the ECS, that contains a set of components which define the entity */
public abstract class Entity {

    protected Set<Component> components = new HashSet<>();

    /**
     * Add a component to the Entity
     *
     * @param c component to add
     * @return if the component was added succesfully
     */
    public boolean addComponent(Component c) {
        return components.add(c);
    }
    /**
     * Remove a component to the Entity
     *
     * @param c component to remove
     * @return if the component was removed succesfully
     */
    public boolean removeComponent(Component c) {
        return components.remove(c);
    }
    /**
     * Check if this entity contains a specific component
     *
     * @param c component to check for
     * @return if this entity contains this component
     */
    public boolean containsComponent(Component c) {
        return components.contains(c);
    }
}
