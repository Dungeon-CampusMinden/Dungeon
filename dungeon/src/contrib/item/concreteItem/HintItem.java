package contrib.item.concreteItem;

import contrib.hud.DialogUtils;
import contrib.item.Item;
import core.Entity;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Objects;

/**
 * An inventory item that, when used, opens an image popup showing a referenced image.
 *
 * <p>Typical use cases include "hint" or "memory" items that let the player re-view a picture they
 * have previously discovered (e.g. the fully assembled image of a solved jigsaw puzzle).
 *
 * <p>The world sprite (used for the dropped representation in the world) defaults to {@code
 * items/rpg/item_paper.png} but can be overridden via the constructor.
 */
public class HintItem extends Item {
  
  private static final String DEFAULT_NAME = "Hint";
  private static final String DEFAULT_DESCRIPTION =
      "A note with an image. [Use] to view the image.";

  private final IPath imagePath;

  /**
   * Creates a new {@link HintItem} with full control over visuals and texts.
   *
   * @param imagePath Path to the image that is shown via {@link DialogUtils#showImagePopUp(String,
   *     int...)} when the item is used from the inventory.
   * @param worldSprite Path to the sprite used for both the inventory icon and the dropped
   *     world-item representation.
   * @param name Display name of the item.
   * @param description Description of the item.
   */
  public HintItem(
      final IPath imagePath, final IPath worldSprite, final String name, final String description) {
    super(name, description, new Animation(Objects.requireNonNull(worldSprite, "worldSprite")));
    this.imagePath = Objects.requireNonNull(imagePath, "imagePath");
  }

  /**
   * Creates a new {@link HintItem} with a custom name and description, using the {@linkplain
   * #DEFAULT_WORLD_SPRITE default world sprite}.
   *
   * @param imagePath Path to the image that is shown when the item is used from the inventory.
   * @param name Display name of the item.
   * @param description Description of the item.
   */
  public HintItem(final IPath imagePath, final String name, final String description) {
    this(imagePath, imagePath, name, description);
  }

  /**
   * Creates a new {@link HintItem} with default name, description and world sprite.
   *
   * @param imagePath Path to the image that is shown when the item is used from the inventory.
   */
  public HintItem(final IPath imagePath) {
    this(imagePath, imagePath, DEFAULT_NAME, DEFAULT_DESCRIPTION);
  }

  /**
   * @return the path of the image that is shown via the image popup when the item is used.
   */
  public IPath imagePath() {
    return imagePath;
  }

  @Override
  public void use(final Entity user) {
    if (user == null) {
      DialogUtils.showImagePopUp(imagePath.pathString());
      return;
    }
    DialogUtils.showImagePopUp(imagePath.pathString(), user.id());
  }
}
