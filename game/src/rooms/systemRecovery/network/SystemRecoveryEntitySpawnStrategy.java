package rooms.systemRecovery.network;

import engine.Entity;
import engine.components.PositionComponent;
import engine.network.config.DefaultEntitySpawnStrategy;
import engine.network.config.EntitySpawnStrategy;
import engine.network.messages.s2c.EntitySpawnEvent;
import feature.collision.CollideSync;
import feature.interaction.InteractionComponent;
import feature.interaction.keypad.KeypadComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.modules.display.DisplayTextComponent;
import rooms.systemRecovery.modules.display.DoorLabelComponent;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;

/** Entity spawn strategy for System Recovery metadata. */
public final class SystemRecoveryEntitySpawnStrategy implements EntitySpawnStrategy {

  /** Metadata key identifying the custom entity type. */
  public static final String METADATA_TYPE = "systemRecovery.type";

  /** Metadata key indicating whether the entity is interactable. */
  public static final String METADATA_INTERACTABLE = "systemRecovery.interactable";

  /** Metadata key indicating that a module socket has been activated. */
  public static final String METADATA_MODULE_SOCKET_ACTIVE = "systemRecovery.moduleSocket.active";

  /** Metadata key identifying a synchronized keypad. */
  public static final String METADATA_KEYPAD = "systemRecovery.keypad";

  public static final String METADATA_KEYPAD_CORRECT_DIGITS = "systemRecovery.keypad.correct";
  public static final String METADATA_KEYPAD_ENTERED_DIGITS = "systemRecovery.keypad.entered";
  public static final String METADATA_KEYPAD_UNLOCKED = "systemRecovery.keypad.unlocked";
  public static final String METADATA_KEYPAD_SHOW_DIGIT_COUNT = "systemRecovery.keypad.showCount";
  public static final String METADATA_DISPLAY_TEXT = "systemRecovery.display.text";
  public static final String METADATA_TERMINAL_STATE = "systemRecovery.terminal.state";
  public static final String METADATA_SAVE_REVISION = "systemRecovery.save.revision";
  public static final String METADATA_SORT_LEFT_ENTITY = "systemRecovery.sort.leftEntity";
  public static final String METADATA_SORT_RIGHT_ENTITY = "systemRecovery.sort.rightEntity";
  public static final String METADATA_BELT_LEFT_PACKAGE = "systemRecovery.belt.leftPackage";
  public static final String METADATA_BELT_RIGHT_PACKAGE = "systemRecovery.belt.rightPackage";
  public static final String METADATA_BELT_SCANNER = "systemRecovery.belt.scanner";
  public static final String METADATA_BELT_PACKAGES = "systemRecovery.belt.packages";
  public static final String METADATA_MODULE_SCAN_RUNNING = "systemRecovery.module.scanRunning";
  public static final String METADATA_MODULE_SCAN_FAULT = "systemRecovery.module.scanFault";
  public static final String METADATA_STORAGE_CELL_STATE = "systemRecovery.storage.cellState";
  public static final String METADATA_STORAGE_CELL_VALUE = "systemRecovery.storage.cellValue";
  public static final String METADATA_SYSTEM_CORE_ACCESS = "systemRecovery.systemCoreAccess";
  public static final String METADATA_SYSTEM_CORE_ALARM = "systemRecovery.systemCoreAlarm";
  public static final String METADATA_SYSTEM_CORE_STAGE = "systemRecovery.systemCoreStage";
  public static final String METADATA_SEARCH_ROBOT_CELL = "systemRecovery.searchRobot.cell";

  /** Metadata prefix for synchronized collider state. */
  public static final String METADATA_COLLIDER_PREFIX = "systemRecovery.collider";

  private static final CollideSync COLLIDE_SYNC = CollideSync.withPrefix(METADATA_COLLIDER_PREFIX);

  private final EntitySpawnStrategy delegate = new DefaultEntitySpawnStrategy();

