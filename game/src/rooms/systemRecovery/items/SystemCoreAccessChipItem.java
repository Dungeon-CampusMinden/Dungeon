package rooms.systemRecovery.items;

import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.inventory.Item;
import feature.inventory.ItemRegistry;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Access module delivered by the search robot for the central system core. */
public final class SystemCoreAccessChipItem extends Item {

  private static final SimpleIPath TEXTURE = new SimpleIPath("objects/tech/Hand_scanner.png");

  static {
    ItemRegistry.register(SystemCoreAccessChipItem.class);
  }

  /** Ensures registration before the item is synchronized. */
  public static void ensureRegistration() {}

  /** Creates one system-core access module. */
  public SystemCoreAccessChipItem() {
    super(
        SystemRecoveryText.key("items.system-core-name"),
        SystemRecoveryText.key("items.system-core-description"),
        new Animation(TEXTURE),
        new Animation(TEXTURE));
  }
}
