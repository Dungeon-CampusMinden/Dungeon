package core.level;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Texture;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.elements.ILevel;
import core.level.generator.IGenerator;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;
import core.systems.LevelSystem;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.components.draw.Painter;
import core.utils.components.draw.PainterConfig;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/** WTF? . */
public class TileLevelAPITest {

  private LevelSystem api;
  private IGenerator generator;
  private Painter painter;
  private IVoidFunction onLevelLoader;
  private ILevel level;

  private MockedConstruction<Texture> textureMockedConstruction;

  /** WTF? . */
  @Before
  public void setup() {
    Texture texture = Mockito.mock(Texture.class);
    TextureMap textureMap = Mockito.mock(TextureMap.class);
    textureMockedConstruction = Mockito.mockConstruction(Texture.class);

    try (MockedStatic<TextureMap> textureMapMock = Mockito.mockStatic(TextureMap.class)) {
      textureMapMock.when(TextureMap::instance).thenReturn(textureMap);
    }
    when(textureMap.textureAt(any())).thenReturn(texture);

    painter = Mockito.mock(Painter.class);
    generator = Mockito.mock(IGenerator.class);
    onLevelLoader = Mockito.mock(IVoidFunction.class);
    level = Mockito.mock(TileLevel.class);
    api = new LevelSystem(painter, generator, onLevelLoader);
    Game.add(api);
  }

  /** WTF? . */
  @After
  public void cleanup() {
    Game.currentLevel(null);
    Game.removeAllEntities();
    Game.removeAllSystems();
    textureMockedConstruction.close();
  }

  /** WTF? . */
  @Test
  public void test_loadLevel() {
    when(generator.level(Mockito.any(), Mockito.any())).thenReturn(level);
    api.loadLevel(LevelSize.MEDIUM, DesignLabel.DEFAULT);
    verify(generator).level(eq(DesignLabel.DEFAULT), eq(LevelSize.MEDIUM));
    verify(onLevelLoader).execute();

    Mockito.verifyNoMoreInteractions(generator);
    Mockito.verifyNoMoreInteractions(onLevelLoader);

    assertEquals(level, LevelSystem.level());
  }

  /** WTF? . */
  @Test
  public void test_loadLevel_noParameter() {
    when(generator.level(Mockito.any(), Mockito.any())).thenReturn(level);
    api.loadLevel();
    verify(generator).level(Mockito.any(), Mockito.any());
    Mockito.verifyNoMoreInteractions(generator);
    verify(onLevelLoader).execute();
    Mockito.verifyNoMoreInteractions(onLevelLoader);
    assertEquals(level, LevelSystem.level());
  }

  /** WTF? . */
  @Test
  public void test_loadLevel_withDesign_noSize() {
    when(generator.level(eq(DesignLabel.DEFAULT), any())).thenReturn(level);
    api.loadLevel(DesignLabel.DEFAULT);
    verify(generator).level(eq(DesignLabel.DEFAULT), any());
    Mockito.verifyNoMoreInteractions(generator);
    verify(onLevelLoader).execute();
    Mockito.verifyNoMoreInteractions(onLevelLoader);
    assertEquals(level, LevelSystem.level());
  }

  /** WTF? . */
  @Test
  public void test_loadLevel_noDesign_WithSize() {
    when(generator.level(any(), eq(LevelSize.SMALL))).thenReturn(level);
    api.loadLevel(LevelSize.SMALL);
    verify(generator).level(any(), eq(LevelSize.SMALL));
    Mockito.verifyNoMoreInteractions(generator);
    verify(onLevelLoader).execute();
    Mockito.verifyNoMoreInteractions(onLevelLoader);
    assertEquals(level, LevelSystem.level());
  }

