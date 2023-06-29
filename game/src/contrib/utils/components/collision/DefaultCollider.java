package contrib.utils.components.collision;

import core.Entity;
import core.level.Tile;

public class DefaultCollider implements ICollide {

    public String message;

    public DefaultCollider(){
        message = "Collide";
    }

    public DefaultCollider(String message){
        this.message = message;
    }
    @Override
    public void onCollision(Entity a, Entity b, Tile.Direction from) {
        System.out.println(message);
    }

    public String getMessage() {
        return message;
    }
}
