package rooms.programming;

import engine.Game;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import rooms.programming.modules.loops.LoopPuzzle;
import rooms.programming.modules.variables.GolemProperty;
import rooms.programming.modules.variables.MagicalEssence;
import rooms.programming.modules.variables.SoulVessel;
import rooms.programming.modules.variables.VariablePuzzle;
import rooms.programming.state.ProgrammingPhase;
import rooms.programming.state.VariablePuzzleStage;

/**
 * Progress for one server-owned room. Clients receive world changes and dialogs, not this state.
 */
public final class ProgrammingRoomController {
  private ProgrammingPhase phase = ProgrammingPhase.VARIABLES;
  private VariablePuzzleStage variableStage = VariablePuzzleStage.VESSELS;
  private int completedLoops;
  private final Set<String> collectedRunes = new HashSet<>();

  /**
   * @return the current room phase
   */
  public ProgrammingPhase phase() {
    return phase;
  }

  /**
   * @return the current assignment stage
   */
  public VariablePuzzleStage variableStage() {
    return variableStage;
  }

  /**
   * @return the number of completed stations in progression order
   */
  public int completedLoops() {
    return completedLoops;
  }

  /**
   * @return an immutable copy of collected rune IDs
   */
  public Set<String> collectedLoopRunes() {
    return Set.copyOf(collectedRunes);
  }

  /**
   * Records the complete vessel binding before the final value assignment can be revealed.
   *
   * @param vessels the submitted assignment
   * @return whether it was accepted, incorrect, or submitted outside the vessel stage
   */
  public PuzzleSubmissionResult submitVessels(Map<GolemProperty, SoulVessel> vessels) {
    requireAuthority();
    if (!variableStageActive(VariablePuzzleStage.VESSELS)) return PuzzleSubmissionResult.INACTIVE;
    if (!VariablePuzzle.vesselsCorrect(vessels)) return PuzzleSubmissionResult.INCORRECT;
    variableStage = VariablePuzzleStage.ESSENCES;
    return PuzzleSubmissionResult.ACCEPTED;
  }

  /**
   * Validates the essence assignment and unlocks the data-type reveal.
   *
   * @param essences the submitted assignment
   * @return whether it was accepted, incorrect, or submitted outside the essence stage
   */
  public PuzzleSubmissionResult submitEssences(Map<GolemProperty, MagicalEssence> essences) {
    requireAuthority();
    if (!variableStageActive(VariablePuzzleStage.ESSENCES)) return PuzzleSubmissionResult.INACTIVE;
    if (!VariablePuzzle.essencesCorrect(essences)) return PuzzleSubmissionResult.INCORRECT;
    variableStage = VariablePuzzleStage.REVEAL;
    return PuzzleSubmissionResult.ACCEPTED;
  }

  /**
   * Completes the data-type reveal and activates the golem.
   *
   * @return accepted if the reveal was active, otherwise inactive
   */
  public PuzzleSubmissionResult activateGolem() {
    requireAuthority();
    if (!variableStageActive(VariablePuzzleStage.REVEAL)) return PuzzleSubmissionResult.INACTIVE;
    variableStage = VariablePuzzleStage.COMPLETE;
    phase = ProgrammingPhase.LOOPS;
    return PuzzleSubmissionResult.ACCEPTED;
  }

  /**
   * Collects a canonical rune once while either playable act is active.
   *
   * @param runeId the rune to collect
   * @return whether collection was accepted, invalid or duplicate, or outside a playable act
   */
  public PuzzleSubmissionResult collectLoopRune(String runeId) {
    requireAuthority();
    if (phase != ProgrammingPhase.VARIABLES && phase != ProgrammingPhase.LOOPS)
      return PuzzleSubmissionResult.INACTIVE;
    if (LoopPuzzle.rune(runeId).isEmpty() || !collectedRunes.add(runeId))
      return PuzzleSubmissionResult.INCORRECT;
    return PuzzleSubmissionResult.ACCEPTED;
  }

  /**
   * Records a physically completed situation, not a preselected loop type.
   *
   * @param challengeId the completed station
   * @return accepted for the current station, incorrect for another, or inactive outside act two
   */
  public PuzzleSubmissionResult completeExecutedLoop(String challengeId) {
    requireAuthority();
    if (phase != ProgrammingPhase.LOOPS) return PuzzleSubmissionResult.INACTIVE;
    if (!LoopPuzzle.challenges().get(completedLoops).equals(challengeId))
      return PuzzleSubmissionResult.INCORRECT;
    completedLoops++;
    if (completedLoops == LoopPuzzle.challenges().size()) phase = ProgrammingPhase.METHODS;
    return PuzzleSubmissionResult.ACCEPTED;
  }

  private boolean variableStageActive(VariablePuzzleStage expected) {
    return phase == ProgrammingPhase.VARIABLES && variableStage == expected;
  }

  private static void requireAuthority() {
    if (Game.isMultiplayerClient())
      throw new IllegalStateException("only the authoritative room may change Programming state");
  }
}
