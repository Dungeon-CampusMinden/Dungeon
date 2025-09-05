package contrib.item.concreteItem;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.components.PositionComponent;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/**
 * Represents a hammer item, which is required to destroy certain destructible objects like stones.
 */
public class ItemHammer extends Item {
  /** The default texture for all hammers . */
  public static final IPath DEFAULT_TEXTURE = new SimpleIPath("items/tools/hammer.png");

  /**
   * Create a {@link Item} that looks like a hammer. It can be collected and stored in the
   * inventory.
   */
  public ItemHammer() {
    super("Hammer", "A powerful tool to destroy objects", new Animation(DEFAULT_TEXTURE));
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
