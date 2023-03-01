package dslToGame;

import dslToGame.graph.Graph;
import dslToGame.graph.Node;
import java.util.HashMap;
import level.levelgraph.LevelNode;

public record ConvertedGraph(
        Graph<String> graph,
        LevelNode root,
        HashMap<Node<String>, LevelNode> nodeToLevelNode,
        HashMap<LevelNode, Node<String>> levelNodeToNode) {}
