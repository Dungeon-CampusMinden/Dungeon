package rooms.morphco.items;

import engine.Entity;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.inventory.Item;
import feature.inventory.ItemRegistry;
import rooms.morphco.puzzles.CircumfixPuzzle;
import rooms.morphco.util.MorphCoText;

/**
 * A broken circumfix part from the pile in A-12.
 *
 * <p>Each part has its own subclass so that the inventory sync can recreate it from its class.
 */
public abstract class CircumfixFragmentItem extends Item {

  static {
    ItemRegistry.register(Ge.class);
    ItemRegistry.register(Be.class);
    ItemRegistry.register(Ig.class);
    ItemRegistry.register(E.class);
    ItemRegistry.register(En.class);
  }

  private final CircumfixPuzzle.Fragment fragment;

  /**
   * Creates a part.
   *
   * @param fragment part of a circumfix
   */
  protected CircumfixFragmentItem(CircumfixPuzzle.Fragment fragment) {
    super(
        MorphCoText.key("items.fragment-name", fragment.label()),
        MorphCoText.key("items.fragment-description"),
        new Animation(texture(fragment)),
        new Animation(texture(fragment)));
    this.fragment = fragment;
  }

  private static SimpleIPath texture(CircumfixPuzzle.Fragment fragment) {
    return new SimpleIPath("rooms/morphCo/art/fragment_" + fragment.text() + ".png");
  }

  /** Ensures that the parts are registered for inventory serialization. */
  public static void ensureRegistration() {
    // Calling this method runs the static registration block.
  }

  /**
   * Creates the item of a part.
   *
   * @param fragment part of a circumfix
   * @return a new item
   */
  public static CircumfixFragmentItem of(CircumfixPuzzle.Fragment fragment) {
    return switch (fragment) {
      case GE -> new Ge();
      case BE -> new Be();
      case IG -> new Ig();
      case E -> new E();
      case EN -> new En();
    };
  }

  /**
   * Returns the part.
   *
   * @return part of a circumfix
   */
  public CircumfixPuzzle.Fragment fragment() {
    return fragment;
  }

  /**
   * Keeps the part in the inventory; it is only used at the assembly station.
   *
   * @param user entity that tried to use the part
   */
  @Override
  public void use(final Entity user) {
    // The assembly station consumes the parts.
  }

  /** {ge}. */
  public static final class Ge extends CircumfixFragmentItem {
    /** Creates the part. */
    public Ge() {
      super(CircumfixPuzzle.Fragment.GE);
    }
  }

  /** {be}. */
  public static final class Be extends CircumfixFragmentItem {
    /** Creates the part. */
    public Be() {
      super(CircumfixPuzzle.Fragment.BE);
    }
  }

  /** {ig}. */
  public static final class Ig extends CircumfixFragmentItem {
    /** Creates the part. */
    public Ig() {
      super(CircumfixPuzzle.Fragment.IG);
    }
  }

  /** {e}. */
  public static final class E extends CircumfixFragmentItem {
    /** Creates the part. */
    public E() {
      super(CircumfixPuzzle.Fragment.E);
    }
  }

  /** {en}. */
  public static final class En extends CircumfixFragmentItem {
    /** Creates the part. */
    public En() {
      super(CircumfixPuzzle.Fragment.EN);
    }
  }
}
