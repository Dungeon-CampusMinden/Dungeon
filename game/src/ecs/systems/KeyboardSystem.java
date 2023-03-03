package ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
                                if (Gdx.input.isKeyPressed(Input.Keys.W))
                                    velocity.setCurrentYVelocity(1 * velocity.getYVelocity());
                                else if (Gdx.input.isKeyPressed(Input.Keys.S))
                                    velocity.setCurrentYVelocity(-1 * velocity.getYVelocity());
                                else if (Gdx.input.isKeyPressed(Input.Keys.D))
                                    velocity.setCurrentXVelocity(1 * velocity.getXVelocity());
                                else if (Gdx.input.isKeyPressed(Input.Keys.A))
                                    velocity.setCurrentXVelocity(-1 * velocity.getXVelocity());
                            });
        }
    }
}
