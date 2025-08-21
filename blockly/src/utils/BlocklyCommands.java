package utils;

import client.Client;
import com.badlogic.gdx.ai.pfa.GraphPath;
import components.AmmunitionComponent;
import components.BlocklyItemComponent;
import components.PushableComponent;
import contrib.components.*;
import contrib.components.BlockComponent;
import contrib.utils.EntityUtils;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;
import core.Component;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.level.utils.LevelUtils;
import core.utils.*;
import core.utils.components.MissingComponentException;
import entities.MiscFactory;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import server.Server;

/** A utility class that contains all methods for Blockly Blocks. */
public class BlocklyCommands {

  private static final String MOVEMENT_FORCE_ID = "Movement";

  /**
   * If this is et to true, the Guard-Monster will not shoot on the hero.
   *
   * <p>Workaround for #1952
   */
  public static boolean DISABLE_SHOOT_ON_HERO = false;

  private static final float FIREBALL_RANGE = Integer.MAX_VALUE;
  private static final float FIREBALL_SPEED = 15f;
  private static final int FIREBALL_DMG = 1;

  /**
   * Moves the hero in it's viewing direction.
   *
   * <p>One move equals one tile.
   */
  public static void move() {
    Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
    Direction viewDirection = EntityUtils.getViewDirection(hero);
    BlocklyCommands.move(viewDirection, hero);
    Game.levelEntities()
        .filter(entity -> entity.name().equals("Blockly Black Knight"))
        .findFirst()
        .ifPresent(
            boss ->
                boss.fetch(PositionComponent.class)
                    .ifPresent(pc -> BlocklyCommands.move(pc.viewDirection(), boss)));
  }

