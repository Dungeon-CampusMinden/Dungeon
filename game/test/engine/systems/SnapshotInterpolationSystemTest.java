package engine.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.graphics.Texture;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.game.PreRunConfiguration;
import engine.network.NetworkTelemetry;
import engine.network.messages.s2c.EntityState;
import engine.network.messages.s2c.LevelState;
import engine.network.messages.s2c.SnapshotMessage;
import engine.utils.Direction;
import engine.utils.Point;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.draw.state.DirectionalState;
import engine.utils.components.draw.state.StateMachine;
import engine.utils.components.path.SimpleIPath;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

/** Tests for {@link SnapshotInterpolationSystem}. */
public class SnapshotInterpolationSystemTest {

  private static final double TICK_NANOS = 1e9 / Game.TICKS_PER_SECOND;
  // A new connection is drawn two ticks behind the server.
  private static final int DELAY_TICKS = 2;

  private long now;
  private SnapshotInterpolationSystem system;

  /** Makes this runtime a multiplayer client with a clock the test moves. */
  @BeforeEach
  public void setup() {
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(false);
    system = new SnapshotInterpolationSystem(() -> now);
    Game.add(system);
  }

  /** Cleans global game state after each test. */
  @AfterEach
  public void cleanup() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    NetworkTelemetry.reset();
    TextureMap.instance().clear();
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);
  }

  /**
   * An entity is drawn on the way between the snapshots around the drawn moment and shows the
   * animation of the earlier one.
   */
  @Test
  public void drawsAnEntityBetweenTheSnapshotsAroundTheDrawnMoment() {
    Entity walker = new Entity(7);
    PositionComponent position = new PositionComponent(new Point(1, 0));
    walker.add(position);
    walker.add(walkingAndIdle());
    Game.add(walker);
    // The snapshot of tick 11 got lost.
    system.snapshot(snapshot(10, walker, new Point(0, 0), "run", Direction.RIGHT), at(10));
    for (int tick = 12; tick <= 11 + DELAY_TICKS; tick++) {
      system.snapshot(
          snapshot(tick, walker, new Point(1, 0), StateMachine.IDLE_STATE, Direction.UP), at(tick));
    }

    now = at(11 + DELAY_TICKS);
    system.execute();

    assertEquals(0.5f, position.renderPosition().x(), 0.001f);
    DrawComponent draw = walker.fetch(DrawComponent.class).orElseThrow();
    assertEquals("run", draw.currentStateName());
    assertEquals(Direction.RIGHT, draw.currentStateData());

    system.snapshot(
        snapshot(12 + DELAY_TICKS, walker, new Point(1, 0), StateMachine.IDLE_STATE, Direction.UP),
        at(12 + DELAY_TICKS));
    now = at(12.01 + DELAY_TICKS);
    system.execute();

    assertEquals(new Point(1, 0), position.renderPosition());
    assertEquals(StateMachine.IDLE_STATE, draw.currentStateName());
    assertEquals(Direction.UP, draw.currentStateData());
  }

  /**
   * A fast entity whose snapshot of one tick got lost still glides: three tiles in two ticks are a
   * move, three tiles in one tick would be a jump.
   */
  @Test
  public void drawsAFastMoveAcrossALostTickAsAGlide() {
    Entity entity = new Entity(7);
    PositionComponent position = new PositionComponent(new Point(3, 0));
    entity.add(position);
    Game.add(entity);
    system.snapshot(snapshot(10, entity, new Point(0, 0), null), at(10));
    for (int tick = 12; tick <= 11 + DELAY_TICKS; tick++) {
      system.snapshot(snapshot(tick, entity, new Point(3, 0), null), at(tick));
    }

    now = at(11 + DELAY_TICKS);
    system.execute();

    assertEquals(1.5f, position.renderPosition().x(), 0.001f);
  }

  /** A teleport is drawn at the tick of its snapshot, not as a glide towards it. */
  @Test
  public void drawsAJumpAtTheTickOfItsSnapshot() {
    Entity entity = new Entity(7);
    PositionComponent position = new PositionComponent(new Point(5, 0));
    entity.add(position);
    Game.add(entity);
    system.snapshot(snapshot(10, entity, new Point(0, 0), null), at(10));
    for (int tick = 11; tick <= 10 + DELAY_TICKS; tick++) {
      system.snapshot(snapshot(tick, entity, new Point(5, 0), null), at(tick));
    }

    now = at(10.5 + DELAY_TICKS);
    system.execute();
    assertEquals(new Point(0, 0), position.renderPosition());

    system.snapshot(
        snapshot(11 + DELAY_TICKS, entity, new Point(5, 0), null), at(11 + DELAY_TICKS));
    now = at(11.01 + DELAY_TICKS);
    system.execute();
    assertEquals(new Point(5, 0), position.renderPosition());
  }

  /**
   * An entity that spawns after its snapshots arrived takes over their animation state, although
   * that state did not change from one snapshot to the next.
   */
  @Test
  public void showsTheAnimationOfAnEntitySpawnedAfterItsSnapshots() {
    Entity walker = new Entity(7);
    walker.add(new PositionComponent(new Point(0, 0)));
    walker.add(walkingAndIdle());
    for (int tick = 10; tick <= 10 + DELAY_TICKS; tick++) {
      system.snapshot(snapshot(tick, walker, new Point(0, 0), "run"), at(tick));
    }
    now = at(10 + DELAY_TICKS);
    system.execute();

    Game.add(walker);
    system.snapshot(
        snapshot(11 + DELAY_TICKS, walker, new Point(0, 0), "run"), at(11 + DELAY_TICKS));
    now = at(11 + DELAY_TICKS);
    system.execute();

    assertEquals("run", walker.fetch(DrawComponent.class).orElseThrow().currentStateName());
  }

  /**
   * An entity whose first snapshot is newer than the drawn moment waits where that snapshot puts
   * it, not at its newest position.
   */
  @Test
  public void drawsANewEntityWhereItsFirstSnapshotPutsIt() {
    Entity old = new Entity(7);
    old.add(new PositionComponent(new Point(0, 0)));
    Game.add(old);
    Entity fresh = new Entity(8);
    PositionComponent position = new PositionComponent(new Point(4, 0));
    fresh.add(position);
    Game.add(fresh);
    for (int tick = 10; tick <= 13; tick++) {
      system.snapshot(snapshot(tick, old, new Point(0, 0), null), at(tick));
    }
    system.snapshot(snapshot(14, fresh, new Point(3, 0), null), at(14));
    system.snapshot(snapshot(15, fresh, new Point(4, 0), null), at(15));

    now = at(15);
    system.execute();

    assertEquals(new Point(3, 0), position.renderPosition());
  }

  /** In a new level the snapshots of the old one no longer say where an entity is drawn. */
  @Test
  public void clearDrawsEntitiesAtTheirPosition() {
    Entity entity = new Entity(7);
    PositionComponent position = new PositionComponent(new Point(0, 0));
    entity.add(position);
    Game.add(entity);
    for (int tick = 10; tick <= 10 + DELAY_TICKS; tick++) {
      system.snapshot(snapshot(tick, entity, new Point(1, 0), null), at(tick));
    }
    now = at(10 + DELAY_TICKS);
    system.execute();
    position.position(new Point(9, 9));

    system.clear();
    system.execute();

    assertEquals(new Point(9, 9), position.renderPosition());
  }

  /**
   * An entity moving at constant speed is drawn at constant speed, without ever waiting for a
   * snapshot, although its snapshots take 100 ms with 30 ms of jitter either way and overtake each
   * other, at frame rates that do and do not match the tick rate.
   *
   * @param framesPerSecond the frame rate of the client
   */
  @ParameterizedTest
  @ValueSource(ints = {60, 37})
  public void drawsConstantSpeedUnderLatencyAndJitter(int framesPerSecond) {
    float tilesPerTick = 5f / Game.TICKS_PER_SECOND;
    Entity entity = new Entity(7);
    PositionComponent position = new PositionComponent(new Point(0, 0));
    entity.add(position);
    Game.add(entity);
    Random random = new Random(3008);
    record Packet(int tick, long arrival) {}
    List<Packet> packets = new ArrayList<>();
    for (int tick = 1; tick <= 25 * Game.TICKS_PER_SECOND; tick++) {
      long latency = 100_000_000L + (long) ((random.nextDouble() * 2 - 1) * 30_000_000L);
      packets.add(new Packet(tick, at(tick) + latency));
    }
    packets.sort(Comparator.comparingLong(Packet::arrival));

    int next = 0;
    int applied = 0;
    int uneven = 0;
    float drawn = 0;
    long drawnAt = 0;
    for (int frame = 1; frame <= 24 * framesPerSecond; frame++) {
      now = (long) (frame * 1e9 / framesPerSecond);
      for (; next < packets.size() && packets.get(next).arrival() <= now; next++) {
        Packet packet = packets.get(next);
        // The client drops a snapshot that a newer one overtook.
        if (packet.tick() <= applied) continue;
        applied = packet.tick();
        Point truth = new Point(packet.tick() * tilesPerTick, 0);
        system.snapshot(snapshot(packet.tick(), entity, truth, null), packet.arrival());
      }
      system.execute();

      float x = position.renderPosition().x();
      // In the first seconds the delay grows to what the connection needs.
      if (frame > 2 * framesPerSecond) {
        double tilesPerSecond = (x - drawn) / ((now - drawnAt) / 1e9);
        if (Math.abs(tilesPerSecond - 5) > 5 * 0.25) uneven++;
      }
      drawn = x;
      drawnAt = now;
    }

    assertEquals(0, uneven, "frames drawn more than a quarter faster or slower");
  }

  private static long at(double tick) {
    return Math.round(tick * TICK_NANOS);
  }

  private static SnapshotMessage snapshot(int tick, Entity entity, Point position, String state) {
    return snapshot(tick, entity, position, state, Direction.RIGHT);
  }

  private static SnapshotMessage snapshot(
      int tick, Entity entity, Point position, String state, Direction direction) {
    EntityState.Builder builder = EntityState.builder().entityId(entity.id()).position(position);
    if (state != null) builder.stateName(state).viewDirection(direction);
    return new SnapshotMessage(tick, List.of(builder.build()), new LevelState(Set.of()));
  }

  private static DrawComponent walkingAndIdle() {
    TextureMap.instance().clear();
    TextureMap.instance()
        .put("test_assets/textures/test_hero/test_hero.png", Mockito.mock(Texture.class));
    Map<String, Animation> animations =
        Animation.loadAnimationSpritesheet(new SimpleIPath("test_assets/textures/test_hero"));
    return new DrawComponent(
        new StateMachine(
            List.of(
                new DirectionalState(StateMachine.IDLE_STATE, animations),
                new DirectionalState("run", animations, "run"))));
  }
}
