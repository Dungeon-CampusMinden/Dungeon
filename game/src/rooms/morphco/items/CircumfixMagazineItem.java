package rooms.morphco.items;

import engine.Entity;
import engine.utils.Point;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.inventory.Item;
import feature.inventory.ItemRegistry;
import java.util.Optional;
import rooms.morphco.util.MorphCoText;

/**
 * The circumfix magazine of A-12 with the three assembled circumfixes.
 *
 * <p>The assembly station hands it out after the last circumfix; the player carries it to A-12 and
 * puts it in there.
 */
public final class CircumfixMagazineItem extends Item {

  static {
    ItemRegistry.register(CircumfixMagazineItem.class);
  }

  private static final SimpleIPath TEXTURE =
      new SimpleIPath("rooms/morphCo/art/circumfix_magazine.png");

  /** Creates the full magazine. */
  public CircumfixMagazineItem() {
    super(
        MorphCoText.key("items.magazine-name"),
        MorphCoText.key("items.magazine-description"),
        new Animation(TEXTURE),
        new Animation(TEXTURE));
  }

  /** Ensures that the magazine is registered for inventory serialization. */
  public static void ensureRegistration() {
    // Calling this method runs the static registration block.
  }

  /**
   * Keeps the magazine in the inventory; A-12 takes it when the player opens its close-up.
   *
   * @param user entity that tried to use the magazine
   */
  @Override
  public void use(final Entity user) {
    // A-12 takes the magazine.
  }

  /**
   * Does nothing: the station hands out a new magazine while nobody carries one, so a dropped one
   * would exist twice.
   *
   * @param position ignored
   * @return empty
   */
  @Override
  public Optional<Entity> drop(final Point position) {
    return Optional.empty();
  }
}
