package engine.systems;

import engine.Entity;
import engine.Game;
import engine.System;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.network.NetworkTelemetry;
import engine.network.client.SnapshotBuffer;
import engine.network.client.SnapshotClock;
import engine.network.messages.s2c.EntityState;
import engine.network.messages.s2c.SnapshotMessage;
import engine.utils.Direction;
import engine.utils.Point;
import java.util.Objects;
import java.util.Optional;
import java.util.function.LongSupplier;

/**
 * Draws the world of a multiplayer client from its snapshots.
 *
 * <p>Snapshots arrive unevenly, so the client draws a moment shortly before the newest one: the
 * {@link SnapshotClock} names that moment, the {@link SnapshotBuffer} the snapshots around it. Each
 * frame, every entity of a snapshot is drawn on the way between the two, with the animation of the
 * earlier one. The local hero is drawn the same way, so the whole world shows one moment.
 *
 * <p>A snapshot sets the {@link PositionComponent#position() position} at once: it is the state of
 * the simulation. This system alone sets where and in which animation state an entity is drawn. An
 * entity no snapshot holds, such as a local one, is drawn at its position.
 */
public final class SnapshotInterpolationSystem extends System {

  /** How many ticks behind the server the client draws. */
  private static final double DELAY_TICKS = 6;

  private final LongSupplier nanoTime;
  private final SnapshotClock clock = new SnapshotClock(DELAY_TICKS);
  private final SnapshotBuffer buffer = new SnapshotBuffer();
  // The snapshot whose animation states the entities show.
  private SnapshotBuffer.Frame shown;
  private long underruns;

  /**
   * Creates the system.
   *
   * @param nanoTime the clock the frames are timed with, as {@link java.lang.System#nanoTime()}
   */
  public SnapshotInterpolationSystem(LongSupplier nanoTime) {
    super(AuthoritativeSide.CLIENT, PositionComponent.class);
    this.nanoTime = nanoTime;
  }

  /**
   * Takes in a snapshot the client applied.
   *
   * @param snapshot the complete state of a server tick
   * @param receivedAtNanos the {@link java.lang.System#nanoTime()} at which the snapshot arrived
   */
  public void snapshot(SnapshotMessage snapshot, long receivedAtNanos) {
    buffer.add(snapshot);
    clock.observe(snapshot.serverTick(), receivedAtNanos);
  }

  /** Forgets all snapshots and draws every entity at its position, for a new level. */
  public void clear() {
    buffer.clear();
    shown = null;
    filteredEntityStream()
        .forEach(
            entity -> entity.fetch(PositionComponent.class).orElseThrow().renderPosition(null));
  }

  @Override
  public void execute() {
    // Without a server the game loop places the render positions between world ticks itself.
    if (!Game.isMultiplayerClient()) return;
    double renderTick = clock.renderTick(nanoTime.getAsLong());
    SnapshotBuffer.Span span = buffer.advance(renderTick).orElse(null);
    if (span == null) return;

    filteredEntityStream()
        .filter(entity -> !entity.isLocal())
        .forEach(entity -> draw(entity, span));
    shown = span.from();

    double reserveTicks = buffer.newestTick().orElseThrow() - renderTick;
    if (reserveTicks < 0) underruns++;
    NetworkTelemetry.recordInterpolation(
        clock.delayTicks() * Game.TICK_SECONDS * 1000,
        reserveTicks * Game.TICK_SECONDS * 1000,
        underruns);
  }

  private void draw(Entity entity, SnapshotBuffer.Span span) {
    PositionComponent position = entity.fetch(PositionComponent.class).orElseThrow();
    EntityState from = span.from().entities().get(entity.id());
    if (from == null) {
      // Newer than the drawn moment: the entity waits where the first snapshot with it puts it.
      position.renderPosition(
          buffer.oldest(entity.id()).flatMap(EntityState::position).orElse(null));
      return;
    }

    Optional<EntityState> to = span.to().map(frame -> frame.entities().get(entity.id()));
    position.renderPosition(
        from.position()
            .map(start -> between(start, to.flatMap(EntityState::position), span))
            .orElse(null));
    if (span.from() != shown) {
      showState(entity, from, shown == null ? null : shown.entities().get(entity.id()));
    }
  }

  // A jump, such as a teleport, shows at the tick of its snapshot instead of as a glide towards it.
  private static Point between(Point start, Optional<Point> end, SnapshotBuffer.Span span) {
    if (end.isEmpty() || start.equals(end.get())) return start;
    int ticks = span.to().orElseThrow().tick() - span.from().tick();
    if (PositionComponent.isJump(start, end.get(), ticks)) return start;
    float share = span.share();
    return new Point(
        start.x() + (end.get().x() - start.x()) * share,
        start.y() + (end.get().y() - start.y()) * share);
  }

  // Sets the animation state of a snapshot once, when it differs from the one shown before; in
  // between the state machine of the entity runs on its own.
  private static void showState(Entity entity, EntityState state, EntityState before) {
    if (state.stateName().isEmpty()) return;
    if (before != null
        && Objects.equals(before.stateName(), state.stateName())
        && Objects.equals(before.viewDirection(), state.viewDirection())) {
      return;
    }
    String stateName = state.stateName().get();
    entity
        .fetch(DrawComponent.class)
        .filter(draw -> draw.hasState(stateName))
        .ifPresent(draw ->
            draw.stateMachine().setState(stateName, direction(state.viewDirection().orElse(null))));
  }

  private static Direction direction(String name) {
    try {
      return name == null ? Direction.DOWN : Direction.valueOf(name);
    } catch (IllegalArgumentException unknown) {
      return Direction.DOWN;
    }
  }
}
