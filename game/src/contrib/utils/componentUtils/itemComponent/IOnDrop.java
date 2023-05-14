package contrib.utils.componentUtils.itemComponent;

import core.Entity;
import core.utils.Point;

public interface IOnDrop {
    void onDrop(Entity user, ItemData which, Point position);
}
