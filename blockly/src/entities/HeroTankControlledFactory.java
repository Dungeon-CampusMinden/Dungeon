package entities;

import contrib.entities.EntityFactory;
import core.Entity;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.configuration.KeyboardConfig;
import core.utils.components.MissingComponentException;
import java.io.IOException;
import utils.BlocklyCommands;
import utils.Direction;

/**
 * This class is used to create a hero entity with tank controls. The hero can only move in the
 * direction it is facing. The tank controls are implemented by removing the original movement
 * controls and adding new controls for moving forward and turning left or right.
 */
public class HeroTankControlledFactory {

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
    pc.registerCallback(
        KeyboardConfig.MOVEMENT_UP.value(), HeroTankControlledFactory::moveEntityInFacingDirection);

    // Add rotation controls
    pc.registerCallback(
        KeyboardConfig.MOVEMENT_LEFT.value(),
        (entity) -> BlocklyCommands.rotate(Direction.LEFT),
        false);
    pc.registerCallback(
        KeyboardConfig.MOVEMENT_RIGHT.value(),
        (entity) -> BlocklyCommands.rotate(Direction.RIGHT),
        false);

    return hero;
  }

  private static void moveEntityInFacingDirection(Entity entity) {
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    PositionComponent.Direction direction = pc.viewDirection();
    VelocityComponent vc =
        entity
            .fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, VelocityComponent.class));

    switch (direction) {
      case UP -> vc.currentYVelocity(vc.yVelocity());
      case DOWN -> vc.currentYVelocity(-vc.yVelocity());
      case LEFT -> vc.currentXVelocity(-vc.xVelocity());
      case RIGHT -> vc.currentXVelocity(vc.xVelocity());
    }
  }
}
