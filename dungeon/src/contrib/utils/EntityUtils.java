package contrib.utils;

import contrib.components.CollideComponent;
import contrib.entities.LeverFactory;
import contrib.entities.SignFactory;
import contrib.utils.components.collide.Collider;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * EntityUtils is a utility class that provides methods for spawning entities in the game. It
 * contains methods for spawning signs, levers, and other entities. It also contains methods for
 * teleporting entities to specific positions and retrieving the position of the player in the game.
 */
public class EntityUtils {

  /**
   * This method is used to spawn a sign entity in the game at a given position. It uses the
   * SignFactory class to create a new sign with the provided text and title. The sign is then added
   * to the game. If an IOException occurs during the creation of the sign, it is caught and a
   * RuntimeException is thrown.
   *
   * @param text The text to be displayed on the sign.
   * @param title The title of the sign.
   * @param pos The position where the sign should be spawned.
   * @param onInteract The action to perform when the sign is interacted with. (sign, whoTriggered)
   * @return The spawned sign entity.
   * @throws RuntimeException if an error occurs while spawning the sign.
   * @see contrib.entities.SignFactory#createSign(String, String, Point, BiConsumer) createSign
   */
  public static Entity spawnSign(
      String text, String title, Point pos, BiConsumer<Entity, Entity> onInteract) {
    Entity sign = SignFactory.createSign(text, title, pos, onInteract);
    Game.add(sign);
    return sign;
  }

  /**
   * This method is used to spawn a sign entity in the game at a given position. It uses the
   * SignFactory class to create a new sign with the provided text and title. The sign is then added
   * to the game. If an IOException occurs during the creation of the sign, it is caught and a
   * RuntimeException is thrown.
   *
   * @param text The text to be displayed on the sign.
   * @param title The title of the sign.
   * @param pos The position where the sign should be spawned.
   * @return The spawned sign entity.
   * @throws RuntimeException if an error occurs while spawning the sign.
   * @see #spawnSign(String, String, Point, BiConsumer) spawnSign
   */
  public static Entity spawnSign(String text, String title, Point pos) {
    return spawnSign(text, title, pos, (e, e2) -> {});
  }

  /**
   * This method is used to spawn a Lever entity in the game at a given position. It uses the
   * LeverFactory class to create a new Lever with the provided onInteract action. The Lever is then
   * added to the game. If an IOException occurs during the creation of the Lever, it is caught and
   * a RuntimeException is thrown.
   *
   * @param pos The position where the Lever should be spawned.
   * @param onInteract The action to perform when the Lever is interacted with. (isOn, lever,
   *     whoTriggered)
   * @return The spawned Lever entity.
   * @throws RuntimeException if an error occurs while spawning the Lever.
   * @see LeverFactory#createLever(Point, ICommand) createLever
   */
  public static Entity spawnLever(Point pos, ICommand onInteract) {
    Entity lever = LeverFactory.createLever(pos, onInteract);
    Game.add(lever);
    return lever;
  }

  /**
   * Teleports the player to a specified point in the game.
   *
   * <p>This method retrieves the player entity from the game and calls the teleportEntityTo method
   * to change the player's position. If the player entity is not present (which can happen if the
   * player has fallen into a pit), the method does nothing.
   *
   * @param point The point to which the player should be teleported.
   */
  public static void teleportPlayerTo(Point point) {
    Entity player = Game.player().orElse(null);
    if (player == null) {
      return;
    }
    teleportEntityTo(player, point);
  }

  /**
   * Teleports an entity to a specified point in the game.
   *
   * <p>This method changes the position of the given entity to the specified point. It does this by
   * fetching the PositionComponent of the entity and setting its position to the given point. If
   * the entity does not have a PositionComponent, a MissingComponentException is thrown.
   *
   * @param entity The entity to be teleported.
   * @param point The point to which the entity should be teleported.
   * @throws MissingComponentException if the entity does not have a PositionComponent.
   */
  public static void teleportEntityTo(Entity entity, Point point) {
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    pc.position(point);
  }

  /**
   * Retrieves the current position of the player in the game.
   *
   * <p>This method retrieves the player entity from the game. If the player entity is not present
   * (which can happen if the player has fallen into a pit), the method returns null.
   *
   * @return The current position of the player, or a null value if the player is not present.
   */
  public static Point getPlayerPosition() {
    // TODO: SMELL!
    // we really shouldn't return `null` if no player was found, but `Optional.empty()` instead!
    return Game.player()
        .map(
            e ->
                e.fetch(PositionComponent.class)
                    .orElseThrow(() -> MissingComponentException.build(e, PositionComponent.class))
                    .position())
        .orElse(null);
  }

  /**
   * Retrieves the current coordinates of the player in the game.
   *
   * <p>This method retrieves the player entity from the game. If the player entity is not present
   * (which can happen if the player has fallen into a pit), the method returns null.
   *
   * @return The current coordinates of the player, or null if the player is not present.
   */
  public static Coordinate getPlayerCoordinate() {
    Point playerPos = getPlayerPosition();
    // TODO: SMELL!
    // we really shouldn't return `null` if no player was found, but `Optional.empty()` instead!
    return playerPos == null ? null : playerPos.toCoordinate();
  }

