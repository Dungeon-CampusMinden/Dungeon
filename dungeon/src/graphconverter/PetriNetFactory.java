package graphconverter;

import petriNet.PetriNet;

import task.Task;

import taskdependencygraph.TaskEdge;

public class PetriNetFactory {

    public static PetriNet defaultNet(Task task) {
        return null;
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
                // Handle unsupported type
                break;
        }
    }

    public static void connectSubtaskMandatory(PetriNet a, PetriNet b) {
        // Implementation for subtask_mandatory
    }

    public static void connectSubtaskOptional(PetriNet a, PetriNet b) {
        // Implementation for subtask_optional
    }

    public static void connectSequence(PetriNet a, PetriNet b) {
        // Implementation for sequence
    }

    public static void connectSequenceAnd(PetriNet a, PetriNet b) {
        // Implementation for sequence_and
    }

    public static void connectSequenceOr(PetriNet a, PetriNet b) {
        // Implementation for sequence_or
    }

    public static void connectConditionalFalse(PetriNet a, PetriNet b) {
        // Implementation for conditional_false
    }

    public static void connectConditionalCorrect(PetriNet a, PetriNet b) {
        // Implementation for conditional_correct
    }
}
