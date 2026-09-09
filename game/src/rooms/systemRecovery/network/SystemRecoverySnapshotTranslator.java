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
import engine.utils.components.draw.TextureMap;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.draw.shader.EnergyFillShader;
import engine.utils.components.draw.shader.OutlineShader;
import engine.utils.components.path.SimpleIPath;
import feature.collision.CollideSync;
import feature.components.CollideComponent;
import feature.interaction.InteractionComponent;
import feature.interaction.keypad.KeypadComponent;
import feature.skills.SkillTools;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.modules.display.DisplayTextComponent;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;

/** Snapshot translator for metadata-backed System Recovery components. */
public final class SystemRecoverySnapshotTranslator implements SnapshotTranslator {

  private static final CollideSync COLLIDE_SYNC =
      CollideSync.withPrefix(SystemRecoveryEntitySpawnStrategy.METADATA_COLLIDER_PREFIX);

  private final SnapshotTranslator delegate = new DefaultSnapshotTranslator();
  private int lastBeltLeftPackage = -1;
  private int lastBeltRightPackage = -1;
  private int lastBeltScanner = -1;

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
                applySortComparisonMetadata(metadata.orElseThrow());
                applyBeltSortMetadata(metadata.orElseThrow());
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

  /** Reproduces the server-selected comparison highlight on every client. */
  private void applySortComparisonMetadata(Map<String, String> metadata) {
    String leftValue = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_SORT_LEFT_ENTITY);
    String rightValue = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_SORT_RIGHT_ENTITY);
    if (leftValue == null || rightValue == null) {
      return;
    }

    clearSortComparisonHighlights();
    Game.levelEntities()
        .filter(entity -> entity.name().startsWith("sort_data_"))
        .forEach(this::addSortFill);
    addSortComparisonHighlight(parseEntityId(leftValue));
    addSortComparisonHighlight(parseEntityId(rightValue));
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
                draw.shaders()
                    .add(
                        "sortComparisonFill",
                        new EnergyFillShader(
                                sortValue(entity) / 100f,
                                Color.CYAN,
                                TextureMap.instance()
                                    .textureAt(new SimpleIPath("objects/tech/CryoBox.png")))
                            .animMagnitude(0)));
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
    addBeltPackageHighlight(leftPackageId, "beltSortLeft", Color.CYAN);
    addBeltPackageHighlight(rightPackageId, "beltSortRight", Color.MAGENTA);
    addBeltScannerHighlight(scannerId, "beltSortScanner", Color.CYAN, 0x00FFFFFF);

    if (leftPackageId >= 0 && rightPackageId >= 0) {
      blinkEntity(scannerId, 0x00FFFFFF);
      blinkEntity(leftPackageId, beltPackageTint(packageValues.getOrDefault(leftPackageId, -1)));
      blinkEntity(rightPackageId, beltPackageTint(rightPackageId, packageValues));
      lastBeltLeftPackage = leftPackageId;
      lastBeltRightPackage = rightPackageId;
    }
    lastBeltScanner = scannerId;
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

  private void addBeltPackageHighlight(int entityId, String shaderName, Color color) {
    if (entityId < 0) return;
    Game.findEntityById(entityId)
        .ifPresent(
            entity ->
                entity
                    .fetch(DrawComponent.class)
                    .ifPresent(
                        draw -> draw.shaders().add(shaderName, new OutlineShader(3, color))));
  }

  private void addBeltScannerHighlight(int entityId, String shaderName, Color color, int tint) {
    if (entityId < 0) return;
    Game.findEntityById(entityId)
        .ifPresent(
            entity ->
                entity
                    .fetch(DrawComponent.class)
                    .ifPresent(
                        draw -> {
                          draw.tintColor(tint);
                          draw.shaders().add(shaderName, new OutlineShader(3, color));
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
                          Color color = beltPackageColor(value);
                          draw.tintColor(beltPackageTint(value));
                          draw.shaders().add("beltPackageColor", new OutlineShader(2, color));
                        }));
  }

  private Color beltPackageColor(int value) {
    return switch (value) {
      case 15 -> Color.RED;
      case 20 -> Color.YELLOW;
      case 30 -> Color.GREEN;
      case 40 -> Color.BLUE;
      case 60 -> Color.MAGENTA;
      default -> Color.WHITE;
    };
  }

  private int beltPackageTint(int entityId, Map<Integer, Integer> packageValues) {
    return beltPackageTint(packageValues.getOrDefault(entityId, -1));
  }

  private int beltPackageTint(int value) {
    return switch (value) {
      case 15 -> 0xFF3333FF;
      case 20 -> 0xFFFFDDFF;
      case 30 -> 0x33CC66FF;
      case 40 -> 0x3399FFFF;
      case 60 -> 0xCC66FFFF;
      default -> 0xFFFFFFFF;
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

  private void blinkEntity(int entityId, int tint) {
    if (entityId < 0) return;
    Game.findEntityById(entityId).ifPresent(entity -> SkillTools.blink(entity, tint, 600, 3));
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
    COLLIDE_SYNC.appendMetadata(entity, metadata);
    return metadata;
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
