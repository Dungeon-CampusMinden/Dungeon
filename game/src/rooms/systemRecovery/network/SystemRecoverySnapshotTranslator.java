package rooms.systemRecovery.network;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.network.DefaultSnapshotTranslator;
import engine.network.MessageDispatcher;
import engine.network.SnapshotTranslator;
import engine.network.messages.s2c.EntityState;
import engine.network.messages.s2c.SnapshotMessage;
import feature.collision.CollideSync;
import feature.interaction.InteractionComponent;
import feature.interaction.keypad.KeypadComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.modules.display.DisplayTextComponent;
import rooms.systemRecovery.modules.display.DoorLabelComponent;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.save.SystemRecoveryAutoSaveHud;

/** Snapshot translator for metadata-backed System Recovery components. */
public final class SystemRecoverySnapshotTranslator implements SnapshotTranslator {

  private static final CollideSync COLLIDE_SYNC =
      CollideSync.withPrefix(SystemRecoveryEntitySpawnStrategy.METADATA_COLLIDER_PREFIX);

  private final SnapshotTranslator delegate = new DefaultSnapshotTranslator();
  private final ManualSortVisualSync manualSortVisualSync = new ManualSortVisualSync();
  private final ConveyorSortVisualSync conveyorSortVisualSync = new ConveyorSortVisualSync();
  private final ModuleScannerVisualSync moduleScannerVisualSync = new ModuleScannerVisualSync();
  private final StorageVisualSync storageVisualSync = new StorageVisualSync();
  private final SearchRobotVisualSync searchRobotVisualSync = new SearchRobotVisualSync();

  /**
   * Builds a snapshot and appends System Recovery metadata for shared components.
   *
   * @param serverTick the current server tick
   * @return a snapshot including custom metadata state when available
   */
  @Override
  public Optional<SnapshotMessage> translateToSnapshot(int serverTick) {
    Optional<SnapshotMessage> baseSnapshot = delegate.translateToSnapshot(serverTick);
    if (baseSnapshot.isEmpty()) {
      return Optional.empty();
    }

    SnapshotMessage snapshot = baseSnapshot.orElseThrow();
    List<EntityState> entities = new ArrayList<>(snapshot.entities());

    Game.levelEntities()
        .forEach(
            entity -> {
              Map<String, String> metadata = snapshotMetadata(entity);
              if (metadata.isEmpty()) {
                return;
              }

              int index = indexOfEntityStateById(entities, entity.id()).orElse(-1);
              if (index >= 0) {
                entities.set(index, withMergedMetadata(entities.get(index), metadata));
              } else {
                entities.add(metadataOnlyState(entity, metadata));
              }
            });

    return Optional.of(new SnapshotMessage(snapshot.serverTick(), entities, snapshot.levelState()));
  }

  /**
   * Applies default snapshot behavior and updates local metadata-backed components.
   *
   * @param snapshot the received snapshot message
   * @param dispatcher the message dispatcher used by the default translator
   */
  @Override
  public void applySnapshot(SnapshotMessage snapshot, MessageDispatcher dispatcher) {
    delegate.applySnapshot(snapshot, dispatcher);

    for (EntityState entityState : snapshot.entities()) {
      Optional<Map<String, String>> metadata = entityState.metadata();
      if (metadata.isEmpty()) {
        continue;
      }
      Game.findEntityById(entityState.entityId())
          .ifPresent(
              entity -> {
                SystemRecoveryComponentSync.applyEntityMetadata(entity, metadata.orElseThrow());
                manualSortVisualSync.apply(entity, metadata.orElseThrow());
                conveyorSortVisualSync.apply(metadata.orElseThrow());
                moduleScannerVisualSync.apply(entity, metadata.orElseThrow());
                storageVisualSync.apply(entity, metadata.orElseThrow());
                searchRobotVisualSync.apply(entity, metadata.orElseThrow());
                SystemCoreVisualSync.applyAlarm(metadata.orElseThrow());
                SystemCoreVisualSync.applyCompletionMetadata(entity, metadata.orElseThrow());
                String terminalState =
                    metadata
                        .orElseThrow()
                        .get(SystemRecoveryEntitySpawnStrategy.METADATA_TERMINAL_STATE);
                if (terminalState != null) {
                  TerminalInterpreter.instance().synchronizeState(Integer.parseInt(terminalState));
                }
                SystemRecoveryAutoSaveHud.acceptRevision(
                    metadata.orElseThrow().get(SystemRecoveryEntitySpawnStrategy.METADATA_SAVE_REVISION));
              });
    }
  }

