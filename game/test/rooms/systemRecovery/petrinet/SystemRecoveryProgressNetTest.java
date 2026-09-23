package rooms.systemRecovery.petrinet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Game;
import engine.tracking.Tracking;
import feature.hints.HintSystem;
import feature.petrinet.PetriNetSystem;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/** Tests the linear, token-backed progress model without depending on world positions. */
class SystemRecoveryProgressNetTest {

  private PetriNetSystem petriNet;

  @BeforeEach
  void setUp() {
    SystemRecoveryProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();

    petriNet = new PetriNetSystem();
    Game.add(petriNet);
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
  void initializesOnlyEnergyArrayAndARegularTickCannotAdvanceIt() {
    assertActive(SystemRecoveryLearningStep.ENERGY_ARRAY);
    assertTokenInvariant();

    petriNet.execute();

    assertActive(SystemRecoveryLearningStep.ENERGY_ARRAY);
    assertTokenInvariant();
  }

  @Test
  void initialLearningPuzzleIsTrackedExactlyOnce() {
    SystemRecoveryProgressNet.reset();

    try (MockedStatic<Tracking> tracking = Mockito.mockStatic(Tracking.class)) {
      SystemRecoveryProgressNet.initialize();
      SystemRecoveryProgressNet.initialize();

      tracking.verify(() -> Tracking.puzzleStarted("energy-array"), Mockito.times(1));
    }
  }

  @Test
  void completionConsumesTwoTokensAndProducesOneTokenInTheDirectSuccessor() {
    assertTrue(SystemRecoveryProgressNet.complete(SystemRecoveryLearningStep.ENERGY_ARRAY));

    assertEquals(0, SystemRecoveryProgressNet.tokenCount(SystemRecoveryLearningStep.ENERGY_ARRAY));
    assertEquals(1, SystemRecoveryProgressNet.tokenCount(SystemRecoveryLearningStep.ENERGY_VALUES));
    assertActive(SystemRecoveryLearningStep.ENERGY_VALUES);
    assertTokenInvariant();
  }

  @Test
  void wrongOrderAndDuplicateCompletionLeaveEveryTokenUnchanged() {
    assertFalse(SystemRecoveryProgressNet.complete(SystemRecoveryLearningStep.MODULE_ARRAY));
    assertEquals(1, SystemRecoveryProgressNet.tokenCount(SystemRecoveryLearningStep.ENERGY_ARRAY));

    assertTrue(SystemRecoveryProgressNet.complete(SystemRecoveryLearningStep.ENERGY_ARRAY));
    assertFalse(SystemRecoveryProgressNet.complete(SystemRecoveryLearningStep.ENERGY_ARRAY));
    assertEquals(0, SystemRecoveryProgressNet.tokenCount(SystemRecoveryLearningStep.ENERGY_ARRAY));
    assertEquals(1, SystemRecoveryProgressNet.tokenCount(SystemRecoveryLearningStep.ENERGY_VALUES));
    assertTokenInvariant();
  }

  @Test
  void missingTransitionRollsBackTheAdditionalToken() {
    petriNet.clear();

    assertFalse(SystemRecoveryProgressNet.complete(SystemRecoveryLearningStep.ENERGY_ARRAY));

    assertActive(SystemRecoveryLearningStep.ENERGY_ARRAY);
    assertEquals(1, SystemRecoveryProgressNet.tokenCount(SystemRecoveryLearningStep.ENERGY_ARRAY));
    assertTokenInvariant();
  }

  @Test
  void fullLearningFlowCompletesEveryStepInOrderWithOneStableToken() {
    SystemRecoveryLearningStep[] steps = SystemRecoveryLearningStep.values();
    for (int i = 0; i < steps.length - 1; i++) {
      SystemRecoveryLearningStep current = steps[i];
      SystemRecoveryLearningStep next = steps[i + 1];

      assertActive(current);
      assertTrue(SystemRecoveryProgressNet.complete(current), "could not complete " + current);
      assertActive(next);
      assertTokenInvariant();
    }

    assertActive(SystemRecoveryLearningStep.COMPLETE);
    assertFalse(SystemRecoveryProgressNet.complete(SystemRecoveryLearningStep.COMPLETE));
    assertTokenInvariant();
  }

  @Test
  void activeStepIsDerivedFromPlaceTokensAndEmptyOrCorruptMarkingsAreNotGuessed() {
    assertEquals(
        SystemRecoveryLearningStep.ENERGY_ARRAY,
        SystemRecoveryProgressNet.activeStep().orElseThrow());
    assertEquals(1, SystemRecoveryProgressNet.tokenCount(SystemRecoveryLearningStep.ENERGY_ARRAY));

    SystemRecoveryProgressNet.reset();
    SystemRecoveryProgressNet.initialize();
    assertActive(SystemRecoveryLearningStep.ENERGY_ARRAY);
  }

  private void assertActive(SystemRecoveryLearningStep expected) {
    assertEquals(expected, SystemRecoveryProgressNet.activeStep().orElseThrow());
  }

  private void assertTokenInvariant() {
    long tokens =
        Arrays.stream(SystemRecoveryLearningStep.values())
            .mapToLong(SystemRecoveryProgressNet::tokenCount)
            .sum();
    assertEquals(1, tokens, "the stable marking must contain exactly one token");
    assertTrue(SystemRecoveryProgressNet.activeStep().isPresent());
  }
}
