package rooms.morphco.level;

import engine.utils.Rectangle;
import engine.utils.logging.DungeonLogger;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Furniture drawn with the room's own art, placed at named points of the level file.
 *
 * <p>A point named {@code deco_<kind>_<number>} (for example {@code deco_server_2}) gets the sprite
 * of that kind, so the furniture can be moved, added and removed in the level editor without
 * touching the code.
 */
final class MorphCoDecor {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(MorphCoDecor.class);
  private static final Pattern POINT = Pattern.compile("deco_([a-z]+)_\\d+");

  /**
   * A kind of furniture.
   *
   * @param texture file inside the room's art folder
   * @param size length of the texture's shorter side in tiles (16 pixels per tile)
   * @param collider solid foot of the object, or {@code null} for an object the player walks past
   */
  private record Kind(String texture, float size, Rectangle collider) {}

  private static final Map<String, Kind> KINDS =
      Map.of(
          "server", new Kind("server.png", 15 / 16f, new Rectangle(0.9f, 0.6f, 0.02f, 0f)),
          "lockers", new Kind("lockers.png", 29 / 16f, new Rectangle(1.9f, 0.7f, 0.05f, 0f)),
          "chair", new Kind("chair.png", 14 / 16f, null),
          "table", new Kind("table.png", 19 / 16f, new Rectangle(2.4f, 0.5f, 0.05f, 0f)),
          "cabinet", new Kind("cabinet.png", 22 / 16f, new Rectangle(1.3f, 0.6f, 0.03f, 0f)),
          "tank", new Kind("tank.png", 18 / 16f, new Rectangle(1f, 0.6f, 0.06f, 0f)),
          "cart", new Kind("cart.png", 18 / 16f, new Rectangle(1f, 0.5f, 0.06f, 0f)));

  private MorphCoDecor() {}

  /**
   * Returns the art file of a furniture point.
   *
   * @param pointName name of a named point
   * @return file inside the room's art folder, or empty if the point is no known furniture
   */
  static Optional<String> texture(String pointName) {
    Matcher matcher = POINT.matcher(pointName);
    if (!matcher.matches()) return Optional.empty();
    return Optional.ofNullable(KINDS.get(matcher.group(1))).map(Kind::texture);
  }

  /**
   * Adds the furniture of all {@code deco_} points.
   *
   * @param level the level with the named points
   */
  static void spawn(MorphCoLevel level) {
    level
        .namedPoints()
        .forEach(
            (name, position) -> {
              Matcher matcher = POINT.matcher(name);
              if (!matcher.matches()) return;
              Kind kind = KINDS.get(matcher.group(1));
              if (kind == null) {
                LOGGER.warn("Unknown furniture in point {}", name);
                return;
              }
              MorphCoProps.texture(
                  name,
                  position,
                  MorphCoProps.ART + kind.texture(),
                  kind.size(),
                  kind.collider(),
                  null);
            });
  }
}
