package graphconverter;

import petriNet.PetriNet;
import petriNet.Place;
import petriNet.Transition;

import task.Task;

import taskdependencygraph.TaskEdge;

import java.util.Set;

public class PetriNetFactory {

    public static PetriNet defaultNet(Task task) {
        // create places and link them to the tasks
        Place taskNotActivated = new Place();
        Place taskActivated = new Place();
        taskActivated.changeStateOnTokenAdd(task, Task.TaskState.ACTIVE);
        Place dummy = new Place();
        Place processingActivated = new Place();
        processingActivated.changeStateOnTokenAdd(task, Task.TaskState.PROCESSING_ACTIVE);
        Place finishedFalse = new Place();
        finishedFalse.observe(task, Task.TaskState.FINISHED_WRONG);
        Place finishedCorrect = new Place();
        finishedFalse.observe(task, Task.TaskState.FINISHED_CORRECT);
        Place end_correct = new Place();
        Place end_false = new Place();
        Place end = new Place();
        end.changeStateOnTokenAdd(task, Task.TaskState.INACTIVE);

        // create transition and connect the to the places
        Transition activateTask = new Transition(Set.of(taskNotActivated), Set.of(taskActivated));
        Transition afterActivated = new Transition(Set.of(taskActivated), Set.of(dummy));
        Transition activateprocessing = new Transition(Set.of(dummy), Set.of(processingActivated));
        Transition correct =
                new Transition(
                        Set.of(processingActivated, finishedCorrect), Set.of(end_correct, end));
        Transition wrong =
                new Transition(Set.of(processingActivated, finishedFalse), Set.of(end_false, end));
        Transition finisehd = new Transition(Set.of(end), Set.of(new Place()));

        // init token
        taskNotActivated.placeToken();

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
                finisehd,
                task);
    }

    public static void connect(PetriNet a, PetriNet b, TaskEdge.Type type) {
        switch (type) {
            case subtask_mandatory:
                connectSubtaskMandatory(a, b);
                break;
            case subtask_optional:
                connectSubtaskOptional(a, b);
                break;
            case sequence:
                connectSequence(a, b);
                break;
            case sequence_and:
                connectSequenceAnd(a, b);
                break;
            case sequence_or:
                connectSequenceOr(a, b);
                break;
            case conditional_false:
                connectConditionalFalse(a, b);
                break;
            case conditional_correct:
                connectConditionalCorrect(a, b);
                break;
            default:
                throw new RuntimeException(
                        "Unsported Edge-Type in TaskDepencyGraph. Can not convert into Petri-Net.");
        }
    }

    public static void connectSubtaskMandatory(PetriNet a, PetriNet b) {
        Place helperInput = new Place();
        Place helperOutput = new Place();
        b.activateTask().addDependency(helperInput);
        b.finisehd().addTokenOnFire(helperOutput);
        a.afterActivated().addTokenOnFire(helperInput);
        a.activateprocessing().addDependency(helperOutput);
    }

    public static void connectSubtaskOptional(PetriNet a, PetriNet b) {
        Place helperInout = new Place();
        a.activateprocessing().addTokenOnFire(helperInout);
        Place helperOutput = new Place();
        a.finisehd().addTokenOnFire(helperOutput);
        Place subtaskNotSolvedPlace = new Place();
        subtaskNotSolvedPlace.changeStateOnTokenAdd(b.task(), Task.TaskState.INACTIVE);
        new Transition(
                Set.of(helperOutput, b.processingActivated()), Set.of(subtaskNotSolvedPlace));
    }

    public static void connectSequence(PetriNet a, PetriNet b) {
        // for now this is the same
        connectSequenceAnd(a, b);
    }

    public static void connectSequenceAnd(PetriNet a, PetriNet b) {
        Place helper = new Place();
        b.finisehd().addTokenOnFire(helper);
        a.activateTask().addDependency(helper);
    }

    public static void connectSequenceOr(PetriNet a, PetriNet b) {
        Place or = new Place();
        a.activateTask().addDependency(or);
        Place helper = new Place();
        b.finisehd().addTokenOnFire(helper);
    }

    public static void connectConditionalFalse(PetriNet a, PetriNet b) {
        Place helper = new Place();
        a.activateTask().addDependency(helper);
        b.wrong().addTokenOnFire(helper);
    }

    public static void connectConditionalCorrect(PetriNet a, PetriNet b) {
        Place helper = new Place();
        a.activateTask().addDependency(helper);
        b.correct().addTokenOnFire(helper);
    }
}
