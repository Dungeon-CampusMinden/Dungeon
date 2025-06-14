package graph.petrinet;

import graph.taskdependencygraph.TaskEdge;
import java.util.Set;
import task.Task;

/**
 * Creates PetriNet for {@link Task} and connect it with other Petri-Nets.
 *
 * <p>Use {@link #defaultNet(Task)} to create the base Petri net.
 *
 * <p>Use {@link #connect(PetriNet, PetriNet, TaskEdge.Type)} to connect the second PetriNet to the
 * first one, based on the dependency type defined in the given {@link TaskEdge.Type}.
 *
 * <p>To add more dependencies, first add a new {@link TaskEdge.Type} and then extend the switch
 * statement in {@link #connect(PetriNet, PetriNet, TaskEdge.Type)}.
 *
 * <p>See <a
 * href="https://github.com/Dungeon-CampusMinden/Dungeon/tree/master/doc/control_mechanisms/petri_net_parsing.md">documentation</a>
 * for more.
 */
public class PetriNetFactory {

  /**
   * Create the base Petri net for a task.
   *
   * @param task Task to link to the Petri net
   * @return Created Petri net
   */
  public static PetriNet defaultNet(final Task task) {
    // create places and link them to the tasks
    Place taskNotActivated = new Place();
    Place or = new Place();
    Place taskActivated = new Place();
    taskActivated.changeStateOnTokenAdd(task, Task.TaskState.ACTIVE);
    Place dummy = new Place();
    Place processingActivated = new Place();
    processingActivated.changeStateOnTokenAdd(task, Task.TaskState.PROCESSING_ACTIVE);
    Place finishedFalse = new Place();
    finishedFalse.observe(task, Task.TaskState.FINISHED_WRONG);
    Place finishedCorrect = new Place();
    finishedCorrect.observe(task, Task.TaskState.FINISHED_CORRECT);
    Place end_correct = new Place();
    Place end_false = new Place();
    Place end = new Place();
    // end.changeStateOnTokenAdd(task, Task.TaskState.INACTIVE);

    // create transition and connect the to the places
    Transition activateTask = new Transition(Set.of(taskNotActivated, or), Set.of(taskActivated));
    Transition afterActivated = new Transition(Set.of(taskActivated), Set.of(dummy));
    Transition activateprocessing = new Transition(Set.of(dummy), Set.of(processingActivated));
    Transition correct =
        new Transition(Set.of(processingActivated, finishedCorrect), Set.of(end_correct, end));
    Transition wrong =
        new Transition(Set.of(processingActivated, finishedFalse), Set.of(end_false, end));
    Transition finished = new Transition(Set.of(end), Set.of(new Place()));

    // will be removed if a or connection is created
    or.placeToken();

    return new PetriNet(
        taskNotActivated,
        activateTask,
        taskActivated,
        afterActivated,
        activateprocessing,
        processingActivated,
        finishedFalse,
        finishedCorrect,
        correct,
        wrong,
        end_correct,
        end_false,
        end,
        finished,
        or,
        task);
  }

  /**
   * Connect the given PetriNet to another PetriNet based on the given type.
   *
   * @param on "Main" net where the second net gets connected (for example, this is the base task)
   * @param connect Second net that will be connected to the first net (for example, this is the
   *     subtask)
   * @param type Type of the dependency between the nets
   */
  public static void connect(final PetriNet on, final PetriNet connect, final TaskEdge.Type type) {
    switch (type) {
      case subtask_mandatory:
        connectSubtaskMandatory(on, connect);
        break;
      case subtask_optional:
        connectSubtaskOptional(on, connect);
        break;
      case sequence:
        connectSequence(on, connect);
        break;
      case sequence_and:
        connectSequenceAnd(on, connect);
        break;
      case sequence_or:
        connectSequenceOr(on, connect);
        break;
      case conditional_false:
        connectConditionalFalse(on, connect);
        break;
      case conditional_correct:
        connectConditionalCorrect(on, connect);
        break;
      default:
        throw new RuntimeException(
            "Unsported Edge-Type in TaskDepencyGraph. Can not convert into Petri-Net.");
    }
  }

