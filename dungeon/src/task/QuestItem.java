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
    private final TaskContentComponent taskContentComponent;

    /**
     * Create a new QuestItem.
     *
     * @param displayName Name of the item, will be shown in the inventory UI.
     * @param description Description of the item, will be shown in the inventory UI.
     * @param inventoryAnimation Texture to show in the inventory.
     * @param worldAnimation Texture to show if the item is dropped on the floor.
     * @param taskContentComponent The TaskContentComponent that stores the Task to which this item
     *     belongs.
     */
    public QuestItem(
            String displayName,
            String description,
            Animation inventoryAnimation,
            Animation worldAnimation,
            TaskContentComponent taskContentComponent) {
        super(displayName, description, inventoryAnimation, worldAnimation);
        this.taskContentComponent = taskContentComponent;
    }

    /**
     * Create a new QuestItem.
     *
     * @param displayName Name of the item, will be shown in the inventory UI.
     * @param description Description of the item, will be shown in the inventory UI.
     * @param animation Texture to show in the inventory and on the floor.
     * @param taskContentComponent The TaskContentComponent that stores the Task to which this item
     *     belongs.
     */
    public QuestItem(
            String displayName,
            String description,
            Animation animation,
            TaskContentComponent taskContentComponent) {
        super(displayName, description, animation);
        this.taskContentComponent = taskContentComponent;
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
