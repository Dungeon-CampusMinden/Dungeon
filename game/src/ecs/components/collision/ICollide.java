package ecs.components.collision;

import ecs.entities.Entity;
import level.elements.tile.Tile;

import java.io.Serializable;

public interface ICollide extends Serializable {
    /**
     * Implements the Collision behavior of a Hitbox having entity
     *
     * @param a is the current Entity
     * @param b is the Entity with whom the Collision happened
     * @param from the direction from a to b
     */
    void onCollision(Entity a, Entity b, Tile.Direction from);
}
