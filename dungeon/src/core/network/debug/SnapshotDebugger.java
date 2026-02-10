package core.network.debug;

import static core.network.codec.NetworkCodec.serialize;

import contrib.item.ItemSnapshot;
import core.network.config.NetworkConfig;
import core.network.messages.s2c.DeltaSnapshotMessage;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.SnapshotMessage;
import core.sound.SoundSpec;
import java.io.IOException;
import java.util.*;

/**
 * Utility class for debugging and logging snapshot message sizes and contents.
 *
 * <p>Can be enabled or disabled at runtime. When enabled, logs detailed information about the size
 * of snapshot messages, including breakdowns by entity and field.
 */
public final class SnapshotDebugger {

  private static final int TOP_ENTITIES_TO_SHOW = 5;
  private static final int FIELD_BREAKDOWN_THRESHOLD = 200;

  private static boolean enabled = false;

  private SnapshotDebugger() {}

  /** Enables snapshot debugging and logging. */
  public static void enable() {
    enabled = true;
    System.out.println("[SnapshotDebugger] ENABLED");
  }

  /** Disables snapshot debugging and logging. */
  public static void disable() {
    enabled = false;
    System.out.println("[SnapshotDebugger] DISABLED");
  }

  /**
   * Returns whether snapshot debugging is currently enabled.
   *
   * @return true if enabled, false otherwise
   */
  public static boolean isEnabled() {
    return enabled;
  }

  /**
   * Logs detailed information about a delta snapshot message.
   *
   * @param delta the delta snapshot message to log
   */
  public static void logDelta(DeltaSnapshotMessage delta) {
    if (!enabled || delta == null) return;

    int totalSize = measureSize(delta);
    if (totalSize <= NetworkConfig.SAFE_UDP_MTU) return;

    int entityCount = delta.changedEntities() != null ? delta.changedEntities().size() : 0;
    int removedCount = delta.removedEntityIds() != null ? delta.removedEntityIds().size() : 0;

    System.out.println();
    System.out.println("======== DELTA SNAPSHOT DEBUG ========");
    System.out.printf(
        "Total: %d bytes %s%n", totalSize, "(EXCEEDS " + NetworkConfig.SAFE_UDP_MTU + " MTU!)");
    System.out.printf("Entities: %d changed, %d removed%n", entityCount, removedCount);
    System.out.printf("Base tick: %d, Server tick: %d%n", delta.baseTick(), delta.serverTick());

    if (delta.deltaLevelState() != null) {
      int levelSize = measureSize(delta.deltaLevelState());
      System.out.printf("Level state: %d bytes%n", levelSize);
    }

    if (entityCount > 0) {
      printEntityBreakdown(delta.changedEntities());
    }

    if (removedCount > 0) {
      System.out.printf("Removed entity IDs: %s%n", delta.removedEntityIds());
    }

    System.out.println("=======================================");
    System.out.println();
  }

  /**
   * Logs detailed information about a full snapshot message.
   *
   * @param snapshot the full snapshot message to log
   */
  public static void logFull(SnapshotMessage snapshot) {
    if (!enabled || snapshot == null) return;

    int totalSize = measureSize(snapshot);
    if (totalSize <= NetworkConfig.SAFE_UDP_MTU) return;
    int entityCount = snapshot.entities() != null ? snapshot.entities().size() : 0;

    System.out.println();
    System.out.println("======== FULL SNAPSHOT DEBUG ========");
    System.out.printf("Total: %d bytes%n", totalSize);
    System.out.printf("Entities: %d%n", entityCount);
    System.out.printf("Server tick: %d%n", snapshot.serverTick());

    if (snapshot.levelState() != null) {
      int levelSize = measureSize(snapshot.levelState());
      System.out.printf("Level state: %d bytes%n", levelSize);
    }

    if (entityCount > 0) {
      printEntityBreakdown(snapshot.entities());
    }

    System.out.println("======================================");
    System.out.println();
  }

