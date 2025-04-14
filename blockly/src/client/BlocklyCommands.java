package client;

import components.BlockComponent;
import contrib.components.*;
import contrib.utils.EntityUtils;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.util.ArrayList;
import java.util.List;
import server.Server;
import utils.Direction;

/**
 * This class contains all Blockly Block Commands that are available.
 *
 * @see HeroCommands
 * @see LevelCommands
 */
public class BlocklyCommands {

  /**
   * Moves the given entities simultaneously in a specific direction.
   *
   * <p>One move equals one tile.
   *
   * @param direction Direction in which the entities will be moved.
   * @param entities Entities to move simultaneously.
   */
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

      Tile targetTile = Game.tileAT(pc.position(), Direction.toPositionCompDirection(direction));
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
        comp.vc.currentXVelocity(direction.x() * comp.vc.xVelocity());
        comp.vc.currentYVelocity(direction.y() * comp.vc.yVelocity());

        lastDistances[i] = distances[i];
        distances[i] = comp.pc.position().distance(comp.targetPosition.toCenteredPoint());

        if (Game.findEntity(entities[i])
            && !(distances[i] <= distanceThreshold || distances[i] > lastDistances[i])) {
          allEntitiesArrived = false;
        }
      }

      if (allEntitiesArrived) break;

      Server.waitDelta();
    }

    for (EntityComponents ec : entityComponents) {
      ec.vc.currentXVelocity(0);
      ec.vc.currentYVelocity(0);
      // check the position-tile via new request in case a new level was loaded
      Tile endTile = Game.tileAT(ec.pc.position());
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
  public static void move(final Entity entity) {
    Direction viewDirection =
        Direction.fromPositionCompDirection(EntityUtils.getViewDirection(entity));
    move(viewDirection, entity);
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
    vc.currentXVelocity(direction.x());
    vc.currentYVelocity(direction.y());
    // so the player can not glitch inside the next tile
    pc.position(oldP);
  }
}
