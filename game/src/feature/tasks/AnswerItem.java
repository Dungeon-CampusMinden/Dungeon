package feature.tasks;

import engine.Entity;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.inventory.Item;
import feature.inventory.ItemRegistry;
import java.util.Map;
import java.util.Objects;

/**
 * An item that stands for one answer of a task, e.g. an item of an {@link AssignmentTask}.
 *
 * <p>The answer identifier lives in the item itself because a world item entity is removed when a
 * player picks the item up. It is part of {@link #itemData()}, so it survives inventory moves,
 * multiplayer snapshots, and saved inventories. Answer items do not stack and are not consumed on
 * use.
 */
public final class AnswerItem extends Item {

  private static final String DATA_KEY_ANSWER_ID = "answerId";
  private static final String DATA_KEY_NAME = "name";
  private static final String DATA_KEY_SPRITE = "sprite";

  static {
    ItemRegistry.register(AnswerItem.class, AnswerItem::fromData);
  }

  private final String answerId;
  private final String name;
  private final String sprite;

  /**
   * Creates an answer item.
   *
   * @param answerId identifier of the answer this item stands for
   * @param name name shown to players
   * @param sprite asset path of the inventory and world sprite
   */
  public AnswerItem(final String answerId, final String name, final String sprite) {
    super(name, "", new Animation(new SimpleIPath(Objects.requireNonNull(sprite, "sprite"))));
    this.answerId = Objects.requireNonNull(answerId, "answerId");
    this.name = name;
    this.sprite = sprite;
  }

  /** Registers the item type with {@link ItemRegistry}; called once from {@link Item}. */
  public static void ensureRegistration() {
    // No-op; class-loading triggers the static block above.
  }

  /**
   * Returns the identifier of the answer this item stands for.
   *
   * @return the answer identifier
   */
  public String answerId() {
    return answerId;
  }

  @Override
  public Map<String, String> itemData() {
    return Map.of(DATA_KEY_ANSWER_ID, answerId, DATA_KEY_NAME, name, DATA_KEY_SPRITE, sprite);
  }

  private static AnswerItem fromData(final Map<String, String> data) {
    return new AnswerItem(
        data.get(DATA_KEY_ANSWER_ID), data.get(DATA_KEY_NAME), data.get(DATA_KEY_SPRITE));
  }

  /** Keeps the item in the inventory; an answer has no use effect. */
  @Override
  public void use(final Entity user) {}
}
