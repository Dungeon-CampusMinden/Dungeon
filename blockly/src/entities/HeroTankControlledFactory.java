package entities;

import client.Client;
import contrib.entities.HeroFactory;
import core.Entity;
import core.components.InputComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.configuration.KeyboardConfig;
import core.utils.Direction;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import java.io.IOException;
import utils.BlocklyCommands;

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
    Entity hero = HeroFactory.newHero();
    InputComponent ic =
        hero.fetch(InputComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, InputComponent.class));

    // Remove any original movement controls
    ic.removeCallback(KeyboardConfig.MOVEMENT_UP.value());
    ic.removeCallback(KeyboardConfig.MOVEMENT_DOWN.value());
    ic.removeCallback(KeyboardConfig.MOVEMENT_LEFT.value());
    ic.removeCallback(KeyboardConfig.MOVEMENT_RIGHT.value());

    // Add tank controls
    ic.registerCallback(
        KeyboardConfig.MOVEMENT_UP.value(), HeroTankControlledFactory::moveEntityInFacingDirection);

    // Add rotation controls
    ic.registerCallback(
        KeyboardConfig.MOVEMENT_LEFT.value(),
        (entity) -> BlocklyCommands.rotate(Direction.LEFT),
        false);
    ic.registerCallback(
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
    Direction direction = pc.viewDirection();
    VelocityComponent vc =
        entity
            .fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, VelocityComponent.class));

    Vector2 newVelocity = Vector2.ZERO;
    switch (direction) {
      case UP -> newVelocity = Vector2.of(0, Client.MOVEMENT_FORCE.y());
      case DOWN -> newVelocity = Vector2.of(0, -Client.MOVEMENT_FORCE.y());
      case LEFT -> newVelocity = Vector2.of(-Client.MOVEMENT_FORCE.x(), 0);
      case RIGHT -> newVelocity = Vector2.of(Client.MOVEMENT_FORCE.x(), 0);
    }
    vc.applyForce("MOVEMENT", newVelocity);
  }
}
