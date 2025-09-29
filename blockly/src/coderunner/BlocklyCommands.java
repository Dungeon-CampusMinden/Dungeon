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
import systems.BlocklyCommandExecuteSystem;

/** A utility class that contains all methods for Blockly Blocks. */
public class BlocklyCommands {

  public enum Commands {
    HERO_MOVE,
    HERO_MOVE_TO_EXIT,
    HERO_TURN_RIGHT,
    HERO_TURN_LEFT,
    HERO_USE_HERE,
    HERO_USE_LEFT,
    HERO_USE_RIGHT,
    HERO_USE_UP,
    HERO_USE_DONW,
    HERO_PUSH,
    HERO_PULL,
    HERO_DROP_CLOVER,
    HERO_DROP_BREADCRUMBS,
    HERO_PICKUP,
    HERO_FIREBALL,
    REST;
  }

  /**
   * Todo Remove HotFix: Magic Translate for <a
   * href="https://github.com/Dungeon-CampusMinden/Dungeon/issues/2448">...</a>
   *
   * <p>Move the position slightly further into the tile to avoid rounding errors at edge positions
   */
  public static final Vector2 MAGIC_OFFSET = Vector2.of(0.3, 0.3);

  /**
   * If this is et to true, the Guard-Monster will not shoot on the hero.
   *
   * <p>Workaround for #1952
   */
  public static boolean DISABLE_SHOOT_ON_HERO = false;

  /**
   * Moves the hero in it's viewing direction.
   *
   * <p>One move equals one tile.
   */
  public static void move() {
    Game.system(BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_MOVE));
  }

  /** Moves the Hero to the Exit Block of the current Level. */
  public static void moveToExit() {
    Game.system(
        BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_MOVE_TO_EXIT));
  }

  /**
   * Rotate the hero in a specific direction.
   *
   * @param direction Direction in which the hero will be rotated.
   */
  public static void rotate(final Direction direction) {
    if (direction == Direction.RIGHT)
      Game.system(
          BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_TURN_RIGHT));
    else
      Game.system(BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_TURN_LEFT));
  }

  /**
   * Shoots a fireball in direction the hero is facing.
   *
   * <p>The hero needs at least one unit of ammunition to successfully shoot a fireball.
   */
  public static void shootFireball() {
    Game.system(BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_FIREBALL));
  }

  /**
   * Triggers an interactable in a direction related to the hero.
   *
   * @param direction Direction in which the hero will search for an interactable.
   */
  public static void interact(Direction direction) {
    switch (direction) {
      case UP ->
          Game.system(
              BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_USE_UP));
      case DOWN ->
          Game.system(
              BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_USE_DONW));
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
   * ItemComponent} at the same tile as the hero.
   *
   * <p>If the hero is not on the map, nothing will happen.
   */
  public static void pickup() {
    Game.system(BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_PICKUP));
  }

  /**
   * Drop a Blockly-Item at the heros position.
   *
   * <p>If the hero is not on the map, nothing will happen.
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

  /** Attempts to push entities in front of the hero. */
  public static void push() {
    Game.system(BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_PUSH));
  }

  /** Attempts to pull entities in front of the hero. */
  public static void pull() {
    Game.system(BlocklyCommandExecuteSystem.class, system -> system.add(Commands.HERO_PULL));
  }

  /** Let the hero do nothing for a short moment. */
  public static void rest() {
    Game.system(BlocklyCommandExecuteSystem.class, system -> system.add(Commands.REST));
  }

  /**
   * Check if the next tile in the given direction is an {@link LevelElement} Type Tile.
   *
   * @param tileElement Tile Type to check for.
   * @param direction Direction to check
   * @return Returns true if the hero is null or a tile of the given type was detected. Otherwise,
   *     returns false.
   */
  public static boolean isNearTile(LevelElement tileElement, final Direction direction) {
    // Check the tile the hero is standing on
    if (direction == Direction.NONE) {
      Tile checkTile =
          Game.hero()
              .flatMap(hero -> hero.fetch(PositionComponent.class))
              .map(PositionComponent::position)
              .map(pos -> pos.translate(MAGIC_OFFSET))
              .flatMap(Game::tileAt)
              .orElse(null);
      return checkTile != null && checkTile.levelElement() == tileElement;
    }
    return targetTile(direction).map(tile -> tile.levelElement() == tileElement).orElse(false);
  }

  /**
   * Check if on the next tile in the given direction an entity with the given component exist.
   *
   * @param componentClass Component-Class to check for.
   * @param direction Direction to check
   * @return Returns true if the hero is null or an entity with the given component was detected.
   *     Otherwise, returns false.
   */
  public static boolean isNearComponent(
      Class<? extends Component> componentClass, final Direction direction) {
    // Check if there is a component on the tile the hero is standing on
    if (direction == Direction.NONE) {
      Tile checkTile =
          Game.hero()
              .flatMap(hero -> hero.fetch(PositionComponent.class))
              .map(PositionComponent::position)
              .map(pos -> pos.translate(MAGIC_OFFSET))
              .flatMap(Game::tileAt)
              .orElse(null);

      return Game.entityAtTile(checkTile).anyMatch(e -> e.isPresent(componentClass));
    }
    return targetTile(direction)
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
   * @param direction the direction to check relative to the hero's position.
   * @return {@code true} if the tile in the given direction is active, {@code false} otherwise.
   */
  public static boolean active(final Direction direction) {
    return targetTile(direction).map(BlocklyCommands::checkTileForDoorOrLevers).orElse(false);
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
   * Gets the target tile in the given direction relative to the hero.
   *
   * @param direction Direction to check relative to hero's view direction
   * @return The target tile, or empty if hero is not found or target tile doesn't exist
   */
  private static Optional<Tile> targetTile(final Direction direction) {
    // find tile in a direction or empty
    Function<Direction, Optional<Tile>> dirToCheck =
        dir ->
            Game.hero()
                .flatMap(hero -> hero.fetch(PositionComponent.class))
                .map(PositionComponent::position)
                .map(pos -> pos.translate(MAGIC_OFFSET))
                .map(pos -> pos.translate(dir))
                .flatMap(Game::tileAt);

    // calculate direction to check relative to hero's view direction
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
    return Game.allEntities()
        .filter(entity -> entity.name().equals("Blockly Black Knight"))
        .findFirst()
        .flatMap(boss -> boss.fetch(PositionComponent.class))
        .map(PositionComponent::viewDirection)
        .map(bossDir -> bossDir.equals(direction))
        .orElse(false);
  }
}
