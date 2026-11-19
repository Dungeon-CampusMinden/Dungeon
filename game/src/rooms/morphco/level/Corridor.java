package rooms.morphco.level;

import engine.Entity;
import java.util.function.BiConsumer;

/**
 * The corridor with its door signs, the shift clock and the restroom. The kitchen and the storage
 * room belong to the {@link SnackQuest}.
 */
final class Corridor {

  private static final String SIGN = "world.corridor.sign-title";

  private final MorphCoLevel level;

  Corridor(MorphCoLevel level) {
    this.level = level;
  }

  /** Creates the signs and objects. */
  void spawn() {
    sign("sign_f04", "world.corridor.sign-f04");
    sign("sign_a12", "world.corridor.sign-a12");
    sign("sign_restroom", "world.corridor.sign-restroom");
    sign("sign_control", "world.corridor.sign-control");
    on(
        "shift-clock",
        (self, player) ->
            MorphCoProps.popup(player, "world.corridor.clock-title", "world.corridor.clock"));
    on(
        "plant-corridor",
        (self, player) -> {
          MorphCoProps.popup(player, "world.f04.plant-title", "world.f04.plant");
          level.seen(MorphCoState.PLANT_PREFIX, "corridor", player);
        });
    on(
        "sink",
        (self, player) ->
            MorphCoProps.popup(player, "world.restroom.sink-title", "world.restroom.sink"));
    on(
        "toilet",
        (self, player) ->
            MorphCoProps.popup(player, "world.restroom.toilet-title", "world.restroom.toilet"));
    on(
        "mirror",
        (self, player) ->
            MorphCoProps.popup(player, "world.restroom.mirror-title", "world.restroom.mirror"));
  }

  private static void on(String name, BiConsumer<Entity, Entity> onInteract) {
    MorphCoObjects.on(name, entity -> MorphCoProps.interact(entity, onInteract));
  }

  private static void sign(String name, String textKey) {
    on(name, (self, player) -> MorphCoProps.popup(player, SIGN, textKey));
  }
}