  Map<String, String> snapshotMetadata(Entity entity) {
    Map<String, String> metadata = new HashMap<>();
    DoorLabelComponent.appendMetadata(entity, metadata);
    entity
        .fetch(DisplayTextComponent.class)
        .ifPresent(
            display ->
                metadata.put(
                    SystemRecoveryEntitySpawnStrategy.METADATA_DISPLAY_TEXT, display.text()));
    if (entity.isPresent(PositionComponent.class) && entity.isPresent(DrawComponent.class)) {
      metadata.put(
          SystemRecoveryEntitySpawnStrategy.METADATA_INTERACTABLE,
          String.valueOf(entity.isPresent(InteractionComponent.class)));
    }
    if ("module_socket_active".equals(entity.name())
        || entity.name().startsWith("module_socket_occupied_")) {
      metadata.put(SystemRecoveryEntitySpawnStrategy.METADATA_MODULE_SOCKET_ACTIVE, "true");
    }
    entity
        .fetch(KeypadComponent.class)
        .ifPresent(keypad -> SystemRecoveryComponentSync.appendKeypadMetadata(keypad, metadata));
    if ("sort_compare_display".equals(entity.name())) {
      int[] comparison = SystemRecoveryLevel.currentSortComparisonEntityIds();
      metadata.put(
          SystemRecoveryEntitySpawnStrategy.METADATA_SORT_LEFT_ENTITY,
          String.valueOf(comparison[0]));
      metadata.put(
          SystemRecoveryEntitySpawnStrategy.METADATA_SORT_RIGHT_ENTITY,
          String.valueOf(comparison[1]));
    }
    if ("bubble_sort_machine".equals(entity.name())) {
      int[] belt = SystemRecoveryLevel.currentBeltSortEntityIds();
      metadata.put(
          SystemRecoveryEntitySpawnStrategy.METADATA_BELT_LEFT_PACKAGE, String.valueOf(belt[0]));
      metadata.put(
          SystemRecoveryEntitySpawnStrategy.METADATA_BELT_RIGHT_PACKAGE, String.valueOf(belt[1]));
      metadata.put(
          SystemRecoveryEntitySpawnStrategy.METADATA_BELT_SCANNER, String.valueOf(belt[2]));
      metadata.put(
          SystemRecoveryEntitySpawnStrategy.METADATA_BELT_PACKAGES,
          SystemRecoveryLevel.currentBeltPackageMetadata());
    }
    if (entity.name() != null && entity.name().endsWith("terminal")) {
      metadata.put(
          SystemRecoveryEntitySpawnStrategy.METADATA_TERMINAL_STATE,
          String.valueOf(TerminalInterpreter.instance().currentState()));
    }
    if ("terminal".equals(entity.name())) {
      metadata.put(
          SystemRecoveryEntitySpawnStrategy.METADATA_SAVE_REVISION,
          String.valueOf(SystemRecoveryLevel.saveRevision()));
    }
    if ("module_scanner".equals(entity.name())) {
      metadata.put(
          SystemRecoveryEntitySpawnStrategy.METADATA_MODULE_SCAN_RUNNING,
          String.valueOf(SystemRecoveryLevel.scannerRunning()));
      metadata.put(
          SystemRecoveryEntitySpawnStrategy.METADATA_MODULE_SCAN_FAULT,
          String.valueOf(SystemRecoveryLevel.scannerFaultDetected()));
    }
    if ("search_robot".equals(entity.name())) {
      metadata.put(
          SystemRecoveryEntitySpawnStrategy.METADATA_SEARCH_ROBOT_CELL,
          SystemRecoveryLevel.currentSearchRobotCell(entity));
    }
    if (entity.name().startsWith("storage_matrix_cell_")) {
      metadata.put(
          SystemRecoveryEntitySpawnStrategy.METADATA_STORAGE_CELL_STATE,
          SystemRecoveryLevel.storageCellState(entity.name()));
      String[] parts = entity.name().split("_");
      if (parts.length >= 5) {
        try {
          int row = Integer.parseInt(parts[3]);
          int column = Integer.parseInt(parts[4]);
          metadata.put(
              SystemRecoveryEntitySpawnStrategy.METADATA_STORAGE_CELL_VALUE,
              String.valueOf(activeStorageCellValue(row, column)));
        } catch (NumberFormatException ignored) {
          // Keep the cell synchronized as an empty visual when an editor name is malformed.
        }
      }
    }
    if ("label_systemcore".equals(entity.name())) {
      metadata.put(
          SystemRecoveryEntitySpawnStrategy.METADATA_SYSTEM_CORE_ACCESS,
          String.valueOf(SystemRecoveryLevel.systemCoreAccessGranted()));
      metadata.put(
          SystemRecoveryEntitySpawnStrategy.METADATA_SYSTEM_CORE_ALARM,
          String.valueOf(SystemRecoveryLevel.systemCoreAlarmActive()));
    }
    if (entity.name() != null && entity.name().startsWith("system_core_")) {
      metadata.put(
          SystemRecoveryEntitySpawnStrategy.METADATA_SYSTEM_CORE_STAGE,
          String.valueOf(SystemRecoveryLevel.systemCoreStage()));
    }
    COLLIDE_SYNC.appendMetadata(entity, metadata);
    return metadata;
  }

