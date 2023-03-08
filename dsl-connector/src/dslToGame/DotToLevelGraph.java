package dslToGame;

import dslToGame.graph.Edge;
import dslToGame.graph.Graph;
import dslToGame.graph.Node;
import java.util.*;
import level.levelgraph.LevelNode;

public class DotToLevelGraph {

    public static ConvertedGraph convert(Graph<String> levelGenGraph) {
        HashMap<Node<String>, LevelNode> nodeToLevelNode = new LinkedHashMap<>();
        HashMap<LevelNode, Node<String>> levelNodeToNode = new LinkedHashMap<>();
        Iterator<Node<String>> iterator = levelGenGraph.getNodeIterator();

        // create LevelNodes
        Node node = iterator.next();
        LevelNode root = new LevelNode();
        nodeToLevelNode.put(node, root);
        levelNodeToNode.put(root, node);

        while (iterator.hasNext()) {
            node = iterator.next();
            LevelNode levelNode = new LevelNode();
            nodeToLevelNode.put(node, levelNode);
            levelNodeToNode.put(levelNode, node);
        }

        // connect Edges
        Iterator<Edge> edgeIterator = levelGenGraph.getEdgeIterator();
        while (edgeIterator.hasNext()) {
            Edge edge = edgeIterator.next();
            Node n1 = edge.getStartNode();
            Node n2 = edge.getEndNode();
            LevelNode ln1 = nodeToLevelNode.get(n1);
            LevelNode ln2 = nodeToLevelNode.get(n2);
            if (!ln1.connect(ln2)) throw new IllegalArgumentException("Graph cant be converted");
        }

        return new ConvertedGraph(levelGenGraph, root, nodeToLevelNode, levelNodeToNode);
    }
}
