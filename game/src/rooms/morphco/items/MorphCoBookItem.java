package rooms.morphco.items;

import engine.Entity;
import engine.utils.Point;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import feature.inventory.Item;
import feature.inventory.ItemRegistry;
import java.util.Optional;
import java.util.Set;
import rooms.morphco.ui.MorphCoDialogs;
import rooms.morphco.util.MorphCoText;

/**
 * The books players carry: the manual of the inflection machine and part 1 from the shelf in F-04,
 * part 2 of "Morphemtypen und wo sie zu finden sind" from the shelf in A-12, and the bound editions
 * that replace them.
 *
 * <p>Both parts bind into the complete edition of "Morphemtypen", its IDs keep the old name
 * "handout" for saved games. Together with the manual they bind into the collected edition that
 * holds every text. Using an item opens it; it cannot be dropped. Each item has its own subclass so
 * that the inventory sync can recreate it from its class.
 */
public abstract class MorphCoBookItem extends Item {

  static {
    ItemRegistry.register(Part1.class);
    ItemRegistry.register(Part2.class);
    ItemRegistry.register(Handout.class);
    ItemRegistry.register(Manual.class);
    ItemRegistry.register(Collected.class);
  }

  private final String document;
  private final Set<String> contents;

  /**
   * Creates the item of a document.
   *
   * @param document document ID, see {@link MorphCoDialogs#MORPH_1}
   * @param description description key below {@code morphCo.items}
   * @param texture inventory icon
   * @param contents documents bound into the book, see {@link MorphCoDialogs#MORPH_1}
   */
  protected MorphCoBookItem(
      String document, String description, String texture, String... contents) {
    super(
        MorphCoText.key("documents." + document + "-title"),
        MorphCoText.key("items." + description),
        new Animation(new SimpleIPath(texture)),
        new Animation(new SimpleIPath(texture)));
    this.document = document;
    this.contents = Set.of(contents);
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
   * Returns the single documents bound into the book.
   *
   * @return the parts and the manual the book holds; a single book holds only its own document
   */
  public Set<String> contents() {
    return contents;
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
   * Does nothing: every player takes their own copy from the shelf, and only the shelf binds the
   * books, so a book picked up from the floor would never be bound.
   *
   * @param position ignored
   * @return empty
   */
  @Override
  public Optional<Entity> drop(final Point position) {
    return Optional.empty();
  }

  /** Part 1 from the shelf in F-04: free morphemes. */
  public static final class Part1 extends MorphCoBookItem {
    /** Creates the volume. */
    public Part1() {
      super(
          MorphCoDialogs.MORPH_1,
          "volume-description",
          "items/rpg/item_book_teal_lines.png",
          MorphCoDialogs.MORPH_1);
    }
  }

  /** Part 2 from the shelf in A-12: bound morphemes. */
  public static final class Part2 extends MorphCoBookItem {
    /** Creates the volume. */
    public Part2() {
      super(
          MorphCoDialogs.MORPH_2,
          "volume-description",
          "items/rpg/item_book_blue_lines.png",
          MorphCoDialogs.MORPH_2);
    }
  }

  /** The complete edition, bound from both parts. */
  public static final class Handout extends MorphCoBookItem {
    /** Creates the complete edition. */
    public Handout() {
      super(
          MorphCoDialogs.HANDOUT,
          "handout-description",
          "items/rpg/item_book_purple_lines.png",
          MorphCoDialogs.MORPH_1,
          MorphCoDialogs.MORPH_2);
    }
  }

  /** The manual of the inflection machine from the shelf in F-04. */
  public static final class Manual extends MorphCoBookItem {
    /** Creates the manual. */
    public Manual() {
      super(
          MorphCoDialogs.HANDBOOK,
          "manual-description",
          "items/rpg/item_book_red_lines.png",
          MorphCoDialogs.HANDBOOK);
    }
  }

  /** The collected edition, bound from the manual and both parts. */
  public static final class Collected extends MorphCoBookItem {
    /** Creates the collected edition. */
    public Collected() {
      super(
          MorphCoDialogs.COLLECTED,
          "collected-description",
          "items/rpg/item_book_brown_square.png",
          MorphCoDialogs.HANDBOOK,
          MorphCoDialogs.MORPH_1,
          MorphCoDialogs.MORPH_2);
    }
  }
}
