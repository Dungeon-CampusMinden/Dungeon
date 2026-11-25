package rooms.morphco.level;

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
    MorphCoObjects.onInteract(
        "shift-clock", (self, player) -> MorphCoProps.note(player, "world.corridor.clock"));
    MorphCoObjects.onInteract(
        "plant-corridor",
        (self, player) -> {
          MorphCoProps.note(player, "world.f04.plant");
          level.seen(MorphCoState.PLANT_PREFIX, "corridor", player);
        });
    MorphCoObjects.onInteract(
        "sink", (self, player) -> MorphCoProps.note(player, "world.restroom.sink"));
    MorphCoObjects.onInteract(
        "toilet", (self, player) -> MorphCoProps.note(player, "world.restroom.toilet"));
    MorphCoObjects.onInteract(
        "mirror", (self, player) -> MorphCoProps.note(player, "world.restroom.mirror"));
  }
}
