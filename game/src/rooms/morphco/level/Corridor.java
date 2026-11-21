package rooms.morphco.level;

import engine.Entity;
import java.util.function.BiConsumer;

/**
 * The corridor with the shift clock and the restroom. The kitchen and the storage room belong to
 * the {@link SnackQuest}. The other door signs only say what is written on them, so they are not
 * interactable.
 */
final class Corridor {

  private final MorphCoLevel level;

  Corridor(MorphCoLevel level) {
    this.level = level;
  }

  /** Creates the signs and objects. */
  void spawn() {
    on("shift-clock", (self, player) -> MorphCoProps.note(player, "world.corridor.clock"));
    on(
        "plant-corridor",
        (self, player) -> {
          MorphCoProps.note(player, "world.f04.plant");
          level.seen(MorphCoState.PLANT_PREFIX, "corridor", player);
        });
    on("sink", (self, player) -> MorphCoProps.note(player, "world.restroom.sink"));
    on("toilet", (self, player) -> MorphCoProps.note(player, "world.restroom.toilet"));
    on("mirror", (self, player) -> MorphCoProps.note(player, "world.restroom.mirror"));
  }

  private static void on(String name, BiConsumer<Entity, Entity> onInteract) {
    MorphCoObjects.on(name, entity -> MorphCoProps.interact(entity, onInteract));
  }
}
