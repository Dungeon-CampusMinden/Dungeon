package contrib.item.concreteItem;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.components.PositionComponent;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/** This item is a big key. It can be used unlock something. */
public class ItemBigKey extends Item {
  /** The default texture for all big keys. */
  public static final IPath BIG_KEY_TEXTURE = new SimpleIPath("items/key/big_key.png");

  /**
   * Create a {@link Item} that looks like a Big Key. It can be collected and stored in the
   * inventory.
   */
  public ItemBigKey() {
    super(
        "Großer Schlüssel",
        "Ein großer goldener Schlüssel. Was er wohl öffnet?",
        new Animation(BIG_KEY_TEXTURE));
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
