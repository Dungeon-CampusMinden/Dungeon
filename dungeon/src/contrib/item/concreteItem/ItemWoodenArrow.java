package contrib.item.concreteItem;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.components.PositionComponent;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/**
 * This item is an arrow. It is used as ammunition for the bow item.
 *
 * <p>Adds an ammunition component to the collecting entity if it doesn't already have one
 */
public class ItemWoodenArrow extends Item {
  /** The default texture for all wooden arrows. */
  public static final IPath DEFAULT_TEXTURE = new SimpleIPath("items/weapon/wooden_arrow.png");

  /** The maximum amount of arrows per stack. */
  public static final int MAX_ARROW_STACK_SIZE = 16;

  /**
   * Create a {@link Item} that looks like an arrow and can be collected to be used as ammunition
   * for the bow item.
   *
   * @param amount The stack size of arrows.
   */
  public ItemWoodenArrow(int amount) {
    super(
        "Wooden Arrow",
        "Ammunition for a Bow",
        new Animation(DEFAULT_TEXTURE),
        new Animation(DEFAULT_TEXTURE),
        amount,
        MAX_ARROW_STACK_SIZE);
  }

  /**
   * Create a {@link Item} that looks like an arrow and can be collected to be used as ammunition
   * for the bow item.
   *
   * <p>The stack size will be set to one.
   */
  public ItemWoodenArrow() {
    this(1);
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
