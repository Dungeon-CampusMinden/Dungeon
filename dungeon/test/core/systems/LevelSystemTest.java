package core.systems;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Texture;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.elements.tile.ExitTile;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.components.draw.TextureMap;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Unit tests for the {@link LevelSystem} class.
 *
 * <p>This class tests loading levels, executing the level system, handling hero movement on end
 * tiles, and ensuring proper callbacks are triggered.
 */
public class LevelSystemTest {

  private LevelSystem api;
  private IVoidFunction onLevelLoader;
  private ILevel level;

  private MockedConstruction<Texture> textureMockedConstruction;

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
    api = new LevelSystem(onLevelLoader);
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
   * Tests executing the LevelSystem when the hero is on the end tile.
   *
   * <p>Verifies that the onEndTile callback triggers reloading the level.
   *
   * @throws IOException if an error occurs during test execution
   */
  @Test
  public void test_execute_heroOnEndTile() throws IOException {
    api.loadLevel(level);
    api.onEndTile(() -> api.loadLevel(level));
    Entity hero = new Entity();
    hero.add(new PositionComponent());
    hero.add(new PlayerComponent(true));
    Game.add(hero);

    ExitTile end = Mockito.mock(ExitTile.class);
    Point p = new Point(3, 3);
    when(end.position()).thenReturn(p);
    when(end.isOpen()).thenReturn(true);
    when(level.tileAt((Point) any())).thenReturn(Optional.of(end));
    Mockito.when(level.endTile()).thenReturn(Optional.of(end));

    hero.fetch(PositionComponent.class).get().position(end);

    Tile[][] layout = new Tile[0][0];
    when(level.layout()).thenReturn(layout);
    api.execute();
    // first on loadLevel(), second on execute()
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
