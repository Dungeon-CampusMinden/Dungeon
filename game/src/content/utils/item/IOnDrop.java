package content.utils.item;

import content.utils.position.Point;
import api.Entity;

public interface IOnDrop {
    void onDrop(Entity user, ItemData which, Point position);
}
