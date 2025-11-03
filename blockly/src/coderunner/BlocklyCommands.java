package coderunner;

import contrib.components.*;
import contrib.utils.EntityUtils;
import core.Component;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.utils.LevelElement;
import core.utils.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import server.Server;
import systems.BlocklyCommandExecuteSystem;

/** A utility class that contains all methods for Blockly Blocks. */
public class BlocklyCommands {

  /**
   * Commands that can be queued in the {@link BlocklyCommandExecuteSystem} and will be executed in
   * the Game-Thread.
   */
  public enum Commands {

    /** Move the player one tile in the current viewing direction. */
    HERO_MOVE,

    /** Turn the player 90 degrees to the right (clockwise). */
    HERO_TURN_RIGHT,

    /** Turn the player 90 degrees to the left (counterclockwise). */
    HERO_TURN_LEFT,

    /** Use the object located on the player's current position. */
    HERO_USE_HERE,

    /**
     * Use the object located to the left of the player (relative to the player's current viewing
     * direction).
     */
    HERO_USE_LEFT,

    /**
     * Use the object located to the right of the player (relative to the player's current viewing
     * direction).
     */
    HERO_USE_RIGHT,

    /**
     * Use the object located infront of the player (relative to the player's current viewing
     * direction).
     */
    HERO_USE_UP,

    /**
     * Use the object located behind the player (relative to the player's current viewing
     * direction).
     */
    HERO_USE_DOWN,

    /**
     * If there is a stone in front of the player, push it forward and move the player one tile
     * ahead.
     */
    HERO_PUSH,

    /**
     * If there is a stone in front of the player, pull it backward and move the player one tile
     * back.
     */
    HERO_PULL,

    /** Drop a clover on the player's current position. */
    HERO_DROP_CLOVER,

    /** Drop breadcrumbs on the player's current position. */
    HERO_DROP_BREADCRUMBS,

    /** Pick up an item from the player's current position. */
    HERO_PICKUP,

    /** Shoot a fireball in the player's current viewing direction. */
    HERO_FIREBALL,

    /** Do nothing for a short amount of time (player rests). */
    REST
  }

  /**
   * Todo Remove HotFix: Magic Translate for <a
   * href="https://github.com/Dungeon-CampusMinden/Dungeon/issues/2448">...</a>
   *
   * <p>Move the position slightly further into the tile to avoid rounding errors at edge positions
   */
  public static final Vector2 MAGIC_OFFSET = Vector2.of(0.3, 0.3);

  /**
   * If this is et to true, the Guard-Monster will not shoot on the player.
   *
   * <p>Workaround for #1952
   */
  public static boolean DISABLE_SHOOT_ON_HERO = false;

