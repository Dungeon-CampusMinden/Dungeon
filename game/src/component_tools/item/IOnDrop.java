package component_tools.item;

import component_tools.position.Point;
import entities.Entity;

public interface IOnDrop {
    void onDrop(Entity user, ItemData which, Point position);
}
