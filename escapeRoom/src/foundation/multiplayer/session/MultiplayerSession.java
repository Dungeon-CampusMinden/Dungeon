package foundation.multiplayer.session;

import foundation.definition.ComposedRiddleDefinition;
import foundation.definition.NumericInputDefinition;
import foundation.definition.RoomDefinition;
import foundation.definition.RosterSlotDefinition;
import foundation.presentation.GamePresentation;
import foundation.presentation.GamePresentation.ComposedPresentation;
import foundation.presentation.GamePresentation.InformationSourcePresentation;
import foundation.presentation.GamePresentation.ResourcePresentation;
import foundation.runtime.Authority;
import foundation.runtime.CodeAttemptResult;
import foundation.runtime.HintPreview;
import foundation.runtime.HintRevealResult;
import foundation.runtime.OperationResult;
import foundation.runtime.OperationStatus;
import foundation.runtime.Projection;
import foundation.runtime.ReleasedHint;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Transport-free adapter between technically ready Dungeon players and one {@link Authority}.
 *
 * <p>Client identifiers map directly to the ordered room slots. The mapping remains stable across
 * temporary disconnects. Initially, the authority starts once the configured minimum is present and
 * every currently observed client has completed the introduction.
 */
public final class MultiplayerSession {
  private final Authority authority;
  private final List<RosterSlotDefinition> slots;
  private final int minimumPlayers;
  private final Map<Short, ClientObservation> observations = new LinkedHashMap<>();
  private final Set<Short> introCompletedClients = new LinkedHashSet<>();
  private final Set<Short> gameplayReadyClients = new LinkedHashSet<>();
  private boolean introPresentationReleased;
  private final Set<String> exitSlots = new LinkedHashSet<>();
  private final Map<String, InformationSourcePresentation> informationSources;

  /**
   * Creates one room-first multiplayer session.
   *
   * @param definition complete room definition including minimum and maximum player counts
   * @param presentation complete locally derived presentation
   */
  public MultiplayerSession(final RoomDefinition definition, final GamePresentation presentation) {
    Objects.requireNonNull(definition, "definition");
    validatePresentation(definition, presentation);
    authority = new Authority(definition);
    slots = definition.roster().slots();
    minimumPlayers = definition.minimumPlayers();
    Map<String, InformationSourcePresentation> sources = new LinkedHashMap<>();
    presentation.riddles().stream()
        .flatMap(riddle -> riddle.informationSources().stream())
        .forEach(source -> sources.put(source.id(), source));
    informationSources = Map.copyOf(sources);
  }

  /**
   * Reconciles the complete set of technically ready players.
   *
   * @param clients ready-state observations from the authoritative server
   */
  public synchronized void reconcileClients(final List<ClientObservation> clients) {
    Objects.requireNonNull(clients, "clients");
    Map<Short, ClientObservation> next = new LinkedHashMap<>();
    Set<Integer> entityIds = new HashSet<>();
    clients.stream()
        .map(client -> Objects.requireNonNull(client, "client"))
        .sorted(Comparator.comparingInt(ClientObservation::clientId))
        .forEach(
            client -> {
              slot(client.clientId());
              if (next.putIfAbsent(client.clientId(), client) != null) {
                throw new IllegalArgumentException("duplicate Foundation client observation");
              }
              if (!entityIds.add(client.entityId())) {
                throw new IllegalArgumentException("duplicate Foundation player entity");
              }
            });

    for (RosterSlotDefinition slot : slots) {
      short clientId = (short) slot.number();
      ClientObservation previous = observations.get(clientId);
      ClientObservation current = next.get(clientId);
      boolean replaced =
          previous != null && current != null && previous.entityId() != current.entityId();
      if (previous != null && (current == null || replaced)) {
        introCompletedClients.remove(clientId);
        if (gameplayReadyClients.remove(clientId)) {
          authority.disconnect(slot.id());
        }
        exitSlots.remove(slot.id());
      }
    }
    observations.clear();
    observations.putAll(next);
    if (observations.size() >= minimumPlayers) {
      introPresentationReleased = true;
    }
    startInitialGameplayIfReady();
  }

  /**
   * Marks one technically ready client as ready to play after its introduction completed.
   *
   * @param clientId authenticated Dungeon client identifier
   * @return whether this confirmation changed the session
   */
  public synchronized boolean completeIntro(final short clientId) {
    ClientObservation observation = observations.get(clientId);
    if (!introPresentationReleased || observation == null || !introCompletedClients.add(clientId)) {
      return false;
    }
    if (authority.projection().timer().started()) {
      makeGameplayReady(clientId);
    } else {
      startInitialGameplayIfReady();
    }
    return true;
  }

