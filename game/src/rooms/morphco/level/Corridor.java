package rooms.morphco.level;

import engine.Entity;
import feature.entities.deco.Deco;

/** The corridor with its door signs, the locked kitchen and storage room and the restroom. */
final class Corridor {

  private static final String SIGN = "world.corridor.sign-title";

  private final MorphCoLevel level;

  Corridor(MorphCoLevel level) {
    this.level = level;
  }

  /** Creates the signs and objects and locks the kitchen and the storage room. */
  void spawn() {
    level.door("door_kitchen").close();
    level.door("door_storage").close();

    sign("sign_f04", "sign_f04.png", 0.94f, "world.corridor.sign-f04");
    sign("sign_a12", "sign_a12.png", 0.94f, "world.corridor.sign-a12");
    sign("sign_restroom", "sign_wc.png", 0.94f, "world.corridor.sign-restroom");
    sign("sign_control", "sign_control.png", 0.81f, "world.corridor.sign-control");
    MorphCoProps.texture(
        "sign_kitchen",
        level.point("sign_kitchen"),
        MorphCoProps.ART + "sign_kitchen.png",
        0.81f,
        null,
        (self, player) -> locked(player, "world.corridor.kitchen"));
    MorphCoProps.texture(
        "sign_storage",
        level.point("sign_storage"),
        MorphCoProps.ART + "sign_storage.png",
        0.81f,
        null,
        (self, player) -> locked(player, "world.corridor.storage"));
    MorphCoProps.deco(
        "plant-corridor",
        level.point("plant_corridor"),
        Deco.PottedTree,
        (self, player) -> {
          MorphCoProps.popup(player, "world.f04.plant-title", "world.f04.plant");
          level.seen(MorphCoState.PLANT_PREFIX, "corridor", player);
        });

    MorphCoProps.deco(
        "sink",
        level.point("sink"),
        Deco.Sink,
        (self, player) ->
            MorphCoProps.popup(player, "world.restroom.sink-title", "world.restroom.sink"));
    MorphCoProps.deco(
        "mirror",
        level.point("mirror"),
        Deco.LabWindowSmall,
        (self, player) ->
            MorphCoProps.popup(player, "world.restroom.mirror-title", "world.restroom.mirror"));
  }

  private void sign(String point, String art, float width, String textKey) {
    MorphCoProps.texture(
        point,
        level.point(point),
        MorphCoProps.ART + art,
        width,
        null,
        (self, player) -> MorphCoProps.popup(player, SIGN, textKey));
  }

  private static void locked(Entity player, String textKey) {
    MorphCoProps.popup(player, "world.corridor.door-locked-title", textKey);
  }
}
