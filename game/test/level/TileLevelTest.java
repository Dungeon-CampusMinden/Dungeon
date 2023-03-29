package level;

import static org.junit.Assert.*;

import com.badlogic.gdx.ai.pfa.GraphPath;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import level.elements.TileLevel;
import level.elements.tile.ExitTile;
import level.elements.tile.FloorTile;
import level.elements.tile.Tile;
import level.elements.tile.WallTile;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import org.junit.Test;
import tools.Point;

public class TileLevelTest {

    @Test
    public void test_levelCTOR_LevelElements() {
        LevelElement[][] elementsLayout =
                new LevelElement[][] {
                    {LevelElement.WALL, LevelElement.FLOOR}, {LevelElement.WALL, LevelElement.EXIT}
                };
        TileLevel tileLevel = new TileLevel(elementsLayout, DesignLabel.DEFAULT);
        Tile[][] layout = tileLevel.getLayout();
        assertSame(elementsLayout[0][0], layout[0][0].getLevelElement());
        assertSame(elementsLayout[1][0], layout[1][0].getLevelElement());
        assertSame(elementsLayout[0][1], layout[0][1].getLevelElement());
        assertSame(elementsLayout[1][1], layout[1][1].getLevelElement());
    }

    @Test
    public void test_levelCTOR_LevelElementsNoExit() {
        LevelElement[][] elementsLayout =
            new LevelElement[][] {
                {LevelElement.WALL, LevelElement.FLOOR}, {LevelElement.WALL, LevelElement.FLOOR}
            };
        TileLevel tileLevel = new TileLevel(elementsLayout, DesignLabel.DEFAULT);
        Tile[][] layout = tileLevel.getLayout();
        assertSame(elementsLayout[0][0], layout[0][0].getLevelElement());
        assertSame(elementsLayout[1][0], layout[1][0].getLevelElement());
        assertTrue("Es muss mindestens einen Ausgang geben!", layout[0][1].getLevelElement() == LevelElement.EXIT ||  layout[1][1].getLevelElement() == LevelElement.EXIT);
    }

    @Test
    public void test_levelCTOR_Tiles() {
        Tile[][] tileLayout =
            new Tile[][] {
                {
                    new WallTile("", new Coordinate(0, 0), DesignLabel.DEFAULT, null),
                    new FloorTile("", new Coordinate(1, 0), DesignLabel.DEFAULT, null)
                },
                {
                    new WallTile("", new Coordinate(0, 1), DesignLabel.DEFAULT, null),
                    new ExitTile("", new Coordinate(1, 1), DesignLabel.DEFAULT, null)
                }
            };
        TileLevel tileLevel = new TileLevel(tileLayout);
        Tile[][] layout = tileLevel.getLayout();
        assertArrayEquals(tileLayout,layout);
    }

    @Test
    public void test_levelCTOR_TilesNoExit() {
        Tile[][] tileLayout =
                new Tile[][] {
                    {
                        new WallTile("", new Coordinate(0, 0), DesignLabel.DEFAULT, null),
                        new FloorTile("", new Coordinate(1, 0), DesignLabel.DEFAULT, null)
                    },
                    {
                        new WallTile("", new Coordinate(0, 1), DesignLabel.DEFAULT, null),
                        new FloorTile("", new Coordinate(1, 1), DesignLabel.DEFAULT, null)
                    }
                };
        TileLevel tileLevel = new TileLevel(tileLayout);
        Tile[][] layout = tileLevel.getLayout();
        assertSame(tileLayout[0][0], layout[0][0]);
        assertSame(tileLayout[1][0], layout[1][0]);
        assertTrue("Es muss mindestens einen Ausgang geben!", layout[0][1].getLevelElement() == LevelElement.EXIT ||  layout[1][1].getLevelElement() == LevelElement.EXIT);
    }

