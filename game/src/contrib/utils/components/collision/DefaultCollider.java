package contrib.utils.components.collision;

import core.Entity;
import core.level.Tile;
import core.utils.TriConsumer;

public class DefaultCollider implements TriConsumer<Entity, Entity, Tile.Direction> {

    public String message;

    public DefaultCollider() {
        message = "Collide";
    }

    public DefaultCollider(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }

    @Override
    public void accept(Entity entity, Entity entity2, Tile.Direction direction) {
        System.out.println(message);
    }
}