  /**
   * Builds a spawn event using default behavior and appends System Recovery metadata where needed.
   *
   * @param entity the source entity
   * @return an Optional containing a spawn event if the entity is spawnable, otherwise empty
   */
  @Override
  public Optional<EntitySpawnEvent> buildSpawnEvent(Entity entity) {
    Optional<EntitySpawnEvent> defaultSpawn = delegate.buildSpawnEvent(entity);
    Map<String, String> metadata = new HashMap<>();
    defaultSpawn.ifPresent(spawnEvent -> metadata.putAll(spawnEvent.metadata()));

    entity
        .fetch(InteractionComponent.class)
        .ifPresent(interaction -> metadata.put(METADATA_INTERACTABLE, String.valueOf(true)));
    entity
        .fetch(KeypadComponent.class)
        .ifPresent(keypad -> SystemRecoveryComponentSync.appendKeypadMetadata(keypad, metadata));
    entity
        .fetch(DisplayTextComponent.class)
        .ifPresent(display -> metadata.put(METADATA_DISPLAY_TEXT, display.text()));
    if (entity.name() != null && entity.name().endsWith("terminal")) {
      metadata.put(
          METADATA_TERMINAL_STATE, String.valueOf(TerminalInterpreter.instance().currentState()));
    }
    if ("terminal".equals(entity.name())) {
      metadata.put(METADATA_SAVE_REVISION, String.valueOf(SystemRecoveryLevel.saveRevision()));
    }
    appendModuleScanMetadata(entity, metadata);
    appendSearchRobotMetadata(entity, metadata);
    COLLIDE_SYNC.appendMetadata(entity, metadata);
    DoorLabelComponent.appendMetadata(entity, metadata);

    if ("label_systemcore".equals(entity.name())) {
      metadata.put(
          METADATA_SYSTEM_CORE_ACCESS,
          String.valueOf(SystemRecoveryLevel.systemCoreAccessGranted()));
      metadata.put(
          METADATA_SYSTEM_CORE_ALARM, String.valueOf(SystemRecoveryLevel.systemCoreAlarmActive()));
    }
    if (isSystemCoreArea(entity)) {
      metadata.put(
          METADATA_SYSTEM_CORE_STAGE, String.valueOf(SystemRecoveryLevel.systemCoreStage()));
    }

    if (defaultSpawn.isPresent() && !metadata.isEmpty()) {
      EntitySpawnEvent base = defaultSpawn.orElseThrow();
      return Optional.of(
          EntitySpawnEvent.builder()
              .entityId(base.entityId())
              .positionComponent(base.positionComponent())
              .drawInfo(base.drawInfo())
              .playerComponent(base.playerComponent())
              .characterClassId(base.characterClassId())
              .shaderComponent(base.shaderComponent())
              .metadata(metadata)
              .build());
    }

    if (defaultSpawn.isPresent()) {
      return defaultSpawn;
    }

    if (metadata.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(
        EntitySpawnEvent.builder()
            .entityId(entity.id())
            .positionComponent(entity.fetch(PositionComponent.class).orElse(null))
            .metadata(metadata)
            .build());
  }

  private void appendModuleScanMetadata(Entity entity, Map<String, String> metadata) {
    if ("module_scanner".equals(entity.name())) {
      metadata.put(
          METADATA_MODULE_SCAN_RUNNING, String.valueOf(SystemRecoveryLevel.scannerRunning()));
      metadata.put(
          METADATA_MODULE_SCAN_FAULT, String.valueOf(SystemRecoveryLevel.scannerFaultDetected()));
    }
  }

  private void appendSearchRobotMetadata(Entity entity, Map<String, String> metadata) {
    if ("search_robot".equals(entity.name())) {
      metadata.put(METADATA_SEARCH_ROBOT_CELL, SystemRecoveryLevel.currentSearchRobotCell(entity));
    }
  }

  private boolean isSystemCoreArea(Entity entity) {
    return entity.name() != null && entity.name().startsWith("system_core_");
  }
}