    @Test
    public void test_findPath_onlyOnePathPossible() {
        Tile[][] layout = new Tile[3][3];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                layout[y][x] = new FloorTile("", new Coordinate(x, y), DesignLabel.DEFAULT, null);
            }
        }
        layout[1][1] = new WallTile("", new Coordinate(1, 1), DesignLabel.DEFAULT, null);
        layout[0][1] = new WallTile("", new Coordinate(1, 0), DesignLabel.DEFAULT, null);
        layout[0][2] = new ExitTile("", new Coordinate(2, 0), DesignLabel.DEFAULT, null);
        TileLevel tileLevel = new TileLevel(layout);
        tileLevel.setStartTile(layout[0][0]);

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
        Tile[][] layout = new Tile[3][3];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                layout[y][x] = new FloorTile("", new Coordinate(x, y), DesignLabel.DEFAULT, null);
            }
        }
        layout[0][1] = new WallTile("", new Coordinate(0, 1), DesignLabel.DEFAULT, null);
        TileLevel tileLevel = new TileLevel(layout);
        tileLevel.setStartTile(layout[0][0]);
        tileLevel.changeTileElementType(layout[0][2], LevelElement.EXIT);
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
    public void test_getTileAt() {
        var levelLayout = new LevelElement[3][3];

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                levelLayout[y][x] = LevelElement.FLOOR;
            }
        }
        var level = new TileLevel(levelLayout, DesignLabel.randomDesign());
        assertEquals(levelLayout[1][2], level.getTileAt(new Coordinate(2, 1)).getLevelElement());
    }

    @Test
    public void test_getRandomTile() {
        var levelLayout = new LevelElement[3][3];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                levelLayout[y][x] = LevelElement.FLOOR;
            }
        }
        var level = new TileLevel(levelLayout, DesignLabel.randomDesign());
        assertNotNull(level.getRandomTile());
    }

    @Test
    public void test_getRandomTile_WithElementType() {
        LevelElement[][] layout = new LevelElement[3][3];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (x < 2) {
                    layout[y][x] = LevelElement.FLOOR;
                } else {
                    layout[y][x] = LevelElement.WALL;
                }
            }
        }
        layout[2][1] = LevelElement.EXIT;

        TileLevel tileLevel = new TileLevel(layout, DesignLabel.DEFAULT);

        Point randomWallPoint = tileLevel.getRandomTilePoint(LevelElement.WALL);
        assertNotNull(randomWallPoint);
        Tile randomWall = tileLevel.getTileAt(randomWallPoint.toCoordinate());
        assertNotNull(randomWall);
        assertEquals(LevelElement.WALL, randomWall.getLevelElement());
    }

    @Test
    public void test_getRandomTilePoint() {
        var levelLayout = new LevelElement[3][3];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                levelLayout[y][x] = LevelElement.FLOOR;
            }
        }
        var level = new TileLevel(levelLayout, DesignLabel.randomDesign());
        Point randomPoint = level.getRandomTilePoint();
        assertNotNull(randomPoint);
        assertNotNull(level.getTileAt(randomPoint.toCoordinate()));
    }

    @Test
    public void test_getRandomTilePoint_WithElementType() {
        LevelElement[][] layout = new LevelElement[3][3];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (x < 2) {
                    layout[y][x] = LevelElement.FLOOR;
                } else {
                    layout[y][x] = LevelElement.WALL;
                }
            }
        }
        layout[2][1] = LevelElement.EXIT;

        TileLevel tileLevel = new TileLevel(layout, DesignLabel.DEFAULT);

        Point randomWallPoint = tileLevel.getRandomTilePoint(LevelElement.WALL);
        Point randomFloorPoint = tileLevel.getRandomTilePoint(LevelElement.FLOOR);
        Tile randomWall = tileLevel.getTileAt(randomWallPoint.toCoordinate());
        Tile randomFloor = tileLevel.getTileAt(randomFloorPoint.toCoordinate());
        assertEquals(LevelElement.WALL, randomWall.getLevelElement());
        assertEquals(LevelElement.FLOOR, randomFloor.getLevelElement());
    }

    @Test
    public void test_getLayout_from_TileLayout() {
        Tile[][] tileLayout =
                new Tile[][] {
                    new Tile[] {
                        new WallTile("", new Coordinate(0, 0), DesignLabel.DEFAULT, null),
                        new FloorTile("", new Coordinate(1, 0), DesignLabel.DEFAULT, null),
                    },
                    new Tile[] {
                        new FloorTile("", new Coordinate(0, 1), DesignLabel.DEFAULT, null),
                        new WallTile("", new Coordinate(1, 1), DesignLabel.DEFAULT, null),
                    }
                };

        var level = new TileLevel(tileLayout);
        assertArrayEquals(tileLayout, level.getLayout());
    }

    @Test
    public void test_toString() {
        LevelElement[][] tileLayout =
                new LevelElement[][] {
                    new LevelElement[] {
                        LevelElement.WALL, LevelElement.FLOOR,
                    },
                    new LevelElement[] {
                        LevelElement.EXIT, LevelElement.WALL,
                    }
                };
        var level = new TileLevel(tileLayout, DesignLabel.DEFAULT);
        StringBuilder compareString = new StringBuilder();
        for (LevelElement[] tiles : tileLayout) {
            for (int x = 0; x < tiles.length; x++) {
                if (tiles[x] == LevelElement.FLOOR) {
                    compareString.append("F");
                } else if (tiles[x] == LevelElement.WALL) {
                    compareString.append("W");
                } else {
                    compareString.append("E");
                }
            }
            compareString.append("\n");
        }
        assertEquals(compareString.toString(), level.printLevel());
    }

    @Test
    public void test_changeTileElementType_SameElementType() {
        LevelElement[][] layout =
                new LevelElement[][] {
                    new LevelElement[] {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}
                };
        TileLevel level = new TileLevel(layout, DesignLabel.DEFAULT);
        level.changeTileElementType(level.getTileAt(new Coordinate(0, 0)), LevelElement.FLOOR);
        assertEquals(3, level.getNodeCount());
        AtomicInteger counter = new AtomicInteger();
        Arrays.stream(level.getLayout())
                .flatMap(Arrays::stream)
                .sorted(Comparator.comparingInt(Tile::getIndex))
                .filter(Tile::isAccessible)
                .forEachOrdered(x -> assertEquals(counter.getAndIncrement(), x.getIndex()));
        assertEquals(3, counter.get());
    }

    @Test
    public void test_changeTileElementType_SameAccess() {
        LevelElement[][] layout =
                new LevelElement[][] {
                    new LevelElement[] {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}
                };
        TileLevel level = new TileLevel(layout, DesignLabel.DEFAULT);
        level.changeTileElementType(level.getTileAt(new Coordinate(0, 0)), LevelElement.EXIT);
        assertEquals(3, level.getNodeCount());
        AtomicInteger counter = new AtomicInteger();
        Arrays.stream(level.getLayout())
                .flatMap(Arrays::stream)
                .filter(Tile::isAccessible)
                .sorted(Comparator.comparingInt(Tile::getIndex))
                .forEachOrdered(x -> assertEquals(counter.getAndIncrement(), x.getIndex()));
        assertEquals(3, counter.get());
    }

    @Test
    public void test_changeTileElementType_toNotAccessible() {
        LevelElement[][] layout =
                new LevelElement[][] {
                    new LevelElement[] {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}
                };
        TileLevel level = new TileLevel(layout, DesignLabel.DEFAULT);
        level.changeTileElementType(level.getTileAt(new Coordinate(0, 0)), LevelElement.WALL);
        assertEquals(2, level.getNodeCount());
        AtomicInteger counter = new AtomicInteger();
        Arrays.stream(level.getLayout())
                .flatMap(Arrays::stream)
                .sorted(Comparator.comparingInt(Tile::getIndex))
                .filter(Tile::isAccessible)
                .forEachOrdered(x -> assertEquals(counter.getAndIncrement(), x.getIndex()));
        assertEquals(2, counter.get());
    }
}
