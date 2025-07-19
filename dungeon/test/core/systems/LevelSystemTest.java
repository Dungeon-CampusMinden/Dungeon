package core.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Texture;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.TileLevel;
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

/** WTF? . */
public class LevelSystemTest {

  private LevelSystem api;
  private IVoidFunction onLevelLoader;
  private ILevel level;

  private MockedConstruction<Texture> textureMockedConstruction;

  /** WTF? . */
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
    level = Mockito.mock(TileLevel.class);
    api = new LevelSystem(onLevelLoader);
    Game.add(api);
  }

  /** WTF? . */
  @AfterEach
  public void cleanup() {
    Game.currentLevel(null);
    Game.removeAllEntities();
    Game.removeAllSystems();
    textureMockedConstruction.close();
  }

  /** WTF? . */
  @Test
  public void test_loadLevel() {
    api.loadLevel(level);
    verify(onLevelLoader).execute();
    Mockito.verifyNoMoreInteractions(onLevelLoader);
    assertEquals(level, LevelSystem.level());
  }

  /** WTF? . */
  @Test
  public void test_execute_noLevel() {
    assertNull(LevelSystem.level());
    api.execute();
    verify(onLevelLoader, times(0)).execute();
  }

  /** WTF? . */
  @Test
  public void test_execute_heroOnEndTile() throws IOException {
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
    when(level.tileAt((Point) any())).thenReturn(end);
    Mockito.when(level.endTile()).thenReturn(Optional.of(end));

    hero.fetch(PositionComponent.class).get().position(end);

    Tile[][] layout = new Tile[0][0];
    when(level.layout()).thenReturn(layout);
    api.execute();
    // first on loadLevel(), second on execute()
    verify(onLevelLoader, times(2)).execute();
  }

  /** WTF? . */
  @Test
  public void test_setLevel() {
    api.loadLevel(level);
    api.onEndTile(() -> api.loadLevel(level));
    verify(onLevelLoader).execute();
    Mockito.verifyNoMoreInteractions(onLevelLoader);
    assertEquals(level, LevelSystem.level());
  }
}
