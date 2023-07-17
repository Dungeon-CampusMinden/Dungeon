package task;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A task that needs to be solved in the game.
 *
 * <p>Stores all the information related to a task, such as the task's status, description, and
 * associated {@link TaskContent}s.
 *
 * <p>All task elements that require representation in the game are stored in the internal {@link
 * #content} collection.
 *
 * <p>The internal collection can be queried as a stream using {@link #contentStream()}, and
 * manipulated using {@link #addContent(TaskContent)}.
 *
 * <p>Each task is associated with a {@link TaskComponent} that handles the meta-control of the
 * task.
 */
public abstract class Task {
    private static final String DEFAULT_TASK_TEXT = "No task description provided";
    private static final TaskState DEFAULT_TASK_STATE = TaskState.INACTIVE;
    private TaskState state;
    private String taskText;
    private TaskComponent managementComponent;
    private Set<TaskContent> content;

    /**
     * Create a new Task with the {@link #DEFAULT_TASK_TEXT} in the {@link #DEFAULT_TASK_STATE},
     * with an empty content-collection and without an {@link TaskComponent}.
     */
    public Task() {
        state = DEFAULT_TASK_STATE;
        taskText = DEFAULT_TASK_TEXT;
        content = new HashSet<>();
    }

    /**
     * Get state of the task.
     *
     * @return the state of the task.
     */
    public TaskState state() {
        return state;
    }

    /**
     * Set the state of the task.
     *
     * @param state new state of the task.
     */
    public void state(final TaskState state) {
        this.state = state;
    }

    /**
     * Get the task text.
     *
     * @return task text
     */
    public String taskText() {
        return taskText;
    }

    /**
     * Set the task text.
     *
     * @param taskText new task text
     */
    public void taskText(final String taskText) {
        this.taskText = taskText;
    }

    /**
     * Get the current task-component that manages this task.
     *
     * @return current manager component
     */
    public TaskComponent managerComponent() {
        return managementComponent;
    }

    /**
     * Set a new task-component to manage this task.
     *
     * @param component new manager-component.
     */
    public void managerComponent(final TaskComponent component) {
        this.managementComponent = component;
    }

    public Stream<TaskContent> contentStream() {
        return content.stream();
    }

    /**
     * Add given element to the internal {@link #content} collection.
     *
     * @param content element to add to the internal collection
     */
    public void addContent(final TaskContent content) {
        this.content.add(content);
    }

    /**
     * Status that a task can assume.
     *
     * <p>ACTIVE - The task can be actively worked on.
     *
     * <p>DEACTIVATE - The task cannot be worked on.
     *
     * <p>FINISHED - The task has been completed and cannot be worked on anymore.
     */
    public enum TaskState {
        ACTIVE,
        INACTIVE,
        FINISHED
    }
}