  private int activeStorageCellValue(int row, int column) {
    return switch (row + "_" + column) {
      case "0_2" -> 1;
      case "1_3" -> 2;
      case "2_1" -> 3;
      default -> 0;
    };
  }

  private EntityState withMergedMetadata(EntityState baseState, Map<String, String> metadata) {
    EntityState.Builder builder = EntityState.builder().entityId(baseState.entityId());
    baseState.entityName().ifPresent(builder::entityName);
    baseState.position().ifPresent(builder::position);
    baseState.viewDirection().ifPresent(builder::viewDirection);
    baseState.rotation().ifPresent(builder::rotation);
    baseState.scale().ifPresent(builder::scale);
    baseState.currentHealth().ifPresent(builder::currentHealth);
    baseState.maxHealth().ifPresent(builder::maxHealth);
    baseState.currentMana().ifPresent(builder::currentMana);
    baseState.maxMana().ifPresent(builder::maxMana);
    baseState.stateName().ifPresent(builder::stateName);
    baseState.tintColor().ifPresent(builder::tintColor);
    baseState.inventory().ifPresent(builder::inventorySlots);
    baseState.shaderComponent().ifPresent(builder::shaderComponent);

    Map<String, String> mergedMetadata = new HashMap<>();
    baseState.metadata().ifPresent(mergedMetadata::putAll);
    mergedMetadata.putAll(metadata);
    builder.metadata(mergedMetadata);
    return builder.build();
  }

  private EntityState metadataOnlyState(Entity entity, Map<String, String> metadata) {
    EntityState.Builder builder = EntityState.builder().entityId(entity.id()).metadata(metadata);
    if (entity.name() != null && !entity.name().isBlank()) {
      builder.entityName(entity.name());
    }
    entity
        .fetch(PositionComponent.class)
        .ifPresent(
            positionComponent -> {
              builder.position(positionComponent.position());
              builder.viewDirection(positionComponent.viewDirection());
              builder.rotation(positionComponent.rotation());
              builder.scale(positionComponent.scale());
            });
    return builder.build();
  }

  private Optional<Integer> indexOfEntityStateById(List<EntityState> entities, int entityId) {
    for (int i = 0; i < entities.size(); i++) {
      if (entities.get(i).entityId() == entityId) {
        return Optional.of(i);
      }
    }
    return Optional.empty();
  }
}
