package level.generator.dummy;

import level.elements.Level;
import level.elements.Node;
import level.elements.Room;
import level.generator.IGenerator;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import tools.Point;

import java.util.ArrayList;
import java.util.List;

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
        Point r1global = new Point(15, -6);
        Point r1local = new Point(0, 0);
        Point r2global = new Point(5, -2);
        Point r2local = new Point(0, 0);
        Point r3global = new Point(0, 0);
        Point r3local = new Point(0, 0);
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

        Level l = new Level(graph, roomlist);
        l.setStartNode(room3Node);
        l.setStartTile(l.getRoomToNode(room3Node).getRandomFloorTile());
        l.setEndNode(room1Node);
        l.setEndTile(l.getRoomToNode(room1Node).getLayout()[3][3]);
        return l;
    }
}
