package contrib.inventory;

import core.Entity;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the UI state for player inventories, such as whether the inventory dialog for a specific
 * player is open or closed.
 *
 * <p>This class provides static methods to interact with player inventory states, including
 * checking, updating, and clearing the inventory open state for players.
 *
 * <p>The state is maintained internally in a thread-safe manner.
 */
public final class InventoryDialogState {
  private static final Map<Integer, Boolean> OPEN_BY_PLAYER_ID = new ConcurrentHashMap<>();

  private InventoryDialogState() {}

  /**
   * Returns whether the player's inventory dialog is currently considered open.
   *
   * @param player the player entity
   * @return true if open, false otherwise
   */
  public static boolean isOpen(Entity player) {
    return player != null && OPEN_BY_PLAYER_ID.getOrDefault(player.id(), false);
  }

  /**
   * Updates the open state for the given player.
   *
   * @param player the player entity
   * @param open true if open, false if closed
   */
  public static void setOpen(Entity player, boolean open) {
    if (player == null) {
      return;
    }

    if (open) {
      OPEN_BY_PLAYER_ID.put(player.id(), true);
    } else {
      OPEN_BY_PLAYER_ID.remove(player.id());
    }
  }

  /**
   * Clears the stored state for the given player.
   *
   * @param player the player entity
   */
  public static void clear(Entity player) {
    if (player != null) {
      OPEN_BY_PLAYER_ID.remove(player.id());
    }
  }
}
