package task;

import contrib.item.Item;

import core.utils.components.draw.Animation;

import task.components.TaskContentComponent;

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
        super(
                DEFAULT_NAME + taskContentComponent.stream().findFirst().get().task().id(),
                taskContentComponent.stream().findFirst().get().toString(),
                inventoryAnimation,
                worldAnimation);
        this.taskContentComponent = taskContentComponent;
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
}
