package core.utils;

import core.Component;
import core.Entity;
import core.System;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class Filter {

    private final Set<Class<? extends Component>> filterRules;
    private final Set<Entity> entities;
    private final Set<System> systems;

    public Filter(Set<Class<? extends Component>> filterRules) {
        this.filterRules = filterRules;
        entities = new HashSet<>();
        systems = new HashSet<>();
    }

    public Filter(){
        filterRules = new HashSet<>();
        entities = new HashSet<>();
        systems = new HashSet<>();
    }

    public boolean add(System system) {
        if (systems.add(system)) {
            entities.forEach(entity -> system.triggerOnAdd(entity));
            return true;
        }
        return false;
    }

    public boolean remove(System system) {
        if (systems.remove(system)) {
            entities.forEach(entity -> system.triggerOnRemove(entity));
            return true;
        }
        return false;
    }

    public boolean add(Entity entity) {
        if (!entities.contains(entity) && accept(entity)) {
            entities.add(entity);
            systems.forEach(system -> system.triggerOnAdd(entity));
            return true;
        }
        return false;
    }

    public boolean remove(Entity entity) {
        if (entities.contains(entity)) {
            entities.remove(entity);
            systems.forEach(system -> system.triggerOnRemove(entity));
            return true;
        }
        return false;
    }

    public void update(Entity entity) {
        if (accept(entity)) add(entity);
        else remove(entity);
    }

    public Stream<Entity> stream() {
        return new HashSet<>(entities).stream();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        else if (o instanceof Filter) return filterRules.equals(((Filter) o).filterRules);
        return false;
    }

    public boolean equals(Set<Class<? extends Component>> o) {
        return o.equals(filterRules);
    }

    /**
     * Check if the given entity has all the components needed to be processed by this system.
     *
     * <p>If one or more additionally components are missing, this system will create a log entry
     * with information about the missing components.
     *
     * @param entity the entity to check
     * @return true if the entity is accepted, false if not.
     */
    protected boolean accept(Entity entity) {
        for (Class<? extends Component> filter : filterRules)
            if (!entity.isPresent(filter)) {
                return false;
            }
        return true;
    }

    public boolean has(System system){
        return systems.contains(system);
    }
}
