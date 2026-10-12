package engine.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import engine.game.ServerProcess;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class TrackingConsentFeatureTest {
  private static final String ROOM_ID = "test-room";
  private static final String OPERATOR_EMAIL = "operator@example.com";
  private static final UUID RUN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @AfterEach
  void clearProcessConsent() {
    System.clearProperty(ServerProcess.TRACKING_CONSENT_PROPERTY);
    TrackingRuntime.localTrackingConsent(null);
  }

  @Test
  void clientDoesNotRestoreSavedDecisionWithoutManagedServerArguments() {
    TrackingConsentFeature feature = newFeature();

    try (MockedStatic<Tracking> tracking = mockStatic(Tracking.class)) {
      feature.initialize(new String[0], () -> failIfSavedDecisionIsRead());

      assertNull(feature.decision());
      assertTrue(TrackingRuntime.localTrackingConsent().isEmpty());
      assertFalse(System.getProperties().containsKey(ServerProcess.TRACKING_CONSENT_PROPERTY));
      tracking.verify(
          () -> Tracking.configureRoom(ROOM_ID, OPERATOR_EMAIL, Optional.of(RUN_ID), false));
    }
  }

  @Test
  void managedServerRestoresSavedDecision() {
    TrackingConsentFeature feature = newFeature();

    try (MockedStatic<Tracking> tracking = mockStatic(Tracking.class)) {
      feature.initialize(new String[] {ServerProcess.SERVER_ARGUMENT}, () -> true);

      assertEquals(Boolean.TRUE, feature.decision());
      assertEquals(Optional.of(true), TrackingRuntime.localTrackingConsent());
      assertEquals("true", System.getProperty(ServerProcess.TRACKING_CONSENT_PROPERTY));
      tracking.verify(
          () -> Tracking.configureRoom(ROOM_ID, OPERATOR_EMAIL, Optional.of(RUN_ID), true));
    }
  }

  @Test
  void explicitManagedServerDecisionTakesPriorityOverSavedDecision() {
    System.setProperty(ServerProcess.TRACKING_CONSENT_PROPERTY, "false");
    TrackingConsentFeature feature = newFeature();

    try (MockedStatic<Tracking> tracking = mockStatic(Tracking.class)) {
      feature.initialize(
          new String[] {ServerProcess.SERVER_ARGUMENT}, () -> failIfSavedDecisionIsRead());

      assertEquals(Boolean.FALSE, feature.decision());
      assertEquals(Optional.of(false), TrackingRuntime.localTrackingConsent());
      assertEquals("false", System.getProperty(ServerProcess.TRACKING_CONSENT_PROPERTY));
      tracking.verify(
          () -> Tracking.configureRoom(ROOM_ID, OPERATOR_EMAIL, Optional.of(RUN_ID), false));
    }
  }

  private static TrackingConsentFeature newFeature() {
    return new TrackingConsentFeature(ROOM_ID, OPERATOR_EMAIL, () -> RUN_ID, ignored -> {});
  }

  private static Boolean failIfSavedDecisionIsRead() {
    throw new AssertionError("The saved decision must be read only for an unconfigured server.");
  }
}
