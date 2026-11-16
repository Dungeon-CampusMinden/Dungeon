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
 * the handout that a player puts together from them in the inventory.
 *
 * <p>Using the item opens it. Dropping one volume onto the other replaces both with the handout.
 * Each item has its own subclass so that the inventory sync can recreate it from its class.
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

  @Override
  public Optional<Item> combine(final Item other) {
    boolean pair =
        this instanceof Part1 && other instanceof Part2
            || this instanceof Part2 && other instanceof Part1;
    return pair ? Optional.of(new Handout()) : Optional.empty();
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

  /** The handout "Morphemtypen", put together from both parts. */
  public static final class Handout extends MorphemeTypesItem {
    /** Creates the handout. */
    public Handout() {
      super(MorphCoDialogs.HANDOUT, "handout-description", "items/rpg/item_paper.png");
    }
  }
}
