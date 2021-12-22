package controller;

import interfaces.IEntity;

/** Keeps a set of entities and calls their update method every frame. */
public class EntityController extends AbstractController<IEntity> {
    /**
     * Removes deletable entities and calls the update and draw method for every registered entity.
     */
    public void update() {
        removeIf(IEntity::removable);
        forEach(IEntity::update);
        forEach(IEntity::draw);
    }
}
