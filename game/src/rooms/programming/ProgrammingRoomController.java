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
   * @return whether it was correct and submitted during the vessel stage
   */
  public boolean submitVessels(Map<GolemProperty, SoulVessel> vessels) {
    requireAuthority();
    if (!variableStageActive(VariablePuzzleStage.VESSELS)
        || !VariablePuzzle.vesselsCorrect(vessels)) return false;
    variableStage = VariablePuzzleStage.ESSENCES;
    return true;
  }

  /**
   * Validates the essence assignment and unlocks the data-type reveal.
   *
   * @param essences the submitted assignment
   * @return whether it was correct and submitted during the essence stage
   */
  public boolean submitEssences(Map<GolemProperty, MagicalEssence> essences) {
    requireAuthority();
    if (!variableStageActive(VariablePuzzleStage.ESSENCES)
        || !VariablePuzzle.essencesCorrect(essences)) return false;
    variableStage = VariablePuzzleStage.REVEAL;
    return true;
  }

  /**
   * Completes the data-type reveal and activates the golem.
   *
   * @return whether the reveal was active
   */
  public boolean activateGolem() {
    requireAuthority();
    if (!variableStageActive(VariablePuzzleStage.REVEAL)) return false;
    variableStage = VariablePuzzleStage.COMPLETE;
    phase = ProgrammingPhase.LOOPS;
    return true;
  }

  /**
   * Collects a canonical rune once while either playable act is active.
   *
   * @param runeId the rune to collect
   * @return whether a known, not yet collected rune was collected during a playable act
   */
  public boolean collectLoopRune(String runeId) {
    requireAuthority();
    return (phase == ProgrammingPhase.VARIABLES || phase == ProgrammingPhase.LOOPS)
        && LoopPuzzle.rune(runeId).isPresent()
        && collectedRunes.add(runeId);
  }

  /**
   * Records a physically completed situation, not a preselected loop type.
   *
   * @param challengeId the completed station
   * @return whether it was the current station during act two
   */
  public boolean completeExecutedLoop(String challengeId) {
    requireAuthority();
    if (phase != ProgrammingPhase.LOOPS
        || !LoopPuzzle.challenges().get(completedLoops).equals(challengeId)) return false;
    completedLoops++;
    if (completedLoops == LoopPuzzle.challenges().size()) phase = ProgrammingPhase.METHODS;
    return true;
  }

  /** Advances only after the authoritative workshop has completed its physical program. */
  public void completeMethods() {
    requireAuthority();
    if (phase == ProgrammingPhase.METHODS) phase = ProgrammingPhase.DECISIONS;
  }

  /** Records arrival at the Herzfeuer after six executed branches. */
  public void completeDecisions() {
    requireAuthority();
    if (phase == ProgrammingPhase.DECISIONS) phase = ProgrammingPhase.COMPLETE;
  }

  /**
   * Restores progression at the beginning of a saved act, without replaying submissions.
   *
   * @param checkpoint Act II, III or IV
   */
  public void restore(ProgrammingPhase checkpoint) {
    requireAuthority();
    phase = checkpoint;
    variableStage = VariablePuzzleStage.COMPLETE;
    completedLoops = checkpoint == ProgrammingPhase.LOOPS ? 0 : LoopPuzzle.challenges().size();
  }

  private boolean variableStageActive(VariablePuzzleStage expected) {
    return phase == ProgrammingPhase.VARIABLES && variableStage == expected;
  }

  private static void requireAuthority() {
    if (Game.isMultiplayerClient())
      throw new IllegalStateException("only the authoritative room may change Programming state");
  }
}
