package api.ecs.items;

import api.ecs.entities.Entity;
import api.tools.Point;

public interface IOnDrop {
    void onDrop(Entity user, ItemData which, Point position);
}
