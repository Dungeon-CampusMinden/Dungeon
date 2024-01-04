package task.game.content;

import contrib.item.Item;
import core.Entity;
import core.utils.components.draw.Animation;
import task.game.components.TaskContentComponent;

/**
 * Used for items that are part of a Task.
 *
 * <p>Extends the Item class and allows it to store a {@link TaskContentComponent} since an item is
 * not an Entity and can't implement it in the ECS way.
 *
 * <p>Use {@link #taskContentComponent()} to retrieve it.
 */
public class QuestItem extends Item {

  private static final String DEFAULT_NAME = "QuestItem for the Quest: ";
  private static final String DEFAULT_DESCRIPTION = "no description";
  private static final String UNDEFINED_QUEST_NAME = DEFAULT_NAME + "undefined Quest";
  private final TaskContentComponent taskContentComponent;

  /**
   * Create a new QuestItem.
   *
   * @param inventoryAnimation Texture to show in the inventory.
   * @param worldAnimation Texture to show if the item is dropped on the floor.
   * @param taskContentComponent The TaskContentComponent that stores the Task to which this item
   *     belongs.
   */
  public QuestItem(
      Animation inventoryAnimation,
      Animation worldAnimation,
      TaskContentComponent taskContentComponent) {
    super(UNDEFINED_QUEST_NAME, DEFAULT_DESCRIPTION, inventoryAnimation, worldAnimation);
    this.taskContentComponent = taskContentComponent;
  }

  @Override
  public String displayName() {
    if (taskContentComponent.content() == null) {
      return UNDEFINED_QUEST_NAME;
    } else {
      return DEFAULT_NAME + "'" + taskContentComponent.content().task().taskName() + "'";
    }
  }

  @Override
  public String description() {
    if (taskContentComponent.content() == null) {
      return DEFAULT_DESCRIPTION;
    } else {
      return taskContentComponent.content().toString();
    }
  }

  /**
   * Create a new QuestItem.
   *
   * @param animation Texture to show in the inventory and on the floor.
   * @param taskContentComponent The TaskContentComponent that stores the Task to which this item
   *     belongs.
   */
  public QuestItem(Animation animation, TaskContentComponent taskContentComponent) {
    this(animation, animation, taskContentComponent);
  }

  /**
   * Retrieves the TaskContentComponent associated with this QuestItem.
   *
   * @return The TaskContentComponent that stores the Task to which this item belongs.
   */
  public TaskContentComponent taskContentComponent() {
    return taskContentComponent;
  }

  @Override
  public void use(Entity e) {}
}
