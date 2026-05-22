package contrib.item.concreteItem;

import contrib.hud.DialogUtils;
import contrib.item.Item;
import contrib.item.ItemRegistry;
import core.Entity;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.LinkedHashMap;
import java.util.Map;
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

  /** Item-data key carrying the path to the popup image. */
  public static final String DATA_KEY_IMAGE_PATH = "imagePath";

  /** Item-data key carrying the path to the world / inventory sprite. */
  public static final String DATA_KEY_WORLD_SPRITE = "worldSprite";

  /** Item-data key carrying the display name. */
  public static final String DATA_KEY_NAME = "name";

  /** Item-data key carrying the description. */
  public static final String DATA_KEY_DESCRIPTION = "description";

  static {
    ItemRegistry.register(HintItem.class, HintItem::fromData);
  }

  /**
   * Forces the static initializer of this class to run, registering the item with {@link
   * ItemRegistry}.
   */
  public static void ensureRegistration() {
    // No-op; class-loading triggers the static block above.
  }

  private final IPath imagePath;
  private final IPath worldSprite;

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
    this.worldSprite = worldSprite;
  }

  /**
   * Creates a new {@link HintItem} with a custom name and description, using the image path also as
   * the world sprite.
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
  public Map<String, String> itemData() {
    Map<String, String> data = new LinkedHashMap<>();
    data.put(DATA_KEY_IMAGE_PATH, imagePath.pathString());
    data.put(DATA_KEY_WORLD_SPRITE, worldSprite.pathString());
    data.put(DATA_KEY_NAME, displayName());
    data.put(DATA_KEY_DESCRIPTION, description());
    return data;
  }

  /**
   * Item factory used by {@link ItemRegistry} on the receiving end of a network or persistence
   * payload.
   *
   * @param data item data map as produced by {@link #itemData()}
   * @return the reconstructed hint item
   */
  private static HintItem fromData(Map<String, String> data) {
    String imagePath = require(data, DATA_KEY_IMAGE_PATH);
    String worldSprite = data.getOrDefault(DATA_KEY_WORLD_SPRITE, imagePath);
    String name = data.getOrDefault(DATA_KEY_NAME, DEFAULT_NAME);
    String description = data.getOrDefault(DATA_KEY_DESCRIPTION, DEFAULT_DESCRIPTION);
    return new HintItem(
        new SimpleIPath(imagePath), new SimpleIPath(worldSprite), name, description);
  }

  private static String require(Map<String, String> data, String key) {
    String value = data.get(key);
    if (value == null) {
      throw new IllegalArgumentException(
          "HintItem itemData is missing required key '" + key + "'");
    }
    return value;
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
