package ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import ecs.components.MissingComponentException;
import ecs.components.PlayableComponent;
import ecs.components.VelocityComponent;
import ecs.components.ai.AIComponent;
import ecs.entities.Entity;
import mydungeon.ECS;

/** Used to control the player */
public class KeyboardSystem extends ECS_System {

    @Override
    public void update() {
        for (Entity entity : ECS.entities) {
            entity.getComponent(PlayableComponent.name).ifPresent(pc ->{

               final VelocityComponent velocity =
                        (VelocityComponent)
                                entity.getComponent(VelocityComponent.name)
                                        .orElseThrow(
                                                () ->
                                                        new MissingComponentException(
                                                                "VelocityComponent"));


                    velocity.setX(0);
                    velocity.setY(0);

                    if (Gdx.input.isKeyPressed(Input.Keys.W))
                        velocity.setY(1 * velocity.getySpeed());
                    else if (Gdx.input.isKeyPressed(Input.Keys.S))
                        velocity.setY(-1 * velocity.getySpeed());
                    else if (Gdx.input.isKeyPressed(Input.Keys.D))
                        velocity.setX(1 * velocity.getxSpeed());
                    else if (Gdx.input.isKeyPressed(Input.Keys.A))
                        velocity.setX(-1 * velocity.getxSpeed());

            });
        }
    }
}
