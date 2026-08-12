package foundation.multiplayer.binding;

import core.Entity;
import core.network.server.ClientState;
import foundation.multiplayer.session.MultiplayerSession;
import foundation.multiplayer.session.MultiplayerSession.ClientObservation;
import foundation.multiplayer.session.MultiplayerSession.SourceInspection;
import foundation.runtime.CodeAttemptResult;
import foundation.runtime.HintPreview;
import foundation.runtime.Projection;
import foundation.runtime.ReleasedHint;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/** Thin player-bound server adapter over one room-first {@link MultiplayerSession}. */
public final class ServerBinding {
  private final MultiplayerSession session;
  private final Supplier<Set<ClientState>> readyClients;

  /**
   * Creates a binding backed by the authoritative set of InitialWorldReady clients.
   *
   * @param session Foundation session
   * @param readyClients current technically ready Dungeon clients
   */
  public ServerBinding(
      final MultiplayerSession session, final Supplier<Set<ClientState>> readyClients) {
    this.session = Objects.requireNonNull(session, "session");
    this.readyClients = Objects.requireNonNull(readyClients, "readyClients");
  }

  /**
   * Reconciles ready players and advances authoritative room time.
   *
   * @param elapsed nonnegative time since the previous tick
   */
  public synchronized void tick(final Duration elapsed) {
    Objects.requireNonNull(elapsed, "elapsed");
    if (elapsed.isNegative()) {
      throw new IllegalArgumentException("Foundation elapsed duration must be nonnegative");
    }
    List<ClientObservation> observations =
        Set.copyOf(Objects.requireNonNull(readyClients.get(), "ready clients")).stream()
            .map(
                client ->
                    new ClientObservation(
                        client.clientId(), client.playerEntity().orElseThrow().id()))
            .toList();
    session.reconcileClients(observations);
    session.advance(elapsed);
  }

  /**
   * Returns the current player-visible projection.
   *
   * @return detached public state
   */
  public synchronized Projection projection() {
    return session.projection();
  }

  /**
   * Applies one authoritative source interaction for a ready server player.
   *
   * @param playerEntity authenticated server player
   * @param riddleId owning riddle identifier
   * @param informationSourceId readable source identifier
   * @return resource presentation result when the player may interact
   */
  public synchronized Optional<SourceInspection> inspectSource(
      final Entity playerEntity, final String riddleId, final String informationSourceId) {
    return clientId(playerEntity)
        .flatMap(clientId -> session.inspectSource(clientId, riddleId, informationSourceId));
  }

  /**
   * Applies one authoritative numeric input attempt for a ready server player.
   *
   * @param playerEntity authenticated server player
   * @param riddleId owning riddle identifier
   * @param inputId numeric input identifier
   * @param attempt raw player input
   * @return evaluated result when the player remains ready
   */
  public synchronized Optional<CodeAttemptResult> enterNumericCode(
      final Entity playerEntity,
      final String riddleId,
      final String inputId,
      final String attempt) {
    return clientId(playerEntity)
        .flatMap(clientId -> session.enterNumericCode(clientId, riddleId, inputId, attempt));
  }

  /**
   * Reports whether one input currently accepts actions.
   *
   * @param playerEntity authenticated server player
   * @param riddleId owning riddle identifier
   * @param inputId input identifier
   * @return whether the player and input are active
   */
  public synchronized boolean inputActive(
      final Entity playerEntity, final String riddleId, final String inputId) {
    if (!gameplayReady(playerEntity)) {
      return false;
    }
    return projection().riddles().stream()
        .filter(riddle -> riddle.id().equals(riddleId))
        .filter(riddle -> riddle.status() == Projection.ProgressStatus.ACTIVE)
        .flatMap(riddle -> riddle.inputs().stream())
        .anyMatch(input -> input.id().equals(inputId) && !input.satisfied());
  }

  /**
   * Previews the next hint category without releasing its content.
   *
   * @param playerEntity authenticated server player
   * @param riddleId riddle identifier
   * @return next non-content hint preview
   */
  public synchronized Optional<HintPreview> previewNextHint(
      final Entity playerEntity, final String riddleId) {
    return clientId(playerEntity).flatMap(clientId -> session.previewNextHint(clientId, riddleId));
  }

  /**
   * Releases the previously previewed hint after server-side confirmation.
   *
   * @param playerEntity authenticated server player
   * @param riddleId riddle identifier
   * @param expectedHintId identity captured by the preview
   * @return newly released hint while the player and preview remain current
   */
  public synchronized Optional<ReleasedHint> confirmNextHint(
      final Entity playerEntity, final String riddleId, final String expectedHintId) {
    return clientId(playerEntity)
        .flatMap(clientId -> session.confirmNextHint(clientId, riddleId, expectedHintId));
  }

  /**
   * Returns gameplay-ready player entities keyed by internal slot identifier.
   *
   * @return immutable slot-to-entity map
   */
  public synchronized Map<String, Integer> readyPlayerEntities() {
    return session.readyPlayerEntities();
  }

  /**
   * Reports whether one server player completed the introduction and may affect gameplay.
   *
   * @param playerEntity authenticated server player
   * @return whether the player is currently gameplay-ready
   */
  public synchronized boolean gameplayReady(final Entity playerEntity) {
    return session
        .readyClientId(Objects.requireNonNull(playerEntity, "playerEntity").id())
        .isPresent();
  }

  /**
   * Returns technically ready player entities, including players still viewing the introduction.
   *
   * @return immutable slot-to-entity map
   */
  public synchronized Map<String, Integer> technicalPlayerEntities() {
    return session.technicalPlayerEntities();
  }

  /**
   * Returns technical players eligible to receive the introduction.
   *
   * @return immutable slot-to-entity map
   */
  public synchronized Map<String, Integer> introEligiblePlayerEntities() {
    return session.introEligiblePlayerEntities();
  }

  /**
   * Confirms that one technically ready player's introduction has completed.
   *
   * @param slotId stable Foundation slot identifier shown the introduction
   * @param entityId server player entity shown the introduction
   * @return whether this confirmation changed the session
   */
  public synchronized boolean completeIntro(final String slotId, final int entityId) {
    Objects.requireNonNull(slotId, "slotId");
    if (!Objects.equals(session.technicalPlayerEntities().get(slotId), entityId)) {
      return false;
    }
    return technicalClientId(entityId).map(session::completeIntro).orElse(false);
  }

  /**
   * Reconciles the complete set of gameplay-ready slots on the authoritative exit tile.
   *
   * @param presentSlotIds gameplay-ready slot identifiers currently on the exit
   */
  public synchronized void reconcileExitPresence(final Set<String> presentSlotIds) {
    session.reconcileExitPresence(presentSlotIds);
  }

  private Optional<Short> clientId(final Entity playerEntity) {
    return session.readyClientId(Objects.requireNonNull(playerEntity, "playerEntity").id());
  }

  private Optional<Short> technicalClientId(final int entityId) {
    return readyClients.get().stream()
        .filter(client -> client.playerEntity().map(Entity::id).orElse(-1) == entityId)
        .map(ClientState::clientId)
        .findFirst();
  }
}
