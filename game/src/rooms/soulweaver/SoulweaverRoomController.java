package rooms.soulweaver;

import engine.Game;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import rooms.soulweaver.modules.loops.LoopPuzzle;
import rooms.soulweaver.modules.variables.GolemProperty;
import rooms.soulweaver.modules.variables.MagicalEssence;
import rooms.soulweaver.modules.variables.SoulVessel;
import rooms.soulweaver.modules.variables.VariablePuzzle;
import rooms.soulweaver.state.SoulweaverPhase;
import rooms.soulweaver.state.VariablePuzzleStage;

/**
 * Progress for one server-owned room. Clients receive world changes and dialogs, not this state.
 */
public final class SoulweaverRoomController {
  private SoulweaverPhase phase = SoulweaverPhase.VARIABLES;
  private VariablePuzzleStage variableStage = VariablePuzzleStage.VESSELS;
  private int completedLoops;
  private final Set<String> collectedRunes = new HashSet<>();

  /**
   * @return the current room phase
   */
  public SoulweaverPhase phase() {
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
    phase = SoulweaverPhase.LOOPS;
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
    return (phase == SoulweaverPhase.VARIABLES || phase == SoulweaverPhase.LOOPS)
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
    if (phase != SoulweaverPhase.LOOPS
        || !LoopPuzzle.challenges().get(completedLoops).equals(challengeId)) return false;
    completedLoops++;
    if (completedLoops == LoopPuzzle.challenges().size()) phase = SoulweaverPhase.METHODS;
    return true;
  }

  /** Advances only after the authoritative workshop has completed its physical program. */
  public void completeMethods() {
    requireAuthority();
    if (phase == SoulweaverPhase.METHODS) phase = SoulweaverPhase.DECISIONS;
  }

  /** Records arrival at the Herzfeuer after six executed branches. */
  public void completeDecisions() {
    requireAuthority();
    if (phase == SoulweaverPhase.DECISIONS) phase = SoulweaverPhase.COMPLETE;
  }

  /**
   * Restores progression at the beginning of a saved act, without replaying submissions.
   *
   * @param checkpoint Act II, III or IV
   */
  public void restore(SoulweaverPhase checkpoint) {
    requireAuthority();
    phase = checkpoint;
    variableStage = VariablePuzzleStage.COMPLETE;
    completedLoops = checkpoint == SoulweaverPhase.LOOPS ? 0 : LoopPuzzle.challenges().size();
  }

  private boolean variableStageActive(VariablePuzzleStage expected) {
    return phase == SoulweaverPhase.VARIABLES && variableStage == expected;
  }

  private static void requireAuthority() {
    if (Game.isMultiplayerClient())
      throw new IllegalStateException("only the authoritative room may change Soulweaver state");
  }
}