  /**
   * Moves the player in it's viewing direction.
   *
   * <p>One move equals one tile.
   */
  public static void move() {
    Game.system(BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_MOVE));
  }

  /**
   * Rotate the player in a specific direction.
   *
   * @param direction Direction in which the player will be rotated.
   */
  public static void rotate(final Direction direction) {
    core.utils.Direction realDirection = direction.toDirection();
    if (realDirection == core.utils.Direction.RIGHT)
      Game.system(
          BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_TURN_RIGHT));
    else if (realDirection == core.utils.Direction.LEFT)
      Game.system(BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_TURN_LEFT));
  }

  /**
   * Shoots a fireball in direction the player is facing.
   *
   * <p>The player needs at least one unit of ammunition to successfully shoot a fireball.
   */
  public static void shootFireball() {
    Game.system(BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_FIREBALL));
  }

  /**
   * Triggers an interactable in a direction related to the player.
   *
   * @param direction Direction in which the player will search for an interactable.
   */
  public static void interact(Direction direction) {
    switch (direction.toDirection()) {
      case UP ->
          Game.system(
              BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_USE_UP));
      case DOWN ->
          Game.system(
              BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_USE_DOWN));
      case LEFT ->
          Game.system(
              BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_USE_LEFT));
      case RIGHT ->
          Game.system(
              BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_USE_RIGHT));
      default ->
          Game.system(
              BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_USE_HERE));
    }
  }

  /**
   * Triggers the interaction (normally a pickup action) for each Entity with an {@link
   * ItemComponent} at the same tile as the player.
   *
   * <p>If the player is not on the map, nothing will happen.
   */
  public static void pickup() {
    Game.system(BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_PICKUP));
  }

  /**
   * Drop a Blockly-Item at the heros position.
   *
   * <p>If the player is not on the map, nothing will happen.
   *
   * @param item Name of the item to drop
   */
  public static void dropItem(String item) {
    switch (item) {
      case BlocklyCommandExecuteSystem.BREADCRUMB ->
          Game.system(
              BlocklyCommandExecuteSystem.class,
              system -> system.add(Commands.HERO_DROP_BREADCRUMBS));
      case BlocklyCommandExecuteSystem.CLOVER ->
          Game.system(
              BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_DROP_CLOVER));
      default ->
          throw new IllegalArgumentException("Can not convert " + item + " to droppable Item.");
    }
  }

  /** Attempts to push entities in front of the player. */
  public static void push() {
    Game.system(BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_PUSH));
  }

  /** Attempts to pull entities in front of the player. */
  public static void pull() {
    Game.system(BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_PULL));
  }

  /** Let the player do nothing for a short moment. */
  public static void rest() {
    Game.system(BlocklyCommandExecuteSystem.class, system -> system.add(Commands.REST));
  }

  /**
   * Check if the next tile in the given direction is an {@link LevelElement} Type Tile.
   *
   * @param tileElement Tile Type to check for.
   * @param direction Direction to check
   * @return Returns true if the player is null or a tile of the given type was detected. Otherwise,
   *     returns false.
   */
  public static boolean isNearTile(LevelElement tileElement, final Direction direction) {
    waitForEmptyQueue();
    core.utils.Direction realDirection = direction.toDirection();
    // Check the tile the player is standing on
    if (realDirection == core.utils.Direction.NONE) {
      Tile checkTile =
          Game.player()
              .flatMap(hero -> hero.fetch(PositionComponent.class))
              .map(PositionComponent::position)
              .map(pos -> pos.translate(MAGIC_OFFSET))
              .flatMap(Game::tileAt)
              .orElse(null);
      return checkTile != null && matchesTile(tileElement, checkTile.levelElement());
    }
    return targetTile(realDirection)
        .map(tile -> matchesTile(tileElement, tile.levelElement()))
        .orElse(false);
  }

  private static boolean matchesTile(LevelElement target, LevelElement actual) {
    if (target == actual) {
      return true;
    }
    // Special case: treat DOOR or EXIT as FLOOR
    return target == LevelElement.FLOOR
        && (actual == LevelElement.DOOR || actual == LevelElement.EXIT);
  }

  /**
   * Check if on the next tile in the given direction an entity with the given component exist.
   *
   * @param componentClass Component-Class to check for.
   * @param direction Direction to check
   * @return Returns true if the player is null or an entity with the given component was detected.
   *     Otherwise, returns false.
   */
  public static boolean isNearComponent(
      Class<? extends Component> componentClass, final Direction direction) {
    // Check if there is a component on the tile the player is standing on
    waitForEmptyQueue();
    core.utils.Direction realDirection = direction.toDirection();
    if (realDirection == core.utils.Direction.NONE) {
      Tile checkTile =
          Game.player()
              .flatMap(hero -> hero.fetch(PositionComponent.class))
              .map(PositionComponent::position)
              .map(pos -> pos.translate(MAGIC_OFFSET))
              .flatMap(Game::tileAt)
              .orElse(null);

      return Game.entityAtTile(checkTile).anyMatch(e -> e.isPresent(componentClass));
    }
    return targetTile(realDirection)
        .map(tile -> Game.entityAtTile(tile).anyMatch(e -> e.isPresent(componentClass)))
        .orElse(false);
  }

  /**
   * Determines whether the specified direction leads to an active state.
   *
   * <p>A tile in the given direction is considered active if:
   *
   * <ul>
   *   <li>it is a {@link DoorTile} and it is "open", or
   *   <li>it contains at least one {@link LeverComponent}, and all found levers are in the "on"
   *       state.
   * </ul>
   *
   * @param direction the direction to check relative to the player's position.
   * @return {@code true} if the tile in the given direction is active, {@code false} otherwise.
   */
  public static boolean active(final Direction direction) {
    waitForEmptyQueue();
    return targetTile(direction.toDirection())
        .map(BlocklyCommands::checkTileForDoorOrLevers)
        .orElse(false);
  }

  /**
   * Determines whether the specified tile is in active state.
   *
   * <p>A tile in the given direction is considered active iff
   *
   * <ul>
   *   <li>it is a {@link DoorTile} and it is "open", or
   *   <li>it contains at least one {@link LeverComponent}, and all found levers are in the "on"
   *       state.
   * </ul>
   *
   * @param tile the direction to check
   * @return {@code true} if the tile is active, {@code false} otherwise.
   */
  private static Boolean checkTileForDoorOrLevers(Tile tile) {
    // is this a door? is it open?
    if (tile instanceof DoorTile doorTile) return doorTile.isOpen();

    // find all levers on a given tile and split those into "isOn" (true) and "isOff" (false)
    Map<Boolean, List<LeverComponent>> levers =
        Game.entityAtTile(tile)
            .flatMap(e -> e.fetch(LeverComponent.class).stream())
            .collect(Collectors.partitioningBy(LeverComponent::isOn));

    // there needs to be at least one lever; all levers need to be "isOn" (true)
    return levers.get(false).isEmpty() && !levers.get(true).isEmpty();
  }

  /**
   * Gets the target tile in the given direction relative to the player.
   *
   * @param direction Direction to check relative to player's view direction
   * @return The target tile, or empty if player is not found or target tile doesn't exist
   */
  private static Optional<Tile> targetTile(final core.utils.Direction direction) {
    // find tile in a direction or empty
    Function<core.utils.Direction, Optional<Tile>> dirToCheck =
        dir ->
            Game.player()
                .flatMap(hero -> hero.fetch(PositionComponent.class))
                .map(PositionComponent::position)
                .map(pos -> pos.translate(MAGIC_OFFSET))
                .map(pos -> pos.translate(dir))
                .flatMap(Game::tileAt);

    // calculate direction to check relative to player's view direction
    return Optional.ofNullable(EntityUtils.getHeroViewDirection())
        .map(d -> d.applyRelative(direction))
        .flatMap(dirToCheck);
  }

  /**
   * Checks whether the boss's view direction equals the given direction.
   *
   * <p>This method is specifically used for the boss in Blockly Chapter 3, Level 4.
   *
   * @param direction the direction to check against
   * @return {@code true} if the boss's view direction equals the given direction; {@code false} if
   *     the direction does not match, or if the boss or its PositionComponent is missing
   */
  public static boolean checkBossViewDirection(Direction direction) {
    waitForEmptyQueue();
    return Game.allEntities()
        .filter(entity -> entity.name().equals(BlocklyCommandExecuteSystem.BLOCKLY_BLACK_KNIGHT))
        .findFirst()
        .flatMap(boss -> boss.fetch(PositionComponent.class))
        .map(PositionComponent::viewDirection)
        .map(bossDir -> bossDir.equals(direction.toDirection()))
        .orElse(false);
  }

  /**
   * Waits until the {@link BlocklyCommandExecuteSystem} queue is empty.
   *
   * <p>This ensures that all queued commands affecting entity movement or state have been processed
   * before continuing. Without this wait, checks that depend on the current position of entities
   * (e.g. verifying whether the player is near a wall) may use false positions and produce
   * incorrect results.
   *
   * <p>Call this method before performing spatial checks that rely on up-to-date entity positions.
   */
  private static void waitForEmptyQueue() {
    Game.system(
        BlocklyCommandExecuteSystem.class,
        system -> {
          while (!system.isEmpty() || system.isRest()) {
            Server.waitDelta();
          }
          // wait one more time to make sure the last command of the queue has finished executing
          Server.waitDelta();
        });
  }
}
