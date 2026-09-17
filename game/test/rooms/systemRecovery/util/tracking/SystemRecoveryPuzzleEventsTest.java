package rooms.systemRecovery.util.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.Game;
import engine.tracking.Tracking;
import feature.hints.HintSystem;
import feature.petrinet.PetriNetSystem;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.riddles.support.RiddleCallbacks;
import rooms.systemRecovery.util.interpreter.TerminalStep;

/** Regression tests for the typed links between authoritative callbacks and learning steps. */
class SystemRecoveryPuzzleEventsTest {

  @BeforeEach
  void setUp() {
    SystemRecoveryProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.add(new PetriNetSystem());
    Game.add(new HintSystem());
    SystemRecoveryProgressNet.initialize();
  }

  @AfterEach
  void tearDown() {
    SystemRecoveryProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
  }

  @Test
  void onlyAcceptedTerminalCallbacksCompleteTheirMappedStep() {
    SystemRecoveryPuzzleEvents.terminalAttempt(
        TerminalStep.ENERGY_ARRAY.stateId(), "wrong source", false, -1);
    assertActive(SystemRecoveryLearningStep.ENERGY_ARRAY);

    SystemRecoveryPuzzleEvents.terminalAttempt(
        TerminalStep.ENERGY_ARRAY.stateId(), "int[] energie = new int[5];", true, -1);
    assertActive(SystemRecoveryLearningStep.ENERGY_VALUES);
  }

  @Test
  void ordinaryPhysicalCallbacksAndSolvedTrackingDoNotImplyProgress() {
    RiddleCallbacks callbacks =
        SystemRecoveryPuzzleEvents.forPuzzle(
            SystemRecoveryPuzzle.MANUAL_SORTING, "sort-display", "choice");

    callbacks.success("swap", -1);
    callbacks.solved();

    assertActive(SystemRecoveryLearningStep.ENERGY_ARRAY);
  }

  @Test
  void specificallyMappedRiddleCompletionAdvancesOnlyItsExpectedStep() {
    activate(SystemRecoveryLearningStep.MANUAL_SORTING);
    RiddleCallbacks callbacks =
        SystemRecoveryPuzzleEvents.forPuzzleCompleting(
            SystemRecoveryPuzzle.MANUAL_SORTING,
            "sort-display",
            "choice",
            SystemRecoveryLearningStep.MANUAL_SORTING);

    callbacks.success("swap", -1);
    assertActive(SystemRecoveryLearningStep.MANUAL_SORTING);
    callbacks.solved();

    assertActive(SystemRecoveryLearningStep.BUBBLE_SORT_CONDITION);
  }

  @Test
  void everyTerminalStateHasOneStableLearningStepMapping() {
    assertEquals(TerminalStep.values().length, mappedTerminalCount());
    for (TerminalStep terminalStep : TerminalStep.values()) {
      assertTrue(SystemRecoveryLearningStep.fromTerminalStep(terminalStep).isPresent());
    }
    assertFalse(SystemRecoveryLearningStep.COMPLETE.terminalStep().isPresent());
  }

  @Test
  void acceptedHintIdsAreTrackedPerParticipantAndPuzzle() {
    Entity host = new Entity("host");
    Entity joiner = new Entity("joiner");
    UUID hostParticipant = UUID.randomUUID();
    UUID joinerParticipant = UUID.randomUUID();

    try (MockedStatic<Tracking> tracking = Mockito.mockStatic(Tracking.class)) {
      tracking
          .when(() -> Tracking.participantForEntity(host.id()))
          .thenReturn(Optional.of(hostParticipant));
      tracking
          .when(() -> Tracking.participantForEntity(joiner.id()))
          .thenReturn(Optional.of(joinerParticipant));

      String[] stages = {"orientation", "approach", "near", "solution"};
      for (String stage : stages) {
        String hintId = "energy-array:" + stage;
        SystemRecoveryPuzzleEvents.hintUsed(SystemRecoveryPuzzle.ENERGY, hintId, host);
        tracking.verify(() -> Tracking.hintUsed("energy-array", hintId, hostParticipant));
      }
      SystemRecoveryPuzzleEvents.hintUsed(
          SystemRecoveryPuzzle.ENERGY, "energy-array:orientation", joiner);

      tracking.verify(
          () -> Tracking.hintUsed("energy-array", "energy-array:orientation", joinerParticipant));
      tracking.verify(() -> Tracking.participantForEntity(host.id()), Mockito.times(stages.length));
      tracking.verify(() -> Tracking.participantForEntity(joiner.id()));
    }
  }

  private long mappedTerminalCount() {
    return java.util.Arrays.stream(SystemRecoveryLearningStep.values())
        .filter(step -> step.terminalStep().isPresent())
        .count();
  }

  private void activate(SystemRecoveryLearningStep target) {
    SystemRecoveryLearningStep current = SystemRecoveryProgressNet.activeStep().orElseThrow();
    while (current != target) {
      assertTrue(SystemRecoveryProgressNet.complete(current));
      current = SystemRecoveryProgressNet.activeStep().orElseThrow();
    }
  }

  private void assertActive(SystemRecoveryLearningStep expected) {
    assertEquals(expected, SystemRecoveryProgressNet.activeStep().orElseThrow());
  }
}
