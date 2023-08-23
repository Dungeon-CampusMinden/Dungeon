package core.systems;

import core.Entity;
import core.System;
import core.components.PlayerComponent;
import core.utils.components.MissingComponentException;

/**
 * The PlayerSystem is used to control the player, it will trigger the {@link
 * PlayerComponent#execute(Entity, boolean)}-Method to execute the Functions registered to Keys.
 */
public final class PlayerSystem extends System {

    private boolean running = true;

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
                .execute(entity, !this.running);
    }

    @Override
    public void stop() {
        this.run = true; // This system can not be stopped.
        this.running = false;
    }

    @Override
    public void run() {
        this.run = true;
        this.running = true;
    }
}