  /**
   * Logs detailed information about a single entity state.
   *
   * @param state the entity state to log
   */
  public static void logEntity(EntityState state) {
    if (!enabled || state == null) return;

    int totalSize = measureSize(state);
    String name = state.entityName().orElse("unnamed");

    System.out.println();
    System.out.printf("======== ENTITY DEBUG: %s (id=%d) ========%n", name, state.entityId());
    System.out.printf("Total: %d bytes%n", totalSize);
    printFieldBreakdown(state);
    System.out.println("==========================================");
    System.out.println();
  }

  private static void printEntityBreakdown(List<EntityState> entities) {
    if (entities == null || entities.isEmpty()) return;

    List<EntitySizeInfo> sizes = new ArrayList<>();
    for (EntityState entity : entities) {
      int size = measureSize(entity);
      String name = entity.entityName().orElse("unnamed");
      sizes.add(new EntitySizeInfo(entity.entityId(), name, size, entity));
    }
    sizes.sort((a, b) -> Integer.compare(b.size, a.size));

    System.out.println();
    System.out.println("Top entities by size:");
    int shown = Math.min(TOP_ENTITIES_TO_SHOW, sizes.size());
    for (int i = 0; i < shown; i++) {
      EntitySizeInfo info = sizes.get(i);
      System.out.printf("  %d. %s (id=%d): %d bytes%n", i + 1, info.name, info.entityId, info.size);

      if (info.size >= FIELD_BREAKDOWN_THRESHOLD) {
        printFieldBreakdown(info.state);
      }
    }

    if (sizes.size() > TOP_ENTITIES_TO_SHOW) {
      int remaining = sizes.size() - TOP_ENTITIES_TO_SHOW;
      int remainingSize =
          sizes.subList(TOP_ENTITIES_TO_SHOW, sizes.size()).stream().mapToInt(e -> e.size).sum();
      System.out.printf("  ... and %d more entities (%d bytes total)%n", remaining, remainingSize);
    }
  }

  private static void printFieldBreakdown(EntityState state) {
    Map<String, Integer> fields = measureEntityFields(state);

    List<Map.Entry<String, Integer>> sorted = new ArrayList<>(fields.entrySet());
    sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

    for (Map.Entry<String, Integer> entry : sorted) {
      if (entry.getValue() > 0) {
        System.out.printf("     - %s: %d bytes%n", entry.getKey(), entry.getValue());

        if (entry.getValue() > 20) {
          switch (entry.getKey()) {
            case "inventory" -> printInventoryBreakdown(state);
            case "sounds" -> printSoundsBreakdown(state);
            case "health" -> printHealthBreakdown(state);
            case "mana" -> printManaBreakdown(state);
            case "stamina" -> printStaminaBreakdown(state);
          }
        }
      }
    }
  }

  private static Map<String, Integer> measureEntityFields(EntityState state) {
    Map<String, Integer> sizes = new LinkedHashMap<>();

    sizes.put("base (id+name+position)", measureBaseFields(state));
    sizes.put("inventory", measureInventory(state));
    sizes.put("sounds", measureSounds(state));
    sizes.put("health", measureHealth(state));
    sizes.put("mana", measureMana(state));
    sizes.put("stamina", measureStamina(state));
    sizes.put("animation (stateName+tint)", measureAnimation(state));

    return sizes;
  }

  private static int measureBaseFields(EntityState state) {
    int size = measureSize(state.entityId());
    if (state.entityName().isPresent()) {
      size += measureSize(state.entityName().get());
    }
    if (state.position().isPresent()) {
      size += measureSize(state.position().get());
    }
    if (state.viewDirection().isPresent()) {
      size += measureSize(state.viewDirection().get());
    }
    if (state.rotation().isPresent()) {
      size += measureSize(state.rotation().get());
    }
    return size;
  }

  private static int measureInventory(EntityState state) {
    if (state.inventory().isEmpty()) return 0;
    return measureSize(state.inventory().get());
  }

  private static int measureSounds(EntityState state) {
    if (state.sounds().isEmpty()) return 0;
    return measureSize(state.sounds().get());
  }

