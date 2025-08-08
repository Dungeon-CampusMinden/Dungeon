package contrib.item.concreteItem;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;
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

  private static final int MAX_ARROW_STACK_SIZE = 16;

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
        Animation.fromSingleImage(DEFAULT_TEXTURE),
        Animation.fromSingleImage(DEFAULT_TEXTURE),
        amount,
        MAX_ARROW_STACK_SIZE);
  }

  @Override
  public void use(final Entity user) {
    Entity hero = Game.hero().orElseThrow();
    PositionComponent posc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    // clicking on an arrow in the inventory drops the whole stack at the current position
    drop(posc.position());
    user.fetch(InventoryComponent.class).ifPresent(component -> component.remove(this));
  }
}