  /** WTF? . */
  @Test
  public void test_execute_draw() {
    IPath textureT1 = new SimpleIPath("dummyPath1");
    IPath textureT2 = new SimpleIPath("dummyPath2");
    IPath textureT3 = new SimpleIPath("dummyPath3");
    IPath textureT4 = new SimpleIPath("dummyPath4");
    Coordinate coordinateT1 = new Coordinate(0, 0);
    Coordinate coordinateT2 = new Coordinate(0, 1);
    Coordinate coordinateT3 = new Coordinate(1, 0);
    Coordinate coordinateT4 = new Coordinate(1, 1);
    LevelElement elementT1 = LevelElement.WALL;
    LevelElement elementT2 = LevelElement.EXIT;
    LevelElement elementT3 = LevelElement.WALL;
    LevelElement elementT4 = LevelElement.SKIP;
    Tile[][] layout = new Tile[2][2];
    layout[0][0] = Mockito.mock(Tile.class);
    when(layout[0][0].levelElement()).thenReturn(elementT1);
    when(layout[0][0].texturePath()).thenReturn(textureT1);
    when(layout[0][0].position()).thenReturn(coordinateT1.toPoint());
    layout[0][1] = Mockito.mock(Tile.class);
    when(layout[0][1].levelElement()).thenReturn(elementT2);
    when(layout[0][1].texturePath()).thenReturn(textureT2);
    when(layout[0][1].position()).thenReturn(coordinateT2.toPoint());
    layout[1][0] = Mockito.mock(Tile.class);
    when(layout[1][0].levelElement()).thenReturn(elementT3);
    when(layout[1][0].texturePath()).thenReturn(textureT3);
    when(layout[1][0].position()).thenReturn(coordinateT3.toPoint());
    layout[1][1] = Mockito.mock(Tile.class);
    when(layout[1][1].levelElement()).thenReturn(elementT4);
    when(layout[1][1].texturePath()).thenReturn(textureT4);
    when(layout[1][1].position()).thenReturn(coordinateT4.toPoint());

    when(level.layout()).thenReturn(layout);

    api.loadLevel(level);
    api.execute();

    verify(level).layout();
    verifyNoMoreInteractions(level);

    verify(layout[0][0]).levelElement();
    verify(layout[0][0]).texturePath();
    verify(layout[0][0]).position();
    // for some reason mockito.verify can't compare the points of the tile correctly
    verify(painter, times(3)).draw(any(Point.class), any(IPath.class), any(PainterConfig.class));
    verifyNoMoreInteractions(layout[0][0]);

    verify(layout[0][1]).levelElement();
    verify(layout[0][1]).texturePath();
    verify(layout[0][1]).position();
    // for some reason mockito.verify can't compare the points of the tile correctly
    verify(painter, times(3)).draw(any(Point.class), any(IPath.class), any(PainterConfig.class));
    verifyNoMoreInteractions(layout[0][1]);
    verify(layout[1][0]).levelElement();
    verify(layout[1][0]).texturePath();
    verify(layout[1][0]).position();
    // for some reason mockito.verify can't compare the points of the tile correctly
    verify(painter, times(3)).draw(any(Point.class), any(IPath.class), any(PainterConfig.class));
    verifyNoMoreInteractions(layout[1][0]);

    // do not draw skip tiles
    verify(layout[1][1]).levelElement();
    verifyNoMoreInteractions(layout[1][1]);
    verifyNoMoreInteractions(painter);
  }

  /** WTF? . */
  @Test
  public void test_execute_noLevel() {
    assertNull(LevelSystem.level());
    when(generator.level(any(), Mockito.any())).thenReturn(level);
    Tile[][] layout = new Tile[0][0];
    when(level.layout()).thenReturn(layout);
    // should load a new level if currentLevel==null
    api.execute();
    verify(onLevelLoader, times(1)).execute();
  }

  /** WTF? . */
  @Test
  public void test_execute_heroOnEndTile() throws IOException {
    when(generator.level(any(), Mockito.any())).thenReturn(level);
    api.loadLevel();
    Entity hero = new Entity();
    hero.add(new PositionComponent());
    hero.add(new PlayerComponent());
    Game.add(hero);

    Tile end = Mockito.mock(Tile.class);
    Point p = new Point(3, 3);
    when(end.position()).thenReturn(p);
    when(level.tileAt((Point) any())).thenReturn(end);
    Mockito.when(level.endTile()).thenReturn(end);

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
    Mockito.verifyNoInteractions(generator);
    verify(onLevelLoader).execute();
    Mockito.verifyNoMoreInteractions(onLevelLoader);
    assertEquals(level, LevelSystem.level());
  }
}
