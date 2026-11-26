package rooms.morphco.items;

import engine.Entity;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import feature.inventory.Item;
import feature.inventory.ItemRegistry;
import java.util.Optional;
import rooms.morphco.ui.MorphCoDialogs;
import rooms.morphco.util.MorphCoText;

/**
 * "Morphemtypen und wo sie zu finden sind": the two volumes from the shelves in F-04 and A-12 and
 * the complete edition that replaces both once a player has taken the second one.
 *
 * <p>Using the item opens it; it cannot be dropped. The complete edition is a book like the
 * volumes; its IDs keep the old name "handout" for saved games. Each item has its own subclass so
 * that the inventory sync can recreate it from its class.
 */
public abstract class MorphemeTypesItem extends Item {

  static {
    ItemRegistry.register(Part1.class);
    ItemRegistry.register(Part2.class);
    ItemRegistry.register(Handout.class);
  }

  private final String document;

  /**
   * Creates the item of a document.
   *
   * @param document document ID, see {@link MorphCoDialogs#MORPH_1}
   * @param description description key below {@code morphCo.items}
   * @param texture inventory icon
   */
  protected MorphemeTypesItem(String document, String description, String texture) {
    super(
        MorphCoText.key("documents." + document + "-title"),
        MorphCoText.key("items." + description),
        new Animation(new SimpleIPath(texture)),
        new Animation(new SimpleIPath(texture)));
    this.document = document;
  }

  /** Ensures that the items are registered for inventory serialization. */
  public static void ensureRegistration() {
    // Calling this method runs the static registration block.
  }

  /**
   * Returns the document that the item shows.
   *
   * @return document ID, see {@link MorphCoDialogs#MORPH_1}
   */
  public String document() {
    return document;
  }

  @Override
  public Optional<String> useHint() {
    return Optional.of("read");
  }

  /**
   * Opens the document for the player and keeps the item.
   *
   * @param user player who reads
   */
  @Override
  public void use(final Entity user) {
    DialogFactory.show(
        DialogContext.builder()
            .type(MorphCoDialogs.DOCUMENT)
            .put(MorphCoDialogs.DOCUMENT_ID, document)
            .build(),
        user.id());
  }

  /**
   * Every player takes their own copy from the shelf, and only the shelf binds two volumes into the
   * complete edition, so a volume picked up from the floor would never be bound.
   */
  @Override
  public boolean canDrop() {
    return false;
  }

  /** Part 1 from the shelf in F-04: free morphemes. */
  public static final class Part1 extends MorphemeTypesItem {
    /** Creates the volume. */
    public Part1() {
      super(MorphCoDialogs.MORPH_1, "volume-description", "items/rpg/item_book_teal_lines.png");
    }
  }

  /** Part 2 from the shelf in A-12: bound morphemes. */
  public static final class Part2 extends MorphemeTypesItem {
    /** Creates the volume. */
    public Part2() {
      super(MorphCoDialogs.MORPH_2, "volume-description", "items/rpg/item_book_blue_lines.png");
    }
  }

  /** The complete edition, bound from both parts. */
  public static final class Handout extends MorphemeTypesItem {
    /** Creates the complete edition. */
    public Handout() {
      super(MorphCoDialogs.HANDOUT, "handout-description", "items/rpg/item_book_purple_lines.png");
    }
  }
}
