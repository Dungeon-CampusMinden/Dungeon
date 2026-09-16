package rooms.systemRecovery.items;

import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.inventory.Item;
import feature.inventory.ItemRegistry;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Inventory item representing a battery for System Recovery puzzles. */
public class BatteryItem extends Item {

  private static final SimpleIPath TEXTURE = new SimpleIPath("objects/tech/system_battery.png");

  static {
    ItemRegistry.register(BatteryItem.class);
  }

  /** Ensures that this room-specific item is registered for inventory serialization. */
  public static void ensureRegistration() {
    // No-op; calling this method triggers the static registration block.
  }

  /** Creates a new battery item. */
  public BatteryItem() {
    super(
        SystemRecoveryText.key("items.battery-name"),
        SystemRecoveryText.key("items.battery-description"),
        new Animation(TEXTURE),
        new Animation(TEXTURE));
  }
}
