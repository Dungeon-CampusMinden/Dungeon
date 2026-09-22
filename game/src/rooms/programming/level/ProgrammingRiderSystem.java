package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.System;
import engine.components.DrawComponent;
import engine.components.InputComponent;
import engine.components.PositionComponent;
import engine.components.VelocityComponent;
import engine.systems.DrawSystem;
import engine.utils.Direction;
import engine.utils.Vector2;
import engine.utils.components.draw.state.StateMachine;
import feature.components.CollideComponent;
import feature.systems.PositionSync;

/** Attaches the real Act IV player to Nox after movement, on the host and receiving clients. */
final class ProgrammingRiderSystem extends System {
  private Entity rider;
  private Entity mount;
  private VelocityComponent walking;
  private boolean controlsDisabled;
  private boolean solid;
  private int depth;
  private Vector2 visualOffset;

  private ProgrammingRiderSystem() {
    super(AuthoritativeSide.BOTH);
    onEntityRemove =
        entity -> {
          if (entity == rider || entity == mount) release();
        };
  }

  /** Applies a newly received mount declaration without creating a second player entity. */
  static void refresh() {
    var system = Game.systems().get(ProgrammingRiderSystem.class);
    if (system == null
        && ProgrammingDecisions.state().map(state -> state.driver() >= 0).orElse(false)) {
      system = new ProgrammingRiderSystem();
      // Registered after the movement systems: the saddle uses this frame's final Nox position.
      Game.add(system);
    }
    if (system != null) system.execute();
  }

  @Override
  public void execute() {
    var state = ProgrammingDecisions.state().orElse(null);
    Entity nextRider =
        state == null
            ? null
            : Game.levelEntities()
                .filter(entity -> entity.id() == state.driver())
                .findFirst()
                .orElse(null);
    Entity nextMount =
        state == null
            ? null
            : Game.levelEntities()
                .filter(entity -> entity.id() == state.golemId())
                .findFirst()
                .orElse(null);
    // Despawn and snapshot application can leave a declared rider partially reconstructed.
    if (nextRider == null
        || nextMount == null
        || !nextRider.isPresent(PositionComponent.class)
        || !nextRider.isPresent(DrawComponent.class)
        || !nextMount.isPresent(PositionComponent.class)
        || !nextMount.isPresent(DrawComponent.class)) {
      release();
      return;
    }
    if (nextRider != rider || nextMount != mount) {
      release();
      rider = nextRider;
      mount = nextMount;
      walking = rider.fetch(VelocityComponent.class).orElse(null);
      controlsDisabled =
          rider.fetch(InputComponent.class).map(InputComponent::deactivateControls).orElse(false);
      solid = rider.fetch(CollideComponent.class).map(CollideComponent::isSolid).orElse(false);
      var draw = rider.fetch(DrawComponent.class).orElseThrow();
      depth =
          Game.isMultiplayerClient()
              ? mount.fetch(DrawComponent.class).orElseThrow().depth()
              : draw.depth();
      visualOffset = draw.visualOffset();
    }
    // The physical player remains inside Nox's floor footprint; only the sprite is raised.
    rider.remove(VelocityComponent.class);
    rider.fetch(InputComponent.class).ifPresent(input -> input.deactivateControls(true));
    rider.fetch(CollideComponent.class).ifPresent(collision -> collision.isSolid(false));
    var origin = mount.fetch(PositionComponent.class).orElseThrow();
    var position = rider.fetch(PositionComponent.class).orElseThrow();
    position.position(origin.position().translate(2, 1));
    position.viewDirection(origin.viewDirection());
    PositionSync.syncPosition(rider);
    var draw = rider.fetch(DrawComponent.class).orElseThrow();
    draw.stateMachine().setState(StateMachine.IDLE_STATE, origin.viewDirection());
    draw.visualOffset(saddle(origin.viewDirection()));
    int mountDepth = mount.fetch(DrawComponent.class).orElseThrow().depth();
    changeDepth(origin.viewDirection() == Direction.UP ? mountDepth + 1 : mountDepth);
  }

  private static Vector2 saddle(Direction direction) {
    return switch (direction) {
      case LEFT -> Vector2.of(-.4f, 3.25f);
      case RIGHT -> Vector2.of(-.5f, 3.25f);
      case UP -> Vector2.of(.05f, 2.8f);
      default -> Vector2.of(.05f, 3.6f);
    };
  }

  private void changeDepth(int value) {
    var draw = rider.fetch(DrawComponent.class).orElse(null);
    if (draw == null || draw.depth() == value) return;
    if (Game.systems().get(DrawSystem.class) instanceof DrawSystem renderer) {
      renderer.changeEntityDepth(rider, value);
      // Removal callbacks can run before or after the renderer's own removal callback.
      if (Game.findEntityById(rider.id()).isEmpty()) renderer.triggerOnRemove(rider);
    } else draw.depth(value);
  }

  private void release() {
    if (rider == null) return;
    if (walking != null) {
      walking.clearForces();
      walking.currentVelocity(Vector2.ZERO);
      rider.add(walking);
    }
    rider
        .fetch(InputComponent.class)
        .ifPresent(input -> input.deactivateControls(controlsDisabled));
    rider.fetch(CollideComponent.class).ifPresent(collision -> collision.isSolid(solid));
    rider.fetch(DrawComponent.class).ifPresent(draw -> draw.visualOffset(visualOffset));
    changeDepth(depth);
    rider = null;
    mount = null;
    walking = null;
  }
}
