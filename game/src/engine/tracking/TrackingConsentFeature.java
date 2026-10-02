package engine.tracking;

import engine.game.GameStarter;
import engine.game.ServerProcess;
import engine.language.Translation;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Shared startup, settings, and run-state flow for rooms that request tracking consent. */
public final class TrackingConsentFeature {
  private static final String TRANSLATION_KEY = "trackingConsent";

  private final String roomId;
  private final String operatorEmail;
  private final Supplier<UUID> runId;
  private final Consumer<Boolean> persistDecision;
  private Boolean decision;

  /**
   * Creates the shared consent flow for one tracked room.
   *
   * @param roomId identifier used to scope tracking events
   * @param operatorEmail recipient of submitted tracking reports
   * @param runId supplier for the current run identifier
   * @param persistDecision callback that stores a consent decision
   */
  public TrackingConsentFeature(
      String roomId,
      String operatorEmail,
      Supplier<UUID> runId,
      Consumer<Boolean> persistDecision) {
    this.roomId = java.util.Objects.requireNonNull(roomId, "roomId");
    this.operatorEmail = java.util.Objects.requireNonNull(operatorEmail, "operatorEmail");
    this.runId = java.util.Objects.requireNonNull(runId, "runId");
    this.persistDecision = java.util.Objects.requireNonNull(persistDecision, "persistDecision");
  }

  /**
   * Resolves this process's decision, configures tracking, and publishes it to a managed server.
   *
   * @param arguments process startup arguments
   * @param savedDecision supplier for a previously saved decision
   */
  public void initialize(String[] arguments, Supplier<Boolean> savedDecision) {
    java.util.Objects.requireNonNull(savedDecision, "savedDecision");
    decision = resolveDecision(arguments, savedDecision);
    TrackingRuntime.localTrackingConsent(decision);
    publishDecision();
    configureRoom();
  }

  /**
   * Returns the current process's consent decision, or {@code null} while undecided.
   *
   * @return current consent decision, or {@code null} if undecided
   */
  public Boolean decision() {
    return decision;
  }

  /**
   * Builds the mandatory main-menu prompt when no decision is known.
   *
   * @return startup prompt, or {@code null} when the decision is already known
   */
  public GameStarter.StartupConsent startupPrompt() {
    if (decision != null) return null;
    Translation translation = new Translation(TRANSLATION_KEY);
    String variant = storageVariant();
    return new GameStarter.StartupConsent(
        translation.text(variant + ".title"),
        translation.text(variant + ".summary"),
        translation.text(variant + ".message"),
        translation.text(variant + ".accept"),
        translation.text(variant + ".decline"),
        this::choose);
  }

  /**
   * Builds the main-menu tracking controls for reviewing consent and deleting local data.
   *
   * @return tracking settings for this room
   */
  public GameStarter.TrackingSettings settings() {
    Translation translation = new Translation(TRANSLATION_KEY);
    String variant = storageVariant();
    String status =
        decision == null
            ? translation.text("status.undecided")
            : translation.text(decision ? "status.enabled" : "status.disabled");
    return new GameStarter.TrackingSettings(
        translation.text(variant + ".settingsTitle"),
        translation.text(variant + ".settingsSummary"),
        translation.text(variant + ".message"),
        status,
        translation.text(variant + ".enable"),
        translation.text(variant + ".disable"),
        translation.text(variant + ".delete"),
        translation.text(variant + ".deleteTitle"),
        translation.text(variant + ".deleteMessage"),
        translation.text(variant + ".deleted"),
        translation.text(variant + ".deleteFailed"),
        decision,
        this::choose,
        this::deleteLocalData);
  }

  private void choose(Boolean consent) {
    decision = java.util.Objects.requireNonNull(consent, "consent");
    TrackingRuntime.localTrackingConsent(decision);
    publishDecision();
    configureRoom();
    persistDecision.accept(decision);
  }

  private boolean deleteLocalData() {
    choose(false);
    return TrackingRuntime.deleteLocalData();
  }

  private void configureRoom() {
    Tracking.configureRoom(
        roomId, operatorEmail, Optional.ofNullable(runId.get()), Boolean.TRUE.equals(decision));
  }

  private void publishDecision() {
    if (decision == null) {
      System.clearProperty(ServerProcess.TRACKING_CONSENT_PROPERTY);
    } else {
      System.setProperty(ServerProcess.TRACKING_CONSENT_PROPERTY, Boolean.toString(decision));
    }
  }

  private String storageVariant() {
    return Tracking.remoteStorageEnabled() ? "central" : "local";
  }

  private static Boolean resolveDecision(String[] arguments, Supplier<Boolean> savedDecision) {
    String property = System.getProperty(ServerProcess.TRACKING_CONSENT_PROPERTY);
    if ("true".equalsIgnoreCase(property)) return true;
    if ("false".equalsIgnoreCase(property)) return false;
    if (arguments != null
        && java.util.Arrays.asList(arguments).contains(ServerProcess.SERVER_ARGUMENT)) {
      return savedDecision.get();
    }
    return null;
  }
}
