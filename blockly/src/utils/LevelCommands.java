package utils;

import contrib.components.LeverComponent;
import contrib.utils.EntityUtils;
import core.Component;
import core.Game;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.utils.MissingHeroException;

/**
 * This class contains all Level Commands that are available.
 *
 * <p>E.g. While {@link HeroCommands} contains methods to {@link HeroCommands#move()} this class
 * implements methods to check specific state inside the current level ({@link #isNearTile(Class,
 * Direction)}
 */
public class LevelCommands {
  /**
   * Check if the next tile in the given direction is an instance from the given class.
   *
   * @param tileClass Tile-Class to check for.
   * @param direction Direction to check
   * @return Returns true if the hero is null or the target tile is from the given class. Otherwise,
   *     returns false.
   */
  public static boolean isNearTile(Class<? extends Tile> tileClass, final Direction direction) {
    Tile targetTile = targetTile(direction);
    if (targetTile == null) {
      return false;
    }
    return tileClass.isInstance(targetTile);
  }

  /**
   * Check if on the next tile in the given direction an entity with the given component exist.
   *
   * @param componentClass Component-Class to check for.
   * @param direction Direction to check
   * @return Returns true if the hero is null or a entity with the given component was detected.
   *     Otherwise, returns false.
   */
  public static boolean isNearComponent(
      Class<? extends Component> componentClass, final Direction direction) {
    Tile targetTile = targetTile(direction);
    if (targetTile == null) {
      return false;
    }
    return Game.entityAtTile(targetTile).anyMatch(e -> e.isPresent(componentClass));
  }

  /**
   * Determines whether the specified direction leads to an active state.
   *
   * <p>A tile in the given direction is considered active if:
   *
   * <p>It is a {@link DoorTile} and is open.
   *
   * <p>It contains at least one {@link LeverComponent}, and all found levers are in the "on" state.
   *
   * @param direction the direction to check relative to the hero's position.
   * @return {@code true} if the tile in the given direction is active, {@code false} otherwise.
   */
  public static boolean active(final Direction direction) {
    Tile targetTile = Game.tileAT(EntityUtils.getHeroPosition().add(direction.toPoint()));
    if (targetTile instanceof DoorTile) return ((DoorTile) targetTile).isOpen();
    return Game.entityAtTile(targetTile)
        .flatMap(e -> e.fetch(LeverComponent.class).stream())
        .allMatch(LeverComponent::isOn);
  }

  /**
   * Gets the target tile in the given direction relative to the hero.
   *
   * @param direction Direction to check relative to hero's view direction
   * @return The target tile, or null if hero is not found or target tile doesn't exist
   */
  private static Tile targetTile(final Direction direction) {
    Coordinate heroCoords = EntityUtils.getHeroCoordinate();
    if (heroCoords == null) {
      return null;
    }
    Direction heroViewDir =
        Direction.fromPositionCompDirection(
            EntityUtils.getViewDirection(Game.hero().orElseThrow(MissingHeroException::new)));
    Direction toCheck = heroViewDir.relativeToAbsoluteDirection(direction);

    Coordinate targetCoords = heroCoords.add(new Coordinate(toCheck.x(), toCheck.y()));
    return Game.tileAT(targetCoords);
  }
}
