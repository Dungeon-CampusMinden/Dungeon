package api.utils.component_utils.itemComponent;

import api.Entity;
import api.utils.Point;

public interface IOnDrop {
    void onDrop(Entity user, ItemData which, Point position);
}
