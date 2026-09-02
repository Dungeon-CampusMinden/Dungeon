package rooms.programming;

import java.util.Map;
import java.util.Optional;
import rooms.programming.modules.loops.LoopPuzzle;
import rooms.programming.modules.loops.LoopType;
import rooms.programming.modules.variables.GolemProperty;
import rooms.programming.modules.variables.MagicalEssence;
import rooms.programming.modules.variables.SoulVessel;
import rooms.programming.modules.variables.VariablePuzzle;
import rooms.programming.state.GolemState;
import rooms.programming.state.ProgrammingPhase;
import rooms.programming.state.ProgrammingStateComponent;
import rooms.programming.state.ProgrammingStateStore;

/** Server-owned puzzle validation and phase progression. */
public final class ProgrammingRoomController {

  private ProgrammingRoomController() {}

  /** Validates both variable-puzzle stages and stores the resulting golem values. */
  public static synchronized PuzzleSubmissionResult submitVariables(
      Map<GolemProperty, SoulVessel> vessels, Map<GolemProperty, MagicalEssence> essences) {
    if (!phaseActive(ProgrammingPhase.VARIABLES)) {
      return PuzzleSubmissionResult.INACTIVE;
    }

    Optional<GolemState> golem = VariablePuzzle.solve(vessels, essences);
    if (golem.isEmpty()) {
      return PuzzleSubmissionResult.INCORRECT;
    }
    ProgrammingStateStore.completeVariables(golem.orElseThrow());
    return PuzzleSubmissionResult.ACCEPTED;
  }

  /** Validates all loop situations and advances to the method phase. */
  public static synchronized PuzzleSubmissionResult submitLoops(Map<String, LoopType> answers) {
    if (!phaseActive(ProgrammingPhase.LOOPS)) {
      return PuzzleSubmissionResult.INACTIVE;
    }
    if (!LoopPuzzle.solved(answers)) {
      return PuzzleSubmissionResult.INCORRECT;
    }
    ProgrammingStateStore.advance();
    return PuzzleSubmissionResult.ACCEPTED;
  }

  private static boolean phaseActive(ProgrammingPhase expected) {
    return ProgrammingStateStore.current()
        .map(ProgrammingStateComponent::phase)
        .filter(expected::equals)
        .isPresent();
  }
}
