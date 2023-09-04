package task;

import core.Component;
import core.Entity;

import dslToGame.DoorComponent;

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
                            .ifPresent(component -> component.door().open());

    private Consumer<Entity> onActivate;
    private final Task task;

    /**
     * Creates a new TaskManagerComponent and adds it to the associated entity.
     *
     * @param task The task managed by this component.
     */
    public TaskComponent(final Task task) {
        this.task = task;
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
