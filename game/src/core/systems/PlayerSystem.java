package core.systems;

import core.Entity;
import core.System;
import core.components.PlayerComponent;

/**
 * The PlayerSystem is used to control the player, it will trigger the {@link
 * PlayerComponent#execute()}-Method to execute the Functions registered to Keys.
 */
public class PlayerSystem extends System {

    public PlayerSystem() {
        super(PlayerComponent.class);
    }

    @Override
    public void execute() {
        getEntityStream().forEach(this::execute);
    }

    private void execute(Entity entity) {
        PlayerComponent pc = (PlayerComponent) (entity.getComponent(PlayerComponent.class).get());
        pc.execute();
    }
}
