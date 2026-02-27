package network;

import core.Entity;
import core.Game;
import core.game.ECSManagement;
import core.network.DefaultSnapshotTranslator;
import core.network.MessageDispatcher;
import core.network.SnapshotTranslator;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.SnapshotMessage;
import core.utils.logging.DungeonLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import modules.computer.ComputerProgress;
import modules.computer.ComputerStateComponent;

/**
 * Snapshot translator for The Last Hour that synchronizes the shared {@link ComputerStateComponent}
 * through metadata.
 */
public final class LastHourSnapshotTranslator implements SnapshotTranslator {

  private static final DungeonLogger LOGGER =
      DungeonLogger.getLogger(LastHourSnapshotTranslator.class);
  private static final String METADATA_PROGRESS = "progress";
  private static final String METADATA_INFECTED = "isInfected";
  private static final String METADATA_VIRUS_TYPE = "virusType";

  private final SnapshotTranslator delegate = new DefaultSnapshotTranslator();

  /**
   * Builds a snapshot and appends the computer state entity as metadata-only state.
   *
   * @param serverTick the current server tick
   * @return a snapshot including computer metadata state when available
   */
  @Override
  public Optional<SnapshotMessage> translateToSnapshot(int serverTick) {
    Optional<SnapshotMessage> baseSnapshot = delegate.translateToSnapshot(serverTick);
    if (baseSnapshot.isEmpty()) {
      return Optional.empty();
    }

    SnapshotMessage snapshot = baseSnapshot.orElseThrow();
    List<EntityState> entities = new ArrayList<>(snapshot.entities());

    ECSManagement.levelEntities(Set.of(ComputerStateComponent.class))
        .findFirst()
        .flatMap(
            entity ->
                entity.fetch(ComputerStateComponent.class).map(state -> Map.entry(entity, state)))
        .ifPresent(
            entry -> {
              Entity entity = entry.getKey();
              entities.removeIf(existing -> existing.entityId() == entity.id());
              entities.add(computerState(entity, entry.getValue()));
            });

    return Optional.of(new SnapshotMessage(snapshot.serverTick(), entities));
  }

  /**
   * Applies the base snapshot behavior and updates local computer state entities from metadata.
   *
   * @param snapshot the received snapshot message
   * @param dispatcher the message dispatcher used by the default translator
   */
  @Override
  public void applySnapshot(SnapshotMessage snapshot, MessageDispatcher dispatcher) {
    delegate.applySnapshot(snapshot, dispatcher);

    for (EntityState entityState : snapshot.entities()) {
      Optional<ComputerStateComponent> mappedState =
          entityState.metadata().flatMap(this::computerStateFromMetadata);
      if (mappedState.isEmpty()) {
        continue;
      }

      ComputerStateComponent computerState = mappedState.orElseThrow();
      Game.findEntityById(entityState.entityId())
          .ifPresent(
              entity -> {
                entity.remove(ComputerStateComponent.class);
                entity.add(computerState);
              });
    }
  }

  private EntityState computerState(Entity entity, ComputerStateComponent state) {
    return EntityState.builder()
        .entityId(entity.id())
        .entityName(entity.name())
        .metadata(
            Map.of(
                METADATA_PROGRESS,
                state.state().name(),
                METADATA_INFECTED,
                String.valueOf(state.isInfected()),
                METADATA_VIRUS_TYPE,
                state.virusType() == null ? "" : state.virusType()))
        .build();
  }

  private Optional<ComputerStateComponent> computerStateFromMetadata(Map<String, String> metadata) {
    String progressRaw = metadata.get(METADATA_PROGRESS);
    if (progressRaw == null || progressRaw.isBlank()) {
      return Optional.empty();
    }

    ComputerProgress progress;
    try {
      progress = ComputerProgress.valueOf(progressRaw);
    } catch (IllegalArgumentException ex) {
      LOGGER.warn("Invalid computer progress metadata '{}'", progressRaw);
      return Optional.empty();
    }

    boolean infected = Boolean.parseBoolean(metadata.getOrDefault(METADATA_INFECTED, "false"));
    String virusType = metadata.getOrDefault(METADATA_VIRUS_TYPE, "");
    return Optional.of(new ComputerStateComponent(progress, infected, virusType));
  }
}
