package graph.petrinet;

import task.Task;

/**
 * Basic Petri Net for a {@link Task}
 *
 * <p>Use the {@link PetriNetFactory} to create a PetriNet and connect it with other. See <a
 * href=https://github.com/Dungeon-CampusMinden/Dungeon/tree/master/doc/control_mechanisms/petri_net_parsing.md>
 * documentation</a> for more.
 *
 * @param taskNotActivated
 * @param activateTask
 * @param taskActivated
 * @param afterActivated
 * @param activateProcessing
 * @param processingActivated
 * @param finishedFalse
 * @param finishedCorrect
 * @param correct
 * @param wrong
 * @param end_correct
 * @param end_wrong
 * @param end
 * @param finished
 * @param or
 * @param task
 */
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
