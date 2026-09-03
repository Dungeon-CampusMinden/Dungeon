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
import rooms.programming.state.VariablePuzzleStage;

/** Server-owned puzzle validation and phase progression. */
public final class ProgrammingRoomController {

  private ProgrammingRoomController() {}

  /** Validates the vessel assignment and unlocks the essence stage. */
  public static synchronized PuzzleSubmissionResult submitVessels(
      Map<GolemProperty, SoulVessel> vessels) {
    if (!variableStageActive(VariablePuzzleStage.VESSELS)) {
      return PuzzleSubmissionResult.INACTIVE;
    }
    if (!VariablePuzzle.vesselsCorrect(vessels)) {
      return PuzzleSubmissionResult.INCORRECT;
    }
    ProgrammingStateStore.completeVessels();
    return PuzzleSubmissionResult.ACCEPTED;
  }

  /** Validates the essence assignment and unlocks the data-type reveal. */
  public static synchronized PuzzleSubmissionResult submitEssences(
      Map<GolemProperty, MagicalEssence> essences) {
    if (!variableStageActive(VariablePuzzleStage.ESSENCES)) {
      return PuzzleSubmissionResult.INACTIVE;
    }

    Optional<GolemState> golem = VariablePuzzle.solveEssences(essences);
    if (golem.isEmpty()) {
      return PuzzleSubmissionResult.INCORRECT;
    }
    ProgrammingStateStore.completeEssences(golem.orElseThrow());
    return PuzzleSubmissionResult.ACCEPTED;
  }

  /** Completes the data-type reveal and activates the golem. */
  public static synchronized PuzzleSubmissionResult activateGolem() {
    if (!variableStageActive(VariablePuzzleStage.REVEAL)) {
      return PuzzleSubmissionResult.INACTIVE;
    }
    ProgrammingStateStore.activateGolem();
    return PuzzleSubmissionResult.ACCEPTED;
  }

  /** Validates one loop choice and records correct situations immediately. */
  public static synchronized PuzzleSubmissionResult submitLoopAnswer(
      String challengeId, LoopType answer) {
    if (!phaseActive(ProgrammingPhase.LOOPS)) {
      return PuzzleSubmissionResult.INACTIVE;
    }
    if (!LoopPuzzle.answerCorrect(challengeId, answer)) {
      return PuzzleSubmissionResult.INCORRECT;
    }

    ProgrammingStateComponent state = ProgrammingStateStore.completeLoopChallenge(challengeId);
    if (LoopPuzzle.allCompleted(state.completedLoopChallenges())) {
      ProgrammingStateStore.advance();
    }
    return PuzzleSubmissionResult.ACCEPTED;
  }

  private static boolean phaseActive(ProgrammingPhase expected) {
    return ProgrammingStateStore.current()
        .map(ProgrammingStateComponent::phase)
        .filter(expected::equals)
        .isPresent();
  }

  private static boolean variableStageActive(VariablePuzzleStage expected) {
    return ProgrammingStateStore.current()
        .filter(state -> state.phase() == ProgrammingPhase.VARIABLES)
        .map(ProgrammingStateComponent::variableStage)
        .filter(expected::equals)
        .isPresent();
  }
}
