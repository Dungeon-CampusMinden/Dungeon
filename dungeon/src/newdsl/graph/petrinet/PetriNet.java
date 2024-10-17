package newdsl.graph.petrinet;

import newdsl.tasks.Task;

public record PetriNet(
    Place taskNotActivated,
    Transition activateTask,
    Place taskActivated,
    Transition afterActivated,
    Transition activateProcessing,
    Place processingActivated,
    Place finishedFalse,
    Place finishedCorrect,
    Transition correct,
    Transition wrong,
    Place end_correct,
    Place end_wrong,
    Place end,
    Transition finished,
    Place or,
    Task task) {}
