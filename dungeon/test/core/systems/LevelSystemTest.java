package core.systems;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.elements.tile.ExitTile;
import core.level.loader.DungeonLoader;
import core.platform.Platform;
import core.platform.fs.FileSystemResourcesAdapter;
import core.utils.IVoidFunction;
import core.utils.Point;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Unit tests for the {@link LevelSystem} class.
 *
 * <p>This class tests loading levels, executing the level system, handling player movement on end
 * tiles, and ensuring proper callbacks are triggered.
 */
public class LevelSystemTest {

  private LevelSystem api;
  private IVoidFunction onLevelLoader;
  private ILevel level;

  /** Clears all levels from the DungeonLoader before all tests. */
  @BeforeAll
  public static void beforeAll() {
    DungeonLoader.clearLevels();
  }

  /** Sets up the test environment before each test. */
  @BeforeEach
  public void setup() {
    Platform.resources(FileSystemResourcesAdapter.autoDetect());

    onLevelLoader = Mockito.mock(IVoidFunction.class);
    level = Mockito.mock(DungeonLevel.class);
    api = new LevelSystem();
    api.onLevelLoad(onLevelLoader);
    Game.add(api);
  }

  /**
   * Cleans up after each test.
   *
   * <p>Removes the current level, all entities, and systems from the game, and closes any mocked
   * constructions.
   */
  @AfterEach
  public void cleanup() {
    Game.currentLevel(null);
    Game.removeAllEntities();
    Game.removeAllSystems();
  }

  /** Tests that a level is loaded correctly and the onLevelLoader callback is executed once. */
  @Test
  public void test_loadLevel() {
    api.loadLevel(level);
    verify(onLevelLoader).execute();
    Mockito.verifyNoMoreInteractions(onLevelLoader);
    assertEquals(level, LevelSystem.level().get());
  }

  /**
   * Tests that executing the LevelSystem with no level loaded does not trigger the onLevelLoader
   * callback.
   */
  @Test
  public void test_execute_noLevel() {
    assertTrue(LevelSystem.level().isEmpty());
    api.execute();
    verify(onLevelLoader, never()).execute();
  }

  /**
   * Tests executing the LevelSystem when the player is on the end tile.
   *
   * <p>Verifies that the onEndTile callback triggers reloading the level.
   */
  @Test
  public void test_execute_heroOnEndTile() {
    api.loadLevel(level);
    api.onEndTile(() -> api.loadLevel(level));

    Entity hero = new Entity();
    hero.add(new PositionComponent());
    hero.add(new PlayerComponent());
    Game.add(hero);

    ExitTile end = Mockito.mock(ExitTile.class);
    Point p = new Point(3, 3);
    when(end.position()).thenReturn(p);
    when(end.isOpen()).thenReturn(true);
    when(level.tileAt((Point) any())).thenReturn(Optional.of(end));
    when(level.endTile()).thenReturn(Optional.of(end));

    hero.fetch(PositionComponent.class).orElseThrow().position(end);

    Tile[][] layout = new Tile[0][0];
    when(level.layout()).thenReturn(layout);

    api.execute();

    verify(onLevelLoader, times(2)).execute();
  }

  /**
   * Tests that setting a level via loadLevel correctly sets the current level and triggers the
   * onLevelLoader callback.
   */
  @Test
  public void test_setLevel() {
    api.loadLevel(level);
    api.onEndTile(() -> api.loadLevel(level));
    verify(onLevelLoader).execute();
    Mockito.verifyNoMoreInteractions(onLevelLoader);
    assertEquals(level, LevelSystem.level().get());
  }
}
