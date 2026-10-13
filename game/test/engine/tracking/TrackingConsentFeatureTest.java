package engine.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import engine.Game;
import engine.game.GameStarter;
import engine.game.ServerProcess;
import engine.language.Language;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class TrackingConsentFeatureTest {
  private static final String ROOM_ID = "test-room";
  private static final String OPERATOR_EMAIL = "operator@example.com";
  private static final UUID RUN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private final List<Boolean> persistedDecisions = new ArrayList<>();

  @BeforeEach
  void registerConsentTexts() {
    Game.localization().registerTranslationFile(Language.DE, "language/escapeRoom/de.json");
    Game.localization().registerTranslationFile(Language.EN, "language/escapeRoom/en.json");
    Game.localization().currentLanguage(Language.DE);
  }

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

  @Test
  void settingsButtonsApplyAndPersistTheChosenDecision() {
    TrackingConsentFeature feature = newFeature();

    try (MockedStatic<Tracking> tracking = mockStatic(Tracking.class)) {
      feature.initialize(new String[0], () -> null);

      feature.settings().decision().accept(true);

      assertEquals(Boolean.TRUE, feature.decision());
      assertEquals(Optional.of(true), TrackingRuntime.localTrackingConsent());
      assertEquals("true", System.getProperty(ServerProcess.TRACKING_CONSENT_PROPERTY));
      assertEquals(List.of(true), persistedDecisions);
      tracking.verify(
          () -> Tracking.configureRoom(ROOM_ID, OPERATOR_EMAIL, Optional.of(RUN_ID), true));
      assertEquals("Status: Tracking aktiviert", feature.settings().status());

      feature.settings().decision().accept(false);

      assertEquals(Boolean.FALSE, feature.decision());
      assertEquals(Optional.of(false), TrackingRuntime.localTrackingConsent());
      assertEquals("false", System.getProperty(ServerProcess.TRACKING_CONSENT_PROPERTY));
      assertEquals(List.of(true, false), persistedDecisions);
      tracking.verify(
          () -> Tracking.configureRoom(ROOM_ID, OPERATOR_EMAIL, Optional.of(RUN_ID), false),
          times(2));
      assertEquals("Status: Tracking deaktiviert", feature.settings().status());
      tracking.verify(Tracking::deleteLocalData, never());
    }
  }

  @Test
  void deleteButtonWithdrawsConsentAndDeletesLocalData() {
    TrackingConsentFeature feature = newFeature();

    try (MockedStatic<Tracking> tracking = mockStatic(Tracking.class)) {
      tracking.when(Tracking::deleteLocalData).thenReturn(true);
      feature.initialize(new String[0], () -> null);
      feature.settings().decision().accept(true);

      assertTrue(feature.settings().deleteLocalData().getAsBoolean());

      assertEquals(Boolean.FALSE, feature.decision());
      assertEquals(Optional.of(false), TrackingRuntime.localTrackingConsent());
      assertEquals("false", System.getProperty(ServerProcess.TRACKING_CONSENT_PROPERTY));
      assertEquals(List.of(true, false), persistedDecisions);
      tracking.verify(Tracking::deleteLocalData);
    }
  }

  @Test
  void settingsProvideDistinctFeedbackForEveryTrackingChange() {
    TrackingConsentFeature feature = newFeature();

    try (MockedStatic<Tracking> tracking = mockStatic(Tracking.class)) {
      feature.initialize(new String[0], () -> null);
      for (boolean remote : new boolean[] {false, true}) {
        tracking.when(Tracking::remoteStorageEnabled).thenReturn(remote);
        for (Language language : List.of(Language.DE, Language.EN)) {
          Game.localization().currentLanguage(language);
          GameStarter.TrackingSettings settings = feature.settings();

          assertTranslated(settings.enabledMessage());
          assertTranslated(settings.disabledMessage());
          assertTranslated(settings.deletedMessage());
          assertTranslated(settings.deleteFailedMessage());
          assertNotEquals(settings.enabledMessage(), settings.disabledMessage());
        }
      }
    }
  }

  private TrackingConsentFeature newFeature() {
    return new TrackingConsentFeature(
        ROOM_ID, OPERATOR_EMAIL, () -> RUN_ID, persistedDecisions::add);
  }

  private static void assertTranslated(String text) {
    assertFalse(text.startsWith("{trackingConsent."), "Missing translation: " + text);
  }

  private static Boolean failIfSavedDecisionIsRead() {
    throw new AssertionError("The saved decision must be read only for an unconfigured server.");
  }
}
