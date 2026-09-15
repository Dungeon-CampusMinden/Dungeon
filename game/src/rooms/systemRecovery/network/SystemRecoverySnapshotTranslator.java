package rooms.systemRecovery.network;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.network.DefaultSnapshotTranslator;
import engine.network.MessageDispatcher;
import engine.network.SnapshotTranslator;
import engine.network.messages.s2c.EntityState;
import engine.network.messages.s2c.SnapshotMessage;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.draw.shader.EnergyFillShader;
import engine.utils.components.draw.shader.HueRemapShader;
import engine.utils.components.draw.shader.OutlineShader;
import engine.utils.components.path.SimpleIPath;
import feature.collision.CollideSync;
import feature.components.CollideComponent;
import feature.interaction.InteractionComponent;
import feature.interaction.keypad.KeypadComponent;
import feature.questlog.QuestLogComponent;
import feature.questlog.QuestLogEntry;
import feature.questlog.QuestLogUtil;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.modules.display.DisplayTextComponent;
import rooms.systemRecovery.modules.display.DoorLabelComponent;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.modules.scanner.ModuleScannerVisualState;
import rooms.systemRecovery.util.shaders.EnergyGlow;
import rooms.systemRecovery.util.shaders.SystemRecoveryAlarm;

/** Snapshot translator for metadata-backed System Recovery components. */
public final class SystemRecoverySnapshotTranslator implements SnapshotTranslator {

  private static final CollideSync COLLIDE_SYNC =
      CollideSync.withPrefix(SystemRecoveryEntitySpawnStrategy.METADATA_COLLIDER_PREFIX);

