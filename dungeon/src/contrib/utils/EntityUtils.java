package contrib.utils;

import contrib.components.CollideComponent;
import contrib.entities.LeverFactory;
import contrib.entities.SignFactory;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.util.Optional;
import java.util.function.BiConsumer;

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

  public static double getDistance(Entity entity, Entity who) {
    return EntityUtils.getPosition(entity).distance(EntityUtils.getPosition(who));
  }
}
