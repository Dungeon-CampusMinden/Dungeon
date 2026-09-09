package rooms.systemRecovery.items;

import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.inventory.Item;
import feature.inventory.ItemRegistry;
import java.util.Map;

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

  /** Creates a sort-program stick with the requested programming state. */
  public SortProgramStickItem(boolean programmed) {
    super(
        programmed ? "Sortierchip (programmiert)" : "Sortierchip (leer)",
        programmed
            ? "Enthält den Bubble-Sort-Vergleich."
            : "Ein leerer USB-Stick für den Sortieralgorithmus.",
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

  @Override
  public Map<String, String> itemData() {
    return Map.of(DATA_KEY_PROGRAMMED, Boolean.toString(programmed));
  }

  private static SortProgramStickItem fromData(Map<String, String> data) {
    return new SortProgramStickItem(
        Boolean.parseBoolean(data.getOrDefault(DATA_KEY_PROGRAMMED, "false")));
  }
}
