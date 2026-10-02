package engine.network.messages.s2c;

import engine.network.messages.NetworkMessage;
import java.util.List;
import java.util.Objects;

/**
 * Server-to-client: the complete quest log as visible to the receiving player.
 *
 * <p>Sent reliably whenever the quest log changes and after a client finished its initial world
 * sync. Entries marked only for their creator are included only for that player.
 *
 * @param available whether the current level provides a quest log
 * @param entries visible quest log entries
 */
public record QuestLogStateMessage(boolean available, List<Entry> entries)
    implements NetworkMessage {

  /** Creates an immutable quest log state. */
  public QuestLogStateMessage {
    entries = List.copyOf(Objects.requireNonNull(entries, "entries"));
  }

  /**
   * One quest log entry.
   *
   * @param tab tab containing the entry
   * @param text entry text
   * @param timestamp game tick at which the entry was created
   * @param userCreated whether a player wrote the entry
   * @param owner owner or creator of the entry
   * @param onlyForCreator whether only the creator may see the entry
   */
  public record Entry(
      String tab,
      String text,
      int timestamp,
      boolean userCreated,
      String owner,
      boolean onlyForCreator) {}
}
