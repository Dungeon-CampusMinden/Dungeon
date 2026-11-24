package engine.network.client;

import engine.Game;
import engine.network.messages.s2c.EntityState;
import engine.network.messages.s2c.SnapshotMessage;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The newest snapshots of a multiplayer client, kept to draw the moments between them.
 *
 * <p>Each snapshot holds the complete state of its server tick. {@link #advance} finds the two
 * snapshots around a tick and forgets what lies before them.
 */
public final class SnapshotBuffer {

  /** Most snapshots kept; older ones go when the drawn tick does not catch up. */
  private static final int CAPACITY = 2 * Game.TICKS_PER_SECOND;

  private final ArrayDeque<Frame> frames = new ArrayDeque<>();

  /**
   * The state of a server tick.
   *
   * @param tick the server tick
   * @param entities the state of every entity in the tick, by entity id
   */
  public record Frame(int tick, Map<Integer, EntityState> entities) {}

  /**
   * The snapshots around a tick.
   *
   * @param from the last snapshot up to the tick, or the oldest one if the tick lies before it
   * @param to the first snapshot after the tick; empty if none arrived yet or the tick lies before
   *     {@code from}
   * @param share how far the tick lies on the way from {@code from} to {@code to}, from 0 to 1
   */
  public record Span(Frame from, Optional<Frame> to, float share) {}

  /**
   * Adds a snapshot. A snapshot that is not newer than the newest one is ignored.
   *
   * @param snapshot the complete state of a server tick
   */
  public void add(SnapshotMessage snapshot) {
    if (!frames.isEmpty() && snapshot.serverTick() <= frames.getLast().tick()) return;
    Map<Integer, EntityState> entities = new HashMap<>();
    snapshot.entities().forEach(state -> entities.put(state.entityId(), state));
    frames.addLast(new Frame(snapshot.serverTick(), entities));
    if (frames.size() > CAPACITY) frames.removeFirst();
  }

  /**
   * Finds the snapshots around a tick and forgets the ones before them.
   *
   * @param tick the server tick to draw
   * @return the snapshots around the tick; empty if there is no snapshot
   */
  public Optional<Span> advance(double tick) {
    if (frames.isEmpty()) return Optional.empty();
    Frame from = frames.removeFirst();
    while (!frames.isEmpty() && frames.getFirst().tick() <= tick) from = frames.removeFirst();
    Frame next = frames.peekFirst();
    frames.addFirst(from);
    if (next == null || tick < from.tick()) return Optional.of(new Span(from, Optional.empty(), 0));
    float share = (float) ((tick - from.tick()) / (next.tick() - from.tick()));
    return Optional.of(new Span(from, Optional.of(next), share));
  }

  /**
   * Finds the oldest known state of an entity.
   *
   * @param entityId the id of the entity
   * @return its state in the oldest snapshot that contains it
   */
  public Optional<EntityState> oldest(int entityId) {
    for (Frame frame : frames) {
      EntityState state = frame.entities().get(entityId);
      if (state != null) return Optional.of(state);
    }
    return Optional.empty();
  }

  /**
   * Gets the tick of the newest snapshot.
   *
   * @return the newest tick; empty if there is no snapshot
   */
  public Optional<Integer> newestTick() {
    return Optional.ofNullable(frames.peekLast()).map(Frame::tick);
  }

  /** Forgets every snapshot. */
  public void clear() {
    frames.clear();
  }
}
