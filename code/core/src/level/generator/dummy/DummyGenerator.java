package level.generator.dummy;

import java.util.ArrayList;
import java.util.List;
import level.elements.Level;
import level.elements.graph.Node;
import level.elements.room.Room;
import level.generator.IGenerator;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;

public class DummyGenerator implements IGenerator {

    @Override
    public Level getLevel() {
        // setup layouts
        int x = 8;
        int y = 8;
        LevelElement[][] layoutRoom1 = new LevelElement[y][x];
        for (int i = 0; i < y; i++)
            for (int j = 0; j < x; j++) {
                if (i == 0 || i == y - 1 || j == 0 || j == x - 1)
                    layoutRoom1[i][j] = LevelElement.WALL;
                else layoutRoom1[i][j] = LevelElement.FLOOR;
            }

        x = 15;
        y = 4;
        LevelElement[][] layoutRoom2 = new LevelElement[y][x];
        for (int i = 0; i < y; i++)
            for (int j = 0; j < x; j++) {
                if (i == 0 || i == y - 1 || j == 0 || j == x - 1)
                    layoutRoom2[i][j] = LevelElement.WALL;
                else layoutRoom2[i][j] = LevelElement.FLOOR;
            }

        x = 15;
        y = 15;
        LevelElement[][] layoutRoom3 = new LevelElement[y][x];
        for (int i = 0; i < y; i++)
            for (int j = 0; j < x; j++) {
                if (i == 0 || i == y - 1 || j == 0 || j == x - 1)
                    layoutRoom3[i][j] = LevelElement.WALL;
                else layoutRoom3[i][j] = LevelElement.FLOOR;
            }

        // place doors
        layoutRoom1[6][0] = LevelElement.FLOOR;
        layoutRoom2[2][14] = LevelElement.FLOOR;
        layoutRoom2[3][2] = LevelElement.FLOOR;
        layoutRoom3[0][7] = LevelElement.FLOOR;

        // hardcode positions
        Coordinate r1global = new Coordinate(15, -6);
        Coordinate r1local = new Coordinate(0, 0);
        Coordinate r2global = new Coordinate(5, -2);
        Coordinate r2local = new Coordinate(0, 0);
        Coordinate r3global = new Coordinate(0, 0);
        Coordinate r3local = new Coordinate(0, 0);
        // create rooms

        // setEnd
        layoutRoom1[3][3] = LevelElement.EXIT;

        Room room1 = new Room(layoutRoom1, DesignLabel.DEFAULT, r1local, r1global);
        Room room2 = new Room(layoutRoom2, DesignLabel.DEFAULT, r2local, r2global);
        Room room3 = new Room(layoutRoom3, DesignLabel.DEFAULT, r3local, r3global);
        List<Room> roomlist = new ArrayList<>();
        roomlist.add(room1);
        roomlist.add(room2);
        roomlist.add(room3);

        // setup graph
        Node room1Node = new Node(0);
        Node room2Node = new Node(1);
        Node room3Node = new Node(2);
        room1Node.connect(room2Node);
        room2Node.connect(room1Node);
        room2Node.connect(room3Node);
        room3Node.connect(room2Node);
        List<Node> graph = new ArrayList<>();
        graph.add(room1Node);
        graph.add(room2Node);
        graph.add(room3Node);

        Level level = Level.getLevel(graph, roomlist);
        level.setStartNode(room3Node);
        level.setStartTile(level.getRoomToNode(room3Node).getRandomFloorTile());
        level.setEndNode(room1Node);
        level.setEndTile(level.getRoomToNode(room1Node).getLayout()[3][3]);
        return level;
    }
}
