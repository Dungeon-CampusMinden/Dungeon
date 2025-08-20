package contrib.item.concreteItem;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.components.PositionComponent;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/** This item is a key. It can be used to unlock something. */
public class ItemKey extends Item {
  /** The default texture for all small keys. */
  public static final IPath KEY_TEXTURE = new SimpleIPath("items/key/small_key.png");

  /**
   * Create a {@link Item} that looks like a Key. It can be collected and stored in the inventory.
   */
  public ItemKey() {
    super("Schlüssel", "Ein silberner Schlüssel. Was er wohl öffnet?", new Animation(KEY_TEXTURE));
  }

  @Override
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
