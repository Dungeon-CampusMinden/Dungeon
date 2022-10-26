package graph;

public class Edge {
    Node from;
    Node to;
    EdgeDirection direction;

    public Edge(Node from, Node to, EdgeDirection direction) {
        this.from = from;
        this.to = to;
        this.direction = direction;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }

    public EdgeDirection getDirection() {
        return direction;
    }
}
