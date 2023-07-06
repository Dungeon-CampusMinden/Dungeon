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
 * manipulated using {@link #addContent(TaskContent)} and {@link #removeContent(TaskContent)}.
 *
 * <p>Each task is associated with a {@link task.TaskManagerComponent} that handles the meta-control
 * of the task.
 */
public abstract class Task {
    private static final String DEFAULT_TASK_TEXT = "No task description provided";
    private static final TaskState DEFAULT_TASK_STATE = TaskState.DEACTIVATE;
    private TaskState state;
    private String taskText;
    private TaskManagerComponent managementComponent;
    private Set<TaskContent> content;

    /**
     * Create a new Task
     *
     * @param state state in which the task should start in
     * @param taskText description of the task, what is to do?
     * @param component {@link TaskManagerComponent} that manages this task
     * @param content collection of {@link TaskContent}s that are part of this task
     */
    public Task(
            final TaskState state,
            final String taskText,
            final TaskManagerComponent component,
            final Set<TaskContent> content) {
        this.state = state;
        this.taskText = taskText;
        this.managementComponent = component;
        this.content = content;
    }

    /**
     * Create a new Task, with an empty content-collection.
     *
     * @param state state in which the task should start in
     * @param taskText description of the task, what is to do?
     * @param component {@link TaskManagerComponent} that manages this task
     */
    public Task(
            final TaskState state, final String taskText, final TaskManagerComponent component) {
        this(state, taskText, component, new HashSet<>());
    }

    /**
     * Create a new Task, with an empty content-collection and without an {@link
     * TaskManagerComponent}.
     *
     * @param state state in which the task should start in
     * @param taskText description of the task, what is to do?
     */
    public Task(final TaskState state, final String taskText) {
        this(state, taskText, null);
    }

    /**
     * Create a new Task in the {@link #DEFAULT_TASK_STATE}, with an empty content-collection and
     * without an {@link TaskManagerComponent}.
     *
     * @param taskText description of the task, what is to do?
     */
    public Task(final String taskText) {
        this(DEFAULT_TASK_STATE, taskText);
    }

    /**
     * Create a new Task with the {@link #DEFAULT_TASK_TEXT} in the {@link #DEFAULT_TASK_STATE},
     * with an empty content-collection and without an {@link TaskManagerComponent}.
     */
    public Task() {
        this(DEFAULT_TASK_TEXT);
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
     * Get the current manager component
     *
     * @return current manager component
     */
    public TaskManagerComponent managerComponent() {
        return managementComponent;
    }

    /**
     * Set a new manager-component.
     *
     * @param component new manager-component.
     */
    public void managerComponent(final TaskManagerComponent component) {
        this.managementComponent = component;
    }

    public Stream<TaskContent> contentStream() {
        return content.stream();
    }

    /**
     * Set the internal collection to a copy of the given one.
     *
     * @param content new {@link TaskContent}s collection
     */
    public void content(final Set<TaskContent> content) {
        this.content = new HashSet<>(content);
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
     * Remove given element from the internal {@link #content} collection.
     *
     * @param content element to remove from the internal collection
     */
    public void removeContent(final TaskContent content) {
        this.content.remove(content);
    }

    /**
     * Check if the internal collection contains the given element
     *
     * @param content element to check for
     * @return true if the element is in the internal collection, false if not
     */
    public boolean hasContent(final TaskContent content) {
        return this.content.contains(content);
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
        DEACTIVATE,
        FINISHED
    }
}
