package ecs.systems;

import com.badlogic.gdx.Gdx;
import configuration.KeyboardConfig;
import ecs.components.MissingComponentException;
import ecs.components.PlayableComponent;
import ecs.components.VelocityComponent;
import ecs.entities.Entity;
import starter.Game;

/** Used to control the player */
public class KeyboardSystem extends ECS_System {

    @Override
    public void update() {
        for (Entity entity : Game.entities) {
            entity.getComponent(PlayableComponent.class)
                    .ifPresent(
                            pc -> {
                                final VelocityComponent velocity =
                                        (VelocityComponent)
                                                entity.getComponent(VelocityComponent.class)
                                                        .orElseThrow(
                                                                () ->
                                                                        new MissingComponentException(
                                                                                "VelocityComponent"));
                                if (Gdx.input.isKeyPressed(KeyboardConfig.MOVEMENT_UP.get()))
                                    velocity.setCurrentYVelocity(1 * velocity.getYVelocity());
                                else if (Gdx.input.isKeyPressed(KeyboardConfig.MOVEMENT_DOWN.get()))
                                    velocity.setCurrentYVelocity(-1 * velocity.getYVelocity());
                                else if (Gdx.input.isKeyPressed(
                                        KeyboardConfig.MOVEMENT_RIGHT.get()))
                                    velocity.setCurrentXVelocity(1 * velocity.getXVelocity());
                                else if (Gdx.input.isKeyPressed(KeyboardConfig.MOVEMENT_LEFT.get()))
                                    velocity.setCurrentXVelocity(-1 * velocity.getXVelocity());
                            });
        }
    }
}
