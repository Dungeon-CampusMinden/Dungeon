package level;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import basiselements.Entity;
import com.badlogic.gdx.ai.pfa.GraphPath;
import level.elements.Tile;
import level.elements.TileLevel;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import tools.Point;

public class TileLevelTest {

    private TileLevel tileLevel;
    private Tile[][] layout;
    private Tile endTile;
    private Tile startTile;

    @Before
    public void setup() {
        layout = new Tile[3][3];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (x < 2) {
                    layout[y][x] =
                            new Tile(
                                    "",
                                    new Coordinate(x, y),
                                    LevelElement.FLOOR,
                                    DesignLabel.DEFAULT);
                } else {
                    layout[y][x] =
                            new Tile(
                                    "",
                                    new Coordinate(x, y),
                                    LevelElement.WALL,
                                    DesignLabel.DEFAULT);
                }
            }
        }

        tileLevel = new TileLevel(layout);
        endTile = tileLevel.getEndTile();
        startTile = tileLevel.getStartTile();
    }

    @Test
    public void test_levelCTOR_LevelElements() {
        LevelElement[][] elementsLayout = new LevelElement[2][2];
        elementsLayout[0][0] = LevelElement.WALL;
        elementsLayout[0][1] = LevelElement.FLOOR;
        elementsLayout[1][0] = LevelElement.WALL;
        elementsLayout[1][1] = LevelElement.FLOOR;
        tileLevel = new TileLevel(elementsLayout, DesignLabel.DEFAULT);
        Tile[][] layout = tileLevel.getLayout();
        assertSame(elementsLayout[0][0], layout[0][0].getLevelElement());
        assertSame(elementsLayout[1][0], layout[1][0].getLevelElement());
        assertTrue(
                elementsLayout[0][1] == layout[0][1].getLevelElement()
                        || LevelElement.EXIT == layout[0][1].getLevelElement());
        assertTrue(
                elementsLayout[1][1] == layout[1][1].getLevelElement()
                        || LevelElement.EXIT == layout[1][1].getLevelElement());
    }

    @Test
    public void test_findPath_onlyOnePathPossible() {
        layout = new Tile[3][3];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                layout[y][x] =
                        new Tile("", new Coordinate(x, y), LevelElement.FLOOR, DesignLabel.DEFAULT);
            }
        }
        layout[1][1] = new Tile("", new Coordinate(1, 1), LevelElement.WALL, DesignLabel.DEFAULT);
        layout[0][1] = new Tile("", new Coordinate(0, 1), LevelElement.WALL, DesignLabel.DEFAULT);
        tileLevel = new TileLevel(layout);
        tileLevel.setStartTile(layout[0][0]);
        tileLevel.setEndTile(layout[0][2]);
        /* How the level layout looks: (S=start, W=Wall,F=Floor,E=exit) SWE FWF FFF */
        GraphPath<Tile> path = tileLevel.findPath(tileLevel.getStartTile(), tileLevel.getEndTile());
        assertEquals(7, path.getCount());
        assertEquals(layout[0][0], path.get(0));
        assertEquals(layout[1][0], path.get(1));
        assertEquals(layout[2][0], path.get(2));
        assertEquals(layout[2][1], path.get(3));
        assertEquals(layout[2][2], path.get(4));
        assertEquals(layout[1][2], path.get(5));
        assertEquals(layout[0][2], path.get(6));
    }

    @Test
    public void test_findPath_moreThanOnePathPossible() {
        layout = new Tile[3][3];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                layout[y][x] =
                        new Tile("", new Coordinate(x, y), LevelElement.FLOOR, DesignLabel.DEFAULT);
            }
        }
        layout[0][1] = new Tile("", new Coordinate(0, 1), LevelElement.WALL, DesignLabel.DEFAULT);
        tileLevel = new TileLevel(layout);
        tileLevel.setStartTile(layout[0][0]);
        tileLevel.setEndTile(layout[0][2]);
        /* How the level layout looks: (S=start, W=Wall,F=Floor,E=exit) SWE FFF FFF */
        // should take the shortest path
        GraphPath<Tile> path = tileLevel.findPath(tileLevel.getStartTile(), tileLevel.getEndTile());
        assertEquals(5, path.getCount());
        assertEquals(layout[0][0], path.get(0));
        assertEquals(layout[1][0], path.get(1));
        assertEquals(layout[1][1], path.get(2));
        assertEquals(layout[1][2], path.get(3));
        assertEquals(layout[0][2], path.get(4));
    }

    @Test
    public void test_findPath_withSkips() {
        var levelElement = new LevelElement[3][2];
        for (int i = 0; i < 3; i++) {
            levelElement[i][0] = LevelElement.SKIP;
        }
        for (int i = 0; i < 3; i++) {
            levelElement[i][1] = LevelElement.FLOOR;
        }
        var level = new TileLevel(levelElement, DesignLabel.randomDesign());
        var start = level.getTileAt(new Coordinate(1, 0));
        var end = level.getTileAt(new Coordinate(1, 2));
        var path = level.findPath(end, start);
        assertEquals(3, path.getCount());
    }

    @Test
    public void test_findPath_withoutSkips() {
        var levelElement = new LevelElement[3][1];

        for (int i = 0; i < 3; i++) {
            levelElement[i][0] = LevelElement.FLOOR;
        }
        var level = new TileLevel(levelElement, DesignLabel.randomDesign());
        var start = level.getTileAt(new Coordinate(0, 0));
        var end = level.getTileAt(new Coordinate(0, 2));
        var path = level.findPath(end, start);
        assertEquals(3, path.getCount());
    }

    @Test
    public void test_isOnEndTile() {
        Entity entity = Mockito.mock(Entity.class);
        when(entity.getPosition()).thenReturn(endTile.getCoordinate().toPoint());
        assertTrue(tileLevel.isOnEndTile(entity));
        when(entity.getPosition()).thenReturn(startTile.getCoordinate().toPoint());
        assertFalse(tileLevel.isOnEndTile(entity));
    }

    @Test
    public void test_getTileAt() {
        assertEquals(layout[1][2], tileLevel.getTileAt(new Coordinate(2, 1)));
    }

    @Test
    public void test_getRandomTile() {
        assertNotNull(tileLevel.getRandomTile());
    }

    @Test
    public void test_getRandomTile_WithElementType() {
        Point randomWallPoint = tileLevel.getRandomTilePoint(LevelElement.WALL);
        assertNotNull(randomWallPoint);
        Tile randomWall = tileLevel.getTileAt(randomWallPoint.toCoordinate());
        assertNotNull(randomWall);
        assertEquals(LevelElement.WALL, randomWall.getLevelElement());
    }

    @Test
    public void test_getRandomTilePoint() {
        Point randomPoint = tileLevel.getRandomTilePoint();
        assertNotNull(randomPoint);
        assertNotNull(tileLevel.getTileAt(randomPoint.toCoordinate()));
    }

    @Test
    public void test_getRandomTilePoint_WithElementType() {
        Point randomWallPoint = tileLevel.getRandomTilePoint(LevelElement.WALL);
        Point randomFloorPoint = tileLevel.getRandomTilePoint(LevelElement.FLOOR);
        Tile randomWall = tileLevel.getTileAt(randomWallPoint.toCoordinate());
        Tile randomFloor = tileLevel.getTileAt(randomFloorPoint.toCoordinate());
        assertEquals(LevelElement.WALL, randomWall.getLevelElement());
        assertEquals(LevelElement.FLOOR, randomFloor.getLevelElement());
    }

    @Test
    public void test_getLayout() {
        assertArrayEquals(layout, tileLevel.getLayout());
    }

    @Test
    public void test_setStartTile() {
        Tile newStart = layout[2][2];
        tileLevel.setStartTile(newStart);
        assertEquals(LevelElement.FLOOR, newStart.getLevelElement());
        assertEquals(newStart, tileLevel.getStartTile());
    }

    @Test
    public void test_setEndTile() {
        Tile newEnd = layout[2][2];
        tileLevel.setEndTile(newEnd);
        assertEquals(LevelElement.FLOOR, endTile.getLevelElement());
        assertEquals(LevelElement.EXIT, newEnd.getLevelElement());
        assertEquals(newEnd, tileLevel.getEndTile());
    }

    @Test
    public void test_toString() {
        StringBuilder compareString = new StringBuilder();
        for (Tile[] tiles : layout) {
            for (int x = 0; x < layout[0].length; x++) {
                if (tiles[x].getLevelElement() == LevelElement.FLOOR) {
                    compareString.append("F");
                } else if (tiles[x].getLevelElement() == LevelElement.WALL) {
                    compareString.append("W");
                } else {
                    compareString.append("E");
                }
            }
            compareString.append("\n");
        }
        assertEquals(compareString.toString(), tileLevel.printLevel());
    }
}
