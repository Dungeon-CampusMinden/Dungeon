package entities.utility;

import contrib.entities.EntityFactory;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.configuration.KeyboardConfig;
import core.utils.MissingHeroException;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.CoreAnimations;
import java.io.IOException;

/**
 * This class is used to control and create the hero with tank controls. It allows the player to
 * only move in its current view direction.
 */
public class HeroTankController {

  /**
   * Creates a new hero with tank controls. The hero can only move in the direction it is facing.
   *
   * @return the hero entity
   * @throws IOException if there is an error creating the hero
   */
  public static Entity newTankControlledHero() throws IOException {
    Entity hero = EntityFactory.newHero();
    PlayerComponent pc =
        hero.fetch(PlayerComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PlayerComponent.class));

    // Remove any original movement controls
    pc.removeCallback(KeyboardConfig.MOVEMENT_UP.value());
    pc.removeCallback(KeyboardConfig.MOVEMENT_DOWN.value());
    pc.removeCallback(KeyboardConfig.MOVEMENT_LEFT.value());
    pc.removeCallback(KeyboardConfig.MOVEMENT_RIGHT.value());

    // Add tank controls
    pc.registerCallback(KeyboardConfig.MOVEMENT_UP.value(), HeroTankController::moveEntityInFacingDirection);

    // Add rotation controls
    pc.registerCallback(
        KeyboardConfig.MOVEMENT_LEFT.value(),
        (entity) -> rotateEntity(entity, PositionComponent.Direction.LEFT),
        false);
    pc.registerCallback(
        KeyboardConfig.MOVEMENT_RIGHT.value(),
        (entity) -> rotateEntity(entity, PositionComponent.Direction.RIGHT),
        false);

    return hero;
  }

  public static void rotateEntity(Entity entity, PositionComponent.Direction direction) {
    if (direction == PositionComponent.Direction.UP
        || direction == PositionComponent.Direction.DOWN) return;

    PositionComponent pc =
        entity.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    DrawComponent dc =
        entity.fetch(DrawComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, DrawComponent.class));
    VelocityComponent vc =
        entity.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, VelocityComponent.class));

    // Stop the hero from moving when rotating
    vc.currentXVelocity(0);
    vc.currentYVelocity(0);

    PositionComponent.Direction newDirection = updateRotation(direction, pc);

    CoreAnimations animation;
    switch (newDirection) {
      case UP -> animation = CoreAnimations.RUN_UP;
      case DOWN -> animation = CoreAnimations.RUN_DOWN;
      case LEFT -> animation = CoreAnimations.RUN_LEFT;
      case RIGHT -> animation = CoreAnimations.RUN_RIGHT;
      default -> throw new IllegalStateException("Unexpected value: " + direction);
    }

    dc.deQueue(CoreAnimations.RUN);
    dc.queueAnimation(animation, CoreAnimations.RUN);
  }

  private static PositionComponent.Direction updateRotation(
      PositionComponent.Direction direction, PositionComponent pc) {
    PositionComponent.Direction currentDirection = pc.viewDirection();
    PositionComponent.Direction newDirection = null;

    switch (direction) {
      case LEFT -> { // Counter-clockwise
        switch (currentDirection) {
          case UP -> newDirection = PositionComponent.Direction.LEFT;
          case LEFT -> newDirection = PositionComponent.Direction.DOWN;
          case DOWN -> newDirection = PositionComponent.Direction.RIGHT;
          case RIGHT -> newDirection = PositionComponent.Direction.UP;
        }
      }
      case RIGHT -> { // Clockwise
        switch (currentDirection) {
          case UP -> newDirection = PositionComponent.Direction.RIGHT;
          case LEFT -> newDirection = PositionComponent.Direction.UP;
          case DOWN -> newDirection = PositionComponent.Direction.LEFT;
          case RIGHT -> newDirection = PositionComponent.Direction.DOWN;
        }
      }
    }

    if (newDirection == null) {
      throw new IllegalStateException("Unexpected value: " + direction);
    }

    pc.viewDirection(newDirection);
    return newDirection;
  }

  public static void moveEntityInFacingDirection(Entity hero) {
    PositionComponent pc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    PositionComponent.Direction direction = pc.viewDirection();
    VelocityComponent vc =
        hero.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, VelocityComponent.class));

    switch (direction) {
      case UP -> vc.currentYVelocity(vc.yVelocity());
      case DOWN -> vc.currentYVelocity(-vc.yVelocity());
      case LEFT -> vc.currentXVelocity(-vc.xVelocity());
      case RIGHT -> vc.currentXVelocity(vc.xVelocity());
    }
  }
}
