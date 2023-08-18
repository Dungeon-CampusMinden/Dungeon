package core.level.generator.graphBased;

import core.Entity;
import core.level.elements.ILevel;

import java.util.*;

/**
 * A level graph that can be further processed into a room-based level.
 *
 * <p>Each node in the graph corresponds to a potential room in the level and holds a collection of
 * entities as payload, which can be queried using {@link LevelGraph.Node#entities()}. The stored
 * entities must be placed in the generated room during the processing.
 *
 * <p>Each node can have a maximum of 4 neighbors, and each edge is oriented towards a {@link
 * LevelGraph.Direction} within the node. The {@link LevelGraph.Direction} indicates the side of the
 * room where the door should be placed. The edges are oriented so that a west edge from node A to B
 * corresponds to an east edge from B to A.
 *
 * <p>Edges are unidirectional.
 *
 * <p>There is no separate data type for edges; instead, the {@link LevelGraph.Node}s store an array
 * of neighboring nodes, and the index in the array indicates the {@link LevelGraph.Direction}
 * through which the nodes are connected.
 *
 * <p>The Dot representation of the graph can be obtained using {@link #toDot()}.
 *
 * <p>Use {@link #add(Set)} to add a new entity collection and thus a new node to the graph.
 */
public final class LevelGraph {
    private Node root;
    private final List<Node> nodes = new ArrayList<>();
    private static final Random RANDOM = new Random();

    /**
     * Creates a new node with the given set as payload and adds it to a random position in the
     * graph.
     *
     * @param set The entity collection to be placed as payload in the new node.
     */
    public void add(Set<Entity> set) {
        Node node = new Node(set);
        nodes.add(node);
        if (root == null) root = node;
        else add(node);
    }

    private void add(Node node) {
        root.addAtRandomDirection(node);
    }

    /**
     * Retrieves a random node from the graph.
     *
     * @return A randomly selected node from the graph.
     */
    public Node random() {
        return nodes.get(RANDOM.nextInt(nodes.size() - 1));
    }

    public Node root() {
        return root;
    }

    public List<Node> nodes() {
        return new ArrayList<>(nodes);
    }

    /**
     * Creates a DOT representation of the graph.
     *
     * @return DOT representation of the graph as a string.
     */
    public String toDot() {
        StringBuilder dotBuilder = new StringBuilder();

        dotBuilder.append("graph LevelGraph {\n");
        dotBuilder.append("    node [shape=box];\n");

        for (Node node : nodes) {
            int nodeId = nodes.indexOf(node);
            dotBuilder.append("    node").append(nodeId).append(" [label=\"");
            for (Entity entity : node.entities()) {
                dotBuilder.append(entity.toString()).append("\\n");
            }
            dotBuilder.append("\"];\n");

            for (Direction direction : Direction.values()) {
                Node neighbour = node.at(direction).orElse(null);
                if (neighbour != null && nodes.indexOf(neighbour) > nodeId) {
                    int neighbourId = nodes.indexOf(neighbour);
                    dotBuilder
                            .append("    node")
                            .append(nodeId)
                            .append(" -- node")
                            .append(neighbourId);
                    dotBuilder.append(" [dir=").append(direction).append("];\n");
                }
            }
        }

        dotBuilder.append("}\n");
        return dotBuilder.toString();
    }

    /**
     * Node in the level graph.
     *
     * <p>Each node in the graph corresponds to a potential room in the level and holds a collection
     * of entities as payload, which can be queried using {@link LevelGraph.Node#entities()}. The
     * stored entities must be placed in the generated room during the processing.
     *
     * <p>Each node can have a maximum of 4 neighbors, and each edge is oriented towards a {@link
     * LevelGraph.Direction} within the node. The {@link LevelGraph.Direction} indicates the side of
     * the room where the door should be placed. The edges are oriented so that a west edge from
     * node A to B corresponds to an east edge from B to A.
     *
     * <p>Edges are unidirectional.
     *
     * <p>There is no separate data type for edges; instead, the {@link LevelGraph.Node}s store an
     * array of neighboring nodes, and the index in the array indicates the {@link
     * LevelGraph.Direction} through which the nodes are connected.
     */
    public static final class Node {
        private final Set<Entity> entities;

        private ILevel level;
        private final Node[] neighbours = new Node[Direction.values().length];

