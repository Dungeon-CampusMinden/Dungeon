package api.utils.componentUtils.itemComponent;

import api.Entity;
import api.utils.Point;

public interface IOnDrop {
    void onDrop(Entity user, ItemData which, Point position);
}
