package petriNet;

public record PetriNet(
        Place taskNotActivated,
        Transition activateTask,
        Place taskActivated,
        Transition afterActivated,
        Transition activateprocessing,
        Place processingActivated,
        Place finishedFalse,
        Place finishedCorrect,
        Transition correct,
        Transition wrong,
        Place end_correct,
        Place end_false,
        Place end,
        Transition finisehd) {}
