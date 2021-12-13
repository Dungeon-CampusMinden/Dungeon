package controller;

import interfaces.IEntity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Keeps a set of entities and calls their update method every frame. */
public class EntityController implements IController<IEntity> {
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

    /** Registers an entity. */
    @Override
    public void add(IEntity element) {
        dungeonEntities.add(element);
    }

    /** Removes an entity from the set. */
    @Override
    public void remove(IEntity element) {
        dungeonEntities.remove(element);
    }

    /** Returns <code>true</code> if the entity is registered. */
    @Override
    public boolean contains(IEntity element) {
        return dungeonEntities.contains(element);
    }

    /** Removes all entities from the set. */
    @Override
    public void removeAll() {
        dungeonEntities.clear();
    }

    /** Returns a copy set of all entities. */
    @Override
    public Set<IEntity> getSet() {
        return new LinkedHashSet<>(dungeonEntities);
    }

    /** Returns a copy list of all entities. */
    @Override
    public List<IEntity> getList() {
        return new ArrayList<>(dungeonEntities);
    }
}
