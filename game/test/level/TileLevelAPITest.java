package level;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.Painter;
import graphic.PainterConfig;
import graphic.textures.TextureMap;
import level.elements.ILevel;
import level.elements.TileLevel;
import level.elements.tile.Tile;
import level.generator.IGenerator;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import level.tools.LevelSize;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import tools.Point;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TextureMap.class})
public class TileLevelAPITest {

    private LevelAPI api;
    private IGenerator generator;
    private Texture texture;
    private TextureMap textureMap;
    private Painter painter;
    private SpriteBatch batch;
    private IOnLevelLoader onLevelLoader;
    private ILevel level;

    @Before
    public void setup() {
        batch = Mockito.mock(SpriteBatch.class);

        texture = Mockito.mock(Texture.class);
        textureMap = Mockito.mock(TextureMap.class);
        PowerMockito.mockStatic(TextureMap.class);
        when(TextureMap.getInstance()).thenReturn(textureMap);
        when(textureMap.getTexture(anyString())).thenReturn(texture);

        painter = Mockito.mock(Painter.class);
        generator = Mockito.mock(IGenerator.class);
        onLevelLoader = Mockito.mock(IOnLevelLoader.class);
        level = Mockito.mock(TileLevel.class);
        api = new LevelAPI(batch, painter, generator, onLevelLoader);
    }

    @Test
    public void test_loadLevel() {
        when(generator.getLevel(Mockito.any(), Mockito.any())).thenReturn(level);
        api.loadLevel(LevelSize.SMALL, DesignLabel.DEFAULT);
        verify(generator).getLevel(DesignLabel.DEFAULT, LevelSize.SMALL);
        Mockito.verifyNoMoreInteractions(generator);
        verify(onLevelLoader).onLevelLoad();
        Mockito.verifyNoMoreInteractions(onLevelLoader);
        assertEquals(level, api.getCurrentLevel());
    }

    @Test
    public void test_loadLevel_noParameter() {
        when(generator.getLevel(Mockito.any(), Mockito.any())).thenReturn(level);
        api.loadLevel();
        verify(generator).getLevel(Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(generator);
        verify(onLevelLoader).onLevelLoad();
        Mockito.verifyNoMoreInteractions(onLevelLoader);
        assertEquals(level, api.getCurrentLevel());
    }

    @Test
    public void test_loadLevel_withDesign_noSize() {
        when(generator.getLevel(eq(DesignLabel.DEFAULT), any())).thenReturn(level);
        api.loadLevel(DesignLabel.DEFAULT);
        verify(generator).getLevel(eq(DesignLabel.DEFAULT), any());
        Mockito.verifyNoMoreInteractions(generator);
        verify(onLevelLoader).onLevelLoad();
        Mockito.verifyNoMoreInteractions(onLevelLoader);
        assertEquals(level, api.getCurrentLevel());
    }

    @Test
    public void test_loadLevel_noDesign_WithSize() {
        when(generator.getLevel(any(), eq(LevelSize.SMALL))).thenReturn(level);
        api.loadLevel(LevelSize.SMALL);
        verify(generator).getLevel(any(), eq(LevelSize.SMALL));
        Mockito.verifyNoMoreInteractions(generator);
        verify(onLevelLoader).onLevelLoad();
        Mockito.verifyNoMoreInteractions(onLevelLoader);
        assertEquals(level, api.getCurrentLevel());
    }

    @Test
    public void test_update() {
        String textureT1 = "dummyPath1";
        String textureT2 = "dummyPath2";
        String textureT3 = "dummyPath3";
        String textureT4 = "dummyPath4";
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
        when(layout[0][0].getLevelElement()).thenReturn(elementT1);
        when(layout[0][0].getTexturePath()).thenReturn(textureT1);
        when(layout[0][0].getCoordinate()).thenReturn(coordinateT1);
        layout[0][1] = Mockito.mock(Tile.class);
        when(layout[0][1].getLevelElement()).thenReturn(elementT2);
        when(layout[0][1].getTexturePath()).thenReturn(textureT2);
        when(layout[0][1].getCoordinate()).thenReturn(coordinateT2);
        layout[1][0] = Mockito.mock(Tile.class);
        when(layout[1][0].getLevelElement()).thenReturn(elementT3);
        when(layout[1][0].getTexturePath()).thenReturn(textureT3);
        when(layout[1][0].getCoordinate()).thenReturn(coordinateT3);
        layout[1][1] = Mockito.mock(Tile.class);
        when(layout[1][1].getLevelElement()).thenReturn(elementT4);
        when(layout[1][1].getTexturePath()).thenReturn(textureT4);
        when(layout[1][1].getCoordinate()).thenReturn(coordinateT4);

        when(level.getLayout()).thenReturn(layout);

        api.setLevel(level);
        api.update();

        verify(level).getLayout();
        verifyNoMoreInteractions(level);

        verify(layout[0][0]).getLevelElement();
        verify(layout[0][0]).getTexturePath();
        verify(layout[0][0]).getCoordinate();
        // for some reason mocktio.verify can't compare the points of the tile correctly
        verify(painter, times(3))
                .draw(any(Point.class), any(String.class), any(PainterConfig.class));
        verifyNoMoreInteractions(layout[0][0]);

        verify(layout[0][1]).getLevelElement();
        verify(layout[0][1]).getTexturePath();
        verify(layout[0][1]).getCoordinate();
        // for some reason mocktio.verify can't compare the points of the tile correctly
        verify(painter, times(3))
                .draw(any(Point.class), any(String.class), any(PainterConfig.class));
        verifyNoMoreInteractions(layout[0][1]);
        verify(layout[1][0]).getLevelElement();
        verify(layout[1][0]).getTexturePath();
        verify(layout[1][0]).getCoordinate();
        // for some reason mocktio.verify can't compare the points of the tile correctly
        verify(painter, times(3))
                .draw(any(Point.class), any(String.class), any(PainterConfig.class));
        verifyNoMoreInteractions(layout[1][0]);

        // do not draw skip tiles
        verify(layout[1][1]).getLevelElement();
        verifyNoMoreInteractions(layout[1][1]);
        verifyNoMoreInteractions(painter);
    }

    @Test
    public void test_setLevel() {
        api.setLevel(level);
        Mockito.verifyNoInteractions(generator);
        verify(onLevelLoader).onLevelLoad();
        Mockito.verifyNoMoreInteractions(onLevelLoader);
        assertEquals(level, api.getCurrentLevel());
    }
}
