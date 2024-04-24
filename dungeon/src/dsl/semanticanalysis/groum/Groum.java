package dsl.semanticanalysis.groum;

import java.util.ArrayList;
import java.util.List;

public class Groum {
  public static Groum NONE = new Groum();

  List<GroumNode> nodes = new ArrayList<>();
  List<GroumEdge> edges = new ArrayList<>();

  public Groum() {}

  public Groum(ArrayList<GroumNode> nodes, ArrayList<GroumEdge> edges) {
    this.nodes.addAll(nodes);
    this.edges.addAll(edges);
  }

  public Groum(GroumNode node) {
    this.addNode(node);
  }

  public List<GroumNode> nodes() {
    return this.nodes;
  }

  public List<GroumEdge> edges() {
    return this.edges;
  }

  public void addNode(GroumNode node) {
    if (this.equals(NONE)) {
      throw new RuntimeException("Tried adding node to null-Groum");
    }
    this.nodes.add(node);
  }

  public void addEdge(GroumEdge edge) {
    if (this.equals(NONE)) {
      throw new RuntimeException("Tried adding edge to null-Groum");
    }
    this.edges.add(edge);
  }

  // X (this) v Y (other)
  public Groum mergeParallel(Groum other) {
    ArrayList<GroumNode> mergedNodes = new ArrayList<>(this.nodes.size() + other.nodes.size());
    mergedNodes.addAll(this.nodes);
    mergedNodes.addAll(other.nodes);
    // mergedNodes = new ArrayList<>(mergedNodes.stream().distinct().toList());

    ArrayList<GroumEdge> mergedEdges = new ArrayList<>(this.edges.size() + other.edges.size());
    mergedEdges.addAll(this.edges);
    mergedEdges.addAll(other.edges);
    // mergedEdges = new ArrayList<>(mergedEdges.stream().distinct().toList());

    return new Groum(mergedNodes, mergedEdges);
  }

  public List<GroumNode> sourceNodes() {
    return this.nodes.stream().filter(n -> n.incoming().isEmpty()).toList();
  }

  public List<GroumNode> sinkNodes() {
    return this.nodes.stream().filter(n -> n.outgoing().isEmpty()).toList();
  }

  // X (this) => Y (other)
  public Groum mergeSequential(GroumNode otherNode) {
    ArrayList<GroumNode> mergedNodes = new ArrayList<>(this.nodes.size() + 1);
    mergedNodes.addAll(this.nodes);
    mergedNodes.add(otherNode);

    ArrayList<GroumEdge> mergedEdges = new ArrayList<>(this.edges.size());
    mergedEdges.addAll(this.edges);

    // sinks
    List<GroumNode> sinkNodes = this.nodes.stream().filter(n -> n.outgoing().isEmpty()).toList();
    // sources
    List<GroumNode> sourceNodes = List.of(otherNode);
    // connect sinks to sources
    for (var sinkNode : sinkNodes) {
      for (var sourceNode : sourceNodes) {
        var edge = new GroumEdge(sinkNode, sourceNode, GroumEdge.GroumEdgeType.temporal);
        mergedEdges.add(edge);
      }
    }

    return new Groum(mergedNodes, mergedEdges);
  }

  // X (this) => Y (other)
  public Groum mergeSequential(Groum other) {
    ArrayList<GroumNode> mergedNodes = new ArrayList<>(this.nodes.size() + other.nodes.size());
    mergedNodes.addAll(this.nodes);
    mergedNodes.addAll(other.nodes);

    ArrayList<GroumEdge> mergedEdges = new ArrayList<>(this.edges.size() + other.edges.size());
    mergedEdges.addAll(this.edges);
    mergedEdges.addAll(other.edges);

    // sinks
    List<GroumNode> sinkNodes = this.nodes.stream().filter(n -> n.outgoing().isEmpty()).toList();
    // sources
    List<GroumNode> sourceNodes = other.nodes.stream().filter(n -> n.incoming().isEmpty()).toList();
    // connect sinks to sources
    for (var sinkNode : sinkNodes) {
      for (var sourceNode : sourceNodes) {
        var edge = new GroumEdge(sinkNode, sourceNode, GroumEdge.GroumEdgeType.temporal);
        mergedEdges.add(edge);
      }
    }

    return new Groum(mergedNodes, mergedEdges);
  }

  // X (this) => Y (other)
  public Groum mergeSequential(Groum other, GroumEdge.GroumEdgeType edgeType) {
    ArrayList<GroumNode> mergedNodes = new ArrayList<>(this.nodes.size() + other.nodes.size());
    mergedNodes.addAll(this.nodes);
    mergedNodes.addAll(other.nodes);

    ArrayList<GroumEdge> mergedEdges = new ArrayList<>(this.edges.size() + other.edges.size());
    mergedEdges.addAll(this.edges);
    mergedEdges.addAll(other.edges);

    // sinks
    List<GroumNode> sinkNodes = this.nodes.stream().filter(n -> n.outgoing().isEmpty()).toList();
    // sources
    List<GroumNode> sourceNodes = other.nodes.stream().filter(n -> n.incoming().isEmpty()).toList();
    // connect sinks to sources
    for (var sinkNode : sinkNodes) {
      for (var sourceNode : sourceNodes) {
        var edge = new GroumEdge(sinkNode, sourceNode, edgeType);
        mergedEdges.add(edge);
      }
    }

    return new Groum(mergedNodes, mergedEdges);
  }
}
