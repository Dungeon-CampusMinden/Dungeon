package systems;

import static coderunner.BlocklyCommands.DISABLE_SHOOT_ON_HERO;
import static coderunner.BlocklyCommands.MAGIC_OFFSET;

import client.Client;
import coderunner.BlocklyCommands;
import com.badlogic.gdx.Gdx;
import components.BlocklyItemComponent;
import components.PushableComponent;
import contrib.components.AIComponent;
import contrib.components.BlockComponent;
import contrib.components.InteractionComponent;
import contrib.components.ItemComponent;
import contrib.systems.EventScheduler;
import contrib.utils.EntityUtils;
import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.utils.*;
import core.utils.components.MissingComponentException;
import entities.MiscFactory;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * A system that executes queued {@link BlocklyCommands.Commands} in the game thread.
 *
 * <p>This system is part of the game loop and processes commands submitted by the Blockly-based
 * programming interface. It maintains an internal queue of commands and executes them in order,
 * translating high-level commands (e.g., "move hero" or "drop item") into game actions such as
 * moving entities, rotating the hero, interacting with objects, or shooting fireballs.
 *
 * <p>The execution is step-based: each update either consumes a command from the queue or advances
 * an ongoing movement until completion. Some commands (e.g., {@code HERO_MOVE}) may span multiple
 * frames due to smooth movement handling.
 *
 * <p>This system is lock-free and uses a {@link ConcurrentLinkedQueue} to enqueue commands from
 * external threads without blocking the game loop.
 */
public class BlocklyCommandExecuteSystem extends System {
  private static final String MOVEMENT_FORCE_ID = "Movement";

  /** String identifier for the breadcrumb item. */
  public static final String BREADCRUMB = "Brotkrumen";

  /** String identifier for the clover item. */
  public static final String CLOVER = "Kleeblatt";

  /** String identifier for the boss entity. */
  public static final String BLOCKLY_BLACK_KNIGHT = "Blockly Black Knight";

  // lock-free and non-blocking queue
  private final Queue<BlocklyCommands.Commands> queue = new ConcurrentLinkedQueue<>();

  private boolean rest = false;
  private final List<Supplier<Boolean>> makeStep = new LinkedList<>();
  private boolean disableQueue;

  /**
   * Main execution method of the system.
   *
   * <p>If no movement is in progress, it polls the next {@link BlocklyCommands.Commands} from the
   * queue and executes it. If a movement is still ongoing, this method advances the movement until
   * completion.
   */
  @Override
  public void execute() {
    if (makeStep.isEmpty()) {
      if (rest || queue.isEmpty()) return;
      switch (queue.poll()) {
        case HERO_MOVE -> move();
        case HERO_TURN_LEFT -> rotate(Direction.LEFT);
        case HERO_TURN_RIGHT -> rotate(Direction.RIGHT);
        case HERO_PULL -> movePushable(false);
        case HERO_PUSH -> movePushable(true);
        case HERO_DROP_BREADCRUMBS -> dropItem(BREADCRUMB);
        case HERO_DROP_CLOVER -> dropItem(CLOVER);
        case HERO_FIREBALL -> shootFireball();
        case HERO_PICKUP -> pickup();
        case HERO_USE_DOWN -> interact(Direction.DOWN);
        case HERO_USE_HERE -> interact(Direction.NONE);
        case HERO_USE_LEFT -> interact(Direction.LEFT);
        case HERO_USE_RIGHT -> interact(Direction.RIGHT);
        case HERO_USE_UP -> interact(Direction.UP);
        case REST -> rest();
      }
    } else {
      List<Supplier> toRemove = new ArrayList<>();
      makeStep.forEach(
          supp -> {
            if (supp.get()) toRemove.add(supp);
          });
      makeStep.removeAll(toRemove);
    }
  }

  /**
   * Adds a command to the queue if the system is running.
   *
   * @param command The command to add.
   */
  public void add(BlocklyCommands.Commands command) {
    if (!disableQueue) queue.add(command);
  }