  /**
   * Retrieves the direction the player is facing.
   *
   * @return the direction the player is facing, or null if there is no player.
   */
  public static Direction getPlayerViewDirection() {
    // TODO: SMELL!
    // we really shouldn't return `null` if no player was found, but `Optional.empty()` instead!
    // this approach has been chosen solely to ensure a symmetric modelling to the existing methods.
    // when refactoring, *all* these methods here should be changed to return `Optional<>`.
    return Game.player().map(EntityUtils::getViewDirection).orElse(null);
  }

  /**
   * Returns the direction the entity is facing.
   *
   * @param entity the entity to get the direction of
   * @return the direction the entity is facing
   */
  public static Direction getViewDirection(Entity entity) {
    return entity
        .fetch(PositionComponent.class)
        .map(PositionComponent::viewDirection)
        .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
  }

  /**
   * Gets the position of an entity. If the entity has a CollideComponent, gets the center position
   * of the collider. If not, but the entity has a DrawComponent, gets the center position of the
   * drawn sprite. If neither component is present, returns the position from the PositionComponent.
   *
   * @param entity The entity to get the position of.
   * @return The position of the entity.
   */
  public static Point getPosition(Entity entity) {
    PositionComponent pc = entity.fetch(PositionComponent.class).orElseThrow();
    Optional<CollideComponent> cco = entity.fetch(CollideComponent.class);
    Optional<DrawComponent> dco = entity.fetch(DrawComponent.class);

    if (cco.isPresent()) {
      return cco.get().collider().absoluteCenter();
    } else if (dco.isPresent()) {
      DrawComponent dc = dco.get();
      return pc.position().translate(dc.getWidth() / 2, dc.getHeight() / 2);
    } else {
      return pc.position();
    }
  }

  /**
   * Returns the distance between two entities based on their positions.
   *
   * <p>The distance is computed using the Euclidean distance between the positions returned by
   * {@link EntityUtils#getPosition(Entity)}.
   *
   * @param entity the first entity
   * @param who the second entity
   * @return the Euclidean distance between the two entities
   */
  public static double getDistance(Entity entity, Entity who) {
    return EntityUtils.getPosition(entity).distance(EntityUtils.getPosition(who));
  }

  /**
   * Fallback radius (in world units) used by {@link #isPointOverEntity(Entity, Point)} for entities
   * that have neither a {@link CollideComponent} nor a {@link DrawComponent}.
   */
  private static final float FALLBACK_HOVER_RADIUS = 0.5f;

  private static final float FALLBACK_HOVER_RADIUS_SQ =
      FALLBACK_HOVER_RADIUS * FALLBACK_HOVER_RADIUS;

  /**
   * Tests whether a world-space point is "over" the given entity, using the best available shape:
   *
   * <ol>
   *   <li>If the entity has a {@link CollideComponent}, the collider's {@code collide(Point)} is
   *       used (exact shape test — hitbox or hitcircle).
   *   <li>Otherwise, if the entity has a {@link DrawComponent}, the sprite's bounding rectangle
   *       ({@link PositionComponent#position()} + width/height) is tested.
   *   <li>As a last resort, a small radius ({@value #FALLBACK_HOVER_RADIUS} world units) around the
   *       entity's position is used.
   * </ol>
   *
   * <p>This method is the single source of truth for determining whether a cursor (or any point) is
   * targeting an entity. It is used on both the server (interaction resolution) and the client
   * (highlight feedback) to guarantee consistent results.
   *
   * @param entity the entity to test
   * @param point the world-space point (typically the cursor position)
   * @return {@code true} if the point is considered to be over the entity
   */
  public static boolean isPointOverEntity(Entity entity, Point point) {
    // 1. Collider-based check
    Optional<CollideComponent> cc = entity.fetch(CollideComponent.class);
    if (cc.isPresent()) {
      Collider collider = cc.get().collider();
      return collider.collide(point);
    }

    // 2. Sprite-bounds check
    Optional<DrawComponent> dc = entity.fetch(DrawComponent.class);
    Optional<PositionComponent> pc = entity.fetch(PositionComponent.class);
    if (dc.isPresent() && pc.isPresent()) {
      Point pos = pc.get().position();
      float w = dc.get().getWidth();
      float h = dc.get().getHeight();
      return point.x() >= pos.x()
          && point.x() <= pos.x() + w
          && point.y() >= pos.y()
          && point.y() <= pos.y() + h;
    }

    // 3. Fallback: small radius around position
    Point ePos = getPosition(entity);
    return ePos.distanceSquared(point) <= FALLBACK_HOVER_RADIUS_SQ;
  }

  /**
   * Finds the entity from the given stream whose bounds contain the specified point. If multiple
   * entities overlap at the point, the one whose center (as returned by {@link
   * #getPosition(Entity)}) is closest to the point is returned.
   *
   * <p>Uses {@link #isPointOverEntity(Entity, Point)} for the containment check.
   *
   * @param point the world-space point to test
   * @param entities the stream of candidate entities
   * @return the entity under the point, or {@link Optional#empty()} if none qualifies
   */
  public static Optional<Entity> findEntityAtPoint(Point point, Stream<Entity> entities) {
    return entities
        .filter(e -> isPointOverEntity(e, point))
        .min(Comparator.comparingDouble(e -> getPosition(e).distanceSquared(point)));
  }
}