  /**
   * Reads one information source and applies its collection input when active.
   *
   * @param clientId authenticated Dungeon client identifier
   * @param riddleId owning riddle identifier
   * @param informationSourceId source identifier
   * @return all existing source contents
   */
  public synchronized Optional<SourceInspection> inspectSource(
      final short clientId, final String riddleId, final String informationSourceId) {
    if (!ready(clientId)) {
      return Optional.empty();
    }
    InformationSourcePresentation source = informationSources.get(informationSourceId);
    if (source == null) {
      return Optional.empty();
    }
    OperationResult result = authority.interactSource(riddleId, informationSourceId);
    return Optional.of(
        new SourceInspection(source.resources(), result.status() == OperationStatus.APPLIED));
  }

  /**
   * Routes one numeric input attempt for a ready player.
   *
   * @param clientId stable Dungeon client identifier
   * @param riddleId owning riddle identifier
   * @param inputId numeric input identifier
   * @param attempt raw numeric attempt
   * @return delegated result, or empty when the player is not ready
   */
  public synchronized Optional<CodeAttemptResult> enterNumericCode(
      final short clientId, final String riddleId, final String inputId, final String attempt) {
    return ready(clientId)
        ? Optional.of(authority.attemptCode(riddleId, inputId, attempt))
        : Optional.empty();
  }

  /**
   * Previews the next hint category for a ready player without releasing content.
   *
   * @param clientId stable Dungeon client identifier
   * @param riddleId riddle identifier
   * @return next non-content preview while the player and riddle are ready
   */
  public synchronized Optional<HintPreview> previewNextHint(
      final short clientId, final String riddleId) {
    return ready(clientId) ? authority.previewHint(riddleId) : Optional.empty();
  }

  /**
   * Releases a previously previewed hint after the ready player confirms it.
   *
   * @param clientId stable Dungeon client identifier
   * @param riddleId riddle identifier
   * @param expectedHintId identity captured by the preview
   * @return newly released hint, or empty when readiness or preview changed
   */
  public synchronized Optional<ReleasedHint> confirmNextHint(
      final short clientId, final String riddleId, final String expectedHintId) {
    if (!ready(clientId)) {
      return Optional.empty();
    }
    HintRevealResult result = authority.revealHint(riddleId, expectedHintId);
    return result.hint();
  }

  /**
   * Reconciles slots currently observed on the authoritative exit tile.
   *
   * @param presentSlotIds complete ready-player exit presence
   */
  public synchronized void reconcileExitPresence(final Set<String> presentSlotIds) {
    Set<String> desired = Set.copyOf(Objects.requireNonNull(presentSlotIds, "presentSlotIds"));
    if (desired.stream().anyMatch(slotId -> !ready(slotId))) {
      throw new IllegalArgumentException("exit presence requires a ready Foundation slot");
    }
    for (String slotId : List.copyOf(exitSlots)) {
      if (!desired.contains(slotId)) {
        authority.leaveExit(slotId);
        exitSlots.remove(slotId);
      }
    }
    for (String slotId : desired) {
      if (exitSlots.add(slotId)) {
        authority.enterExit(slotId);
      }
    }
  }

  /**
   * Advances authoritative room time.
   *
   * @param elapsed nonnegative elapsed duration
   * @return delegated authority result
   */
  public synchronized OperationResult advance(final Duration elapsed) {
    return authority.advance(elapsed);
  }

  /**
   * Returns the current detached player-visible authority projection.
   *
   * @return public room state
   */
  public synchronized Projection projection() {
    return authority.projection();
  }

  /**
   * Returns gameplay-ready player entities keyed by their internal slot identifier.
   *
   * @return immutable slot-to-entity map in slot order
   */
  public synchronized Map<String, Integer> readyPlayerEntities() {
    Map<String, Integer> entities = new LinkedHashMap<>();
    for (RosterSlotDefinition slot : slots) {
      ClientObservation observation = observations.get((short) slot.number());
      if (observation != null && gameplayReadyClients.contains((short) slot.number())) {
        entities.put(slot.id(), observation.entityId());
      }
    }
    return Map.copyOf(entities);
  }

  /**
   * Returns technically ready player entities, including players still viewing the introduction.
   *
   * @return immutable slot-to-entity map in slot order
   */
  public synchronized Map<String, Integer> technicalPlayerEntities() {
    Map<String, Integer> entities = new LinkedHashMap<>();
    for (RosterSlotDefinition slot : slots) {
      ClientObservation observation = observations.get((short) slot.number());
      if (observation != null) {
        entities.put(slot.id(), observation.entityId());
      }
    }
    return Map.copyOf(entities);
  }

  /**
   * Returns technical players eligible to receive the introduction.
   *
   * <p>The initial introduction group is released once the technical minimum is reached.
   * Afterwards, reconnecting and later joining technical players remain eligible immediately.
   *
   * @return immutable slot-to-entity map in slot order
   */
  public synchronized Map<String, Integer> introEligiblePlayerEntities() {
    return introPresentationReleased ? technicalPlayerEntities() : Map.of();
  }

