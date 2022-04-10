package controller;

import basiselements.Entity;

/** Keeps a set of entities and calls their update method every frame. */
public class EntityController extends AbstractController<Entity> {
    /**
     * Removes deletable entities and calls the update and draw method for every registered entity.
     */
    public void update() {
        removeIf(Entity::removable);
        forEach(Entity::update);
        forEach(Entity::draw);
    }
}
