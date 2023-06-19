package core.systems;

import core.Entity;
import core.System;
import core.components.PlayerComponent;
import core.utils.components.MissingComponentException;

/**
 * The PlayerSystem is used to control the player, it will trigger the {@link
 * PlayerComponent#execute()}-Method to execute the Functions registered to Keys.
 */
public final class PlayerSystem extends System {

    public PlayerSystem() {
        super(PlayerComponent.class);
    }

    @Override
    public void execute() {
        entityStream().forEach(this::execute);
    }

    private void execute(Entity entity) {
        entity.fetch(PlayerComponent.class)
                .orElseThrow(() -> MissingComponentException.build(entity, PlayerComponent.class))
                .execute();
    }
}
