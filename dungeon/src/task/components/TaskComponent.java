package task.components;

import core.Component;
import core.Entity;
import core.level.elements.tile.DoorTile;

import task.Task;

import java.util.function.Consumer;

/**
 * Marks an entity as a management entity for a task.
 *
 * <p>Management entities handle a task, such as starting or ending it, but are not directly part of
 * the task itself.
 *
 * <p>Example: A wizard who needs to be interacted with by the player to activate the task, and then
 * the solution item needs to be brought to them.
 *
 * <p>{@link TaskComponent} stores a reference to the corresponding {@link Task}
 */
public final class TaskComponent implements Component {

    private static final Consumer<Entity> EMPTY_ON_ACTIVATE = (taskmanager) -> {};

    /** Fetches the {@link DoorComponent} from the given entity and opens the door. */
    public static final Consumer<Entity> DOOR_OPENER =
            entity ->
                    entity.fetch(DoorComponent.class)
                            .ifPresent(
                                    component ->
                                            component.doors().stream().forEach(DoorTile::open));

    private Consumer<Entity> onActivate;
    private final Task task;

    /**
     * Creates a new TaskManagerComponent and adds it to the associated entity.
     *
     * <p>Automatically adds this component to the given entity and sets the entity as the manager
     * entity of the given task.
     *
     * @param task The task managed by this component.
     * @param entity Entity that should contain the TaskComponent.
     */
    public TaskComponent(final Task task, final Entity entity) {
        this.task = task;
        entity.addComponent(this);
        task.managerEntity(entity);
        onActivate = DOOR_OPENER;
    }

    /**
     * Returns the task managed by this component.
     *
     * @return The task managed by this component.
     */
    public Task task() {
        return task;
    }

    /**
     * Set the function to execute when the associated task is set to active.
     *
     * @param callback The new callback function.
     */
    public void onActivate(Consumer<Entity> callback) {
        this.onActivate = callback;
    }

    /**
     * Execute the callback function.
     *
     * @param taskManager Entity that implements this component.
     */
    public void activate(Entity taskManager) {
        onActivate.accept(taskManager);
    }
}
