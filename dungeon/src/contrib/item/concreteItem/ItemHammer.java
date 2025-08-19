package contrib.item.concreteItem;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.components.PositionComponent;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/**
 * This item is a hammer. It is required for destroying objects like a stone.
 */
public class ItemHammer extends Item {

  public static final IPath DEFAULT_TEXTURE = new SimpleIPath("items/tools/hammer.png");

  public ItemHammer() {
    super(
      "Hammer", "A powerful tool to destroy objects", Animation.fromSingleImage(DEFAULT_TEXTURE));
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
