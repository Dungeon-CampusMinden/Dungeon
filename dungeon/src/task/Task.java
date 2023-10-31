package task;

import core.Entity;
import core.utils.logging.CustomLogLevel;

import petriNet.Place;

import semanticanalysis.types.DSLType;

import task.components.TaskComponent;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;
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

    private static final Logger LOGGER = Logger.getLogger(Task.class.getSimpleName());
    private static final Set<Task> ALL_TASKS = new HashSet<>();
    private static final String DEFAULT_TASK_TEXT = "No task description provided";
    private static final TaskState DEFAULT_TASK_STATE = TaskState.INACTIVE;
    private static final float DEFAULT_POINTS = 1f;
    private static final float DEFAULT_POINTS_TO_SOLVE = DEFAULT_POINTS;
    private static int _id = 0;
    private final int id;
    private final Set<Place> observer = new HashSet<>();
    protected List<TaskContent> content;
    protected BiFunction<Task, Set<TaskContent>, Float> scoringFunction;
    protected Function<Task, Set<TaskContent>> answerPickingFunction;
    protected float points;
    protected Set<TaskContent> container;
    private TaskState state;
    private String taskText;
    private Entity managementEntity;
    private Set<Set<Entity>> entitySets = new HashSet<>();
    private float pointsToSolve;

    /**
     * Create a new Task with the {@link #DEFAULT_TASK_TEXT} in the {@link #DEFAULT_TASK_STATE},
     * with an empty content-collection and without an {@link TaskComponent}.
     */
    public Task() {
        this.id = _id++;
        ALL_TASKS.add(this);
        state = DEFAULT_TASK_STATE;
        taskText = DEFAULT_TASK_TEXT;
        content = new LinkedList<>();
        points = DEFAULT_POINTS;
        pointsToSolve = DEFAULT_POINTS_TO_SOLVE;
        container = new HashSet<>();
    }

    /**
     * Get a stream of all Task-Objects that exist.
     *
     * @return Stream of all Task-Objects that ever exist.
     */
    public static Stream<Task> allTasks() {
        return new HashSet<>(ALL_TASKS).stream();
    }

    /** Clear the {@link #ALL_TASKS} Set. */
    public static void cleanupAllTask() {
        ALL_TASKS.clear();
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
     * Add a {@link TaskContent} as container to this task.
     *
     * <p>A container (like a chest) will be used to find the given answers of a player in the game.
     *
     * @param container container to add.
     */
    public void addContainer(TaskContent container) {
        this.container.add(container);
    }

    /**
     * Get the Container of this task.
     *
     * @return Container of this task as stream.
     */
    public Stream containerStream() {
        return new HashSet<>(container).stream();
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
     * @param state The new state of the task.
     * @return true if the state was changed successfully, false if not.
     */
    public boolean state(final TaskState state) {
        if (this.state == state) return false;
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
     * Get the TaskContent instance at the given index.
     *
     * @param index index of the wanted task content
     * @return the task content at the given index.
     */
    public TaskContent contentByIndex(int index) {
        return content.get(index);
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
     * Set the Set of Entity-Sets.
     *
     * <p>For each Set<Entity> in the outer set, the level generator will generate a room for that
     * where the entities of the inner collection are placed.
     *
     * @param entitySets Set that contains the Set of Entities that are related to the task.
     */
    public void entitieSets(Set<Set<Entity>> entitySets) {
        this.entitySets = entitySets;
    }

    /**
     * Get the collection of Entity Sets.
     *
     * @return A set that contains sets of entities related to the task.
     */
    public Set<Set<Entity>> entitySets() {
        return new HashSet<>(entitySets);
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
     * Set the function to pick the given answers out of the game.
     *
     * @param answerPickingFunction the answer picking function to set.
     */
    public void answerPickingFunction(Function<Task, Set<TaskContent>> answerPickingFunction) {
        this.scoringFunction = scoringFunction;
    }

    /**
     * Callback function to pick the given answers out of the game.
     *
     * @return the callback function
     */
    public Function<Task, Set<TaskContent>> answerPickingFunction() {
        return answerPickingFunction;
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
     * Execute the scoring function.
     *
     * <p>This will mark the task as done.
     *
     * <p>This will change the task state.
     *
     * <p>This will inform the petri net about the task state changes.
     *
     * <p>This will log the result.
     *
     * <p>This will give the player a reward, if the task was solved correctly.
     *
     * @return reached points.
     */
    public float gradeTask() {
        return gradeTask(answerPickingFunction.apply(this));
    }

    /**
     * Execute the scoring function.
     *
     * <p>This will mark the task as done.
     *
     * <p>This will change the task state.
     *
     * <p>This will inform the petri net about the task state changes.
     *
     * <p>This will log the result.
     *
     * <p>This will give the player a reward, if the task was solved correctly.
     *
     * @param givenAnswers the given answers to the question of this tak.
     * @return reached points.
     */
    public float gradeTask(Set<TaskContent> givenAnswers) {
        float score = scoringFunction.apply(this, givenAnswers);
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder
                .append("Task: ")
                .append(taskText)
                .append(" was solved with ")
                .append(score)
                .append("/")
                .append(points)
                .append(" Points");
        msgBuilder.append("The task was solved");
        if (score >= pointsToSolve) msgBuilder.append(" successfully");
        else msgBuilder.append(" unsuccessfully");

        msgBuilder.append(" Given answers: ");

        for (TaskContent answer : givenAnswers) {
            msgBuilder.append(answer.toString());
        }
        String msg = msgBuilder.toString();
        LOGGER.log(CustomLogLevel.TASK, msg);

        if (score >= pointsToSolve) state(TaskState.FINISHED_CORRECT);
        else state(TaskState.FINISHED_WRONG);

        return score;
    }

    /**
     * Get the amount of points that this task is worth.
     *
     * @return points that this task is worth.
     */
    public float points() {
        return points;
    }

    /**
     * Set the amount of points that this task is worth.
     *
     * @param points points that this task is worth.
     * @param pointsToSolve amount of points that is needed to solve this task sucessfully.
     */
    public void points(float points, float pointsToSolve) {
        this.points = points;
        this.pointsToSolve = pointsToSolve;
    }

    public int id() {
        return id;
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
        PROCESSING_ACTIVE,
        FINISHED_CORRECT,
        FINISHED_WRONG
    }
}
