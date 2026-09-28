package rooms.systemRecovery.items;

import engine.Entity;
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
  /** Serialized code entered into the editor's fill-in fields. */
  public static final String DATA_KEY_DRAFT = "draft";

  private static final SimpleIPath TEXTURE = new SimpleIPath("items/usb-side-blue.png");

  static {
    ItemRegistry.register(SearchProgramChipItem.class, SearchProgramChipItem::fromData);
  }

  private final boolean programmed;
  private final String draft;

  /** Ensures registration before the item is synchronized. */
  public static void ensureRegistration() {}

  /** Creates an empty locator chip. */
  public SearchProgramChipItem() {
    this(false);
  }

  /**
   * Creates a locator chip with the requested programming state.
   *
   * @param programmed whether the chip already contains the search program
   */
  public SearchProgramChipItem(boolean programmed) {
    this(programmed, "");
  }

  /**
   * Creates a locator chip with its saved fill-in draft.
   *
   * @param programmed whether the chip contains a validated program
   * @param draft saved content of the editor's fill-in fields
   */
  public SearchProgramChipItem(boolean programmed, String draft) {
    super(
        SystemRecoveryText.key(
            programmed ? "items.search-programmed-name" : "items.search-empty-name"),
        programmed
            ? SystemRecoveryText.key("items.search-programmed-description")
            : SystemRecoveryText.key("items.search-empty-description"),
        new Animation(TEXTURE),
        new Animation(TEXTURE));
    this.programmed = programmed;
    this.draft = draft == null ? "" : draft;
  }

  /**
   * @return whether the nested search program is stored on this chip
   */
  public boolean programmed() {
    return programmed;
  }

  /**
   * Returns the saved values from the chip editor's fill-in fields.
   *
   * @return encoded fill-in field values
   */
  public String draft() {
    return draft;
  }

  /**
   * Keeps the chip in the inventory until a computer or the search-robot controller transfers it.
   *
   * @param user entity that attempted to use the chip
   */
  @Override
  public void use(final Entity user) {
    // Intentionally empty: puzzle interactions handle the transfer explicitly.
  }

  @Override
  public Map<String, String> itemData() {
    return Map.of(DATA_KEY_PROGRAMMED, Boolean.toString(programmed), DATA_KEY_DRAFT, draft);
  }

  private static SearchProgramChipItem fromData(Map<String, String> data) {
    return new SearchProgramChipItem(
        Boolean.parseBoolean(data.getOrDefault(DATA_KEY_PROGRAMMED, "false")),
        data.getOrDefault(DATA_KEY_DRAFT, ""));
  }
}
