package feature.inventory.items;

import engine.Entity;
import engine.components.PositionComponent;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.IPath;
import engine.utils.components.path.SimpleIPath;
import feature.components.InventoryComponent;
import feature.inventory.Item;

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