  /**
   * Connect the given PetriNet to another PetriNet as a mandatory subtask.
   *
   * @param on "Main" net where the second net gets connected
   * @param connect Second net that will be connected to the first net as a mandatory subtask.
   */
  public static void connectSubtaskMandatory(final PetriNet on, final PetriNet connect) {
    Place helperInput = new Place();
    Place helperOutput = new Place();
    connect.activateTask().addDependency(helperInput);
    connect.finished().addTokenOnFire(helperOutput);
    on.afterActivated().addTokenOnFire(helperInput);
    on.activateProcessing().addDependency(helperOutput);
  }

  /**
   * Connect the given PetriNet to another PetriNet as a optional subtask.
   *
   * @param on "Main" net where the second net gets connected
   * @param connect Second net that will be connected to the first net as a optional subtask.
   */
  public static void connectSubtaskOptional(final PetriNet on, final PetriNet connect) {
    Place helperInput = new Place();
    on.activateProcessing().addTokenOnFire(helperInput);
    connect.activateTask().addDependency(helperInput);
    Place helperOutput = new Place();
    on.finished().addTokenOnFire(helperOutput);
    Place subtaskNotSolvedPlace = new Place();
    subtaskNotSolvedPlace.changeStateOnTokenAdd(connect.task(), Task.TaskState.INACTIVE);
    Transition optionalAbort =
        new Transition(
            Set.of(helperOutput, connect.processingActivated()), Set.of(subtaskNotSolvedPlace));
  }

  /**
   * Connect the given PetriNet to another PetriNet as mandatory pretask.
   *
   * @param on "Main" net where the second net gets connected
   * @param connect Second net that will be connected as mandatory pretask to the main task.
   */
  public static void connectSequence(final PetriNet on, PetriNet connect) {
    // for now this is the same
    connectSequenceAnd(on, connect);
  }

  /**
   * Connect the given PetriNet to another PetriNet as mandatory pretask.
   *
   * @param on "Main" net where the second net gets connected
   * @param connect Second net that will be connected as mandatory pretask to the main task.
   */
  public static void connectSequenceAnd(final PetriNet on, PetriNet connect) {
    Place helper = new Place();
    connect.finished().addTokenOnFire(helper);
    on.activateTask().addDependency(helper);
  }

  /**
   * Connect the given PetriNet to another PetriNet as a mandatory pre-task.
   *
   * <p>You can connect multiple pre-tasks with "or"; only one is needed to trigger the transition.
   *
   * @param on "Main" net where the second net gets connected
   * @param connect Second net that will be connected as a mandatory pre-task to the main task.
   */
  public static void connectSequenceOr(final PetriNet on, final PetriNet connect) {
    on.or().removeToken();
    connect.finished().addTokenOnFire(on.or());
  }

  /**
   * Connect the given PetriNet to another PetriNet as mandatory pretask that needs to be finished
   * wrong.
   *
   * @param on "Main" net where the second net gets connected
   * @param connect Second net that will be connected as mandatory pretask to the main task.
   */
  public static void connectConditionalFalse(final PetriNet on, final PetriNet connect) {
    Place helper = new Place();
    on.activateTask().addDependency(helper);
    connect.wrong().addTokenOnFire(helper);
  }

  /**
   * Connect the given PetriNet to another PetriNet as mandatory pretask that needs to be finished
   * correct.
   *
   * @param on "Main" net where the second net gets connected
   * @param connect Second net that will be connected as mandatory pretask to the main task.
   */
  public static void connectConditionalCorrect(final PetriNet on, final PetriNet connect) {
    Place helper = new Place();
    on.activateTask().addDependency(helper);
    connect.correct().addTokenOnFire(helper);
  }
}
