package hint;

import core.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * A component that stores all {@link Hint} objects collected by a player.
 *
 * <p>The {@code HintStorage} acts as a container that can be attached to an entity to track which
 * hints the player has discovered during gameplay. These stored hints are the ones displayed in the
 * {@link HintLogDialog} when the player opens the hint log dialog.
 *
 * <p>The class ensures that duplicate hints are not stored and provides methods for adding,
 * removing, clearing, and retrieving the list of collected hints.
 */
public class HintLogComponent implements Component {

  /** The list of collected hints. */
  List<Hint> hints;

  /** Creates an empty hint storage. */
  public HintLogComponent() {
    hints = new ArrayList<>();
  }

  /**
   * Adds a hint to the storage if it is not already present.
   *
   * @param hint the {@link Hint} to add
   * @return {@code true} if the hint was successfully added, {@code false} if it was already in the
   *     storage
   */
  public boolean addHint(Hint hint) {
    if (hints.contains(hint)) return false;
    return hints.add(hint);
  }

  /**
   * Returns a copy of the list of collected hints.
   *
   * <p>This ensures the internal list cannot be modified directly.
   *
   * @return a new {@link List} containing all collected hints
   */
  public List<Hint> hints() {
    return new ArrayList<>(hints);
  }

  /** Removes all collected hints from the storage. */
  public void clear() {
    hints.clear();
  }

  /**
   * Removes a specific hint from the storage.
   *
   * @param hint the {@link Hint} to remove
   * @return {@code true} if the hint was found and removed, {@code false} otherwise
   */
  public boolean removeHint(Hint hint) {
    return hints.remove(hint);
  }

  /**
   * Removes all hints from this storage that are contained in the given array.
   *
   * <p>Converts the array to a list and removes all matching hints from the internal storage in a
   * single operation.
   *
   * @param hints an array of {@link Hint} objects to remove
   * @return {@code true} if at least one hint was removed from the storage, {@code false} if none
   *     of the hints were present
   */
  public boolean removeHint(Hint[] hints) {
    return this.hints.removeAll(List.of(hints));
  }
}
