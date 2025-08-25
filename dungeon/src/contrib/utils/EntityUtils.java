package contrib.utils;

import contrib.entities.LeverFactory;
import contrib.entities.SignFactory;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.util.function.BiConsumer;

/**
 * EntityUtils is a utility class that provides methods for spawning entities in the game. It
 * contains methods for spawning signs, levers, and other entities. It also contains methods for
 * teleporting entities to specific positions and retrieving the position of the hero in the game.
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
   * Teleports the hero to a specified point in the game.
   *
   * <p>This method retrieves the hero entity from the game and calls the teleportEntityTo method to
   * change the hero's position. If the hero entity is not present (which can happen if the hero has
   * fallen into a pit), the method does nothing.
   *
   * @param point The point to which the hero should be teleported.
   */
  public static void teleportHeroTo(Point point) {
    Entity hero = Game.hero().orElse(null);
    if (hero == null) {
      return;
    }
    teleportEntityTo(hero, point);
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
   * Retrieves the current position of the hero in the game.
   *
   * <p>This method retrieves the hero entity from the game. If the hero entity is not present
   * (which can happen if the hero has fallen into a pit), the method returns null.
   *
   * @return The current position of the hero, or a null value if the hero is not present.
   */
  public static Point getHeroPosition() {
    // TODO: SMELL!
    // we really shouldn't return `null` if no hero was found, but `Optional.empty()` instead!
    return Game.hero()
        .map(
            e ->
                e.fetch(PositionComponent.class)
                    .orElseThrow(() -> MissingComponentException.build(e, PositionComponent.class))
                    .position())
        .orElse(null);
  }

  /**
   * Retrieves the current coordinates of the hero in the game.
   *
   * <p>This method retrieves the hero entity from the game. If the hero entity is not present
   * (which can happen if the hero has fallen into a pit), the method returns null.
   *
   * @return The current coordinates of the hero, or null if the hero is not present.
   */
  public static Coordinate getHeroCoordinate() {
    Point heroPos = getHeroPosition();
    // TODO: SMELL!
    // we really shouldn't return `null` if no hero was found, but `Optional.empty()` instead!
    return heroPos == null ? null : heroPos.toCoordinate();
  }

  /**
   * Retrieves the direction the hero is facing.
   *
   * @return the direction the hero is facing, or null if there is no hero.
   */
  public static Direction getHeroViewDirection() {
    // TODO: SMELL!
    // we really shouldn't return `null` if no hero was found, but `Optional.empty()` instead!
    // this approach has been chosen solely to ensure a symmetric modelling to the existing methods.
    // when refactoring, *all* these methods here should be changed to return `Optional<>`.
    return Game.hero().map(EntityUtils::getViewDirection).orElse(null);
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
}