        /**
         * Creates a new node with the given collection as payload.
         *
         * @param entities The entity collection stored in this node.
         */
        private Node(final Set<Entity> entities) {
            this.entities = entities;
        }

        /**
         * Adds a neighbor in the given direction.
         *
         * <p>Note: This method will not check if the spot is free; an already set neighbor will
         * potentially be overwritten.
         *
         * @param node The neighbor to be added.
         * @param direction The direction at which the neighbor should be added from this node's
         *     perspective (in the neighbor, this corresponds to the opposite direction).
         */
        private void add(final Node node, final Direction direction) {
            neighbours[direction.value] = node;
        }

        /**
         * Adds the given node as a neighbor at a random available position.
         *
         * <p>If the two nodes are already directly connected, no change will occur in the graph.
         *
         * <p>A random {@link Direction} is selected. If this node does not have a neighbor in that
         * direction and the given node also does not have a neighbor in the opposite {@link
         * Direction}, the two nodes will be connected at that position.
         *
         * <p>If this node already has a neighbor in the randomly selected direction, {@link
         * #addAtRandomDirection(Node)} will be called on that neighbor with the given node as the
         * parameter.
         *
         * <p>If this node does not have a neighbor in the randomly selected direction, but the
         * given node has one in the opposite direction, {@link #addAtRandomDirection(Node)} with
         * this node as parameter will be called on the neighbor of the given node in the opposite
         * {@link Direction} of the randomly selected direction.
         *
         * @param node The node to be connected to this node.
         */
        public void addAtRandomDirection(final Node node) {
            if (isNeighbourWith(node)) return;

            // select random connection direction
            Direction random = Direction.of(RANDOM.nextInt(0, 4));
            Optional<Node> neighbour = at(random);

            if (neighbour.isPresent()) {
                // add node to the neighbour
                neighbour.get().addAtRandomDirection(node);
            } else {
                // add it to this node

                Optional<Node> child = node.at(random);

                if (child.isPresent()) {
                    // add this node to the neighbour at the random direction of this node
                    child.get().addAtRandomDirection(this);
                } else {
                    // other has space on the selected direction
                    node.add(this, Direction.opposite(random));
                    add(node, random);
                }
            }
        }
        /**
         * Retrieves the neighbor in the given direction.
         *
         * @param direction The direction to check.
         * @return An Optional containing the neighbor node in the given direction, or empty if
         *     there is no neighbor in that direction.
         */
        public Optional<Node> at(Direction direction) {
            return Optional.ofNullable(neighbours[direction.value]);
        }
        /**
         * Checks if this node and the given node are direct neighbors.
         *
         * @param node The node to check for neighbor relationship.
         * @return True if the nodes are neighbors, false if not.
         */
        public boolean isNeighbourWith(Node node) {
            for (Node neighbour : neighbours) if (neighbour == node) return true;
            return false;
        }

        /**
         * Retrieves the entity collection of this node.
         *
         * @return The entity collection of this node.
         */
        public Set<Entity> entities() {
            return entities;
        }
        /**
         * Retrieves a copy of the neighbor node array.
         *
         * @return The neighbor node array.
         */
        public Node[] neighbours() {
            Node[] copy = new Node[neighbours.length];
            java.lang.System.arraycopy(neighbours, 0, copy, 0, neighbours.length);
            return copy;
        }

        public void level(ILevel level) {
            this.level = level;
        }

        public ILevel level() {
            return level;
        }
    }

    /** The different directions in which nodes can be connected to each other. */
    public enum Direction {
        NORTH(0),
        EAST(1),
        SOUTH(2),
        WEST(3);

        private final int value;

        Direction(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        /**
         * Retrieves the opposite direction.
         *
         * @param from The direction from which the opposite direction is sought.
         * @return The opposite direction.
         */
        public static Direction opposite(Direction from) {
            return switch (from) {
                case NORTH -> SOUTH;
                case EAST -> WEST;
                case SOUTH -> NORTH;
                case WEST -> EAST;
            };
        }

        /**
         * Returns the Direction enum value corresponding to the given integer value.
         *
         * @param v The integer value representing a direction.
         * @return The Direction enum value, or null if the value does not correspond to any
         *     direction.
         */
        public static Direction of(int v) {
            return switch (v) {
                case 0 -> NORTH;
                case 1 -> EAST;
                case 2 -> SOUTH;
                case 3 -> WEST;
                default -> null;
            };
        }
    }
}
