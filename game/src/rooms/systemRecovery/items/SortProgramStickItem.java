package rooms.systemRecovery.items;

import engine.Entity;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.inventory.Item;
import feature.inventory.ItemRegistry;
import java.util.Map;
import rooms.systemRecovery.util.SystemRecoveryText;

/** USB stick used to transfer the bubble-sort condition between the computer and the machine. */
public final class SortProgramStickItem extends Item {

  /** Serialized state key. */
  public static final String DATA_KEY_PROGRAMMED = "programmed";

  // The repository currently ships the red USB sprite; keep the sort chip independent from the
  // Last Hour color variants, whose blue asset is not part of this checkout.
  private static final SimpleIPath TEXTURE = new SimpleIPath("items/usb-side-red.png");

  static {
    ItemRegistry.register(SortProgramStickItem.class, SortProgramStickItem::fromData);
  }

  private final boolean programmed;

  /** Ensures registration before an item crosses the network. */
  public static void ensureRegistration() {}

  /** Creates an empty sort-program stick. */
  public SortProgramStickItem() {
    this(false);
  }

  /**
   * Creates a sort-program stick with the requested programming state.
   *
   * @param programmed whether the stick already contains the sort program
   */
  public SortProgramStickItem(boolean programmed) {
    super(
        SystemRecoveryText.key(programmed ? "items.sort-programmed-name" : "items.sort-empty-name"),
        programmed
            ? SystemRecoveryText.key("items.sort-programmed-description")
            : SystemRecoveryText.key("items.sort-empty-description"),
        new Animation(TEXTURE),
        new Animation(TEXTURE));
    this.programmed = programmed;
  }

  /**
   * @return whether the bubble-sort program is stored on this stick
   */
  public boolean programmed() {
    return programmed;
  }

  /**
   * Keeps the stick in the inventory until a computer or the bubble-sort machine transfers it.
   *
   * @param user entity that attempted to use the stick
   */
  @Override
  public void use(final Entity user) {
    // Intentionally empty: puzzle interactions handle the transfer explicitly.
  }

  @Override
  public Map<String, String> itemData() {
    return Map.of(DATA_KEY_PROGRAMMED, Boolean.toString(programmed));
  }

  private static SortProgramStickItem fromData(Map<String, String> data) {
    return new SortProgramStickItem(
        Boolean.parseBoolean(data.getOrDefault(DATA_KEY_PROGRAMMED, "false")));
  }
}