  private static int measureHealth(EntityState state) {
    int size = 0;
    if (state.currentHealth().isPresent()) {
      int currentSize = measureSize(state.currentHealth().get());
      System.out.printf(
          "       currentHealth (%d): %d bytes%n", state.currentHealth().get(), currentSize);
      size += currentSize;
    }
    if (state.maxHealth().isPresent()) {
      int maxSize = measureSize(state.maxHealth().get());
      System.out.printf("       maxHealth (%d): %d bytes%n", state.maxHealth().get(), maxSize);
      size += maxSize;
    }
    return size;
  }

  private static int measureMana(EntityState state) {
    int size = 0;
    if (state.currentMana().isPresent()) {
      int currentSize = measureSize(state.currentMana().get());
      System.out.printf(
          "       currentMana (%f): %d bytes%n", state.currentMana().get(), currentSize);
      size += currentSize;
    }
    if (state.maxMana().isPresent()) {
      int maxSize = measureSize(state.maxMana().get());
      System.out.printf("       maxMana (%f): %d bytes%n", state.maxMana().get(), maxSize);
      size += maxSize;
    }
    return size;
  }

  private static int measureStamina(EntityState state) {
    int size = 0;
    if (state.currentStamina().isPresent()) {
      int currentSize = measureSize(state.currentStamina().get());
      System.out.printf(
          "       currentStamina (%f): %d bytes%n", state.currentStamina().get(), currentSize);
      size += currentSize;
    }
    if (state.maxStamina().isPresent()) {
      int maxSize = measureSize(state.maxStamina().get());
      System.out.printf("       maxStamina (%f): %d bytes%n", state.maxStamina().get(), maxSize);
      size += maxSize;
    }
    return size;
  }

  private static int measureAnimation(EntityState state) {
    int size = 0;
    if (state.stateName().isPresent()) {
      size += measureSize(state.stateName().get());
    }
    if (state.tintColor().isPresent()) {
      size += measureSize(state.tintColor().get());
    }
    return size;
  }

  private static void printInventoryBreakdown(EntityState state) {
    if (state.inventory().isEmpty()) return;

    ItemSnapshot[] inventory = state.inventory().get();
    if (inventory.length == 0) return;

    System.out.printf("       Inventory (%d slots):%n", inventory.length);

    int shown = 0;
    for (int i = 0; i < inventory.length && shown < 5; i++) {
      if (inventory[i] != null) {
        ItemSnapshot item = inventory[i];
        int itemSize = measureSize(item);
        System.out.printf(
            "         [%d] %s x%d: %d bytes%n",
            i, item.itemClass(), item.stackSize() & 0xFF, itemSize);

        printItemSnapshotFieldBreakdown(item);
        shown++;
      }
    }

    int emptySlots = 0;
    int totalEmptySize = 0;
    for (ItemSnapshot item : inventory) {
      if (item == null) {
        emptySlots++;
        totalEmptySize += measureSize(item);
      }
    }

    if (emptySlots > 0) {
      System.out.printf(
          "         ... %d empty slots (%d bytes overhead)%n", emptySlots, totalEmptySize);
    }
  }

  private static void printItemSnapshotFieldBreakdown(ItemSnapshot item) {
    int itemClassSize = item.itemClass() != null ? measureSize(item.itemClass()) : 0;
    int stackSizeSize = measureSize(item.stackSize());

    System.out.printf("           - itemClass (%s): %d bytes%n", item.itemClass(), itemClassSize);
    System.out.printf(
        "           - stackSize (%d): %d bytes%n", item.stackSize() & 0xFF, stackSizeSize);
  }

