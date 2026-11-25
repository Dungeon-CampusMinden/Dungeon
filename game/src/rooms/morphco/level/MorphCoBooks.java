package rooms.morphco.level;

import engine.Entity;
import engine.components.PlayerComponent;
import engine.sound.Sounds;
import feature.components.InventoryComponent;
import feature.inventory.Item;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import rooms.morphco.items.MorphemeTypesItem;
import rooms.morphco.ui.MorphCoDialogs;
import rooms.morphco.util.MorphCoSounds;

/**
 * The volumes of "Morphemtypen und wo sie zu finden sind" that players carry.
 *
 * <p>Every player takes their own copy from the shelf. The room state remembers by player name
 * which of the books a player has received, so a loaded checkpoint hands them out again. Taking the
 * second volume binds both into the handout, which replaces them.
 */
final class MorphCoBooks {

  private MorphCoBooks() {}

  /**
   * Opens a volume from a shelf and gives it to the player, unless the player already carries it or
   * the handout. If the player carries the other volume, both are bound into the handout.
   *
   * @param player reading player
   * @param volume volume on the shelf
   */
  static void take(Entity player, MorphemeTypesItem volume) {
    Optional<MorphemeTypesItem> received =
        player.fetch(InventoryComponent.class).flatMap(inventory -> receive(inventory, volume));
    if (received.isEmpty()) {
      MorphCoDocuments.show(player, volume.document());
      return;
    }
    MorphemeTypesItem book = received.get();
    Sounds.playFor(MorphCoSounds.TAKE, player);
    MorphCoProps.popupThen(
        player,
        "documents." + book.document() + "-title",
        book instanceof MorphemeTypesItem.Handout ? "items.volumes-bound" : "items.volume-taken",
        () -> MorphCoDocuments.show(player, book.document()));
  }

  /**
   * Adds a volume to an inventory, or replaces the other volume there with the handout.
   *
   * @param inventory inventory of the player
   * @param volume volume on the shelf
   * @return the added book, or empty if the inventory already holds the volume or the handout or is
   *     full
   */
  static Optional<MorphemeTypesItem> receive(
      InventoryComponent inventory, MorphemeTypesItem volume) {
    if (inventory.hasItem(volume.getClass())
        || inventory.hasItem(MorphemeTypesItem.Handout.class)) {
      return Optional.empty();
    }
    Optional<Item> other = inventory.itemOfClass(MorphemeTypesItem.class);
    other.ifPresent(inventory::remove);
    MorphemeTypesItem book = other.isPresent() ? new MorphemeTypesItem.Handout() : volume;
    return inventory.add(book) ? Optional.of(book) : Optional.empty();
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
          if (item instanceof MorphemeTypesItem book
              && level.observe(flag(book.document(), name))) {
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
    if (state.has(flag(MorphCoDialogs.HANDOUT, name))) {
      inventory.add(new MorphemeTypesItem.Handout());
      return;
    }
    for (MorphemeTypesItem volume :
        List.of(new MorphemeTypesItem.Part1(), new MorphemeTypesItem.Part2())) {
      if (state.has(flag(volume.document(), name))) inventory.add(volume);
    }
  }

  /**
   * Checks whether any player has put the handout together.
   *
   * @param state room state
   * @return whether a player has received the handout
   */
  static boolean anyHandout(MorphCoState state) {
    return state.count(flag(MorphCoDialogs.HANDOUT, "")) > 0;
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
