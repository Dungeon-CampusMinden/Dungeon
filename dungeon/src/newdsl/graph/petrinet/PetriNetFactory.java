package newdsl.graph.petrinet;

import newdsl.graph.TaskEdge;
import newdsl.tasks.Task;
import newdsl.tasks.TaskState;

import java.util.Set;

public class PetriNetFactory {

    public static PetriNet defaultNet(final Task task) {
        // create places and link them to the tasks
        Place taskNotActivated = new Place();
        Place or = new Place();
        Place taskActivated = new Place();
        taskActivated.changeStateOnTokenAdd(task, TaskState.ACTIVE);
        Place dummy = new Place();
        Place processingActivated = new Place();
        processingActivated.changeStateOnTokenAdd(task, TaskState.PROCESSING_ACTIVE);
        Place finishedFalse = new Place();
        finishedFalse.observe(task, TaskState.FINISHED_WRONG);
        Place finishedCorrect = new Place();
        finishedCorrect.observe(task, TaskState.FINISHED_CORRECT);
        Place end_correct = new Place();
        Place end_false = new Place();
        Place end = new Place();
        // end.changeStateOnTokenAdd(task, Task.TaskState.INACTIVE);

        // create transition and connect the to the places
        Transition activateTask = new Transition(Set.of(taskNotActivated, or), Set.of(taskActivated));
        Transition afterActivated = new Transition(Set.of(taskActivated), Set.of(dummy));
        Transition activateprocessing = new Transition(Set.of(dummy), Set.of(processingActivated));
        Transition correct = new Transition(Set.of(processingActivated, finishedCorrect), Set.of(end_correct, end));
        Transition wrong = new Transition(Set.of(processingActivated, finishedFalse), Set.of(end_false, end));
        Transition finished = new Transition(Set.of(end), Set.of(new Place()));

        // will be removed if a or connection is created
        or.placeToken();

        return new PetriNet(taskNotActivated, activateTask, taskActivated, afterActivated, activateprocessing, processingActivated, finishedFalse, finishedCorrect, correct, wrong, end_correct, end_false, end, finished, or, task);
    }

    public static void connect(final PetriNet on, final PetriNet connect, final TaskEdge.EdgeType type) {
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
            case conditional_false:
                connectConditionalFalse(on, connect);
                break;
            case conditional_correct:
                connectConditionalCorrect(on, connect);
                break;
            default:
                throw new RuntimeException("Unsported Edge-Type in TaskDepencyGraph. Can not convert into Petri-Net.");
        }
    }

    public static void connectSubtaskMandatory(final PetriNet on, final PetriNet connect) {
        Place helperInput = new Place();
        Place helperOutput = new Place();
        connect.activateTask().addDependency(helperInput);
        connect.finished().addTokenOnFire(helperOutput);
        on.afterActivated().addTokenOnFire(helperInput);
        on.activateProcessing().addDependency(helperOutput);
    }

    public static void connectSubtaskOptional(final PetriNet on, final PetriNet connect) {
        Place helperInput = new Place();
        on.activateProcessing().addTokenOnFire(helperInput);
        connect.activateTask().addDependency(helperInput);
        Place helperOutput = new Place();
        on.finished().addTokenOnFire(helperOutput);
        Place subtaskNotSolvedPlace = new Place();
        subtaskNotSolvedPlace.changeStateOnTokenAdd(connect.task(), TaskState.INACTIVE);
        Transition optionalAbort = new Transition(Set.of(helperOutput, connect.processingActivated()), Set.of(subtaskNotSolvedPlace));
    }


    public static void connectSequence(final PetriNet on, PetriNet connect) {
        // for now this is the same
        connectSequenceAnd(on, connect);
    }

    public static void connectSequenceAnd(final PetriNet on, PetriNet connect) {
        Place helper = new Place();
        connect.finished().addTokenOnFire(helper);
        on.activateTask().addDependency(helper);
    }


    public static void connectSequenceOr(final PetriNet on, final PetriNet connect) {
        on.or().removeToken();
        connect.finished().addTokenOnFire(on.or());
    }

    public static void connectConditionalFalse(final PetriNet on, final PetriNet connect) {
        Place helper = new Place();
        on.activateTask().addDependency(helper);
        connect.wrong().addTokenOnFire(helper);
    }


    public static void connectConditionalCorrect(final PetriNet on, final PetriNet connect) {
        Place helper = new Place();
        on.activateTask().addDependency(helper);
        connect.correct().addTokenOnFire(helper);
    }
}
