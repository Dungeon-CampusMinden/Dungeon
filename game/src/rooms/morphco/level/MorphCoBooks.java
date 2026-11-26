package rooms.morphco.level;

import engine.Entity;
import engine.components.PlayerComponent;
import engine.sound.Sounds;
import feature.components.InventoryComponent;
import feature.inventory.Item;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import rooms.morphco.items.MorphCoBookItem;
import rooms.morphco.ui.MorphCoDialogs;
import rooms.morphco.util.MorphCoSounds;

/**
 * The books that players carry: the manual of the inflection machine and the volumes of
 * "Morphemtypen und wo sie zu finden sind".
 *
 * <p>Every player takes their own copy from the shelf. The room state remembers by player name
 * which of the books a player has received, so a loaded checkpoint hands them out again. Both
 * volumes bind into the handout; once the manual joins them, everything binds into the collected
 * edition, which replaces the other books.
 */
final class MorphCoBooks {

  private static final Set<String> VOLUMES = Set.of(MorphCoDialogs.MORPH_1, MorphCoDialogs.MORPH_2);

  private MorphCoBooks() {}

  /**
   * Opens a book from a shelf and gives it to the player, unless the player already carries its
   * text. If the book completes both volumes, the player's books are bound into one.
   *
   * @param player reading player
   * @param book book on the shelf
   */
  static void take(Entity player, MorphCoBookItem book) {
    Optional<MorphCoBookItem> received =
        player.fetch(InventoryComponent.class).flatMap(inventory -> receive(inventory, book));
    if (received.isEmpty()) {
      MorphCoDocuments.show(player, book.document());
      return;
    }
    MorphCoBookItem carried = received.get();
    Sounds.playFor(MorphCoSounds.TAKE, player);
    MorphCoProps.popupThen(
        player,
        "documents." + carried.document() + "-title",
        switch (carried) {
          case MorphCoBookItem.Collected collected -> "items.collected-bound";
          case MorphCoBookItem.Handout handout -> "items.volumes-bound";
          default -> "items.volume-taken";
        },
        () -> MorphCoDocuments.show(player, carried.document()));
  }

  /**
   * Adds a book to an inventory. Once the inventory holds both volumes, its books are replaced by
   * the handout, or by the collected edition if the manual is among them.
   *
   * @param inventory inventory of the player
   * @param book book from the shelf or of a checkpoint
   * @return the added book, or empty if the inventory already holds its text or is full
   */
  static Optional<MorphCoBookItem> receive(InventoryComponent inventory, MorphCoBookItem book) {
    List<Item> carried = inventory.items(MorphCoBookItem.class);
    Set<String> texts = new HashSet<>();
    carried.forEach(item -> texts.addAll(((MorphCoBookItem) item).contents()));
    if (texts.containsAll(book.contents())) return Optional.empty();
    texts.addAll(book.contents());
    MorphCoBookItem added = book;
    if (texts.containsAll(VOLUMES)) {
      added =
          texts.contains(MorphCoDialogs.HANDBOOK)
              ? new MorphCoBookItem.Collected()
              : new MorphCoBookItem.Handout();
      carried.forEach(inventory::remove);
    }
    return inventory.add(added) ? Optional.of(added) : Optional.empty();
  }

  /**
   * Hands a joining player the books of the checkpoint and records every book the player receives
   * from now on.
   *
   * @param level room level
   * @param player joining player
   */
  static void carry(MorphCoLevel level, Entity player) {
    String name = player.fetch(PlayerComponent.class).orElseThrow().playerName();
    InventoryComponent inventory = player.fetch(InventoryComponent.class).orElseThrow();
    restore(level.state(), name, inventory);
    Consumer<Item> previous = inventory.onItemAdded();
    inventory.onItemAdded(
        previous.andThen(item -> {
          if (item instanceof MorphCoBookItem book && level.observe(flag(book.document(), name))) {
            level.save();
          }
        }));
  }

  /**
   * Adds the books that the state records for a player.
   *
   * @param state room state, possibly restored from a checkpoint
   * @param name player name
   * @param inventory inventory of the player
   */
  static void restore(MorphCoState state, String name, InventoryComponent inventory) {
    for (MorphCoBookItem book :
        List.of(
            new MorphCoBookItem.Collected(),
            new MorphCoBookItem.Handout(),
            new MorphCoBookItem.Manual(),
            new MorphCoBookItem.Part1(),
            new MorphCoBookItem.Part2())) {
      if (state.has(flag(book.document(), name))) receive(inventory, book);
    }
  }

  /**
   * Checks whether any player has put the handout together, on its own or in the collected edition.
   *
   * @param state room state
   * @return whether a player has received the handout or the collected edition
   */
  static boolean anyHandout(MorphCoState state) {
    return state.count(flag(MorphCoDialogs.HANDOUT, "")) > 0
        || state.count(flag(MorphCoDialogs.COLLECTED, "")) > 0;
  }

  /**
   * Returns the flag that records a book of a player.
   *
   * @param document document of the book, see {@link MorphCoDialogs#MORPH_1}
   * @param name player name
   * @return flag below {@link MorphCoState#BOOK_ITEM_PREFIX}
   */
  static String flag(String document, String name) {
    return MorphCoState.BOOK_ITEM_PREFIX + document + ":" + name;
  }
}
