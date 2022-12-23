package ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import ecs.components.VelocityComponent;
import ecs.entitys.Entity;
import java.util.Map;
import mydungeon.ECS;

public class KeyboardSystem extends ECS_System {

    @Override
    public void update() {
        for (Map.Entry<Entity, VelocityComponent> entry : ECS.velocityComponentMap.entrySet()) {
            Entity entity = entry.getKey();
            VelocityComponent velocity = ECS.velocityComponentMap.get(entity);

            velocity.setX(0);
            velocity.setY(0);

            if (Gdx.input.isKeyPressed(Input.Keys.W)) velocity.setY(1 * velocity.getySpeed());
            else if (Gdx.input.isKeyPressed(Input.Keys.S)) velocity.setY(-1 * velocity.getySpeed());
            else if (Gdx.input.isKeyPressed(Input.Keys.D)) velocity.setX(1 * velocity.getxSpeed());
            else if (Gdx.input.isKeyPressed(Input.Keys.A)) velocity.setX(-1 * velocity.getxSpeed());
        }
    }
}
