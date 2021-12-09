package controller;

import interfaces.IEntity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Keeps a set of entities and calls their update method every frame. */
public class EntityController {
    /** Contains all the entities this controller handles. */
    private final Set<IEntity> dungeonEntities = new LinkedHashSet<>();

    /**
     * Removes deletable entities and calls the update and draw method for every registered entity.
     */
    public void update() {
        dungeonEntities.removeIf(IEntity::removable);
        dungeonEntities.forEach(IEntity::update);
        dungeonEntities.forEach(IEntity::draw);
    }

    /** Register an entity. */
    public void addEntity(IEntity entity) {
        dungeonEntities.add(entity);
    }

    /** Returns <code>true</code> if the entity is registered. */
    public boolean containsEntity(IEntity entity) {
        return dungeonEntities.contains(entity);
    }

    /** Removes an entity from the set. */
    public void removeEntity(IEntity entity) {
        dungeonEntities.remove(entity);
    }

    /** Removes all entities from the set. */
    public void removeAll() {
        dungeonEntities.clear();
    }

    /** Returns a copy set of all entities. */
    public Set<IEntity> getEntitiesSet() {
        return new LinkedHashSet<>(dungeonEntities);
    }

    /** Returns a copy list of all entities. */
    public List<IEntity> getEntitiesList() {
        return new ArrayList<>(dungeonEntities);
    }
}