  private static void printSoundsBreakdown(EntityState state) {
    if (state.sounds().isEmpty()) return;

    List<SoundSpec> sounds = state.sounds().get();
    if (sounds.isEmpty()) return;

    System.out.printf("       Sounds (%d total):%n", sounds.size());

    int shown = Math.min(3, sounds.size());
    for (int i = 0; i < shown; i++) {
      SoundSpec sound = sounds.get(i);
      int soundSize = measureSize(sound);
      System.out.printf(
          "         [%d] %s: %d bytes (volume: %.2f, pitch: %.2f, looping: %s)%n",
          i, sound.soundName(), soundSize, sound.baseVolume(), sound.pitch(), sound.looping());

      printSoundSpecFieldBreakdown(sound);
    }

    if (sounds.size() > shown) {
      int remaining = sounds.size() - shown;
      int remainingSize = 0;
      for (int i = shown; i < sounds.size(); i++) {
        remainingSize += measureSize(sounds.get(i));
      }
      System.out.printf(
          "         ... and %d more sounds (%d bytes total)%n", remaining, remainingSize);
    }
  }

  private static void printSoundSpecFieldBreakdown(SoundSpec sound) {
    int instanceIdSize = measureSize(sound.instanceId());
    int soundNameSize = sound.soundName() != null ? measureSize(sound.soundName()) : 0;
    int baseVolumeSize = measureSize(sound.baseVolume());
    int loopingSize = measureSize(sound.looping());
    int pitchSize = measureSize(sound.pitch());
    int panSize = measureSize(sound.pan());
    int maxDistanceSize = measureSize(sound.maxDistance());
    int attenuationFactorSize = measureSize(sound.attenuationFactor());

    System.out.printf("           - instanceId: %d bytes%n", instanceIdSize);
    System.out.printf("           - soundName (%s): %d bytes%n", sound.soundName(), soundNameSize);
    System.out.printf(
        "           - baseVolume (%.2f): %d bytes%n", sound.baseVolume(), baseVolumeSize);
    System.out.printf("           - looping (%s): %d bytes%n", sound.looping(), loopingSize);
    System.out.printf("           - pitch (%.2f): %d bytes%n", sound.pitch(), pitchSize);
    System.out.printf("           - pan (%.2f): %d bytes%n", sound.pan(), panSize);
    System.out.printf(
        "           - maxDistance (%.2f): %d bytes%n", sound.maxDistance(), maxDistanceSize);
    System.out.printf(
        "           - attenuationFactor (%.2f): %d bytes%n",
        sound.attenuationFactor(), attenuationFactorSize);
  }

  private static void printHealthBreakdown(EntityState state) {
    if (state.currentHealth().isPresent()) {
      System.out.printf(
          "       currentHealth: %d bytes (value: %d)%n",
          measureSize(state.currentHealth().get()), state.currentHealth().get());
    }
    if (state.maxHealth().isPresent()) {
      System.out.printf(
          "       maxHealth: %d bytes (value: %d)%n",
          measureSize(state.maxHealth().get()), state.maxHealth().get());
    }
  }

  private static void printManaBreakdown(EntityState state) {
    if (state.currentMana().isPresent()) {
      System.out.printf(
          "       currentMana: %d bytes (value: %f)%n",
          measureSize(state.currentMana().get()), state.currentMana().get());
    }
    if (state.maxMana().isPresent()) {
      System.out.printf(
          "       maxMana: %d bytes (value: %f)%n",
          measureSize(state.maxMana().get()), state.maxMana().get());
    }
  }

  private static void printStaminaBreakdown(EntityState state) {
    if (state.currentStamina().isPresent()) {
      System.out.printf(
          "       currentStamina: %d bytes (value: %f)%n",
          measureSize(state.currentStamina().get()), state.currentStamina().get());
    }
    if (state.maxStamina().isPresent()) {
      System.out.printf(
          "       maxStamina: %d bytes (value: %f)%n",
          measureSize(state.maxStamina().get()), state.maxStamina().get());
    }
  }

  private static int measureSize(Object obj) {
    if (obj == null) return 0;
    try {
      byte[] data = serialize(obj);
      return data.length;
    } catch (IOException e) {
      System.err.println("[SnapshotDebugger] Failed to measure size: " + e.getMessage());
      return -1;
    }
  }

  private record EntitySizeInfo(int entityId, String name, int size, EntityState state) {}
}
