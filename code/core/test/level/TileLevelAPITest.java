package level;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.Painter;
import level.elements.ILevel;
import level.elements.Tile;
import level.elements.TileLevel;
import level.generator.IGenerator;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TileLevelAPITest {

    private LevelAPI api;
    private IGenerator generator;
    private Painter painter;
    private SpriteBatch batch;
    private IOnLevelLoader onLevelLoader;
    private ILevel level;

    @Before
    public void setup() {
        batch = Mockito.mock(SpriteBatch.class);
        painter = Mockito.mock(Painter.class);
        generator = Mockito.mock(IGenerator.class);
        onLevelLoader = Mockito.mock(IOnLevelLoader.class);
        level = Mockito.mock(TileLevel.class);
        api = new LevelAPI(batch, painter, generator, onLevelLoader);
    }

    @Test
    public void test_loadLevel() {
        when(generator.getLevel()).thenReturn(level);
        api.loadLevel();
        verify(generator).getLevel();
        Mockito.verifyNoMoreInteractions(generator);
        verify(onLevelLoader).onLevelLoad();
        Mockito.verifyNoMoreInteractions(onLevelLoader);
        assertEquals(level, api.getCurrentLevel());
    }

    @Test
    public void test_loadLevel_withDesign() {
        when(generator.getLevel(DesignLabel.DEFAULT)).thenReturn(level);
        api.loadLevel(DesignLabel.DEFAULT);
        verify(generator).getLevel(DesignLabel.DEFAULT);
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
        verify(layout[0][0], times(2)).getCoordinate();
        // for some reason mocktio.verify can't compare the points of the tile correctly
        verify(painter).draw(eq(textureT1), any(), eq(batch));
        verifyNoMoreInteractions(layout[0][0]);

        verify(layout[0][1]).getLevelElement();
        verify(layout[0][1]).getTexturePath();
        verify(layout[0][1], times(2)).getCoordinate();
        // for some reason mocktio.verify can't compare the points of the tile correctly
        verify(painter).draw(eq(textureT2), any(), eq(batch));
        verifyNoMoreInteractions(layout[0][1]);
        verify(layout[1][0]).getLevelElement();
        verify(layout[1][0]).getTexturePath();
        verify(layout[1][0], times(2)).getCoordinate();
        // for some reason mocktio.verify can't compare the points of the tile correctly
        verify(painter).draw(eq(textureT3), any(), eq(batch));
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
