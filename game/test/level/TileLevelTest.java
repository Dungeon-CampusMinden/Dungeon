package level;

import static org.junit.Assert.*;

import com.badlogic.gdx.ai.pfa.GraphPath;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import level.elements.TileLevel;
import level.elements.astar.TileConnection;
import level.elements.tile.ExitTile;
import level.elements.tile.FloorTile;
import level.elements.tile.Tile;
import level.elements.tile.TileFactory;
import level.elements.tile.WallTile;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import org.junit.Test;
import tools.Point;

public class TileLevelTest {

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
        assertArrayEquals(tileLayout, layout);
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
        assertTrue(
                "Es muss mindestens einen Ausgang geben!",
                layout[0][1].getLevelElement() == LevelElement.EXIT
                        || layout[1][1].getLevelElement() == LevelElement.EXIT);
    }

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
        assertTrue(
                "Es muss mindestens einen Ausgang geben!",
                layout[0][1].getLevelElement() == LevelElement.EXIT
                        || layout[1][1].getLevelElement() == LevelElement.EXIT);
    }

    @Test
    public void test_levelCTOR_LevelElementsNoFloors() {
        LevelElement[][] elementsLayout =
                new LevelElement[][] {
                    {LevelElement.WALL, LevelElement.WALL}, {LevelElement.WALL, LevelElement.WALL}
                };

        TileLevel level = new TileLevel(elementsLayout, DesignLabel.DEFAULT);
        assertNull(level.getStartTile());
        assertNull(level.getEndTile());
    }

    @Test
    public void test_levelCTOR_LevelElementsEnoughFloorsForStartButNotExit() {
        LevelElement[][] elementsLayout =
                new LevelElement[][] {
                    {LevelElement.WALL, LevelElement.FLOOR}, {LevelElement.WALL, LevelElement.WALL}
                };

        TileLevel level = new TileLevel(elementsLayout, DesignLabel.DEFAULT);
        assertNotNull(level.getStartTile());
        assertNull(level.getEndTile());
    }

    @Test
    public void test_levelCTOR_LevelElements_connections() {
        LevelElement[][] elementsLayout =
                new LevelElement[][] {{LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.EXIT}};
        TileLevel tileLevel = new TileLevel(elementsLayout, DesignLabel.DEFAULT);
        Tile[][] layout = tileLevel.getLayout();
        assertEquals(1, layout[0][0].getConnections().size);
        assertSame(layout[0][1], layout[0][0].getConnections().first().getToNode());
        assertEquals(2, layout[0][1].getConnections().size);
        assertSame(layout[0][0], layout[0][1].getConnections().get(0).getToNode());
        assertSame(layout[0][2], layout[0][1].getConnections().get(1).getToNode());
        assertEquals(1, layout[0][2].getConnections().size);
        assertSame(layout[0][1], layout[0][2].getConnections().first().getToNode());
    }

    @Test
    public void test_levelCTOR_LevelElements_tileTypeLists() {
        LevelElement[][] elementsLayout =
                new LevelElement[][] {
                    {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.EXIT, LevelElement.SKIP},
                    {LevelElement.WALL, LevelElement.WALL, LevelElement.SKIP, LevelElement.SKIP},
                    {LevelElement.DOOR, LevelElement.DOOR, LevelElement.HOLE, LevelElement.HOLE},
                };
        TileLevel tileLevel = new TileLevel(elementsLayout, DesignLabel.DEFAULT);
        assertEquals(2, tileLevel.getFloorTiles().size());
        assertEquals(2, tileLevel.getDoorTiles().size());
        assertEquals(2, tileLevel.getHoleTiles().size());
        assertEquals(2, tileLevel.getWallTiles().size());
        assertEquals(3, tileLevel.getSkipTiles().size());
    }

    @Test
    public void test_nodeCount_NoAccessible() {
        LevelElement[][] elementsLayout =
                new LevelElement[][] {
                    {LevelElement.WALL, LevelElement.WALL, LevelElement.WALL, LevelElement.WALL},
                };
        TileLevel tileLevel = new TileLevel(elementsLayout, DesignLabel.DEFAULT);
        assertEquals(0, tileLevel.getNodeCount());
    }

    @Test
    public void test_nodeCount_OneAccessible() {
        LevelElement[][] elementsLayout =
                new LevelElement[][] {
                    {LevelElement.FLOOR, LevelElement.WALL, LevelElement.WALL, LevelElement.WALL},
                };
        TileLevel tileLevel = new TileLevel(elementsLayout, DesignLabel.DEFAULT);
        assertEquals(1, tileLevel.getNodeCount());
    }

    @Test
    public void test_nodeCount_FourAccessible() {
        LevelElement[][] elementsLayout =
                new LevelElement[][] {
                    {
                        LevelElement.FLOOR,
                        LevelElement.FLOOR,
                        LevelElement.FLOOR,
                        LevelElement.FLOOR
                    },
                };
        TileLevel tileLevel = new TileLevel(elementsLayout, DesignLabel.DEFAULT);
        assertEquals(4, tileLevel.getNodeCount());
    }

    @Test
    public void test_setRandomEnd() {
        LevelElement[][] elementsLayout =
                new LevelElement[][] {
                    {LevelElement.EXIT, LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR},
                };
        TileLevel tileLevel = new TileLevel(elementsLayout, DesignLabel.DEFAULT);
        Tile oldEndTile = tileLevel.getEndTile();
        tileLevel.setRandomEnd();
        assertNotSame(tileLevel.getStartTile(), tileLevel.getEndTile());
        assertNotSame(oldEndTile, tileLevel.getEndTile());
    }

    @Test
    public void test_setRandomEnd_NoFreeFloors() {
        LevelElement[][] elementsLayout =
                new LevelElement[][] {
                    {LevelElement.FLOOR},
                };
        TileLevel tileLevel = new TileLevel(elementsLayout, DesignLabel.DEFAULT);
        tileLevel.setRandomEnd();
        assertNotSame(tileLevel.getStartTile(), tileLevel.getEndTile());
        assertNull(tileLevel.getEndTile());
    }

    @Test
    public void test_setRandomEnd_NoFloors() {
        LevelElement[][] elementsLayout =
                new LevelElement[][] {
                    {LevelElement.WALL},
                };
        TileLevel tileLevel = new TileLevel(elementsLayout, DesignLabel.DEFAULT);
        tileLevel.setRandomEnd();
        assertNull(tileLevel.getEndTile());
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
            Arrays.fill(levelLayout[y], LevelElement.FLOOR);
        }
        levelLayout[0][0] = LevelElement.EXIT;
        var level = new TileLevel(levelLayout, DesignLabel.randomDesign());
        assertEquals(levelLayout[1][2], level.getTileAt(new Coordinate(2, 1)).getLevelElement());
    }

    @Test
    public void test_getRandomTile() {
        var levelLayout = new LevelElement[3][3];
        for (int y = 0; y < 3; y++) {
            Arrays.fill(levelLayout[y], LevelElement.FLOOR);
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
            Arrays.fill(levelLayout[y], LevelElement.FLOOR);
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
    public void test_addTile_FloorTile() {
        TileLevel level =
                new TileLevel(
                        new LevelElement[][] {
                            {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}
                        },
                        DesignLabel.DEFAULT);
        Tile tile =
                TileFactory.createTile(
                        "", new Coordinate(1, 0), LevelElement.FLOOR, DesignLabel.DEFAULT);
        level.removeTile(level.getLayout()[0][1]);
        level.getLayout()[0][1] = tile;
        level.addTile(tile);
        assertTrue(
                "tile needs to be added to specific Tile list",
                level.getFloorTiles().contains(tile));
        assertEquals(level.getNodeCount() - 1, tile.getIndex());
        assertTrue(
                "All neighbouring tiles need to be informed about the new tile",
                level.getFloorTiles().stream()
                        .filter(x -> !(x == tile))
                        .allMatch(
                                x ->
                                        x.getConnections().size == 1
                                                && x.getConnections()
                                                        .contains(
                                                                new TileConnection(x, tile),
                                                                false)));
        assertSame("tile needs to know its new level", level, tile.getLevel());
        assertEquals(
                "each accessible tile needs to have a unique index",
                3,
                Arrays.stream(level.getLayout())
                        .flatMap(x -> Arrays.stream(x).map(Tile::getIndex))
                        .distinct()
                        .count());
    }

    @Test
    public void test_addTile_ExitTile() {
        TileLevel level =
                new TileLevel(
                        new LevelElement[][] {
                            {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}
                        },
                        DesignLabel.DEFAULT);
        Tile tile =
                TileFactory.createTile(
                        "", new Coordinate(1, 0), LevelElement.EXIT, DesignLabel.DEFAULT);
        level.removeTile(level.getLayout()[0][1]);
        level.getLayout()[0][1] = tile;
        level.addTile(tile);
        assertTrue(
                "tile needs to be added to specific Tile list",
                level.getExitTiles().contains(tile));
        assertEquals(level.getNodeCount() - 1, tile.getIndex());
        assertTrue(
                "All neighbouring tiles need to be informed about the new tile",
                level.getFloorTiles().stream()
                        .filter(x -> !(x == tile))
                        .allMatch(
                                x ->
                                        x.getConnections().size == 1
                                                && x.getConnections()
                                                        .contains(
                                                                new TileConnection(x, tile),
                                                                false)));
        assertSame("tile needs to know its new level", level, tile.getLevel());
        assertEquals(
                "each accessible tile needs to have a unique index",
                3,
                Arrays.stream(level.getLayout())
                        .flatMap(x -> Arrays.stream(x).map(Tile::getIndex))
                        .distinct()
                        .count());
    }

    @Test
    public void test_addTile_DoorTile() {
        TileLevel level =
                new TileLevel(
                        new LevelElement[][] {
                            {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}
                        },
                        DesignLabel.DEFAULT);
        Tile tile =
                TileFactory.createTile(
                        "", new Coordinate(1, 0), LevelElement.DOOR, DesignLabel.DEFAULT);
        level.removeTile(level.getLayout()[0][1]);
        level.getLayout()[0][1] = tile;
        level.addTile(tile);
        assertTrue(
                "tile needs to be added to specific Tile list",
                level.getDoorTiles().contains(tile));
        assertEquals(level.getNodeCount() - 1, tile.getIndex());
        assertTrue(
                "All neighbouring tiles need to be informed about the new tile",
                level.getFloorTiles().stream()
                        .filter(x -> !(x == tile))
                        .allMatch(
                                x ->
                                        x.getConnections().size == 1
                                                && x.getConnections()
                                                        .contains(
                                                                new TileConnection(x, tile),
                                                                false)));
        assertSame("tile needs to know its new level", level, tile.getLevel());
        assertEquals(
                "each accessible tile needs to have a unique index",
                3,
                Arrays.stream(level.getLayout())
                        .flatMap(x -> Arrays.stream(x).map(Tile::getIndex))
                        .distinct()
                        .count());
    }

    @Test
    public void test_addTile_SkipTile() {
        TileLevel level =
                new TileLevel(
                        new LevelElement[][] {
                            {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}
                        },
                        DesignLabel.DEFAULT);
        Tile tile =
                TileFactory.createTile(
                        "", new Coordinate(1, 0), LevelElement.SKIP, DesignLabel.DEFAULT);
        level.removeTile(level.getLayout()[0][1]);
        level.getLayout()[0][1] = tile;
        level.addTile(tile);
        assertTrue(
                "tile needs to be added to specific Tile list",
                level.getSkipTiles().contains(tile));
        assertEquals(0, tile.getIndex());
        assertTrue(
                "All neighbouring tiles need to be informed about the new tile",
                level.getFloorTiles().stream()
                        .filter(x -> !(x == tile))
                        .allMatch(x -> x.getConnections().size == 0));
        assertSame("tile needs to know its new level", level, tile.getLevel());
        assertEquals(
                "each accessible tile needs to have a unique index",
                2,
                Arrays.stream(level.getLayout())
                        .flatMap(
                                x ->
                                        Arrays.stream(x)
                                                .filter(Tile::isAccessible)
                                                .map(Tile::getIndex))
                        .distinct()
                        .count());
    }

    @Test
    public void test_addTile_WallTile() {
        TileLevel level =
                new TileLevel(
                        new LevelElement[][] {
                            {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}
                        },
                        DesignLabel.DEFAULT);
        Tile tile =
                TileFactory.createTile(
                        "", new Coordinate(1, 0), LevelElement.WALL, DesignLabel.DEFAULT);
        level.removeTile(level.getLayout()[0][1]);
        level.getLayout()[0][1] = tile;
        level.addTile(tile);
        assertTrue(
                "tile needs to be added to specific Tile list",
                level.getWallTiles().contains(tile));
        assertEquals(0, tile.getIndex());
        assertTrue(
                "All neighbouring tiles need to be informed about the new tile",
                level.getFloorTiles().stream()
                        .filter(x -> !(x == tile))
                        .allMatch(x -> x.getConnections().size == 0));
        assertSame("tile needs to know its new level", level, tile.getLevel());
        assertEquals(
                "each accessible tile needs to have a unique index",
                2,
                Arrays.stream(level.getLayout())
                        .flatMap(
                                x ->
                                        Arrays.stream(x)
                                                .filter(Tile::isAccessible)
                                                .map(Tile::getIndex))
                        .distinct()
                        .count());
    }

    @Test
    public void test_addTile_HoleTile() {
        TileLevel level =
                new TileLevel(
                        new LevelElement[][] {
                            {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}
                        },
                        DesignLabel.DEFAULT);
        Tile tile =
                TileFactory.createTile(
                        "", new Coordinate(1, 0), LevelElement.HOLE, DesignLabel.DEFAULT);
        level.removeTile(level.getLayout()[0][1]);
        level.getLayout()[0][1] = tile;
        level.addTile(tile);
        assertTrue(
                "tile needs to be added to specific Tile list",
                level.getHoleTiles().contains(tile));
        assertEquals(0, tile.getIndex());
        assertTrue(
                "All neighbouring tiles need to be informed about the new tile",
                level.getFloorTiles().stream()
                        .filter(x -> !(x == tile))
                        .allMatch(x -> x.getConnections().size == 0));
        assertSame("tile needs to know its new level", level, tile.getLevel());
        assertEquals(
                "each accessible tile needs to have a unique index",
                2,
                Arrays.stream(level.getLayout())
                        .flatMap(
                                x ->
                                        Arrays.stream(x)
                                                .filter(Tile::isAccessible)
                                                .map(Tile::getIndex))
                        .distinct()
                        .count());
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

    @Test
    public void test_changeTileElementType_notOnLevel() {
        LevelElement[][] layout =
                new LevelElement[][] {
                    new LevelElement[] {LevelElement.FLOOR, LevelElement.FLOOR, LevelElement.FLOOR}
                };
        TileLevel level = new TileLevel(layout, DesignLabel.DEFAULT);
        level.changeTileElementType(
                TileFactory.createTile(
                        "", new Coordinate(1, 0), LevelElement.FLOOR, DesignLabel.DEFAULT),
                LevelElement.WALL);
        assertEquals(3, level.getNodeCount());
        AtomicInteger counter = new AtomicInteger();
        Arrays.stream(level.getLayout())
                .flatMap(Arrays::stream)
                .sorted(Comparator.comparingInt(Tile::getIndex))
                .filter(Tile::isAccessible)
                .forEachOrdered(x -> assertEquals(counter.getAndIncrement(), x.getIndex()));
        assertNotEquals(LevelElement.WALL, level.getTileAt(new Coordinate(1, 0)).getLevelElement());
        assertEquals(3, counter.get());
    }
}
