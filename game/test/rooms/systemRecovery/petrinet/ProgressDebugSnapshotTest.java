package rooms.systemRecovery.petrinet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Contract tests for the explicit, localized Petri progress inspection. */
class ProgressDebugSnapshotTest {

  @Test
  void snapshotExposesActiveStepHintsTokenCountsAndTransitionWeights() {
    ProgressDebugSnapshot snapshot =
        ProgressDebugSnapshot.fromMarking(
            Map.of(SystemRecoveryLearningStep.MANUAL_SORTING, 1),
            8,
            2,
            4,
            SystemRecoveryLearningStep.TRANSPORT_COLLECT_LOOP.hintKey(),
            SystemRecoveryLearningStep.MODULE_ARRAY.hintKey(),
            "wrong-order");

    assertTrue(snapshot.consistent());
    assertEquals(SystemRecoveryLearningStep.MANUAL_SORTING.hintKey(), snapshot.activeStepKey());
    assertEquals(1, snapshot.totalTokens());
    assertEquals(2, snapshot.hintIndex());
    assertEquals(4, snapshot.hintCount());
    assertEquals(
        1, snapshot.tokenCounts().get(SystemRecoveryLearningStep.MANUAL_SORTING.hintKey()));
    assertEquals(0, snapshot.tokenCounts().get(SystemRecoveryLearningStep.ENERGY_ARRAY.hintKey()));
    assertEquals(
        SystemRecoveryLearningStep.TRANSPORT_COLLECT_LOOP.hintKey(),
        snapshot.lastAcceptedStepKey());
    assertEquals(SystemRecoveryLearningStep.MODULE_ARRAY.hintKey(), snapshot.lastRejectedStepKey());
    assertEquals("wrong-order", snapshot.rejectionReasonKey());
    assertEquals("completed", snapshot.steps().get(0).statusKey());
    assertEquals("active", snapshot.steps().get(13).statusKey());
    assertEquals("locked", snapshot.steps().get(14).statusKey());
    assertEquals(2, snapshot.inputWeight());
    assertEquals(1, snapshot.outputWeight());
  }

  @Test
  void multipleMarkedPlacesAreShownAsAnInvariantError() {
    ProgressDebugSnapshot snapshot =
        ProgressDebugSnapshot.fromMarking(
            Map.of(
                SystemRecoveryLearningStep.ENERGY_ARRAY, 1,
                SystemRecoveryLearningStep.MODULE_ARRAY, 1),
            0,
            0,
            4,
            "",
            "",
            "");

    assertFalse(snapshot.consistent());
    assertEquals("", snapshot.activeStepKey());
    assertEquals("invalid", snapshot.steps().get(0).statusKey());
    assertEquals("invalid", snapshot.steps().get(3).statusKey());
  }

  @Test
  void snapshotAndRenderedPayloadAreImmutableAndStayBelowEightKiB() {
    ProgressDebugSnapshot snapshot =
        ProgressDebugSnapshot.fromMarking(
            Map.of(SystemRecoveryLearningStep.COMPLETE, 1),
            16,
            0,
            0,
            SystemRecoveryLearningStep.CORE_SEARCH.hintKey(),
            "unknown-step",
            "invalid-request");

    assertThrows(UnsupportedOperationException.class, () -> snapshot.tokenCounts().clear());
    assertThrows(UnsupportedOperationException.class, () -> snapshot.steps().clear());
    assertTrue(
        snapshot.dialogPayload().getBytes(StandardCharsets.UTF_8).length < 8 * 1024,
        "explicit debug response must remain below the 8 KiB budget");
  }
}
