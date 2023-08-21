package core.systems;

import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.level.utils.LevelElement;
import core.utils.components.MissingComponentException;

/**
 * The {@link PositionSystem} checks if an entity has an illegal position and then changes the
 * position to a random position in the currently active level.
 *
 * <p>Entities with the {@link PositionComponent} will be processed by this system.
 *
 * <p>If the position of an entity is equal to {@link PositionComponent#ILLEGAL_POSITION}, the
 * position of the entity will be set to a random accessible tile in the current level.
 *
 * <p>Note: In most cases, the position of an entity equals {@link
 * PositionComponent#ILLEGAL_POSITION} during the first frame of the currently active level. This
 * occurs because sometimes entities are created before the level is loaded.
 */
public class PositionSystem extends System {

    /** Create a new VelocitySystem */
    public PositionSystem() {
        super(PositionComponent.class);
    }

    @Override
    public void execute() {
        entityStream()
                .map(this::buildDataObject)
                .filter(data -> data.pc.position().equals(PositionComponent.ILLEGAL_POSITION))
                .forEach(this::randomPosition);
    }

    private void randomPosition(PSData data) {
        if (Game.currentLevel() != null) data.pc.position(Game.randomTile(LevelElement.FLOOR));
    }

    private PSData buildDataObject(Entity e) {

        PositionComponent pc =
                e.fetch(PositionComponent.class)
                        .orElseThrow(
                                () -> MissingComponentException.build(e, PositionComponent.class));

        return new PSData(e, pc);
    }

    private record PSData(Entity e, PositionComponent pc) {}
}
