package dslToGame;

import graph.Graph;
import graph.Node;
import java.util.HashMap;
import levelgraph.LevelNode;

public record ConvertedGraph(
        Graph<String> graph,
        LevelNode root,
        HashMap<Node<String>, LevelNode> nodeToLevelNode,
        HashMap<LevelNode, Node<String>> levelNodeToNode) {}
