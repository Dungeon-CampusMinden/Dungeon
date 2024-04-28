package task;

import contrib.components.InventoryComponent;
import contrib.components.ItemComponent;
import core.Entity;
import core.Game;
import core.utils.MissingHeroException;
import core.utils.components.MissingComponentException;
import dsl.annotation.DSLType;
import graph.petrinet.Place;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Stream;
import task.game.components.TaskComponent;
import task.game.components.TaskContentComponent;
import task.game.content.QuestItem;

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

  /** The default explanation for a task. */
  public static final String DEFAULT_EXPLANATION = "No explanation provided";

  private static final Logger LOGGER = Logger.getLogger(Task.class.getName());
  private static final Logger SOL_LOGGER = Logger.getLogger("TaskSolutionLogger");
  private static final Set<Task> ALL_TASKS = new HashSet<>();
  private static final List<Task> SOLVED_TASK_IN_ORDER = new ArrayList<>();
  private static final String DEFAULT_TASK_TEXT = "No task description provided";
  private static final String DEFAULT_TASK_NAME = "No task name provided";
  private static final TaskState DEFAULT_TASK_STATE = TaskState.INACTIVE;
  private static final float DEFAULT_POINTS = 1f;
  private static final float DEFAULT_POINTS_TO_SOLVE = DEFAULT_POINTS;
  private static int _id = 0;

  static {
    try {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy'T'HH-mm-ss");
      String timestamp = dateFormat.format(new Date());
      String directoryPath = System.getProperty("BASELOGDIR", "logs/") + "solutions/";
      String filepath = directoryPath + timestamp + ".log";
      Files.createDirectories(Paths.get(directoryPath));
      FileHandler fileHandler = new FileHandler(filepath);
      fileHandler.setFormatter(new SimpleFormatter());

      SOL_LOGGER.addHandler(fileHandler);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private final int id;
  private final Set<Place> observer = new HashSet<>();
  protected List<TaskContent> content;
  protected BiFunction<Task, Set<TaskContent>, Float> scoringFunction;
  protected Function<Task, Set<TaskContent>> answerPickingFunction;
  protected Function<? extends Task, Set<Set<Entity>>> scenarioBuilderFunction;
  protected float points;
  protected Set<TaskContent> container;
  private TaskState state;
  private String taskText;
  private String scenarioText;
  private String taskName;
  private Entity managementEntity;
  private Set<Set<Entity>> entitySets = new HashSet<>();
  private float pointsToSolve;
  private String explanation = DEFAULT_EXPLANATION;
  private float achievedPoints;

  /**
   * Create a new Task with the {@link #DEFAULT_TASK_TEXT} in the {@link #DEFAULT_TASK_STATE}, with
   * an empty content-collection and without an {@link TaskComponent}.
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
    taskName = DEFAULT_TASK_NAME;
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
   * Get a stream of all solved tasks, in order of solving.
   *
   * @return Stream of all solved tasks.
   */
  public static Stream<Task> allSolvedTaskInOrder() {
    return new ArrayList<>(SOLVED_TASK_IN_ORDER).stream();
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
    if (state == TaskState.FINISHED_CORRECT || state == TaskState.FINISHED_WRONG)
      SOLVED_TASK_IN_ORDER.add(this);
    else if (state == TaskState.ACTIVE && managementEntity != null) {
      managementEntity.fetch(TaskComponent.class).ifPresent(tc -> tc.activate(managementEntity));
    }

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
   * Get the task name.
   *
   * @return task name
   */
  public String taskName() {
    return taskName;
  }

  /**
   * Get the scenario text.
   *
   * @return scenario text
   */
  public String scenarioText() {
    return scenarioText;
  }

  /**
   * Get the forced scenario builder.
   *
   * @return the scenario builder
   */
  public Function<? extends Task, Set<Set<Entity>>> scenarioBuilderFunction() {
    return this.scenarioBuilderFunction;
  }

  /**
   * Set the forced scenario builder.
   *
   * @param func new scenario builder
   */
  public void scenarioBuilderFunction(Function<? extends Task, Set<Set<Entity>>> func) {
    this.scenarioBuilderFunction = func;
  }

  /**
   * Set the scenario text.
   *
   * @param scenarioText new scenario text
   */
  public void scenarioText(String scenarioText) {
    this.scenarioText = scenarioText;
  }

  /**
   * Set the explanation on how to solve the task, that will be shown after a task was solved with
   * mistakes.
   *
   * @param explanation Text that explains how to solve the task.
   */
  public void explanation(String explanation) {
    this.explanation = explanation;
  }

  /**
   * Get an explanation on how to solve the task.
   *
   * @return Explanation on how to solve the task.
   */
  public String explanation() {
    return explanation;
  }

  /**
   * Set the task name.
   *
   * @param taskName new task name
   */
  public void taskName(String taskName) {
    this.taskName = taskName;
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
   * @return true if the manager was set successfully, false if not.
   */
  public boolean managerEntity(final Entity taskmanager) {
    if (taskmanager.isPresent(TaskComponent.class)) {
      this.managementEntity = taskmanager;
      return true;
    } else return false;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
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
   * Add given elements to the internal {@link #content} collection.
   *
   * @param content elements to add to the internal collection
   */
  public void addContent(final TaskContent... content) {
    for (TaskContent c : content) {
      c.task(this);
      this.content.add(c);
    }
  }

  /**
   * Set the Set of Entity-Sets.
   *
   * <p>For each Entity in the outer set, the level generator will generate a room for that where
   * the entities of the inner collection are placed.
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
   * Callback function to score the task, given a Set of TaskContent.
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
    this.answerPickingFunction = answerPickingFunction;
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
        .append(id)
        .append(System.lineSeparator())
        .append(taskName)
        .append(System.lineSeparator())
        .append(taskText)
        .append(System.lineSeparator())
        .append(" was solved with ")
        .append(score)
        .append("/")
        .append(points)
        .append(" Points.")
        .append(System.lineSeparator());
    msgBuilder.append("The task was solved");
    if (score >= pointsToSolve) msgBuilder.append(" successfully.");
    else msgBuilder.append(" unsuccessfully.");
    msgBuilder.append(System.lineSeparator());
    msgBuilder.append("Given answers: ");

    for (TaskContent answer : givenAnswers) {
      msgBuilder.append(answer.toString()).append(System.lineSeparator());
    }
    String msg = msgBuilder.toString();
    SOL_LOGGER.info(msg);

    if (score >= pointsToSolve) state(TaskState.FINISHED_CORRECT);
    else state(TaskState.FINISHED_WRONG);
    achievedPoints = score;

    removeQuestItems();
    return score;
  }

  private void removeQuestItems() {
    Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
    Task t = this;
    // remove all quest items in invetorys
    Game.allEntities()
        .filter(entity -> entity.isPresent(InventoryComponent.class))
        .forEach(entity -> removeQuestItemFromInventory(entity));
    Game.allEntities()
        .filter(entity -> entity.isPresent(ItemComponent.class))
        .forEach(
            entity -> {
              ItemComponent ic =
                  entity
                      .fetch(ItemComponent.class)
                      .orElseThrow(
                          () -> MissingComponentException.build(entity, ItemComponent.class));
              if (ic.item() instanceof QuestItem) {
                if (((QuestItem) ic.item()).taskContentComponent().content().task().equals(t)) {
                  Game.remove(entity);
                }
              }
            });
  }

  private void removeQuestItemFromInventory(Entity hero) {
    InventoryComponent ic =
        hero.fetch(InventoryComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, InventoryComponent.class));
    Task t = this;
    ic.items(QuestItem.class)
        .forEach(
            item -> {
              if (((QuestItem) item).taskContentComponent().content().task().equals(t)) {
                ic.remove(item);
              }
            });
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
   * @param pointsToSolve amount of points that is needed to solve this task successfully.
   */
  public void points(float points, float pointsToSolve) {
    this.points = points;
    this.pointsToSolve = pointsToSolve;
  }

  /**
   * Find the entity that stores the given content.
   *
   * @param taskContent Content we are looking for.
   * @return the entity that stores the given content, empty if no entity stores the content.
   */
  public Optional<Entity> find(TaskContent taskContent) {
    return Game.allEntities()
        .filter(e -> e.isPresent(TaskContentComponent.class))
        .filter(
            e -> {
              TaskContentComponent taskContentComponent =
                  e.fetch(TaskContentComponent.class)
                      .orElseThrow(
                          () -> MissingComponentException.build(e, TaskContentComponent.class));
              return taskContentComponent.content().equals(taskContent);
            })
        .findFirst();
  }

  /**
   * Get the achieved points for the solution of this task.
   *
   * <p>Make sure to check if the Task state is {@link TaskState#FINISHED_CORRECT} or {@link
   * TaskState#FINISHED_WRONG} before.
   *
   * @return the achieved points for the task.
   */
  public float achievedPoints() {
    return achievedPoints;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public int id() {
    return id;
  }

  /**
   * Get a String representation of the correct answers, to show on the HUD.
   *
   * @return String representation of the correct answers
   */
  public abstract String correctAnswersAsString();

  /** Status that a task can assume. */
  public enum TaskState {
    /** ACTIVE - The task can be actively worked on. */
    ACTIVE,
    /** INACTIVE - The task cannot be worked on. */
    INACTIVE,
    /** FINISHED_PERFECT - The task has been perfectly completed and cannot be worked on anymore. */
    PROCESSING_ACTIVE,
    /** FINISHED_OKAY - The task has been okay completed and cannot be worked on anymore. */
    FINISHED_CORRECT,
    /** FINISHED_BAD - The task has been completed poorly and cannot be worked on anymore. */
    FINISHED_WRONG
  }
}
