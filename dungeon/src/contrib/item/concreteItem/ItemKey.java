package contrib.item.concreteItem;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.components.PositionComponent;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/** This item is a key to open a chest. */
public class ItemKey extends Item {
  /** The default texture for all big keys. */
  public static final IPath KEY_TEXTURE = new SimpleIPath("items/key/small_key.png");

  /**
   * Create a {@link Item} that looks like a Key. It can be collected and stored in the inventory.
   */
  public ItemKey() {
    super("Key", "May open a chest", Animation.fromSingleImage(KEY_TEXTURE));
  }

  public void use(final Entity user) {
    user.fetch(PositionComponent.class)
        .map(PositionComponent::position)
        .ifPresent(
            pos -> {
              drop(pos);
              user.fetch(InventoryComponent.class).ifPresent(inv -> inv.remove(this));
            });
  }
}
