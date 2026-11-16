package rooms.morphco.level;

import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.sound.SoundSpec;
import feature.components.InventoryComponent;
import feature.inventory.Item;
import java.util.List;
import java.util.function.Consumer;
import rooms.morphco.items.MorphemeTypesItem;
import rooms.morphco.ui.MorphCoDialogs;

/**
 * The volumes of "Morphemtypen und wo sie zu finden sind" that players carry.
 *
 * <p>Every player takes their own copy from the shelf. The room state remembers by player name
 * which of the books a player has received, so a loaded checkpoint hands them out again; the
 * handout replaces both volumes.
 */
final class MorphCoBooks {

  private MorphCoBooks() {}

  /**
   * Opens a volume from a shelf and gives it to the player, unless the player already carries it or
   * the handout.
   *
   * @param player reading player
   * @param volume volume on the shelf
   */
  static void take(Entity player, MorphemeTypesItem volume) {
    boolean taken =
        player
            .fetch(InventoryComponent.class)
            .filter(inventory -> !inventory.hasItem(volume.getClass()))
            .filter(inventory -> !inventory.hasItem(MorphemeTypesItem.Handout.class))
            .map(inventory -> inventory.add(volume))
            .orElse(false);
    if (!taken) {
      MorphCoDocuments.show(player, volume.document());
      return;
    }
    Game.audio().playGlobal(SoundSpec.builder("kenney_ui_drop_001").targets(player.id()));
    MorphCoProps.popupThen(
        player,
        "documents." + volume.document() + "-title",
        "items.volume-taken",
        () -> MorphCoDocuments.show(player, volume.document()));
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
        previous.andThen(
            item -> {
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
