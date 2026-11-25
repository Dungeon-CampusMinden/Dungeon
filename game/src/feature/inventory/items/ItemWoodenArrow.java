package feature.inventory.items;

import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.IPath;
import engine.utils.components.path.SimpleIPath;
import feature.inventory.Item;

/**
 * This item is an arrow. It is used as ammunition for the bow item.
 *
 * <p>Adds an ammunition component to the collecting entity if it doesn't already have one
 */
public class ItemWoodenArrow extends Item {
  /** The default texture for all wooden arrows. */
  public static final IPath DEFAULT_TEXTURE = new SimpleIPath("items/weapon/wooden_arrow.png");

  /**
   * Create a {@link Item} that looks like an arrow and can be collected to be used as ammunition
   * for the bow item.
   */
  public ItemWoodenArrow() {
    super(
        "Wooden Arrow",
        "Ammunition for a Bow",
        new Animation(DEFAULT_TEXTURE),
        new Animation(DEFAULT_TEXTURE));
  }
}