  /** Moves the Hero to the Exit Block of the current Level. */
  public static void moveToExit() {
    if (Game.endTiles().isEmpty()) return;
    Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
    Tile exitTile = Game.endTiles().stream().findFirst().orElse(null);
    if (exitTile == null) return;

    PositionComponent pc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));

    GraphPath<Tile> pathToExit = LevelUtils.calculatePath(pc.coordinate(), exitTile.coordinate());

    for (Tile nextTile : pathToExit) {
      Tile currentTile = Game.tileAt(pc.position()).orElse(null);
      if (currentTile != nextTile) {
        Direction viewDirection = EntityUtils.getViewDirection(hero);
        Direction targetDirection = currentTile.directionTo(nextTile)[0];
        while (viewDirection != targetDirection) {
          rotate(Direction.RIGHT);
          viewDirection = EntityUtils.getViewDirection(hero);
        }
        move();
      }
    }
  }

  /**
   * Rotate the hero in a specific direction.
   *
   * @param direction Direction in which the hero will be rotated.
   */
  public static void rotate(final Direction direction) {
    if (direction == Direction.UP || direction == Direction.DOWN) {
      return; // no rotation
    }
    Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
    Direction viewDirection = EntityUtils.getViewDirection(hero);
    Direction newDirection =
        switch (viewDirection) {
          case UP -> direction == Direction.LEFT ? Direction.LEFT : Direction.RIGHT;
          case DOWN -> direction == Direction.LEFT ? Direction.RIGHT : Direction.LEFT;
          case LEFT -> direction == Direction.LEFT ? Direction.DOWN : Direction.UP;
          case RIGHT -> direction == Direction.LEFT ? Direction.UP : Direction.DOWN;
          case NONE -> viewDirection; // no change
        };
    BlocklyCommands.turnEntity(hero, newDirection);
    Game.allEntities()
        .filter(entity -> entity.name().equals("Blockly Black Knight"))
        .findFirst()
        .ifPresent(boss -> BlocklyCommands.turnEntity(boss, newDirection.opposite()));
    Server.waitDelta();
  }

  /**
   * Shoots a fireball in direction the hero is facing.
   *
   * <p>The hero needs at least one unit of ammunition to successfully shoot a fireball.
   */
  public static void shootFireball() {
    Entity hero = Game.hero().orElseThrow(MissingHeroException::new);

    hero.fetch(AmmunitionComponent.class)
        .filter(AmmunitionComponent::checkAmmunition)
        .ifPresent(ac -> aimAndShoot(ac, hero));
  }

  /**
   * Triggers an interactable in a direction related to the hero.
   *
   * @param direction Direction in which the hero will search for an interactable.
   */
  public static void interact(Direction direction) {
    Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
    PositionComponent pc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));

    Tile inDirection;

    if (direction == Direction.NONE) {
      inDirection = Game.tileAt(pc.position()).orElse(null);
    } else {
      inDirection =
          Game.tileAt(pc.position(), pc.viewDirection().applyRelative(direction)).orElse(null);
    }

    Game.entityAtTile(inDirection)
        .forEach(
            entity ->
                entity
                    .fetch(InteractionComponent.class)
                    .ifPresent(
                        interactionComponent ->
                            interactionComponent.triggerInteraction(entity, hero)));
  }

  /**
   * Triggers the interaction (normally a pickup action) for each Entity with an {@link
   * ItemComponent} at the same tile as the hero.
   *
   * <p>If the hero is not on the map, nothing will happen.
   */
  public static void pickup() {
    Game.hero()
        .ifPresent(
            hero ->
                Game.entityAtTile(Game.tileAt(EntityUtils.getHeroCoordinate()).orElse(null))
                    .filter(e -> e.isPresent(BlocklyItemComponent.class))
                    .forEach(
                        item ->
                            item.fetch(InteractionComponent.class)
                                .ifPresent(ic -> ic.triggerInteraction(item, hero))));
  }

  /**
   * Drop a Blockly-Item at the heros position.
   *
   * <p>If the hero is not on the map, nothing will happen.
   *
   * @param item Name of the item to drop
   */
  public static void dropItem(String item) {
    Point heroPos = EntityUtils.getHeroPosition();
    if (heroPos == null) {
      return; // hero is not on the map
    }
    switch (item) {
      case "Brotkrumen" -> Game.add(MiscFactory.breadcrumb(heroPos));
      case "Kleeblatt" -> Game.add(MiscFactory.clover(heroPos));
      default ->
          throw new IllegalArgumentException("Can not convert " + item + " to droppable Item.");
    }
  }

  /** Attempts to push entities in front of the hero. */
  public static void push() {
    movePushable(true);
  }

  /** Attempts to pull entities in front of the hero. */
  public static void pull() {
    movePushable(false);
  }

  /**
   * Shoots a fireball in direction the hero is facing.
   *
   * @param ac AmmunitionComponent of the hero, ammunition amount will be reduced by 1
   * @param hero Entity to be used as hero for positioning
   */
  private static void aimAndShoot(AmmunitionComponent ac, Entity hero) {
    newFireballSkill(hero).execute(hero);
    ac.spendAmmo();
    Server.waitDelta();
  }

  /**
   * Create a new fireball for the given entity.
   *
   * @param hero Entity to be used as hero for positioning
   * @return Nice new fireball, ready to be launched.
   */
  private static Skill newFireballSkill(Entity hero) {
    return new Skill(
        new FireballSkill(
            () ->
                hero.fetch(CollideComponent.class)
                    .map(cc -> cc.center(hero))
                    .map(p -> p.translate(EntityUtils.getViewDirection(hero)))
                    .orElseThrow(
                        () -> MissingComponentException.build(hero, CollideComponent.class)),
            FIREBALL_RANGE,
            FIREBALL_SPEED,
            FIREBALL_DMG),
        1);
  }

  /**
   * Attempts to pull or push entities in front of the hero.
   *
   * <p>If there is a pushable entity in the tile in front of the hero, it checks if the tile behind
   * the player (for pull) or in front of the entities (for push) is accessible. If accessible, the
   * hero and the entity are moved simultaneously in the corresponding direction.
   *
   * <p>The pulled/pushed entity temporarily loses its blocking component while moving and regains
   * it after.
   *
   * @param push True if you want to push, false if you want to pull.
   */
  private static void movePushable(boolean push) {
    Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
    DISABLE_SHOOT_ON_HERO = true;
    PositionComponent heroPC =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    Direction viewDirection = heroPC.viewDirection();
    Tile inFront = Game.tileAt(heroPC.position(), viewDirection).orElse(null);
    Tile checkTile;
    Direction moveDirection;
    if (push) {
      checkTile = Game.tileAt(inFront.position(), viewDirection).orElse(null);
      moveDirection = viewDirection;
    } else {
      checkTile = Game.tileAt(heroPC.position(), viewDirection.opposite()).orElse(null);
      moveDirection = viewDirection.opposite();
    }
    if (!checkTile.isAccessible()
        || Game.entityAtTile(checkTile).anyMatch(e -> e.isPresent(BlockComponent.class))
        || Game.entityAtTile(checkTile).anyMatch(e -> e.isPresent(AIComponent.class))) return;
    ArrayList<Entity> toMove =
        new ArrayList<>(
            Game.entityAtTile(inFront).filter(e -> e.isPresent(PushableComponent.class)).toList());
    if (toMove.isEmpty()) return;

    // remove the BlockComponent so the avoid blocking the hero while moving simultaneously
    toMove.forEach(entity -> entity.remove(BlockComponent.class));
    toMove.add(hero);
    BlocklyCommands.move(moveDirection, toMove.toArray(Entity[]::new));
    toMove.remove(hero);
    // give BlockComponent back
    toMove.forEach(entity -> entity.add(new BlockComponent()));
    BlocklyCommands.turnEntity(hero, viewDirection);
    Server.waitDelta();
    DISABLE_SHOOT_ON_HERO = false;
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
      Tile checkTile = Game.tileAt(EntityUtils.getHeroCoordinate()).orElse(null);
      return checkTile.levelElement() == tileElement;
    }
    return targetTile(direction).map(tile -> tile.levelElement() == tileElement).orElse(false);
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
    // Check if there is a component on the tile the hero is standing on
    if (direction == Direction.NONE) {
      Tile checkTile = Game.tileAt(EntityUtils.getHeroCoordinate()).orElse(null);
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
            Optional.ofNullable(EntityUtils.getHeroCoordinate())
                .map(coord -> coord.translate(dir))
                .flatMap(Game::tileAt);

    // calculate direction to check relative to hero's view direction
    return Optional.ofNullable(EntityUtils.getHeroViewDirection())
        .map(d -> d.applyRelative(direction))
        .flatMap(dirToCheck);
  }

  /**
   * Moves the given entities simultaneously in a specific direction.
   *
   * <p>One move equals one tile.
   *
   * @param direction Direction in which the entities will be moved.
   * @param entities Entities to move simultaneously.
   */
  @HideLanguage
  public static void move(final Direction direction, final Entity... entities) {
    double distanceThreshold = 0.1;

    record EntityComponents(
        PositionComponent pc, VelocityComponent vc, Coordinate targetPosition) {}

    List<EntityComponents> entityComponents = new ArrayList<>();

    for (Entity entity : entities) {
      PositionComponent pc =
          entity
              .fetch(PositionComponent.class)
              .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));

      VelocityComponent vc =
          entity
              .fetch(VelocityComponent.class)
              .orElseThrow(() -> MissingComponentException.build(entity, VelocityComponent.class));

      Tile targetTile = Game.tileAt(pc.position(), direction).orElse(null);
      if (targetTile == null
          || (!targetTile.isAccessible() && !(targetTile instanceof PitTile))
          || Game.entityAtTile(targetTile).anyMatch(e -> e.isPresent(BlockComponent.class))) {
        return; // if any target tile is not accessible, don't move anyone
      }

      entityComponents.add(new EntityComponents(pc, vc, targetTile.coordinate()));
    }

    double[] distances =
        entityComponents.stream()
            .mapToDouble(e -> e.pc.position().distance(e.targetPosition.toCenteredPoint()))
            .toArray();
    double[] lastDistances = new double[entities.length];

    while (true) {
      boolean allEntitiesArrived = true;
      for (int i = 0; i < entities.length; i++) {
        EntityComponents comp = entityComponents.get(i);
        comp.vc.clearForces();
        comp.vc.currentVelocity(Vector2.ZERO);
        comp.vc.applyForce(MOVEMENT_FORCE_ID, direction.scale((Client.MOVEMENT_FORCE.x())));

        lastDistances[i] = distances[i];
        distances[i] = comp.pc.position().distance(comp.targetPosition.toCenteredPoint());

        if (comp.vc().maxSpeed() > 0
            && Game.existInLevel(entities[i])
            && !(distances[i] <= distanceThreshold || distances[i] > lastDistances[i])) {
          allEntitiesArrived = false;
        }
      }

      if (allEntitiesArrived) break;

      Server.waitDelta();
    }

    for (EntityComponents ec : entityComponents) {
      ec.vc.currentVelocity(Vector2.ZERO);
      ec.vc.clearForces();
      // check the position-tile via new request in case a new level was loaded
      Tile endTile = Game.tileAt(ec.pc.position()).orElse(null);
      if (endTile != null) ec.pc.position(endTile); // snap to grid
    }
  }

  /**
   * Moves the given entity in it's viewing direction.
   *
   * <p>One move equals one tile.
   *
   * @param entity Entity to move in its viewing direction.
   */
  @HideLanguage
  public static void move(final Entity entity) {
    move(EntityUtils.getViewDirection(entity), entity);
  }

  /**
   * Turns the given entity in a specific direction.
   *
   * <p>This will also update the animation.
   *
   * <p>This does not call {@link Server#waitDelta()}.
   *
   * @param entity Entity to turn.
   * @param direction direction to turn to.
   */
  @HideLanguage
  public static void turnEntity(Entity entity, Direction direction) {
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    VelocityComponent vc =
        entity
            .fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, VelocityComponent.class));
    Point oldP = pc.position();
    vc.applyForce(MOVEMENT_FORCE_ID, direction);
    // so the player can not glitch inside the next tile
    pc.position(oldP);
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

  /** Let the hero do nothing for a short moment. */
  public static void rest() {
    Server.waitDelta();
  }

  /**
   * Executes a given function a specified number of times.
   *
   * @param counter the number of times to execute the function; must be non-negative
   * @param function the function to be executed repeatedly
   */
  public static void times(int counter, IVoidFunction function) {
    IntStream.range(0, counter).forEach(value -> function.execute());
  }
}
