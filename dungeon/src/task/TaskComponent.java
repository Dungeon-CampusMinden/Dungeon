package task;

import core.Component;

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
}
