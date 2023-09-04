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

    public static final Consumer<Entity> DOOR_OPENER =
            entity ->
                    entity.fetch(DoorComponent.class)
                            .ifPresent(component -> component.door().open());

    private Consumer onActivate;
    private final Task task;

    /**
     * Creates a new TaskManagerComponent and add it to the associated entity.
     *
     * @param task the task this component manages
     */
    public TaskComponent(final Task task) {
        this.task = task;
    }

    /**
     * Returns task this component manages.
     *
     * @return task that this component manages
     */
    public Task task() {
        return task;
    }

    /**
     * Set the function to execute if the associated task is set to active.
     *
     * @param callback new callback function.
     */
    public void onActivate(Consumer<Entity> callback) {
        this.onActivate = callback;
    }

    /**
     * Execute the callback function.
     *
     * @param taskmanager Entity that implements this component.
     */
    public void activate(Entity taskmanager) {
        onActivate.accept(taskmanager);
    }
}