  /**
   * Resolves the gameplay-ready client that owns one server player entity.
   *
   * @param entityId authoritative player entity identifier
   * @return stable client identifier while that player is gameplay-ready
   */
  public synchronized Optional<Short> readyClientId(final int entityId) {
    return observations.entrySet().stream()
        .filter(entry -> gameplayReadyClients.contains(entry.getKey()))
        .filter(entry -> entry.getValue().entityId() == entityId)
        .map(Map.Entry::getKey)
        .findFirst();
  }

  private boolean ready(final short clientId) {
    return observations.containsKey(clientId) && gameplayReadyClients.contains(clientId);
  }

  private void startInitialGameplayIfReady() {
    if (authority.projection().timer().started()
        || observations.size() < minimumPlayers
        || !introCompletedClients.containsAll(observations.keySet())) {
      return;
    }
    List.copyOf(observations.keySet()).forEach(this::makeGameplayReady);
  }

  private void makeGameplayReady(final short clientId) {
    if (!observations.containsKey(clientId)
        || gameplayReadyClients.contains(clientId)
        || authority.projection().terminal().isPresent()) {
      return;
    }
    RosterSlotDefinition slot = slot(clientId);
    authority.connect(slot.id());
    authority.markSpawned(slot.id());
    gameplayReadyClients.add(clientId);
  }

  private boolean ready(final String slotId) {
    return slots.stream()
        .filter(slot -> slot.id().equals(slotId))
        .findFirst()
        .map(slot -> ready((short) slot.number()))
        .orElse(false);
  }

  private RosterSlotDefinition slot(final short clientId) {
    int number = Short.toUnsignedInt(clientId);
    if (number < 1 || number > slots.size()) {
      throw new IllegalArgumentException("client is outside Foundation room capacity");
    }
    return slots.get(number - 1);
  }

  private static void validatePresentation(
      final RoomDefinition definition, final GamePresentation presentation) {
    Objects.requireNonNull(presentation, "presentation");
    List<ComposedRiddleDefinition> definitions =
        definition.progression().riddleNodes().stream().map(node -> node.riddle()).toList();
    if (definitions.size() != presentation.riddles().size()) {
      throw new IllegalArgumentException(
          "presentation riddle count must match Foundation definition");
    }
    for (int index = 0; index < definitions.size(); index++) {
      ComposedRiddleDefinition definitionRiddle = definitions.get(index);
      ComposedPresentation presented = presentation.riddles().get(index);
      if (!definitionRiddle.id().equals(presented.id())) {
        throw new IllegalArgumentException("presentation riddles must match definition order");
      }
      if (!definitionRiddle.informationSources().stream()
              .map(source -> source.id() + "\u0000" + source.surfaceId())
              .toList()
              .equals(
                  presented.informationSources().stream()
                      .map(source -> source.id() + "\u0000" + source.surfaceId())
                      .toList())
          || !definitionRiddle.informationSources().stream()
              .map(source -> source.resourceIds())
              .toList()
              .equals(
                  presented.informationSources().stream()
                      .map(
                          source ->
                              source.resources().stream().map(ResourcePresentation::id).toList())
                      .toList())
          || !definitionRiddle.inputs().stream()
              .filter(NumericInputDefinition.class::isInstance)
              .map(NumericInputDefinition.class::cast)
              .map(input -> input.id() + "\u0000" + input.surfaceId())
              .toList()
              .equals(
                  presented.inputs().stream()
                      .map(input -> input.id() + "\u0000" + input.surfaceId())
                      .toList())) {
        throw new IllegalArgumentException(
            "composed presentation must match Foundation definition");
      }
    }
  }

  /**
   * Immutable observation of one Dungeon client.
   *
   * @param clientId stable positive Dungeon client identifier
   * @param entityId technically ready player's entity identity
   */
  public record ClientObservation(short clientId, int entityId) {
    /**
     * Creates a bounded observation.
     *
     * @param clientId stable positive Dungeon client identifier
     * @param entityId technically ready player's entity identity
     */
    public ClientObservation {
      if (clientId <= 0) {
        throw new IllegalArgumentException("client ID must be positive");
      }
      if (entityId < 0) {
        throw new IllegalArgumentException("entity ID must be nonnegative");
      }
    }
  }

  /**
   * Player-facing result of one authoritative source interaction.
   *
   * @param resources ordered resources to present
   * @param newlySatisfied whether the interaction satisfied a collection input
   */
  public record SourceInspection(List<ResourcePresentation> resources, boolean newlySatisfied) {
    /**
     * Creates an immutable nonempty source presentation result.
     *
     * @param resources ordered resources to present
     * @param newlySatisfied whether the interaction satisfied a collection input
     */
    public SourceInspection {
      resources = List.copyOf(Objects.requireNonNull(resources, "resources"));
      if (resources.isEmpty() || resources.stream().anyMatch(Objects::isNull)) {
        throw new IllegalArgumentException("source inspection requires resources");
      }
    }
  }
}
