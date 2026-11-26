package feature.inventory.items;

import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.IPath;
import engine.utils.components.path.SimpleIPath;
import feature.inventory.Item;

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
}
