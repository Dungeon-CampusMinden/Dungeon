package rooms.systemRecovery.items;

import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.inventory.Item;
import feature.inventory.ItemRegistry;
import java.util.Map;
import rooms.systemRecovery.util.SystemRecoveryText;

/**
 * Programmable locator chip used by the search robot.
 *
 * <p>The chip is empty when it leaves the two-dimensional storage and becomes usable only after the
 * player has completed the nested matrix search in the computer.
 */
public final class SearchProgramChipItem extends Item {

  /** Serialized state key. */
  public static final String DATA_KEY_PROGRAMMED = "programmed";

  private static final SimpleIPath TEXTURE = new SimpleIPath("items/usb-side-red.png");

  static {
    ItemRegistry.register(SearchProgramChipItem.class, SearchProgramChipItem::fromData);
  }

  private final boolean programmed;

  /** Ensures registration before the item is synchronized. */
  public static void ensureRegistration() {}

  /** Creates an empty locator chip. */
  public SearchProgramChipItem() {
    this(false);
  }

  /** Creates a locator chip with the requested programming state. */
  public SearchProgramChipItem(boolean programmed) {
    super(
        SystemRecoveryText.text(
            programmed ? "items.search-programmed-name" : "items.search-empty-name"),
        programmed
            ? SystemRecoveryText.text("items.search-programmed-description")
            : SystemRecoveryText.text("items.search-empty-description"),
        new Animation(TEXTURE),
        new Animation(TEXTURE));
    this.programmed = programmed;
  }

  /**
   * @return whether the nested search program is stored on this chip
   */
  public boolean programmed() {
    return programmed;
  }

  @Override
  public Map<String, String> itemData() {
    return Map.of(DATA_KEY_PROGRAMMED, Boolean.toString(programmed));
  }

  private static SearchProgramChipItem fromData(Map<String, String> data) {
    return new SearchProgramChipItem(
        Boolean.parseBoolean(data.getOrDefault(DATA_KEY_PROGRAMMED, "false")));
  }
}
