package contrib.hud.inventory;

import core.Entity;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for managing and tracking the state of a player's inventory dialog.
 *
 * <p>This class provides methods to determine if the dialog is open, update its state,
 * and clear the stored state for a specific player.
 *
 * <p>The state is identified and stored using a mapping of player IDs to their dialog
 * open status.
 *
 * <p>This class is thread-safe.
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