  /**
   * Checks whether there are no more commands left in the queue and no movements currently in
   * progress.
   *
   * @return true if there are no queued or pending actions, false otherwise.
   */
  public boolean isEmpty() {
    return queue.isEmpty() && makeStep.isEmpty();
  }

  /**
   * Returns whether the rest state is active.
   *
   * @return {@code true} if rest is enabled, {@code false} otherwise
   */
  public boolean isRest() {
    return rest;
  }

  /** Clears the command queue and cancels all currently scheduled steps. */
  public void clear() {
    fullStop();
    makeStep.clear();
    queue.clear();
    run();
  }

  /**
   * Rotates the player into a given direction.
   *
   * @param direction The rotation direction (left or right). Vertical directions are ignored.
   */
  private void rotate(final Direction direction) {
    if (direction == Direction.UP || direction == Direction.DOWN) {
      return; // no rotation
    }
    Entity hero = Game.player().orElseThrow(MissingHeroException::new);
    Direction viewDirection = EntityUtils.getViewDirection(hero);
    Direction newDirection = viewDirection.applyRelative(direction);
    turnEntity(hero, newDirection);
    Game.levelEntities()
        .filter(entity -> entity.name().equals(BLOCKLY_BLACK_KNIGHT))
        .findFirst()
        .flatMap(
            boss ->
                boss.fetch(VelocityComponent.class).filter(vc -> vc.maxSpeed() > 0).map(vc -> boss))
        .ifPresent(boss -> turnEntity(boss, newDirection.opposite()));
  }

  /**
   * Moves the player one tile forward in its current viewing direction.
   *
   * <p>Also moves the "Blockly Black Knight" entity if present.
   */
  private void move() {
    Entity hero = Game.player().orElseThrow(MissingHeroException::new);
    Direction viewDirection = EntityUtils.getViewDirection(hero);
    move(viewDirection, () -> {}, hero);
    Game.levelEntities()
        .filter(entity -> entity.name().equals(BLOCKLY_BLACK_KNIGHT))
        .findFirst()
        .ifPresent(
            boss ->
                boss.fetch(VelocityComponent.class)
                    .filter(vc -> vc.maxSpeed() > 0)
                    .flatMap(vc -> boss.fetch(PositionComponent.class))
                    .ifPresent(pc -> move(pc.viewDirection(), () -> {}, boss)));
  }

  /**
   * Attempts to pull or push entities in front of the player.
   *
   * <p>If there is a pushable entity in the tile in front of the player, it checks if the tile
   * behind the player (for pull) or in front of the entities (for push) is accessible. If
   * accessible, the player and the entity are moved simultaneously in the corresponding direction.
   *
   * <p>The pulled/pushed entity temporarily loses its blocking component while moving and regains
   * it after.
   *
   * @param push True if you want to push, false if you want to pull.
   */
  private void movePushable(boolean push) {
    Entity hero = Game.player().orElseThrow(MissingHeroException::new);
    // do not push or pull if the player is frozen
    if (hero.fetch(VelocityComponent.class)
        .map(VelocityComponent::maxSpeed)
        .filter(s -> s == 0)
        .isPresent()) return;
    DISABLE_SHOOT_ON_HERO = true;

    PositionComponent heroPC =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    Direction viewDirection = heroPC.viewDirection();

    Optional<Tile> inFrontOpt =
        Game.tileAt(heroPC.position().translate(MAGIC_OFFSET), viewDirection);
    if (inFrontOpt.isEmpty()) {
      DISABLE_SHOOT_ON_HERO = false;
      return;
    }
    Tile inFront = inFrontOpt.get();

    Direction moveDirection;
    Optional<Tile> checkTileOpt;

    if (push) {
      checkTileOpt = Game.tileAt(inFront.position(), viewDirection);
      moveDirection = viewDirection;
    } else {
      checkTileOpt =
          Game.tileAt(heroPC.position().translate(MAGIC_OFFSET), viewDirection.opposite());
      moveDirection = viewDirection.opposite();
    }

    if (checkTileOpt.isEmpty()
        || !checkTileOpt.get().isAccessible()
        || Game.entityAtTile(checkTileOpt.get()).anyMatch(e -> e.isPresent(BlockComponent.class))
        || Game.entityAtTile(checkTileOpt.get()).anyMatch(e -> e.isPresent(AIComponent.class))) {
      DISABLE_SHOOT_ON_HERO = false;
      return;
    }
    ArrayList<Entity> toMove =
        new ArrayList<>(
            Game.entityAtTile(inFront).filter(e -> e.isPresent(PushableComponent.class)).toList());
    if (toMove.isEmpty()) return;

    // remove the BlockComponent to avoid blocking the player while moving simultaneously
    toMove.forEach(entity -> entity.remove(BlockComponent.class));
    toMove.add(hero);
    move(
        moveDirection,
        () -> {
          toMove.remove(hero);
          // give BlockComponent back
          toMove.forEach(entity -> entity.add(new BlockComponent()));
          turnEntity(hero, viewDirection);
          DISABLE_SHOOT_ON_HERO = false;
        },
        toMove.toArray(Entity[]::new));
  }

