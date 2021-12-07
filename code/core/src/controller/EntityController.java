package controller;

import interfaces.IEntity;

import java.util.LinkedHashSet;
import java.util.Set;

/** Keeps a set of entities and calls their update method every frame */
public class EntityController {
    /** Contains all the entities this controller handles. */
    private final Set<IEntity> dungeonEntities;

    /** Keeps a list of entities and calls their update method every frame */
    public EntityController() {
        dungeonEntities = new LinkedHashSet<>();
    }

    /**
     * calls the update method for every entity in the list. removes entity if deletable is set true
     */
    public void update() {
        dungeonEntities.removeIf(IEntity::removable);
        dungeonEntities.forEach(IEntity::update);
        dungeonEntities.forEach(IEntity::draw);
    }

    /** add an entity to the list */
    public void addEntity(IEntity entity) {
        dungeonEntities.add(entity);
    }

    /** removes entity from the list */
    public void removeEntity(IEntity entity) {
        dungeonEntities.remove(entity);
    }

    /** removes all entities from the list */
    public void removeAll() {
        dungeonEntities.clear();
    }

    /** returns entity list */
    public Set<IEntity> getEntities() {
        return dungeonEntities;
    }
}
