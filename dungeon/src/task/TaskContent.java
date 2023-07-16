package task;

/**
 * A part of the content of a complete task.
 *
 * <p>TaskContent objects are represented by the {@link TaskContentComponent} of entities in the
 * game.
 *
 * <p>A TaskContent can, for example, be an answer option for a quiz question or a rule for
 * replacement tasks.
 */
public abstract class TaskContent {
    private final Task task;

    /**
     * Creates a new TaskContent.
     *
     * @param task Task to which this content belongs.
     */
    public TaskContent(final Task task) {
        this.task = task;
    }

    /**
     * Return associated Task.
     *
     * @return task to which this content belongs.
     */
    public Task task() {
        return task;
    }
}