  /**
   * Moves the given entities simultaneously in a specific direction.
   *
   * <p>One move equals one tile.
   *
   * @param direction Direction in which the entities will move.
   * @param onFinish Callback to execute after the movement finishes.
   * @param entities Entities to move.
   */
  private void move(final Direction direction, IVoidFunction onFinish, final Entity... entities) {
    double distanceThreshold = 0.1;

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
      Tile targetTile = Game.tileAt(pc.position().translate(MAGIC_OFFSET), direction).orElse(null);
      if (targetTile == null
          || (!targetTile.isAccessible() && !(targetTile instanceof PitTile))
          || Game.entityAtTile(targetTile).anyMatch(e -> e.isPresent(BlockComponent.class))) {
        return; // if any target tile is not accessible, don't move anyone
      }

      entityComponents.add(new EntityComponents(pc, vc, targetTile.coordinate()));
    }

    double[] distances =
        entityComponents.stream()
            .mapToDouble(e -> e.pc.position().distance(e.targetPosition.toPoint()))
            .toArray();
    double[] lastDistances = new double[entities.length];
    this.makeStep.add(
        () -> {
          boolean allEntitiesArrived = true;
          for (int i = 0; i < entities.length; i++) {
            EntityComponents comp = entityComponents.get(i);
            comp.vc.clearForces();
            comp.vc.currentVelocity(Vector2.ZERO);
            comp.vc.applyForce(MOVEMENT_FORCE_ID, direction.scale((Client.MOVEMENT_FORCE.x())));

            lastDistances[i] = distances[i];
            distances[i] = comp.pc.position().distance(comp.targetPosition.toPoint());

            if (comp.vc().maxSpeed() > 0
                && Game.existInLevel(entities[i])
                && !(distances[i] <= distanceThreshold || distances[i] > lastDistances[i])) {
              allEntitiesArrived = false;
            }
          }
          if (allEntitiesArrived) {

            for (EntityComponents ec : entityComponents) {
              // TODO FIND A WAY TO MAKE THE HERO MOVE FASTER
              ec.vc.currentVelocity(Vector2.ZERO);
              ec.vc.clearForces();
              // check the position-tile via new request in case a new level was loaded
              Game.tileAt(ec.targetPosition().translate(MAGIC_OFFSET)).ifPresent(ec.pc::position);
            }
            onFinish.execute();
          }

          return allEntitiesArrived;
        });
  }

  /**
   * Moves the given entity in it's viewing direction.
   *
   * <p>One move equals one tile.
   *
   * @param entity Entity to move in its viewing direction.
   */
  private void move(final Entity entity) {
    move(EntityUtils.getViewDirection(entity), () -> {}, entity);
  }

  /**
   * Turns an entity into a specific direction and updates its animation.
   *
   * @param entity The entity to turn.
   * @param direction The new direction.
   */
  private void turnEntity(Entity entity, Direction direction) {
    entity.fetch(PositionComponent.class).ifPresent(pc -> pc.viewDirection(direction));
  }

  /**
   * Shoots a fireball in the player's viewing direction.
   *
   * <p>Requires the player to have ammunition. After shooting, the player briefly rests.
   */
  private void shootFireball() {
    FireballScheduler.shoot();
    rest(10);
  }

  /**
   * Triggers an interactable in a direction related to the player.
   *
   * @param direction Direction in which the player will search for an interactable.
   */
  private void interact(Direction direction) {
    Entity hero = Game.player().orElseThrow(MissingHeroException::new);
    PositionComponent pc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));

    Tile inDirection;

    if (direction == Direction.NONE) {
      inDirection = Game.tileAt(pc.position().translate(MAGIC_OFFSET)).orElse(null);
    } else {
      inDirection =
          Game.tileAt(
                  pc.position().translate(MAGIC_OFFSET),
                  pc.viewDirection().applyRelative(direction))
              .orElse(null);
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
   * ItemComponent} at the same tile as the player.
   *
   * <p>If the player is not on the map, nothing will happen.
   */
  private void pickup() {
    Game.player()
        .ifPresent(
            hero ->
                hero.fetch(PositionComponent.class)
                    .map(PositionComponent::position)
                    .map(pos -> pos.translate(MAGIC_OFFSET))
                    .flatMap(Game::tileAt)
                    .map(Game::entityAtTile)
                    .ifPresent(
                        stream ->
                            stream
                                .filter(e -> e.isPresent(BlocklyItemComponent.class))
                                .forEach(
                                    item ->
                                        item.fetch(InteractionComponent.class)
                                            .ifPresent(ic -> ic.triggerInteraction(item, hero)))));
  }

  /**
   * Drop a Blockly-Item at the heros position.
   *
   * <p>If the player is not on the map, nothing will happen.
   *
   * @param item Name of the item to drop
   */
  private void dropItem(String item) {
    Point heroPos =
        Game.player()
            .flatMap(hero -> hero.fetch(PositionComponent.class))
            .map(PositionComponent::position)
            .map(pos -> pos.translate(MAGIC_OFFSET))
            .orElse(null);

    switch (item) {
      case BREADCRUMB -> Game.add(MiscFactory.breadcrumb(heroPos));
      case CLOVER -> Game.add(MiscFactory.clover(heroPos));
      default ->
          throw new IllegalArgumentException("Can not convert " + item + " to droppable Item.");
    }
  }

  /** Let the player do nothing for a short moment. */
  private void rest() {
    rest = true;
    EventScheduler.scheduleAction(() -> rest = false, (long) (Gdx.graphics.getDeltaTime() * 1000));
  }

  /**
   * Lets the player do nothing for a period of time scaled by a multiplier.
   *
   * @param mul Time multiplier applied to the rest duration.
   */
  private void rest(int mul) {
    rest = true;
    EventScheduler.scheduleAction(
        () -> rest = false, (long) (Gdx.graphics.getDeltaTime() * 1000 * mul));
  }

  /** Stop the system and lock the queue, so no new commands can be added. */
  public void fullStop() {
    super.stop();
    this.disableQueue = true;
  }

  /** Reactivate the system and unlock the queue. */
  @Override
  public void run() {
    super.run();
    this.disableQueue = false;
  }

  /**
   * Helper record bundling the core components of an entity during movement.
   *
   * <p>This record is used internally when moving multiple entities simultaneously. It groups the
   * {@link PositionComponent}, {@link VelocityComponent}, and the target {@link Coordinate} for the
   * current move.
   *
   * @param pc The position component of the entity.
   * @param vc The velocity component of the entity.
   * @param targetPosition The coordinate the entity should move to.
   */
  private record EntityComponents(
      PositionComponent pc, VelocityComponent vc, Coordinate targetPosition) {}
}