  private final SnapshotTranslator delegate = new DefaultSnapshotTranslator();
  private int lastBeltLeftPackage = -1;
  private int lastBeltRightPackage = -1;
  private int lastBeltScanner = -1;
  private int lastSortLeftEntity = Integer.MIN_VALUE;
  private int lastSortRightEntity = Integer.MIN_VALUE;
  private boolean sortComparisonInitialized;
  private boolean lastModuleScanFault;

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
                applyInteractableMetadata(entity, metadata.orElseThrow());
                keypadFromMetadata(metadata.orElseThrow())
                    .ifPresent(keypad -> applyKeypadState(entity, keypad));
                applyDisplayMetadata(entity, metadata.orElseThrow());
                questLogFromMetadata(metadata.orElseThrow())
                    .ifPresent(questLog -> applyQuestLogState(entity, questLog));
                applySortComparisonMetadata(metadata.orElseThrow());
                applyBeltSortMetadata(metadata.orElseThrow());
                applyModuleScanMetadata(entity, metadata.orElseThrow());
                applyStorageCellMetadata(entity, metadata.orElseThrow());
                applySystemCoreAlarm(metadata.orElseThrow());
                applySystemCoreVisualMetadata(entity, metadata.orElseThrow());
                String terminalState =
                    metadata
                        .orElseThrow()
                        .get(SystemRecoveryEntitySpawnStrategy.METADATA_TERMINAL_STATE);
                if (terminalState != null) {
                  TerminalInterpreter.instance().synchronizeState(Integer.parseInt(terminalState));
                }
                collideComponentFromMetadata(metadata.orElseThrow())
                    .ifPresent(collideState -> COLLIDE_SYNC.apply(entity, collideState));
              });
    }
  }

  private Optional<KeypadComponent> keypadFromMetadata(Map<String, String> metadata) {
    if (!Boolean.parseBoolean(
        metadata.getOrDefault(SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD, "false"))) {
      return Optional.empty();
    }
    return Optional.of(
        new KeypadComponent(
            parseDigits(
                metadata.getOrDefault(
                    SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_CORRECT_DIGITS, "")),
            parseDigits(
                metadata.getOrDefault(
                    SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_ENTERED_DIGITS, "")),
            Boolean.parseBoolean(
                metadata.getOrDefault(
                    SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_UNLOCKED, "false")),
            Boolean.parseBoolean(
                metadata.getOrDefault(
                    SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_SHOW_DIGIT_COUNT, "true"))));
  }

  private void applyKeypadState(Entity entity, KeypadComponent state) {
    KeypadComponent keypad = entity.fetch(KeypadComponent.class).orElse(null);
    if (keypad == null) {
      entity.add(state);
      return;
    }
    keypad.enteredDigits().clear();
    keypad.enteredDigits().addAll(state.enteredDigits());
    keypad.isUnlocked(state.isUnlocked());
    keypad.showDigitCount(state.showDigitCount());
  }

  private void applyDisplayMetadata(Entity entity, Map<String, String> metadata) {
    String text = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_DISPLAY_TEXT);
    if (text != null) {
      entity
          .fetch(DisplayTextComponent.class)
          .ifPresentOrElse(
              display -> display.text(text), () -> entity.add(new DisplayTextComponent(text)));
    }
  }

  /**
   * Applies the authoritative system-core alarm state on a graphical client.
   *
   * <p>This is public because the initial entity-spawn path must apply the same state as regular
   * snapshots. Without this, a client joining after the system-core access script was accepted
   * could miss the alarm until a later state transition.
   *
   * @param metadata synchronized System Recovery metadata
   */
  public static void applySystemCoreAlarm(Map<String, String> metadata) {
    String alarm = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_SYSTEM_CORE_ALARM);
    if (alarm != null) {
      if (Boolean.parseBoolean(alarm)) SystemRecoveryAlarm.activate();
      else SystemRecoveryAlarm.deactivate();
      return;
    }
    if (Boolean.parseBoolean(
        metadata.getOrDefault(
            SystemRecoveryEntitySpawnStrategy.METADATA_SYSTEM_CORE_ACCESS, "false"))) {
      SystemRecoveryAlarm.activate();
    }
  }

  /**
   * Projects the authoritative system-core section state into the local completion shader.
   *
   * <p>Only the small stage value is synchronized. The shader itself is created locally, which
   * keeps the network payload small while still making late joins and live clients agree on the
   * same visual state.
   *
   * @param entity the system-core visual entity
   * @param metadata synchronized System Recovery metadata
   */
  public static void applySystemCoreVisualMetadata(
      Entity entity, Map<String, String> metadata) {
    String stageValue =
        metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_SYSTEM_CORE_STAGE);
    if (stageValue == null || entity.name() == null) return;

    int stage;
    try {
      stage = Integer.parseInt(stageValue);
    } catch (NumberFormatException ignored) {
      return;
    }

    int requiredStage = requiredSystemCoreStage(entity.name());
    if (requiredStage < 0) return;
    boolean completed = stage >= requiredStage;
    entity
        .fetch(DrawComponent.class)
        .ifPresent(
            draw -> {
              if (completed) {
                draw.tintColor(0x66FF66FF);
                if (draw.shaders().get("systemCoreComplete") == null) {
                  draw.shaders()
                      .add(
                          "systemCoreComplete",
                          new OutlineShader(2, Color.GREEN, 1.8f, 0.25f));
                }
              } else {
                draw.shaders().remove("systemCoreComplete");
              }
            });
  }

  private static int requiredSystemCoreStage(String entityName) {
    if (entityName.startsWith("system_core_sort_")) return 1;
    if (entityName.startsWith("system_core_module_")) return 2;
    if (entityName.startsWith("system_core_map_")) return 3;
    return -1;
  }

  /** Reproduces the server-selected comparison highlight on every client. */
  private void applySortComparisonMetadata(Map<String, String> metadata) {
    String leftValue = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_SORT_LEFT_ENTITY);
    String rightValue = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_SORT_RIGHT_ENTITY);
    if (leftValue == null || rightValue == null) {
      return;
    }

    int leftEntityId = parseEntityId(leftValue);
    int rightEntityId = parseEntityId(rightValue);
    if (sortComparisonInitialized
        && leftEntityId == lastSortLeftEntity
        && rightEntityId == lastSortRightEntity) {
      return;
    }

    clearSortComparisonHighlights();
    Game.levelEntities()
        .filter(entity -> entity.name().startsWith("sort_data_"))
        .forEach(this::addSortFill);
    addSortComparisonHighlight(leftEntityId);
    addSortComparisonHighlight(rightEntityId);
    lastSortLeftEntity = leftEntityId;
    lastSortRightEntity = rightEntityId;
    sortComparisonInitialized = true;
  }

  private void clearSortComparisonHighlights() {
    Game.levelEntities()
        .forEach(
            entity ->
                entity
                    .fetch(DrawComponent.class)
                    .ifPresent(
                        draw -> {
                          draw.shaders().remove("sortComparison");
                          draw.shaders().remove("sortComparisonFill");
                        }));
  }

  private void addSortComparisonHighlight(int entityId) {
    if (entityId < 0) {
      return;
    }
    Game.findEntityById(entityId)
        .ifPresent(
            entity ->
                entity
                    .fetch(DrawComponent.class)
                    .ifPresent(
                        draw ->
                            draw.shaders()
                                .add("sortComparison", new OutlineShader(2, Color.CYAN))));
  }

  private void addSortFill(Entity entity) {
    entity
        .fetch(DrawComponent.class)
        .ifPresent(
            draw ->
                {
                  draw.shaders()
                      .add(
                          "sortComparisonFill",
                          new EnergyFillShader(
                                  sortValue(entity) / 100f,
                                  Color.CYAN,
                                  "objects/tech/CryoBox.png")
                              .animMagnitude(0));
                  EnergyGlow.addTo(draw);
                });
  }

  private int sortValue(Entity entity) {
    String name = entity.name();
    int separator = name.lastIndexOf('_');
    if (separator < 0) {
      return 0;
    }
    try {
      return Integer.parseInt(name.substring(separator + 1));
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }

  private int parseEntityId(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ignored) {
      return -1;
    }
  }

  /** Applies distinct client-side colors and blink pulses to the active conveyor comparison. */
  private void applyBeltSortMetadata(Map<String, String> metadata) {
    String leftPackage = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_BELT_LEFT_PACKAGE);
    String rightPackage =
        metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_BELT_RIGHT_PACKAGE);
    String scanner = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_BELT_SCANNER);
    String packageMetadata = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_BELT_PACKAGES);
    if (leftPackage == null || rightPackage == null || scanner == null || packageMetadata == null) {
      return;
    }

    int leftPackageId = parseEntityId(leftPackage);
    int rightPackageId = parseEntityId(rightPackage);
    int scannerId = parseEntityId(scanner);
    if (leftPackageId == lastBeltLeftPackage
        && rightPackageId == lastBeltRightPackage
        && scannerId == lastBeltScanner) {
      return;
    }

    resetTint(lastBeltLeftPackage);
    resetTint(lastBeltRightPackage);
    resetTint(lastBeltScanner);
    clearBeltSortHighlights();
    Map<Integer, Integer> packageValues = parseBeltPackageMetadata(packageMetadata);
    packageValues.forEach(this::addBeltPackageColor);

    if (leftPackageId >= 0 && rightPackageId >= 0) {
      addBeltHighlight(leftPackageId, "beltSortLeft", Color.YELLOW);
      addBeltHighlight(rightPackageId, "beltSortRight", Color.CYAN);
      addBeltHighlight(scannerId, "beltSortScanner", Color.WHITE);
      lastBeltLeftPackage = leftPackageId;
      lastBeltRightPackage = rightPackageId;
    }
    lastBeltScanner = scannerId;
  }

  /** Synchronizes the scanner lifecycle and persistent GPU fault state on every client. */
  private void applyModuleScanMetadata(Entity entity, Map<String, String> metadata) {
    if (!"module_scanner".equals(entity.name())) return;

    String scanRunningValue =
        metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_MODULE_SCAN_RUNNING);
    String scanFaultValue = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_MODULE_SCAN_FAULT);
    if (scanRunningValue != null) {
      ModuleScannerVisualState state =
          entity
              .fetch(ModuleScannerVisualState.class)
              .orElseGet(
                  () -> {
                    ModuleScannerVisualState created = new ModuleScannerVisualState();
                    entity.add(created);
                    return created;
                  });
      state.scanning(Boolean.parseBoolean(scanRunningValue));
    }

    if (scanFaultValue == null) return;
    boolean scanFault = Boolean.parseBoolean(scanFaultValue);
    if (scanFault == lastModuleScanFault) return;

    clearModuleFaultHighlight();
    if (scanFault) addModuleFaultHighlight();
    lastModuleScanFault = scanFault;
  }

  private void clearModuleFaultHighlight() {
    Game.levelEntities()
        .forEach(
            entity ->
                entity.fetch(DrawComponent.class)
                    .ifPresent(draw -> draw.shaders().remove("moduleScannerFault")));
  }

  private void addModuleFaultHighlight() {
    Game.levelEntities()
        .filter(entity -> "module_gpu".equals(entity.name()))
        .findFirst()
        .flatMap(entity -> entity.fetch(DrawComponent.class))
        .ifPresent(
            draw -> draw.shaders().add("moduleScannerFault", new OutlineShader(2, Color.RED)));
  }

  /** Applies the authoritative visual state of one 3x4 storage cell. */
  private void applyStorageCellMetadata(Entity entity, Map<String, String> metadata) {
    String state = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_STORAGE_CELL_STATE);
    if (state == null || !entity.name().startsWith("storage_matrix_cell_")) return;

    int value =
        parseEntityId(
            metadata.getOrDefault(
                SystemRecoveryEntitySpawnStrategy.METADATA_STORAGE_CELL_VALUE, "0"));
    int tint = storageCellTint(state, value);
    entity
        .fetch(PositionComponent.class)
        .ifPresent(
            position -> Game.tileAt(position.position()).ifPresent(tile -> tile.tintColor(tint)));
  }

  private int storageCellTint(String state, int value) {
    return switch (state) {
      case "active" -> 0x4D7EA8FF;
      case "target" -> 0xF0D248FF;
      case "filled" ->
          switch (value) {
            case 1 -> 0x42C8E6FF;
            case 2 -> 0xF0B84AFF;
            case 3 -> 0xD66CFFFF;
            default -> 0x4D7EA8FF;
          };
      default -> -1;
    };
  }

  private void clearBeltSortHighlights() {
    Game.levelEntities()
        .forEach(
            entity ->
                entity
                    .fetch(DrawComponent.class)
                    .ifPresent(
                        draw -> {
                          draw.shaders().remove("beltSortLeft");
                          draw.shaders().remove("beltSortRight");
                          draw.shaders().remove("beltSortScanner");
                          draw.shaders().remove("beltPackageColor");
                        }));
  }

  private void addBeltPackageColor(int entityId, int value) {
    Game.findEntityById(entityId)
        .ifPresent(
            entity ->
                entity
                    .fetch(DrawComponent.class)
                    .ifPresent(
                        draw -> {
                          draw.tintColor(0xFFFFFFFF);
                          draw.shaders()
                              .add(
                                  "beltPackageColor",
                                  new HueRemapShader(0.08f, beltPackageHue(value), 0.12f));
                        }));
  }

  /** Highlights exactly one active comparison pair with a thin, non-scheduled outline. */
  private void addBeltHighlight(int entityId, String shaderName, Color color) {
    if (entityId < 0) return;
    Game.findEntityById(entityId)
        .ifPresent(
            entity ->
                entity
                    .fetch(DrawComponent.class)
                    .ifPresent(
                        draw -> draw.shaders().add(shaderName, new OutlineShader(1, color))));
  }

  /** Returns the target hue used to distinguish the five package weights. */
  private float beltPackageHue(int value) {
    return switch (value) {
      case 15 -> 0.00f;
      case 20 -> 0.14f;
      case 30 -> 0.33f;
      case 40 -> 0.60f;
      case 60 -> 0.85f;
      default -> 0.08f;
    };
  }

  private Map<Integer, Integer> parseBeltPackageMetadata(String metadata) {
    Map<Integer, Integer> values = new HashMap<>();
    for (String entry : metadata.split(",")) {
      String[] parts = entry.split(":", 2);
      if (parts.length != 2) continue;
      try {
        values.put(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
      } catch (NumberFormatException ignored) {
        // Ignore malformed debug metadata and keep the remaining packages visible.
      }
    }
    return values;
  }

  private void resetTint(int entityId) {
    if (entityId < 0) return;
    Game.findEntityById(entityId)
        .flatMap(entity -> entity.fetch(DrawComponent.class))
        .ifPresent(draw -> draw.tintColor(0xFFFFFFFF));
  }

  private List<Integer> parseDigits(String value) {
    if (value.isBlank()) {
      return new ArrayList<>();
    }
    List<Integer> digits = new ArrayList<>();
    for (String digit : value.split(",")) {
      digits.add(Integer.parseInt(digit));
    }
    return digits;
  }

  /**
   * Creates a {@link CollideComponent} from metadata.
   *
   * @param metadata the metadata to parse
   * @return the reconstructed collider component, if metadata is present and valid
   */
  public static Optional<CollideComponent> collideComponentFromMetadata(
      Map<String, String> metadata) {
    return COLLIDE_SYNC.fromMetadata(metadata);
  }

  /**
   * Applies interactable metadata to a client-side entity.
   *
   * @param entity the target entity
   * @param metadata the metadata map
   */
  public static void applyInteractableMetadata(Entity entity, Map<String, String> metadata) {
    DoorLabelComponent.applyMetadata(entity, metadata);
    String interactable = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_INTERACTABLE);
    if (interactable == null) {
      return;
    }

    if (Boolean.parseBoolean(interactable)) {
      if (!entity.isPresent(InteractionComponent.class)) {
        entity.add(new InteractionComponent());
      }
    } else {
      entity.remove(InteractionComponent.class);
    }

    if (Boolean.parseBoolean(
        metadata.getOrDefault(
            SystemRecoveryEntitySpawnStrategy.METADATA_MODULE_SOCKET_ACTIVE, "false"))) {
      entity.add(
          new DrawComponent(new Animation(new SimpleIPath("objects/tech/Screen_info_1.png"))));
    }
  }

  private Map<String, String> snapshotMetadata(Entity entity) {
    Map<String, String> metadata = new HashMap<>();
    DoorLabelComponent.appendMetadata(entity, metadata);
    entity
        .fetch(QuestLogComponent.class)
        .ifPresent(questLog -> metadata.putAll(questLogMetadata(questLog)));
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
    entity.fetch(KeypadComponent.class).ifPresent(keypad -> appendKeypadMetadata(keypad, metadata));
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
    if ("module_scanner".equals(entity.name())) {
      metadata.put(
          SystemRecoveryEntitySpawnStrategy.METADATA_MODULE_SCAN_RUNNING,
          String.valueOf(SystemRecoveryLevel.scannerRunning()));
      metadata.put(
          SystemRecoveryEntitySpawnStrategy.METADATA_MODULE_SCAN_FAULT,
          String.valueOf(SystemRecoveryLevel.scannerFaultDetected()));
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

  /** Serializes the authoritative questlog for initial spawns and snapshots. */
  public static Map<String, String> questLogMetadata(QuestLogComponent questLog) {
    return Map.of(
        SystemRecoveryEntitySpawnStrategy.METADATA_TYPE,
        SystemRecoveryEntitySpawnStrategy.TYPE_QUESTLOG,
        SystemRecoveryEntitySpawnStrategy.METADATA_QUESTLOG_ENTRIES,
        serializeQuestLog(questLog));
  }

  /** Restores a questlog from synchronized System Recovery metadata. */
  public static Optional<QuestLogComponent> questLogFromMetadata(Map<String, String> metadata) {
    if (!SystemRecoveryEntitySpawnStrategy.TYPE_QUESTLOG.equals(
        metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_TYPE))) {
      return Optional.empty();
    }

    QuestLogComponent questLog = new QuestLogComponent();
    String serialized =
        metadata.getOrDefault(SystemRecoveryEntitySpawnStrategy.METADATA_QUESTLOG_ENTRIES, "");
    if (serialized.isBlank()) {
      return Optional.of(questLog);
    }

    for (String serializedEntry : serialized.split(";")) {
      String[] fields = serializedEntry.split(",", -1);
      if (fields.length != 6) {
        continue;
      }
      try {
        questLog.add(
            decode(serializedEntryField(fields, 0)),
            new QuestLogEntry(
                decode(serializedEntryField(fields, 1)),
                Integer.parseInt(fields[2]),
                Boolean.parseBoolean(fields[3]),
                decode(serializedEntryField(fields, 4)),
                Boolean.parseBoolean(fields[5])));
      } catch (IllegalArgumentException ignored) {
        // Ignore malformed entries and keep valid questlog entries available.
      }
    }
    return Optional.of(questLog);
  }

  private void applyQuestLogState(Entity entity, QuestLogComponent questLog) {
    entity.remove(QuestLogComponent.class);
    entity.add(questLog);
    QuestLogUtil.setClientQuestLog(entity);
  }

  private static String serializeQuestLog(QuestLogComponent questLog) {
    return questLog.getEntries().entrySet().stream()
        .flatMap(
            tab ->
                tab.getValue().stream().map(entry -> serializeQuestLogEntry(tab.getKey(), entry)))
        .collect(Collectors.joining(";"));
  }

  private static String serializeQuestLogEntry(String tab, QuestLogEntry entry) {
    return String.join(
        ",",
        encode(tab),
        encode(entry.text()),
        String.valueOf(entry.timestamp()),
        String.valueOf(entry.userCreated()),
        encode(entry.owner()),
        String.valueOf(entry.onlyForCreator()));
  }

  private static String encode(String value) {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }

  private static String decode(String value) {
    return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
  }

  private static String serializedEntryField(String[] fields, int index) {
    return fields[index];
  }

  private void appendKeypadMetadata(KeypadComponent keypad, Map<String, String> metadata) {
    metadata.put(SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD, "true");
    metadata.put(
        SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_CORRECT_DIGITS,
        digitsToString(keypad.correctDigits()));
    metadata.put(
        SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_ENTERED_DIGITS,
        digitsToString(keypad.enteredDigits()));
    metadata.put(
        SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_UNLOCKED,
        String.valueOf(keypad.isUnlocked()));
    metadata.put(
        SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_SHOW_DIGIT_COUNT,
        String.valueOf(keypad.showDigitCount()));
  }

  private String digitsToString(List<Integer> digits) {
    return digits.stream()
        .map(String::valueOf)
        .reduce((left, right) -> left + "," + right)
        .orElse("");
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
