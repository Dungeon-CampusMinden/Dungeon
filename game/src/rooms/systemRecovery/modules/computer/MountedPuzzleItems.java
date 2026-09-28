package rooms.systemRecovery.modules.computer;

import engine.Entity;
import engine.components.PlayerComponent;
import engine.game.PreRunConfiguration;
import feature.inventory.Item;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import rooms.systemRecovery.items.SearchProgramChipItem;
import rooms.systemRecovery.items.SortProgramStickItem;
import rooms.systemRecovery.items.SystemCoreAccessChipItem;

/** Tracks puzzle items temporarily removed from player inventory while mounted in a device. */
public final class MountedPuzzleItems {

  private static final Map<Integer, MountedItem> ITEMS_BY_PLAYER = new ConcurrentHashMap<>();

  private MountedPuzzleItems() {}

  /**
   * Records a puzzle item removed from the player's inventory and installed in a device.
   *
   * @param player owner of the item
   * @param item puzzle item currently installed in a device
   */
  public static void mount(Entity player, Item item) {
    if (player == null || !isPuzzleItem(item)) return;
    String playerName =
        player
            .fetch(PlayerComponent.class)
            .map(PlayerComponent::playerName)
            .filter(name -> name != null && !name.isBlank())
            .orElse(PreRunConfiguration.username());
    ITEMS_BY_PLAYER.put(player.id(), new MountedItem(player.id(), playerName, item));
  }

  /**
   * Removes the record only when it still refers to the same item instance.
   *
   * @param playerId runtime ID of the owning player
   * @param expectedItem item instance expected to be installed
   * @return whether the item record was removed
   */
  public static boolean unmount(int playerId, Item expectedItem) {
    MountedItem mounted = ITEMS_BY_PLAYER.get(playerId);
    return mounted != null
        && mounted.item() == expectedItem
        && ITEMS_BY_PLAYER.remove(playerId, mounted);
  }

  /** @return a stable snapshot of all currently mounted puzzle items */
  public static List<MountedItem> snapshot() {
    return List.copyOf(ITEMS_BY_PLAYER.values());
  }

  /** Drops all mounted-item records when a new level/run is initialized. */
  public static void clear() {
    ITEMS_BY_PLAYER.clear();
  }

  private static boolean isPuzzleItem(Item item) {
    return item instanceof SortProgramStickItem
        || item instanceof SearchProgramChipItem
        || item instanceof SystemCoreAccessChipItem;
  }

  /**
   * Item retained in a computer or puzzle machine; a checkpoint restores it to its owner.
   *
   * @param playerId owning player's runtime entity ID
   * @param playerName saved owner identity used to restore inventory
   * @param item item currently mounted in a device
   */
  public record MountedItem(int playerId, String playerName, Item item) {}
}
