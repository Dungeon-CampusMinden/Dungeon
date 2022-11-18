package dslToGame;

import graph.Graph;
import graph.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import levelgraph.LevelNode;

public class DotToLevelGraph {

    public static ConvertedGraph convert(Graph<String> levelGenGraph) {

        /*
           - 1. Über alle Nodes iterieren und diese in HashMap abspeichern
           - 2. Über alle Edeges iterieren und zu jedem Node die Anzahl der Verbindungen in der Map speichern
                   - wenn ein Node mehr als 4 Verbindungen hat => GraphNotSupported
           - 3. HashMap mit Nodes (key) LevelNodes (value) anlegen
           - 4. Über Node/Edges-HashMap iterieren und Edges einzeichnen  an der ersten passenden Stelle (?)


           A -> B
           A -> C
           A -> D
           B -> E
           B -> F
           B -> G

           A->D
           D->G
           G->A

               A
           B   C   D
          E F G

                 D
            E    G
        F   B    A   C
                 D
                 G




        */

        // maph: GraphNode -> LevelNode, second map LeveNode -> GraphNode

        HashMap<Node<String>, LevelNode> nodeToLevelNode = null;
        HashMap<LevelNode, Node<String>> levelNodeToNode = null;

        List<LevelNode> levelNodes = new ArrayList<>();
        Iterator<Node<String>> iterator = levelGenGraph.getNodeIterator();
        // create nodes
        while (levelGenGraph.getNodeIterator().hasNext()) {
            Node n = iterator.next();
            levelNodes.add(new LevelNode(n.getIdx()));
        }

        // connect am ersten möglichen connect punkt
        // linkrs, rechts, unten (oben nicht!)

        // todo EDGES einzeichnen

        return new ConvertedGraph(
                levelGenGraph, levelNodes.get(0), nodeToLevelNode, levelNodeToNode);

        /*
         //DUMMY
        LevelNode root = new LevelNode();
        root.connect(new LevelNode(),DoorDirection.LEFT, DoorTile.DoorColor.GREEN);
        root.connect(new LevelNode(), DoorDirection.RIGHT, DoorTile.DoorColor.BLUE);
        return root;
        */

    }
}
