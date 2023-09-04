package task;

import core.Entity;

import petriNet.Place;

import semanticanalysis.types.DSLType;

import java.util.*;
import java.util.function.BiFunction;
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
 *
 * <p>Using {@link #registerPlace(Place)}, {@link Place}s can be registered to this task. If the
 * task state changes, each registered place will be notified.
 */
@DSLType
public abstract class Task {

    private static final Set<Task> ALL_TASKS = new HashSet<>();
    private static final String DEFAULT_TASK_TEXT = "No task description provided";
    private static final TaskState DEFAULT_TASK_STATE = TaskState.INACTIVE;
    private TaskState state;
    private String taskText;
    private final Set<Place> observer = new HashSet<>();
    private Entity managementEntity;

    protected List<TaskContent> content;
    protected BiFunction<Task, Set<TaskContent>, Float> scoringFunction;

    /**
     * Create a new Task with the {@link #DEFAULT_TASK_TEXT} in the {@link #DEFAULT_TASK_STATE},
     * with an empty content-collection and without an {@link TaskComponent}.
     */
    public Task() {
        ALL_TASKS.add(this);
        state = DEFAULT_TASK_STATE;
        taskText = DEFAULT_TASK_TEXT;
        content = new LinkedList<>();
    }
    /**
     * Register a {@link Place} with this task.
     *
     * <p>If this task's {@link TaskState} changes, {@link Place#notify(Task, TaskState)} will be
     * called for each registered place.
     *
     * @param place The place to register.
     */
    public void registerPlace(Place place) {
        observer.add(place);
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
     * <p>Each registered {@link Place} will be notified.
     *
     * <p>A {@link TaskState#ACTIVE} cannot be changed to {@link TaskState#INACTIVE}, and a {@link
     * TaskState#FINISHED_PERFECT} or {@link * TaskState#FINISHED_OKAY} or {@link *
     * TaskState#FINISHED_BAD} cannot be changed to {@link TaskState#ACTIVE} or {@link
     * TaskState#INACTIVE}.
     *
     * @param state The new state of the task.
     * @return true if the state was changed successfully, false if not.
     */
    public boolean state(final TaskState state) {
        if (this.state == state
                || this.state == TaskState.FINISHED_BAD
                || this.state == TaskState.FINISHED_OKAY
                || this.state == TaskState.FINISHED_PERFECT) return false;
        if (this.state == TaskState.ACTIVE && state == TaskState.INACTIVE) return false;
        this.state = state;

        observer.forEach(place -> place.notify(this, state));

        if (state == TaskState.ACTIVE && managementEntity != null)
            managementEntity
                    .fetch(TaskComponent.class)
                    .ifPresent(tc -> tc.activate(managementEntity));
        return true;
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
     * Get the current task-manager for this task.
     *
     * @return current manager.
     */
    public Optional<Entity> managerEntity() {
        return Optional.ofNullable(managementEntity);
    }

    /**
     * Set a new Entity that implements the {@link TaskComponent} to manage this task.
     *
     * @param taskmanager new manager.
     */
    public boolean managerEntity(final Entity taskmanager) {
        if (taskmanager.isPresent(TaskComponent.class)) {
            this.managementEntity = taskmanager;
            return true;
        } else return false;
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
     * Callback function to score the task, given a Set of TaskContent
     *
     * @return the callback function
     */
    public BiFunction<Task, Set<TaskContent>, Float> scoringFunction() {
        return scoringFunction;
    }

    /**
     * Set the scoring function for this Task.
     *
     * @param scoringFunction the scoring function to set.
     */
    public void scoringFunction(BiFunction<Task, Set<TaskContent>, Float> scoringFunction) {
        this.scoringFunction = scoringFunction;
    }

    /**
     * Get a stream of all Task-Objects that exist.
     *
     * @return Stream of all Task-Objects that ever exist.
     */
    public static Stream<Task> allTasks() {
        return new HashSet<>(ALL_TASKS).stream();
    }

    /**
     * Status that a task can assume.
     *
     * <p>ACTIVE - The task can be actively worked on.
     *
     * <p>INACTIVE - The task cannot be worked on.
     *
     * <p>FINISHED_PERFECT - The task has been perfectly completed and cannot be worked on anymore.
     *
     * <p>FINISHED_OKAY - The task has been okay completed and cannot be worked on anymore.
     *
     * <p>FINISHED_BAD - The task has been completed poorly and cannot be worked on anymore.
     */
    public enum TaskState {
        ACTIVE,
        INACTIVE,
        FINISHED_PERFECT,
        FINISHED_OKAY,
        FINISHED_BAD
    }
}
