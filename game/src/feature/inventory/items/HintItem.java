package feature.inventory.items;

import engine.Entity;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.IPath;
import engine.utils.components.path.SimpleIPath;
import feature.hud.DialogUtils;
import feature.inventory.Item;
import feature.inventory.ItemRegistry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import rooms.lasthour.util.translation.TranslationKey;

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
  private final String translationKey;

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
      final IPath imagePath,
      final IPath worldSprite,
      final String name,
      final String description,
      String translationKey) {
    super(name, description, new Animation(Objects.requireNonNull(worldSprite, "worldSprite")));
    this.imagePath = Objects.requireNonNull(imagePath, "imagePath");
    this.worldSprite = worldSprite;
    this.translationKey = translationKey;
  }

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
    this(imagePath, worldSprite, name, description, "");
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
    this(imagePath, imagePath, TranslationKey.HintItemName, TranslationKey.HintItemDescription);
  }

  /**
   * Creates a new {@link HintItem} with default name, description and world sprite.
   *
   * @param imagePath Path to the image that is shown when the item is used from the inventory.
   * @param translationKey key for the translation, used when the item is being used.
   */
  public HintItem(final IPath imagePath, String translationKey) {
    this(
        imagePath,
        imagePath,
        TranslationKey.HintItemName,
        TranslationKey.HintItemDescription,
        translationKey);
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
    String name = data.getOrDefault(DATA_KEY_NAME, TranslationKey.HintItemName);
    String description =
        data.getOrDefault(DATA_KEY_DESCRIPTION, TranslationKey.HintItemDescription);
    return new HintItem(
        new SimpleIPath(imagePath), new SimpleIPath(worldSprite), name, description);
  }

  private static String require(Map<String, String> data, String key) {
    String value = data.get(key);
    if (value == null) {
      throw new IllegalArgumentException("HintItem itemData is missing required key '" + key + "'");
    }
    return value;
  }

  @Override
  public void use(final Entity user) {
    if (user == null) {
      if (translationKey.isEmpty()) DialogUtils.showImagePopUp(imagePath.pathString());
      else DialogUtils.showImagePopUp(translationKey);
      return;
    }
    if (translationKey.isEmpty()) DialogUtils.showImagePopUp(imagePath.pathString(), user.id());
    else DialogUtils.showImagePopUp(translationKey, user.id());
  }
}
