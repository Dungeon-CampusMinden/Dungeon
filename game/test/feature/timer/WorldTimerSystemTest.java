package feature.timer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.GdxNativesLoader;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link WorldTimerSystem}. */
class WorldTimerSystemTest {

  private final AtomicInteger expired = new AtomicInteger();
  private GL20 previousGl;
  private GL20 previousGl20;

  @BeforeEach
  void setUp() {
    GdxNativesLoader.load();
    previousGl = Gdx.gl;
    previousGl20 = Gdx.gl20;
    Gdx.gl = mock(GL20.class);
    Gdx.gl20 = Gdx.gl;
    Game.playClock().synchronize(5_000, false);
  }

  @AfterEach
  void tearDown() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.playClock().synchronize(0, false);
    Gdx.gl = previousGl;
    Gdx.gl20 = previousGl20;
  }

  // Shows the timer once and returns the width of its display.
  private float run(WorldTimerComponent timer) {
    Entity entity = new Entity("timer");
    entity.add(new PositionComponent(new Point(0, 0)));
    entity.add(timer);
    Game.add(entity);
    WorldTimerSystem system = new WorldTimerSystem().onTimerExpired(expired::incrementAndGet);
    Game.add(system);
    system.execute();
    Game.remove(entity);
    return entity.fetch(DrawComponent.class).orElseThrow().getWidth();
  }

  @Test
  void stopwatchNeverExpires() {
    run(WorldTimerComponent.stopwatch(0));

    assertEquals(0, expired.get());
  }

  @Test
  void countdownExpiresAtZero() {
    run(new WorldTimerComponent(0, 1));

    assertEquals(1, expired.get());
  }

  @Test
  void stopwatchShowsHoursAndMinutesAfterAnHour() {
    Game.playClock().synchronize(59 * 60_000 + 59_000, false);
    float minutesAndSeconds = run(WorldTimerComponent.stopwatch(0));
    // 100 minutes would need a third digit as minutes and seconds; 01:40 does not.
    Game.playClock().synchronize(100 * 60_000, false);

    assertEquals(minutesAndSeconds, run(WorldTimerComponent.stopwatch(0)));
  }
}
