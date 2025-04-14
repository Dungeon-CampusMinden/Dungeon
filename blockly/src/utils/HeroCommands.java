package utils;

import com.badlogic.gdx.ai.pfa.GraphPath;
import components.AmmunitionComponent;
import components.BlockComponent;
import components.BlocklyItemComponent;
import components.PushableComponent;
import contrib.components.AIComponent;
import contrib.components.CollideComponent;
import contrib.components.InteractionComponent;
import contrib.components.ItemComponent;
import contrib.utils.EntityUtils;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.LevelUtils;
import core.utils.MissingHeroException;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import entities.MiscFactory;
import java.util.ArrayList;
import server.Server;

/**
 * This class implements all {@link BlocklyCommands} from the {@link contrib.entities.HeroFactory
 * Hero's} view.
 *
 * <p>E.g. While {@link BlocklyCommands} implements {@link BlocklyCommands#move(Entity)} this class
 * implements a {@link #move()} that moves the hero.
 *
 * <p>The main use of this class is for the {@link server.LanguageServer} to provide better context
 * for the user.
 */
public class HeroCommands {
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
    Direction viewDirection =
        Direction.fromPositionCompDirection(EntityUtils.getViewDirection(hero));
    BlocklyCommands.move(viewDirection, hero);
  }

  /** Moves the Hero to the Exit Block of the current Level. */
  public static void moveToExit() {
    if (Game.currentLevel().exitTiles().isEmpty()) return;
    Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
    Tile exitTile = Game.currentLevel().exitTiles().getFirst();

    PositionComponent pc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));

    GraphPath<Tile> pathToExit =
        LevelUtils.calculatePath(pc.position().toCoordinate(), exitTile.coordinate());

    for (Tile nextTile : pathToExit) {
      Tile currentTile = Game.tileAT(pc.position().toCoordinate());
      if (currentTile != nextTile) {
        PositionComponent.Direction viewDirection = EntityUtils.getViewDirection(hero);
        PositionComponent.Direction targetDirection =
            Direction.convertTileDirectionToPosDirection(currentTile.directionTo(nextTile)[0]);
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
    Direction viewDirection =
        Direction.fromPositionCompDirection(EntityUtils.getViewDirection(hero));
    Direction newDirection =
        switch (viewDirection) {
          case UP -> direction == Direction.LEFT ? Direction.LEFT : Direction.RIGHT;
          case DOWN -> direction == Direction.LEFT ? Direction.RIGHT : Direction.LEFT;
          case LEFT -> direction == Direction.LEFT ? Direction.DOWN : Direction.UP;
          case RIGHT -> direction == Direction.LEFT ? Direction.UP : Direction.DOWN;
          default -> throw new IllegalArgumentException("Can not rotate in " + viewDirection);
        };
    BlocklyCommands.turnEntity(hero, newDirection);
    Server.waitDelta();
  }

  /**
   * Shoots a fireball in direction the hero is facing.
   *
   * <p>The hero needs at least one unit of ammunition to successfully shoot a fireball.
   */
  public static void shootFireball() {
    Game.hero().orElseThrow(MissingHeroException::new).fetch(AmmunitionComponent.class).stream()
        .filter(AmmunitionComponent::checkAmmunition)
        .forEach(HeroCommands::aimAndShoot);
  }

  /** Triggers each interactable in front of the hero. */
  public static void interact() {
    Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
    PositionComponent pc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    Tile inFront = Game.tileAT(pc.position(), pc.viewDirection());
    Game.entityAtTile(inFront)
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
                Game.entityAtTile(Game.tileAT(EntityUtils.getHeroCoordinate()))
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
   */
  private static void aimAndShoot(AmmunitionComponent ac) {
    Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
    Direction viewDirection =
        Direction.fromPositionCompDirection(EntityUtils.getViewDirection(hero));
    Skill fireball =
        new Skill(
            new FireballSkill(
                () ->
                    hero.fetch(CollideComponent.class)
                        .map(cc -> cc.center(hero))
                        .map(p -> p.add(viewDirection.toPoint()))
                        .orElseThrow(
                            () -> MissingComponentException.build(hero, CollideComponent.class)),
                FIREBALL_RANGE,
                FIREBALL_SPEED,
                FIREBALL_DMG),
            1);
    fireball.execute(hero);
    ac.spendAmmo();
    Server.waitDelta();
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
    PositionComponent heroPC =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    PositionComponent.Direction viewDirection = heroPC.viewDirection();
    Tile inFront = Game.tileAT(heroPC.position(), viewDirection);
    Tile checkTile;
    Direction moveDirection;
    if (push) {
      checkTile = Game.tileAT(inFront.position(), viewDirection);
      moveDirection = Direction.fromPositionCompDirection(viewDirection);
    } else {
      checkTile = Game.tileAT(heroPC.position(), viewDirection.opposite());
      moveDirection = Direction.fromPositionCompDirection(viewDirection.opposite());
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
    BlocklyCommands.turnEntity(hero, Direction.fromPositionCompDirection(viewDirection));
    Server.waitDelta();
  }
}
