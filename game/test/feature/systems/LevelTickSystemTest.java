package feature.systems;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import engine.level.DungeonLevel;
import engine.level.elements.ILevel;
import engine.resources.MutableRes;
import engine.resources.Res;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link LevelTickSystem}.
 *
 * <p>The tests pass their own level handle to the system, so they do not depend on the global game
 * state.
 */
public class LevelTickSystemTest {

  /** The first execute on a level is reported as the first tick. */
  @Test
  public void firstExecuteIsFirstTick() {
    DungeonLevel level = mock(DungeonLevel.class);
    LevelTickSystem system = new LevelTickSystem(Res.of(level));

    system.execute();

    verify(level).onTick(true);
  }

  /** Later executes on the same level are not reported as the first tick. */
  @Test
  public void laterExecutesAreNotFirstTick() {
    DungeonLevel level = mock(DungeonLevel.class);
    LevelTickSystem system = new LevelTickSystem(Res.of(level));

    system.execute();
    system.execute();
    system.execute();

    verify(level).onTick(true);
    verify(level, times(2)).onTick(false);
  }

  /** After a level change, the new level gets its first tick and the old level is not ticked. */
  @Test
  public void newLevelGetsFirstTick() {
    DungeonLevel first = mock(DungeonLevel.class);
    DungeonLevel second = mock(DungeonLevel.class);
    MutableRes<ILevel> level = new MutableRes<>(first);
    LevelTickSystem system = new LevelTickSystem(level);
    system.execute();

    level.set(second);
    system.execute();

    verify(second).onTick(true);
    verify(first, never()).onTick(false);
  }

  /** A level that is loaded after some executes without a level still gets its first tick. */
  @Test
  public void levelLoadedLaterGetsFirstTick() {
    DungeonLevel level = mock(DungeonLevel.class);
    MutableRes<ILevel> handle = new MutableRes<>(null);
    LevelTickSystem system = new LevelTickSystem(handle);
    system.execute();
    system.execute();

    handle.set(level);
    system.execute();

    verify(level).onTick(true);
  }

  /** A level that does not implement ITickable is not ticked and causes no error. */
  @Test
  public void nonTickableLevelIsIgnored() {
    ILevel level = mock(ILevel.class);
    LevelTickSystem system = new LevelTickSystem(Res.of(level));

    system.execute();

    verifyNoInteractions(level);
  }

  /** Without a level, executing does nothing and causes no error. */
  @Test
  public void noLevelDoesNothing() {
    LevelTickSystem system = new LevelTickSystem(Res.empty());

    system.execute();
  }
}
