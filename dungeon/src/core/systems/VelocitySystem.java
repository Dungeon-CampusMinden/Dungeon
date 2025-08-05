package core.systems;

import com.badlogic.gdx.Gdx;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.CoreAnimationPriorities;
import core.utils.components.draw.CoreAnimations;
import java.util.Optional;

/**
 * The VelocitySystem controls the movement of the entities in the game.
 *
 * <p>Entities with the {@link VelocityComponent}, {@link PositionComponent}, and {@link
 * DrawComponent} will be processed by this system.
 *
 * <p>The system will take the {@link VelocityComponent#currentVelocity()} and calculate the new
 * position of the entity based on their current position stored in the {@link PositionComponent}.
 * If the new position is a valid position, which means the tile they would stand on is accessible,
 * the new position will be set. If the new position is walled, the {@link
 * VelocityComponent#onWallHit()} callback will be executed.
 *
 * <p>This system will also queue the corresponding run or idle animation.
 *
 * <p>At the end, the {@link VelocityComponent#currentVelocity(Vector2)} will be set to 0.
 *
 * @see VelocityComponent
 * @see DrawComponent
 * @see PositionComponent
 * @see core.level.elements.ILevel
 */
public final class VelocitySystem extends System {

  // default time an Animation should be enqueued
  private static final int DEFAULT_FRAME_TIME = 1;

  /** Create a new VelocitySystem. */
  public VelocitySystem() {
    super(VelocityComponent.class, PositionComponent.class, DrawComponent.class);
  }

  /** Updates the position of all entities based on their velocity. */
  @Override
  public void execute() {
    filteredEntityStream(VelocityComponent.class, PositionComponent.class, DrawComponent.class)
        .map(this::buildDataObject)
        .forEach(this::updatePosition);
  }

  private void updatePosition(VSData vsd) {
    Vector2 originalVelocity = vsd.vc.currentVelocity();
    Vector2 velocity = originalVelocity;

    float maxSpeed = Math.max(Math.abs(vsd.vc.velocity().x()), Math.abs(vsd.vc.velocity().y()));
    // Limit velocity to maxSpeed (primarily for diagonal movement)
    if (velocity.length() > maxSpeed) {
      velocity = velocity.normalize();
      velocity = velocity.scale(maxSpeed);
    }

    if (Gdx.graphics != null) {
      velocity = velocity.scale(Gdx.graphics.getDeltaTime());
    }

    float newX = vsd.pc.position().x() + velocity.x();
    float newY = vsd.pc.position().y() + velocity.y();
    boolean hitWall = false;
    boolean canEnterOpenPits =
        vsd.e.fetch(VelocityComponent.class).map(VelocityComponent::canEnterOpenPits).orElse(false);
    try {
      Optional<Tile> tileXY = Game.tileAT(new Point(newX, newY));
      Optional<Tile> tileX = Game.tileAT(new Point(newX, vsd.pc.position().y()));
      Optional<Tile> tileY = Game.tileAT(new Point(vsd.pc.position().x(), newY));
      Optional<Tile> currentTile = Game.tileAT(vsd.pc.position());

      if (isAccessible(tileXY, canEnterOpenPits)) {
        vsd.pc.position(new Point(newX, newY));
        movementAnimation(vsd);
      } else if (isAccessible(tileX, canEnterOpenPits)) {
        hitWall = true;
        vsd.pc.position(new Point(newX, vsd.pc.position().y()));
        movementAnimation(vsd);
        vsd.vc.currentVelocity(Vector2.of(velocity.x(), 0.0f));
      } else if (isAccessible(tileY, canEnterOpenPits)) {
        hitWall = true;
        vsd.pc.position(new Point(vsd.pc.position().x(), newY));
        movementAnimation(vsd);
        vsd.vc.currentVelocity(Vector2.of(0.0f, velocity.y()));
      } else {
        hitWall = true;
      }

      if (hitWall) {
        vsd.vc.onWallHit().accept(vsd.e);
      }

      // Friction
      float friction = currentTile.map(Tile::friction).orElse(0.0f);
      float damp = Math.max(0.0f, 1.0f - friction);

      Vector2 toDampen = hitWall ? originalVelocity : velocity;
      float newVX = toDampen.x() * damp;
      if (Math.abs(newVX) < 0.01f) newVX = 0.0f;
      float newVY = toDampen.y() * damp;
      if (Math.abs(newVY) < 0.01f) newVY = 0.0f;

      vsd.vc.currentVelocity(Vector2.of(newVX, newVY));

    } catch (NullPointerException e) {
      vsd.pc.position(PositionComponent.ILLEGAL_POSITION);
      LOGGER.warning("Entity " + e + " is out of bound");
    }
  }
  /**
   * Helper method to check whether a tile is accessible.
   *
   * <p>A tile is considered accessible if it is present and either directly {@link Tile#isAccessible()},
   * or—if {@code canEnterPitTiles} is {@code true}—represents a {@link LevelElement#PIT}.
   *
   * @param tileOpt The {@link Optional} tile to check.
   * @param canEnterPitTiles Whether PIT tiles are allowed to be entered.
   * @return {@code true} if the tile is present and accessible; {@code false} otherwise.
   */
  private boolean isAccessible(Optional<Tile> tileOpt, boolean canEnterPitTiles) {
    return tileOpt
      .filter(t -> t.isAccessible() || (canEnterPitTiles && t.levelElement() == LevelElement.PIT))
      .isPresent();
  }

