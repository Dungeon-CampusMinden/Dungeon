package engine.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.level.Tile;
import engine.level.elements.ILevel;
import engine.level.elements.tile.ExitTile;
import engine.level.loader.DungeonLoader;
import engine.utils.IVoidFunction;
import engine.utils.Point;
import engine.utils.Tuple;
import engine.utils.components.draw.TextureMap;
import feature.components.CollideComponent;
import feature.systems.PositionSync;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
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

  private MockedConstruction<Texture> textureMockedConstruction;

  /** Clears all levels from the DungeonLoader before all tests. */
  @BeforeAll
  public static void beforeAll() {
    DungeonLoader.clearLevels();
  }

  /**
   * Sets up mocks and initializes the LevelSystem before each test.
   *
   * <p>Mocks the {@link Texture} and {@link TextureMap}, initializes a mocked level, and adds the
   * system to the game.
   */
  @BeforeEach
  public void setup() {
    Texture texture = Mockito.mock(Texture.class);
    TextureMap textureMap = Mockito.mock(TextureMap.class);
    textureMockedConstruction = Mockito.mockConstruction(Texture.class);

    try (MockedStatic<TextureMap> textureMapMock = Mockito.mockStatic(TextureMap.class)) {
      textureMapMock.when(TextureMap::instance).thenReturn(textureMap);
    }
    when(textureMap.textureAt(any())).thenReturn(texture);

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
    DungeonLoader.clearLevels();
    Game.removeAllEntities();
    Game.removeAllSystems();
    textureMockedConstruction.close();
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
    hero.add(new PositionComponent(new Point(2.6f, 3)));
    hero.add(new CollideComponent());
    hero.add(new PlayerComponent());
    PositionSync.syncPosition(hero);
    Game.add(hero);

    ExitTile end = Mockito.mock(ExitTile.class);
    Point p = new Point(3, 3);
    when(end.position()).thenReturn(p);
    when(end.isOpen()).thenReturn(true);
    when(level.tileAtEntity(hero)).thenCallRealMethod();
    when(level.tileAt((Point) any()))
        .thenAnswer(
            invocation ->
                invocation.<Point>getArgument(0).toCoordinate().equals(p.toCoordinate())
                    ? Optional.of(end)
                    : Optional.empty());
    Mockito.when(level.endTile()).thenReturn(Optional.of(end));

    Tile[][] layout = new Tile[0][0];
    when(level.layout()).thenReturn(layout);
    api.execute();
    // first on loadLevel(), second on execute()
    verify(onLevelLoader, times(2)).execute();
  }

  /**
   * Tests that setting a registered in-memory level correctly sets the current level, triggers the
   * onLevelLoader callback, and models the absent resource variant.
   */
  @Test
  public void test_setLevel() {
    DungeonLoader.addLevel(new Tuple<>("in-memory", DungeonLevel.class));
    DungeonLoader.loadInMemoryLevel("in-memory", (DungeonLevel) level);
    api.onEndTile(() -> api.loadLevel(level));
    verify(onLevelLoader).execute();
    Mockito.verifyNoMoreInteractions(onLevelLoader);
    assertEquals(level, LevelSystem.level().get());
    assertTrue(DungeonLoader.currentVariantIndex().isEmpty());
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, DungeonLoader::reloadCurrentLevel);
    assertEquals(
        "Cannot reload an in-memory level without a resource variant", exception.getMessage());
  }
}