  private void movementAnimation(VSData vsd) {
    float x = vsd.vc.currentVelocity().x();
    float y = vsd.vc.currentVelocity().y();

    // move
    if (x != 0 || y != 0) {
      vsd.dc.deQueueByPriority(CoreAnimationPriorities.RUN.priority());
      if (x > 0) {
        vsd.dc.queueAnimation(CoreAnimations.RUN_RIGHT, CoreAnimations.RUN);
        vsd.pc.viewDirection(Direction.RIGHT);
      } else if (x < 0) {
        vsd.dc.queueAnimation(CoreAnimations.RUN_LEFT, CoreAnimations.RUN);
        vsd.pc.viewDirection(Direction.LEFT);
      } else if (y > 0) {
        vsd.dc.queueAnimation(CoreAnimations.RUN_UP, CoreAnimations.RUN);
        vsd.pc.viewDirection(Direction.UP);
      } else if (y < 0) {
        vsd.dc.queueAnimation(CoreAnimations.RUN_DOWN, CoreAnimations.RUN);
        vsd.pc.viewDirection(Direction.DOWN);
      }

      vsd.vc.previousVelocity(Vector2.of(x, y));

      vsd.dc.deQueueByPriority(CoreAnimationPriorities.IDLE.priority());
    }
    // idle
    else {
      // each drawComponent has an idle animation, so no check is needed
      switch (vsd.pc.viewDirection()) {
        case UP ->
            vsd.dc.queueAnimation(
                DEFAULT_FRAME_TIME,
                CoreAnimations.IDLE_UP,
                CoreAnimations.IDLE,
                CoreAnimations.IDLE_DOWN,
                CoreAnimations.IDLE_LEFT,
                CoreAnimations.IDLE_RIGHT);
        case LEFT ->
            vsd.dc.queueAnimation(
                DEFAULT_FRAME_TIME,
                CoreAnimations.IDLE_LEFT,
                CoreAnimations.IDLE,
                CoreAnimations.IDLE_RIGHT,
                CoreAnimations.IDLE_DOWN,
                CoreAnimations.IDLE_UP);
        case DOWN ->
            vsd.dc.queueAnimation(
                DEFAULT_FRAME_TIME,
                CoreAnimations.IDLE_DOWN,
                CoreAnimations.IDLE,
                CoreAnimations.IDLE_UP,
                CoreAnimations.IDLE_LEFT,
                CoreAnimations.IDLE_RIGHT);
        case RIGHT ->
            vsd.dc.queueAnimation(
                DEFAULT_FRAME_TIME,
                CoreAnimations.IDLE_RIGHT,
                CoreAnimations.IDLE,
                CoreAnimations.IDLE_LEFT,
                CoreAnimations.IDLE_DOWN,
                CoreAnimations.IDLE_UP);
        case NONE -> // Invalid direction
            LOGGER.warning(
                "Entity "
                    + vsd.e.id()
                    + " has an invalid view direction: "
                    + vsd.pc.viewDirection()
                    + ". Cannot queue idle animation.");
      }
    }
  }

  private VSData buildDataObject(Entity e) {
    VelocityComponent vc =
        e.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, VelocityComponent.class));

    PositionComponent pc =
        e.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, PositionComponent.class));

    DrawComponent dc =
        e.fetch(DrawComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, DrawComponent.class));

    return new VSData(e, vc, pc, dc);
  }

  private record VSData(Entity e, VelocityComponent vc, PositionComponent pc, DrawComponent dc) {}
}
